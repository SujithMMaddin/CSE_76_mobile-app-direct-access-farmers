package com.example.directaccessfarmers;

import android.util.Log;

import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper to lock bidding for a crop (finalize winner + highest bid).
 * Public method lockCropBidding(...) can be called from activities or scheduled jobs.
 */
public class BidLockHelper {

    public interface LockCallback {
        void onLocked(boolean success, String message);
    }

    /**
     * Lock bidding for the given cropId. Queries the Bids subcollection for top bid,
     * then updates the parent crop document with isSold=true, winnerId, winnerName, highestBid.
     *
     * This method is idempotent and safe to call multiple times.
     *
     * @param cropId firestore doc id under "Crops"
     * @param db FirebaseFirestore instance
     * @param callback optional callback (may be null)
     */
    public static void lockCropBidding(String cropId, FirebaseFirestore db, LockCallback callback) {
        if (cropId == null || cropId.isEmpty()) {
            if (callback != null) callback.onLocked(false, "invalid cropId");
            return;
        }

        CollectionReference bidsRef = db.collection("Crops").document(cropId).collection("Bids");
        bidsRef.orderBy("bidValue", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String,Object> updateMap = new HashMap<>();
                    updateMap.put("isSold", true);
                    if (querySnapshot.isEmpty()) {
                        // no bids
                        updateMap.put("winnerId", "");
                        updateMap.put("winnerName", "");
                        updateMap.put("highestBid", 0.0);
                    } else {
                        DocumentSnapshot top = querySnapshot.getDocuments().get(0);
                        Double topVal = top.getDouble("bidValue");
                        String cid = top.getString("customerId");
                        String cname = top.getString("customerName");
                        if (topVal == null) topVal = 0.0;
                        updateMap.put("winnerId", cid != null ? cid : "");
                        updateMap.put("winnerName", cname != null ? cname : "");
                        updateMap.put("highestBid", topVal);
                    }

                    db.collection("Crops").document(cropId)
                            .update(updateMap)
                            .addOnSuccessListener(unused -> {
                                if (callback != null) callback.onLocked(true, "locked");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("BidLockHelper", "Failed to update crop: " + e.getMessage());
                                if (callback != null) callback.onLocked(false, e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("BidLockHelper", "Failed to read bids: " + e.getMessage());
                    if (callback != null) callback.onLocked(false, e.getMessage());
                });
    }
}
