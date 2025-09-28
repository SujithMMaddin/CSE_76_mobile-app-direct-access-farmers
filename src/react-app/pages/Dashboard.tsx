import { useState, useEffect, useCallback } from "react";
import { useAuth } from "@/react-app/contexts/AuthContext";
import { useUserProfile } from "@/react-app/hooks/useUserProfile";
import { useNavigate } from "react-router";
import Header from "@/react-app/components/Header";
import type { Crop, Transaction } from "@/shared/types";
import {
  Wheat,
  Plus,
  Clock,
  MapPin,
  IndianRupee,
  Users,
  TrendingUp,
  Package,
} from "lucide-react";

export default function Dashboard() {
  const { user } = useAuth();
  const { profile, hasProfile } = useUserProfile();
  const navigate = useNavigate();
  const [crops, setCrops] = useState<Crop[]>([]);
  const [ownCrops, setOwnCrops] = useState<Crop[]>([]);
  const [marketplaceCrops, setMarketplaceCrops] = useState<Crop[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [stats, setStats] = useState({
    totalListings: 0,
    activeBids: 0,
    totalEarnings: 0,
    totalPurchases: 0,
  });

  useEffect(() => {
    if (!user) {
      navigate("/");
      return;
    }
    if (!hasProfile) {
      navigate("/profile-setup");
      return;
    }
  }, [user, hasProfile, navigate]);

  const fetchCrops = useCallback(async () => {
    try {
      setIsLoading(true);
      const idToken = await user?.getIdToken();
      if (profile?.role === "farmer") {
        // Fetch own crops
        const responseOwn = await fetch("/api/crops?own=true", {
          headers: {
            Authorization: `Bearer ${idToken}`,
          },
        });
        const ownData = await responseOwn.json();
        setOwnCrops(ownData);

        // Fetch all crops for marketplace
        const responseAll = await fetch("/api/crops", {
          headers: {
            Authorization: `Bearer ${idToken}`,
          },
        });
        const allData = await responseAll.json();
        setMarketplaceCrops(allData);
      } else {
        // Buyers fetch all crops
        const response = await fetch("/api/crops", {
          headers: {
            Authorization: `Bearer ${idToken}`,
          },
        });
        const data = await response.json();
        setCrops(data);
      }
    } catch (error) {
      console.error("Failed to fetch crops:", error);
    } finally {
      setIsLoading(false);
    }
  }, [user, profile]);

  const fetchStats = useCallback(async () => {
    try {
      const idToken = await user?.getIdToken();
      const response = await fetch("/api/transactions", {
        headers: {
          Authorization: `Bearer ${idToken}`,
        },
      });
      const transactions = await response.json();

      if (profile?.role === "farmer") {
        const farmerTransactions = transactions.filter(
          (t: Transaction) => t.farmer_id === user?.uid
        );
        setStats({
          totalListings: ownCrops.length,
          activeBids: 0, // Would need additional API call
          totalEarnings: farmerTransactions.reduce(
            (sum: number, t: Transaction) =>
              t.payment_status === "SUCCESS" ? sum + t.total_amount : sum,
            0
          ),
          totalPurchases: 0,
        });
      } else {
        const buyerTransactions = transactions.filter(
          (t: Transaction) => t.buyer_id === user?.uid
        );
        setStats({
          totalListings: 0,
          activeBids: 0, // Would need additional API call
          totalEarnings: 0,
          totalPurchases: buyerTransactions.reduce(
            (sum: number, t: Transaction) =>
              t.payment_status === "SUCCESS" ? sum + t.total_amount : sum,
            0
          ),
        });
      }
    } catch (error) {
      console.error("Failed to fetch stats:", error);
    }
  }, [profile, user, ownCrops]);

  useEffect(() => {
    fetchCrops();
    fetchStats();
  }, [profile, fetchCrops, fetchStats]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatTimeRemaining = (auctionEndsAt: string) => {
    const now = new Date();
    const endDate = new Date(auctionEndsAt);
    const diff = endDate.getTime() - now.getTime();

    if (diff <= 0) return "Ended";

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days}d ${hours % 24}h`;
    return `${hours}h`;
  };

  if (!user || !profile) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Section */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Welcome back, {profile.name}!
          </h1>
          <p className="text-gray-600">
            {profile.role === "farmer"
              ? "Manage your crop listings and track sales"
              : "Discover fresh crops and place competitive bids"}
          </p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {profile.role === "farmer" ? (
            <>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                <div className="flex items-center">
                  <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                    <Package className="w-6 h-6 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">
                      Total Listings
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {stats.totalListings}
                    </p>
                  </div>
                </div>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                <div className="flex items-center">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Users className="w-6 h-6 text-blue-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">
                      Active Bids
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {stats.activeBids}
                    </p>
                  </div>
                </div>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                <div className="flex items-center">
                  <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center">
                    <TrendingUp className="w-6 h-6 text-emerald-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">
                      Total Earnings
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {formatCurrency(stats.totalEarnings)}
                    </p>
                  </div>
                </div>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                <button
                  onClick={() => navigate("/create-crop")}
                  className="w-full h-full flex flex-col items-center justify-center text-green-600 hover:text-green-700 transition-colors min-h-[100px]"
                >
                  <Plus className="w-8 h-8 mb-2" />
                  <span className="font-medium">List New Crop</span>
                </button>
              </div>
            </>
          ) : (
            <>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                <div className="flex items-center">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Users className="w-6 h-6 text-blue-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">
                      Active Bids
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {stats.activeBids}
                    </p>
                  </div>
                </div>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                <div className="flex items-center">
                  <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                    <Package className="w-6 h-6 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600">
                      Total Purchases
                    </p>
                    <p className="text-2xl font-bold text-gray-900">
                      {formatCurrency(stats.totalPurchases)}
                    </p>
                  </div>
                </div>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200 md:col-span-2">
                <div className="flex items-center justify-center h-full">
                  <div className="text-center">
                    <Wheat className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                    <p className="text-gray-600">
                      Browse available crops below
                    </p>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Crops Section */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900">
              {profile.role === "farmer"
                ? "Your Crop Listings"
                : "Available Crops"}
            </h2>
          </div>

          {isLoading ? (
            <div className="p-8 text-center">
              <div className="animate-spin w-8 h-8 border-4 border-green-500 border-t-transparent rounded-full mx-auto mb-4"></div>
              <p className="text-gray-600">Loading crops...</p>
            </div>
          ) : (profile.role === "farmer" ? ownCrops.length : crops.length) ===
            0 ? (
            <div className="p-8 text-center">
              <Wheat className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {profile.role === "farmer"
                  ? "No crops listed yet"
                  : "No crops available"}
              </h3>
              <p className="text-gray-600 mb-4">
                {profile.role === "farmer"
                  ? "Start by creating your first crop listing"
                  : "Check back later for new crop listings"}
              </p>
              {profile.role === "farmer" && (
                <button
                  onClick={() => navigate("/create-crop")}
                  className="bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded-lg font-medium transition-colors"
                >
                  Create Your First Listing
                </button>
              )}
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {(profile.role === "farmer" ? ownCrops : crops).map((crop) => (
                <div
                  key={crop.id}
                  className="p-6 hover:bg-gray-50 transition-colors cursor-pointer"
                  onClick={() => navigate(`/crops/${crop.id}`)}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-gray-900">
                          {crop.crop_name}
                          {crop.variety && (
                            <span className="text-gray-600 font-normal">
                              {" "}
                              - {crop.variety}
                            </span>
                          )}
                        </h3>
                        <span
                          className={`px-2 py-1 text-xs font-medium rounded-full ${
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

                      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 text-sm text-gray-600">
                        <div className="flex items-center">
                          <Package className="w-4 h-4 mr-1" />
                          {crop.available_quantity}kg available
                        </div>
                        <div className="flex items-center">
                          <IndianRupee className="w-4 h-4 mr-1" />
                          {formatCurrency(crop.base_price_per_kg)}/kg base
                        </div>
                        {crop.location && (
                          <div className="flex items-center">
                            <MapPin className="w-4 h-4 mr-1" />
                            {crop.location}
                          </div>
                        )}
                        {crop.status === "OPEN" && (
                          <div className="flex items-center">
                            <Clock className="w-4 h-4 mr-1" />
                            {formatTimeRemaining(crop.auction_ends_at)}
                          </div>
                        )}
                      </div>

                      {crop.farmer_name && profile.role === "buyer" && (
                        <p className="text-sm text-gray-500 mt-2">
                          By {crop.farmer_name}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Marketplace Section for Farmers */}
        {profile.role === "farmer" && marketplaceCrops.length > 0 && (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 mt-8">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-bold text-gray-900">Marketplace</h2>
            </div>
            <div className="divide-y divide-gray-200">
              {marketplaceCrops.map((crop) => (
                <div
                  key={crop.id}
                  className="p-6 hover:bg-gray-50 transition-colors cursor-pointer"
                  onClick={() => navigate(`/crops/${crop.id}`)}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <h3 className="text-lg font-semibold text-gray-900">
                          {crop.crop_name}
                          {crop.variety && (
                            <span className="text-gray-600 font-normal">
                              {" "}
                              - {crop.variety}
                            </span>
                          )}
                        </h3>
                        <span
                          className={`px-2 py-1 text-xs font-medium rounded-full ${
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

                      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 text-sm text-gray-600">
                        <div className="flex items-center">
                          <Package className="w-4 h-4 mr-1" />
                          {crop.available_quantity}kg available
                        </div>
                        <div className="flex items-center">
                          <IndianRupee className="w-4 h-4 mr-1" />
                          {formatCurrency(crop.base_price_per_kg)}/kg base
                        </div>
                        {crop.location && (
                          <div className="flex items-center">
                            <MapPin className="w-4 h-4 mr-1" />
                            {crop.location}
                          </div>
                        )}
                        {crop.status === "OPEN" && (
                          <div className="flex items-center">
                            <Clock className="w-4 h-4 mr-1" />
                            {formatTimeRemaining(crop.auction_ends_at)}
                          </div>
                        )}
                      </div>

                      {crop.farmer_name && (
                        <p className="text-sm text-gray-500 mt-2">
                          By {crop.farmer_name}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
