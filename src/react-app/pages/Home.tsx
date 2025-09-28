import { useAuth } from "@/react-app/contexts/AuthContext";
import { useUserProfile } from "@/react-app/hooks/useUserProfile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { Sprout, Users, TrendingUp, Shield, ArrowRight } from "lucide-react";
import InstallPrompt from "@/react-app/components/InstallPrompt";
import LoginModal from "@/react-app/components/LoginModal";

export default function Home() {
  const { user, loading } = useAuth();
  const { isLoading, hasProfile } = useUserProfile();
  const navigate = useNavigate();
  const [showLoginModal, setShowLoginModal] = useState(false);

  useEffect(() => {
    if (user && !isLoading) {
      if (!hasProfile) {
        navigate("/profile-setup");
      } else {
        navigate("/dashboard");
      }
    }
  }, [user, isLoading, hasProfile, navigate]);

  if (loading || (user && isLoading)) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen">
        <div className="animate-spin">
          <Sprout className="w-10 h-10 text-green-600" />
        </div>
      </div>
    );
  }

  if (user) return null; // Will redirect via useEffect

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 via-white to-emerald-50">
      {/* Header */}
      <header className="relative">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-emerald-600 rounded-xl flex items-center justify-center">
                <Sprout className="w-6 h-6 text-white" />
              </div>
              <span className="text-2xl font-bold bg-gradient-to-r from-green-600 to-emerald-600 bg-clip-text text-transparent">
                FarmDirect
              </span>
            </div>
            <button
              onClick={() => setShowLoginModal(true)}
              className="bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2 shadow-lg"
            >
              <span>Get Started</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative py-20 lg:py-32">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
              Direct Access for
              <span className="block bg-gradient-to-r from-green-600 to-emerald-600 bg-clip-text text-transparent">
                Farmers & Buyers
              </span>
            </h1>
            <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto leading-relaxed">
              A transparent bidding marketplace that connects farmers directly
              with verified buyers, ensuring fair prices and eliminating
              middlemen.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={() => setShowLoginModal(true)}
                className="bg-green-600 hover:bg-green-700 text-white px-8 py-4 rounded-xl font-semibold transition-all transform hover:scale-105 shadow-xl flex items-center justify-center space-x-2"
              >
                <span>Start Selling Crops</span>
                <ArrowRight className="w-5 h-5" />
              </button>
              <button
                onClick={() => setShowLoginModal(true)}
                className="bg-white hover:bg-gray-50 text-green-600 border-2 border-green-600 px-8 py-4 rounded-xl font-semibold transition-all transform hover:scale-105 shadow-lg"
              >
                Browse & Buy
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Why Choose FarmDirect?
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Revolutionizing agricultural commerce through technology and
              transparency
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center p-8 rounded-2xl bg-gradient-to-br from-green-50 to-emerald-50 border border-green-100">
              <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-600 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <TrendingUp className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-4">
                Fair Price Discovery
              </h3>
              <p className="text-gray-600">
                Transparent bidding system ensures farmers get the best market
                price for their crops
              </p>
            </div>

            <div className="text-center p-8 rounded-2xl bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-100">
              <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <Users className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-4">
                Direct Connection
              </h3>
              <p className="text-gray-600">
                Connect farmers directly with verified buyers, eliminating
                middleman commissions
              </p>
            </div>

            <div className="text-center p-8 rounded-2xl bg-gradient-to-br from-purple-50 to-pink-50 border border-purple-100">
              <div className="w-16 h-16 bg-gradient-to-br from-purple-500 to-pink-600 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <Shield className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-4">
                Secure Transactions
              </h3>
              <p className="text-gray-600">
                Secure payment system with buyer verification ensures safe and
                reliable transactions
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How it Works */}
      <section className="py-20 bg-gradient-to-br from-gray-50 to-green-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              How It Works
            </h2>
            <p className="text-xl text-gray-600">
              Simple steps to start buying or selling
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
            {/* For Farmers */}
            <div className="space-y-6">
              <h3 className="text-2xl font-bold text-green-600 mb-6">
                For Farmers
              </h3>
              <div className="space-y-4">
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-green-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    1
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Create Your Profile
                    </h4>
                    <p className="text-gray-600">
                      Register and verify your farmer profile
                    </p>
                  </div>
                </div>
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-green-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    2
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      List Your Crops
                    </h4>
                    <p className="text-gray-600">
                      Add crop details, quantities, and set auction dates
                    </p>
                  </div>
                </div>
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-green-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    3
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Receive & Accept Bids
                    </h4>
                    <p className="text-gray-600">
                      Review competitive bids and accept the best offers
                    </p>
                  </div>
                </div>
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-green-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    4
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Get Paid Securely
                    </h4>
                    <p className="text-gray-600">
                      Receive payment directly to your account
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* For Buyers */}
            <div className="space-y-6">
              <h3 className="text-2xl font-bold text-blue-600 mb-6">
                For Buyers
              </h3>
              <div className="space-y-4">
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    1
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Register & Verify
                    </h4>
                    <p className="text-gray-600">
                      Create buyer account and complete verification
                    </p>
                  </div>
                </div>
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    2
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Browse Crops
                    </h4>
                    <p className="text-gray-600">
                      Search and filter available crop listings
                    </p>
                  </div>
                </div>
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    3
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Place Competitive Bids
                    </h4>
                    <p className="text-gray-600">
                      Bid on crops with transparent pricing
                    </p>
                  </div>
                </div>
                <div className="flex items-start space-x-4">
                  <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold">
                    4
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">
                      Secure Purchase
                    </h4>
                    <p className="text-gray-600">
                      Complete payment and arrange delivery
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 bg-gradient-to-r from-green-600 to-emerald-600">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-6">
            Ready to Transform Agricultural Trading?
          </h2>
          <p className="text-xl text-green-100 mb-8">
            Join thousands of farmers and buyers already using FarmDirect
          </p>
          <button
            onClick={() => setShowLoginModal(true)}
            className="bg-white text-green-600 px-8 py-4 rounded-xl font-semibold text-lg transition-all transform hover:scale-105 shadow-xl flex items-center justify-center space-x-2 mx-auto"
          >
            <span>Get Started Today</span>
            <ArrowRight className="w-5 h-5" />
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="flex items-center justify-center space-x-2 mb-4">
            <div className="w-8 h-8 bg-gradient-to-br from-green-500 to-emerald-600 rounded-lg flex items-center justify-center">
              <Sprout className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold">FarmDirect</span>
          </div>
          <p className="text-gray-400">
            Empowering farmers and buyers through direct, transparent
            marketplace
          </p>
        </div>
      </footer>

      <InstallPrompt />
      <LoginModal
        isOpen={showLoginModal}
        onClose={() => setShowLoginModal(false)}
      />
    </div>
  );
}
