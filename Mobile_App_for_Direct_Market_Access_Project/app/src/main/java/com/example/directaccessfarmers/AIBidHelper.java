package com.example.directaccessfarmers;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIBidHelper {

    private static final String TAG = "AIBidHelper";
    private FirebaseFirestore db;

    public AIBidHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public interface BidSuggestionCallback {
        void onSuggestionReady(double suggestedBid, String explanation);
        void onError(String error);
    }

    public void calculateOptimalBid(String cropId, double userBid, double basePrice,
                                    String cropName, BidSuggestionCallback callback) {


        db.collection(Constants.COLLECTION_BIDS)
                .whereEqualTo("cropName", cropName)
                .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Double> historicalBids = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        Double bidValue = doc.getDouble(Constants.FIELD_BID_VALUE);
                        if (bidValue != null) {
                            historicalBids.add(bidValue);
                        }
                    });

                    double suggestedBid = calculateAISuggestion(userBid, basePrice, historicalBids, cropName);
                    String explanation = generateExplanation(userBid, suggestedBid, basePrice, historicalBids);

                    callback.onSuggestionReady(suggestedBid, explanation);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching historical data", e);

                    double simpleSuggestion = calculateSimpleSuggestion(userBid, basePrice);
                    callback.onSuggestionReady(simpleSuggestion, "Basic suggestion based on your bid and crop price");
                });
    }

    private double calculateAISuggestion(double userBid, double basePrice,
                                         List<Double> historicalBids, String cropName) {


        double marketTrend = calculateMarketTrend(historicalBids, basePrice);


        double demandFactor = calculateDemandFactor(historicalBids);


        double seasonalAdjustment = getSeasonalAdjustment(cropName);


        double aiSuggestion = (userBid * Constants.CUSTOMER_PREFERENCE_WEIGHT) +
                (basePrice * Constants.BASE_PRICE_WEIGHT) +
                (marketTrend * Constants.MARKET_TREND_WEIGHT);


        aiSuggestion *= demandFactor * seasonalAdjustment;


        double minBid = basePrice * Constants.MIN_BID_MULTIPLIER;
        double maxBid = basePrice * Constants.MAX_BID_MULTIPLIER;

        return Math.max(minBid, Math.min(maxBid, aiSuggestion));
    }

    private double calculateMarketTrend(List<Double> historicalBids, double basePrice) {
        if (historicalBids.isEmpty()) return basePrice;

        double avgHistorical = historicalBids.stream().mapToDouble(Double::doubleValue).average().orElse(basePrice);


        int recentCount = Math.min(10, historicalBids.size());
        double recentAvg = historicalBids.subList(0, recentCount)
                .stream().mapToDouble(Double::doubleValue).average().orElse(avgHistorical);

        return (recentAvg + avgHistorical) / 2;
    }

    private double calculateDemandFactor(List<Double> historicalBids) {
        if (historicalBids.size() < 5) return 1.0;
        if (historicalBids.size() < 15) return 1.1;
        return 1.2;
    }

    private double getSeasonalAdjustment(String cropName) {

        Random random = new Random(cropName.hashCode());
        return 0.95 + (random.nextDouble() * 0.1);
    }

    private double calculateSimpleSuggestion(double userBid, double basePrice) {
        return (userBid * 0.6) + (basePrice * 0.4);
    }

    private String generateExplanation(double userBid, double suggestedBid,
                                       double basePrice, List<Double> historical) {
        StringBuilder explanation = new StringBuilder();

        if (suggestedBid > userBid) {
            explanation.append("AI suggests higher bid due to ");
            if (!historical.isEmpty()) {
                double avgHistorical = historical.stream().mapToDouble(Double::doubleValue).average().orElse(basePrice);
                if (avgHistorical > basePrice) {
                    explanation.append("strong market demand");
                } else {
                    explanation.append("competitive pricing");
                }
            } else {
                explanation.append("market conditions");
            }
        } else if (suggestedBid < userBid) {
            explanation.append("AI suggests lower bid - you can save money while staying competitive");
        } else {
            explanation.append("Your bid is optimal for current market conditions");
        }

        return explanation.toString();
    }
}