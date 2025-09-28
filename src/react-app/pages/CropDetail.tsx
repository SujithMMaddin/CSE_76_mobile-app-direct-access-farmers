import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router";
import { useAuth } from "@/react-app/contexts/AuthContext";
import { useUserProfile } from "@/react-app/hooks/useUserProfile";
import Header from "@/react-app/components/Header";
import type { CropWithBids } from "@/shared/types";
import {
  ArrowLeft,
  MapPin,
  Clock,
  Package,
  IndianRupee,
  User,
  CheckCircle,
  XCircle,
} from "lucide-react";

export default function CropDetail() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const { profile } = useUserProfile();
  const navigate = useNavigate();
  const [crop, setCrop] = useState<CropWithBids | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [bidAmount, setBidAmount] = useState("");
  const [bidQuantity, setBidQuantity] = useState("");
  const [isSubmittingBid, setIsSubmittingBid] = useState(false);
  const [isAcceptingBid, setIsAcceptingBid] = useState<number | null>(null);

  const fetchCrop = useCallback(async () => {
    try {
      const idToken = await user?.getIdToken();
      const response = await fetch(`/api/crops/${id}`, {
        headers: {
          Authorization: `Bearer ${idToken}`,
        },
      });
      if (!response.ok) {
        throw new Error("Failed to fetch crop");
      }
      const data = await response.json();
      setCrop(data);
    } catch (error) {
      console.error("Failed to fetch crop:", error);
      navigate("/dashboard");
    } finally {
      setIsLoading(false);
    }
  }, [id, navigate, user]);

  useEffect(() => {
    if (id) {
      fetchCrop();
    }
  }, [id, fetchCrop]);

  const handlePlaceBid = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!crop || !bidAmount || !bidQuantity) return;

    setIsSubmittingBid(true);
    try {
      const idToken = await user?.getIdToken();
      const response = await fetch(`/api/crops/${crop.id}/bids`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${idToken}`,
        },
        body: JSON.stringify({
          bid_amount_per_kg: parseFloat(bidAmount),
          bid_quantity: parseInt(bidQuantity),
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || "Failed to place bid");
      }

      setBidAmount("");
      setBidQuantity("");
      await fetchCrop(); // Refresh to show new bid
      alert("Bid placed successfully!");
    } catch (error) {
      console.error("Failed to place bid:", error);
      alert(error instanceof Error ? error.message : "Failed to place bid");
    } finally {
      setIsSubmittingBid(false);
    }
  };

  const handleAcceptBid = async (bidId: number) => {
    setIsAcceptingBid(bidId);
    try {
      const idToken = await user?.getIdToken();
      const response = await fetch(`/api/bids/${bidId}/accept`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${idToken}`,
        },
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || "Failed to accept bid");
      }

      await fetchCrop(); // Refresh to show updated status
      alert("Bid accepted successfully!");
    } catch (error) {
      console.error("Failed to accept bid:", error);
      alert(error instanceof Error ? error.message : "Failed to accept bid");
    } finally {
      setIsAcceptingBid(null);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString("en-IN", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatTimeRemaining = (auctionEndsAt: string) => {
    const now = new Date();
    const endDate = new Date(auctionEndsAt);
    const diff = endDate.getTime() - now.getTime();

    if (diff <= 0) return "Auction Ended";

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days} days, ${hours % 24} hours remaining`;
    return `${hours} hours remaining`;
  };

  const isAuctionActive =
    crop &&
    crop.status === "OPEN" &&
    new Date(crop.auction_ends_at) > new Date();
  const isOwner = crop && user && crop.farmer_id === user.uid;
  const canBid = profile?.role === "buyer" && isAuctionActive && !isOwner;

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
            <div className="bg-white rounded-xl p-6">
              <div className="h-6 bg-gray-200 rounded w-1/2 mb-4"></div>
              <div className="space-y-3">
                <div className="h-4 bg-gray-200 rounded w-full"></div>
                <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!crop) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Crop Not Found
          </h1>
          <button
            onClick={() => navigate("/dashboard")}
            className="text-green-600 hover:text-green-700"
          >
            Return to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate("/dashboard")}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Dashboard
        </button>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          {/* Header */}
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-start justify-between">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 mb-2">
                  {crop.crop_name}
                  {crop.variety && (
                    <span className="text-gray-600 font-normal">
                      {" "}
                      - {crop.variety}
                    </span>
                  )}
                </h1>
                <div className="flex items-center space-x-4 text-sm text-gray-600">
                  <div className="flex items-center">
                    <User className="w-4 h-4 mr-1" />
                    {crop.farmer_name}
                  </div>
                  {crop.location && (
                    <div className="flex items-center">
                      <MapPin className="w-4 h-4 mr-1" />
                      {crop.location}
                    </div>
                  )}
                </div>
              </div>
              <span
                className={`px-3 py-1 text-sm font-medium rounded-full ${
                  crop.status === "OPEN"
                    ? "bg-green-100 text-green-800"
                    : crop.status === "SOLD"
                    ? "bg-gray-100 text-gray-800"
                    : "bg-red-100 text-red-800"
                }`}
              >
                {crop.status}
              </span>
            </div>
          </div>

          {/* Details */}
          <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900">
                Crop Details
              </h3>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Total Quantity</span>
                  <span className="font-medium">{crop.total_quantity} kg</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Available Quantity</span>
                  <span className="font-medium">
                    {crop.available_quantity} kg
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Lot Size</span>
                  <span className="font-medium">{crop.lot_size} kg</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Base Price</span>
                  <span className="font-medium">
                    {formatCurrency(crop.base_price_per_kg)}/kg
                  </span>
                </div>
                {crop.harvest_date && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Harvest Date</span>
                    <span className="font-medium">
                      {new Date(crop.harvest_date).toLocaleDateString("en-IN")}
                    </span>
                  </div>
                )}
              </div>

              {crop.description && (
                <div>
                  <h4 className="font-medium text-gray-900 mb-2">
                    Description
                  </h4>
                  <p className="text-gray-600">{crop.description}</p>
                </div>
              )}
            </div>

            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900">
                Auction Details
              </h3>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Auction Ends</span>
                  <span className="font-medium">
                    {formatDateTime(crop.auction_ends_at)}
                  </span>
                </div>
                <div className="flex items-center">
                  <Clock className="w-4 h-4 mr-2 text-gray-400" />
                  <span
                    className={`text-sm ${
                      isAuctionActive ? "text-green-600" : "text-red-600"
                    }`}
                  >
                    {formatTimeRemaining(crop.auction_ends_at)}
                  </span>
                </div>
              </div>

              {/* Bidding Form for Buyers */}
              {canBid && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <h4 className="font-medium text-gray-900 mb-3">
                    Place Your Bid
                  </h4>
                  <form onSubmit={handlePlaceBid} className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Bid Amount (per kg)
                      </label>
                      <div className="relative">
                        <IndianRupee className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                        <input
                          type="number"
                          step="0.01"
                          min={crop.base_price_per_kg}
                          value={bidAmount}
                          onChange={(e) => setBidAmount(e.target.value)}
                          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                          placeholder={`Min. ${formatCurrency(
                            crop.base_price_per_kg
                          )}`}
                          required
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Quantity (kg)
                      </label>
                      <div className="relative">
                        <Package className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                        <input
                          type="number"
                          min="1"
                          max={crop.available_quantity}
                          value={bidQuantity}
                          onChange={(e) => setBidQuantity(e.target.value)}
                          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                          placeholder={`Max. ${crop.available_quantity} kg`}
                          required
                        />
                      </div>
                    </div>
                    <button
                      type="submit"
                      disabled={isSubmittingBid}
                      className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white py-2 px-4 rounded-lg font-medium transition-colors"
                    >
                      {isSubmittingBid ? "Placing Bid..." : "Place Bid"}
                    </button>
                  </form>
                </div>
              )}
            </div>
          </div>

          {/* Bids Section */}
          {crop.bids.length > 0 && (
            <div className="border-t border-gray-200">
              <div className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Current Bids ({crop.bids.length})
                </h3>
                <div className="space-y-3">
                  {crop.bids.map((bid, index) => (
                    <div
                      key={bid.id}
                      className={`flex items-center justify-between p-4 rounded-lg border ${
                        bid.status === "ACCEPTED"
                          ? "bg-green-50 border-green-200"
                          : bid.status === "REJECTED"
                          ? "bg-red-50 border-red-200"
                          : "bg-gray-50 border-gray-200"
                      }`}
                    >
                      <div className="flex items-center space-x-4">
                        <div
                          className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${
                            index === 0 && bid.status === "PLACED"
                              ? "bg-yellow-500 text-white"
                              : "bg-gray-300 text-gray-600"
                          }`}
                        >
                          {index + 1}
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">
                            {formatCurrency(bid.bid_amount_per_kg)}/kg
                          </p>
                          <p className="text-sm text-gray-600">
                            {bid.bid_quantity} kg by {bid.buyer_name}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center space-x-3">
                        <div className="text-right">
                          <p className="font-medium text-gray-900">
                            {formatCurrency(
                              bid.bid_amount_per_kg * bid.bid_quantity
                            )}
                          </p>
                          <p className="text-xs text-gray-500">
                            {formatDateTime(bid.created_at)}
                          </p>
                        </div>

                        {bid.status === "ACCEPTED" && (
                          <CheckCircle className="w-5 h-5 text-green-600" />
                        )}
                        {bid.status === "REJECTED" && (
                          <XCircle className="w-5 h-5 text-red-600" />
                        )}
                        {bid.status === "PLACED" &&
                          isOwner &&
                          isAuctionActive && (
                            <button
                              onClick={() => handleAcceptBid(bid.id)}
                              disabled={isAcceptingBid === bid.id}
                              className="bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white px-3 py-1 rounded text-sm font-medium transition-colors"
                            >
                              {isAcceptingBid === bid.id
                                ? "Accepting..."
                                : "Accept"}
                            </button>
                          )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
