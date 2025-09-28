import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "@/react-app/contexts/AuthContext";
import { useUserProfile } from "@/react-app/hooks/useUserProfile";
import Header from "@/react-app/components/Header";
import {
  ArrowLeft,
  Wheat,
  Package,
  IndianRupee,
  Calendar,
  Clock,
  MapPin,
  FileText,
} from "lucide-react";

export default function CreateCrop() {
  const { user } = useAuth();
  const { profile } = useUserProfile();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    crop_name: "",
    variety: "",
    total_quantity: "",
    lot_size: "",
    base_price_per_kg: "",
    harvest_date: "",
    auction_ends_at: "",
    location: profile?.location || "",
    description: "",
  });

  useEffect(() => {
    // Check if user is farmer
    if (!user || !profile || profile.role !== "farmer") {
      navigate("/dashboard");
      return;
    }
  }, [user, profile, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const idToken = await user?.getIdToken();
      const response = await fetch("/api/crops", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${idToken}`,
        },
        body: JSON.stringify({
          crop_name: formData.crop_name,
          variety: formData.variety || undefined,
          total_quantity: parseInt(formData.total_quantity),
          lot_size: parseInt(formData.lot_size),
          base_price_per_kg: parseFloat(formData.base_price_per_kg),
          harvest_date: formData.harvest_date || undefined,
          auction_ends_at: new Date(formData.auction_ends_at).toISOString(),
          location: formData.location || undefined,
          description: formData.description || undefined,
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || "Failed to create crop listing");
      }

      const result = await response.json();
      navigate(`/crops/${result.cropId}`);
    } catch (error) {
      console.error("Failed to create crop listing:", error);
      alert(
        error instanceof Error ? error.message : "Failed to create crop listing"
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  // Set minimum date for auction end (24 hours from now)
  const minAuctionDate = new Date();
  minAuctionDate.setDate(minAuctionDate.getDate() + 1);
  const minAuctionDateString = minAuctionDate.toISOString().slice(0, 16);

  const commonCrops = [
    "Rice",
    "Wheat",
    "Maize",
    "Sugarcane",
    "Cotton",
    "Jute",
    "Tea",
    "Coffee",
    "Coconut",
    "Groundnut",
    "Sunflower",
    "Mustard",
    "Soybean",
    "Sesame",
    "Castor",
    "Linseed",
    "Safflower",
    "Niger",
    "Potato",
    "Onion",
    "Tomato",
    "Brinjal",
    "Cauliflower",
    "Cabbage",
    "Okra",
    "Chilli",
    "Turmeric",
    "Coriander",
    "Cumin",
    "Fenugreek",
    "Garlic",
    "Ginger",
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate("/dashboard")}
          className="flex items-center text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Dashboard
        </button>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <Wheat className="w-5 h-5 text-green-600" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">
                  Create Crop Listing
                </h1>
                <p className="text-gray-600">
                  List your crop for buyers to bid on
                </p>
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-6">
            {/* Crop Information */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900">
                Crop Information
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Wheat className="w-4 h-4 inline mr-1" />
                    Crop Name *
                  </label>
                  <select
                    name="crop_name"
                    value={formData.crop_name}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  >
                    <option value="">Select a crop</option>
                    {commonCrops.map((crop) => (
                      <option key={crop} value={crop}>
                        {crop}
                      </option>
                    ))}
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Variety
                  </label>
                  <input
                    type="text"
                    name="variety"
                    value={formData.variety}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                    placeholder="e.g., Basmati, Hybrid"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <FileText className="w-4 h-4 inline mr-1" />
                  Description
                </label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  rows={3}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  placeholder="Describe your crop quality, growing conditions, etc."
                />
              </div>
            </div>

            {/* Quantity & Pricing */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900">
                Quantity & Pricing
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Package className="w-4 h-4 inline mr-1" />
                    Total Quantity (kg) *
                  </label>
                  <input
                    type="number"
                    name="total_quantity"
                    value={formData.total_quantity}
                    onChange={handleChange}
                    min="1"
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                    placeholder="1000"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Minimum Lot Size (kg) *
                  </label>
                  <input
                    type="number"
                    name="lot_size"
                    value={formData.lot_size}
                    onChange={handleChange}
                    min="1"
                    max={
                      formData.total_quantity
                        ? parseInt(formData.total_quantity)
                        : undefined
                    }
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                    placeholder="50"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <IndianRupee className="w-4 h-4 inline mr-1" />
                    Base Price (per kg) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    name="base_price_per_kg"
                    value={formData.base_price_per_kg}
                    onChange={handleChange}
                    min="0.01"
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                    placeholder="25.00"
                  />
                </div>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="text-sm text-blue-700">
                  <strong>Note:</strong> The base price is the minimum amount
                  buyers can bid. Higher bids will get priority when you accept
                  offers.
                </p>
              </div>
            </div>

            {/* Dates & Location */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900">
                Dates & Location
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Calendar className="w-4 h-4 inline mr-1" />
                    Harvest Date
                  </label>
                  <input
                    type="date"
                    name="harvest_date"
                    value={formData.harvest_date}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    <Clock className="w-4 h-4 inline mr-1" />
                    Auction End Date & Time *
                  </label>
                  <input
                    type="datetime-local"
                    name="auction_ends_at"
                    value={formData.auction_ends_at}
                    onChange={handleChange}
                    min={minAuctionDateString}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <MapPin className="w-4 h-4 inline mr-1" />
                  Location
                </label>
                <input
                  type="text"
                  name="location"
                  value={formData.location}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  placeholder="City, State"
                />
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex items-center justify-between pt-6 border-t border-gray-200">
              <button
                type="button"
                onClick={() => navigate("/dashboard")}
                className="text-gray-600 hover:text-gray-900 font-medium transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white px-8 py-3 rounded-lg font-semibold transition-colors"
              >
                {isSubmitting ? "Creating Listing..." : "Create Listing"}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
