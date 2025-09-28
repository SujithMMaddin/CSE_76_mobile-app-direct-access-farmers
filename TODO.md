# TODO: Ensure Crops Visible to All Buyers and Bidding Functionality

## Overview

- **Goal**: All authenticated users (buyers and farmers) can view all crop listings. Buyers can bid on any open listing. Farmers can create/edit/delete only their own listings. Dashboard shows role-specific views: Buyers see "Available Crops" (all), Farmers see "Your Crop Listings" (own) + "Marketplace" (all).
- **Key Changes**: API requires auth for reads, filters by ownership for farmers' own listings, adds edit/delete endpoints. Frontend updates dashboard fetches and adds marketplace section for farmers. CropDetail adds edit/delete for owners.
- **Security**: Firestore rules (user to set): Allow reads for all auth users, writes only by owner (farmerId match).

## Steps

- [x] **Step 1: Update API (src/worker/index.ts)**

  - Add `firebaseAuthMiddleware` to GET `/api/crops` (require auth, return 401 if not).
  - Support query param `?own=true`: If set and user is farmer, filter crops where `farmerId === user.id`. Else, return all OPEN crops.
  - Add PUT `/api/crops/:id` endpoint: Farmer-only, ownership check (`farmerId === user.id`), update fields (except farmerId/status if sold), validate schema.
  - Add DELETE `/api/crops/:id` endpoint: Farmer-only, ownership check, delete from Firestore.
  - Test API changes with wrangler dev (ensure auth required, filtering works, edit/delete only by owner).

- [ ] **Step 2: Update Dashboard (src/react-app/pages/Dashboard.tsx)**

  - Modify `fetchCrops`: For buyers, fetch `/api/crops`. For farmers, fetch `/api/crops?own=true` for own listings, and `/api/crops` for marketplace.
  - Add "Marketplace" section for farmers: Display all crops (similar to buyer's view), clickable to CropDetail.
  - Update stats: For farmers, use ownCrops.length for totalListings.
  - Handle empty states: Farmers see own empty, but marketplace if populated.
  - Test: Buyers see all crops. Farmers see own + all in marketplace.

- [ ] **Step 3: Update CropDetail (src/react-app/pages/CropDetail.tsx)**

  - For owning farmers (`crop.farmer_id === user.uid` and status="OPEN"): Add "Edit" button (inline form or navigate to edit page – use PUT API).
  - Add "Delete" button: Confirm dialog, call DELETE API, navigate back on success.
  - Test: Farmers can edit/delete own open listings; buyers/farmers can't edit others.

- [ ] **Step 4: Set Firestore Rules**

  - In Firebase console, set rules as suggested:
    ```
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        match /crops/{cropId} {
          allow read: if request.auth != null;
          allow create: if request.auth != null && request.resource.data.farmerId == request.auth.uid;
          allow update, delete: if request.auth != null && resource.data.farmerId == request.auth.uid;
        }
        match /user_profiles/{userId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
      }
    }
    ```
  - Verify: Reads work for all auth, writes only for owner.

- [ ] **Step 5: Full Testing**
  - Run `npm run dev` for frontend, `wrangler dev` for API.
  - Test flows: Farmer creates listing → Buyer sees in dashboard/marketplace → Buyer bids → Farmer edits own, deletes if needed.
  - Check security: Unauth can't read; non-owners can't write.
  - Deploy if ready.

## Notes

- No new files needed.
- If issues, check console for auth/Firestore errors.
- Progress: Update this file as steps complete.
