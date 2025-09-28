import { useState, useEffect, useCallback } from "react";
import { useAuth } from "@/react-app/contexts/AuthContext";
import { useUserProfile } from "@/react-app/hooks/useUserProfile";
import { useNavigate } from "react-router";
import Header from "@/react-app/components/Header";
import type { Transaction } from "@/shared/types";
import {
  ArrowLeft,
  Package,
  Calendar,
  User,
  CheckCircle,
  XCircle,
  Clock,
  CreditCard,
} from "lucide-react";

export default function Transactions() {
  const { user } = useAuth();
  const { profile } = useUserProfile();
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [payingTransaction, setPayingTransaction] = useState<number | null>(
    null
  );

  const fetchTransactions = useCallback(async () => {
    try {
      const idToken = await user?.getIdToken();
      const response = await fetch("/api/transactions", {
        headers: {
          Authorization: `Bearer ${idToken}`,
        },
      });
      if (!response.ok) {
        throw new Error("Failed to fetch transactions");
      }
      const data = await response.json();
      setTransactions(data);
    } catch (error) {
      console.error("Failed to fetch transactions:", error);
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (!user) {
      navigate("/");
      return;
    }
    fetchTransactions();
  }, [user, fetchTransactions, navigate]);

  const handlePayment = async (transactionId: number) => {
    setPayingTransaction(transactionId);
    try {
      const idToken = await user?.getIdToken();
      const response = await fetch(`/api/transactions/${transactionId}/pay`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${idToken}`,
        },
      });

      if (!response.ok) {
        throw new Error("Payment failed");
      }

      const result = await response.json();

      if (result.success) {
        alert("Payment successful!");
      } else {
        alert("Payment failed. Please try again.");
      }

      await fetchTransactions(); // Refresh transactions
    } catch (error) {
      console.error("Payment failed:", error);
      alert("Payment failed. Please try again.");
    } finally {
      setPayingTransaction(null);
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

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return <CheckCircle className="w-5 h-5 text-green-600" />;
      case "FAILED":
        return <XCircle className="w-5 h-5 text-red-600" />;
      default:
        return <Clock className="w-5 h-5 text-yellow-600" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "SUCCESS":
        return "bg-green-100 text-green-800";
      case "FAILED":
        return "bg-red-100 text-red-800";
      default:
        return "bg-yellow-100 text-yellow-800";
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="bg-white rounded-xl p-6">
                  <div className="h-6 bg-gray-200 rounded w-1/2 mb-4"></div>
                  <div className="space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-full"></div>
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
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

        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Transactions
          </h1>
          <p className="text-gray-600">
            {profile?.role === "farmer"
              ? "Track your sales and earnings"
              : "View your purchases and payment history"}
          </p>
        </div>

        {transactions.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8 text-center">
            <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              No Transactions Yet
            </h3>
            <p className="text-gray-600 mb-4">
              {profile?.role === "farmer"
                ? "You haven't sold any crops yet. Create your first listing to get started."
                : "You haven't made any purchases yet. Browse available crops to start buying."}
            </p>
            <button
              onClick={() => navigate("/dashboard")}
              className="bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded-lg font-medium transition-colors"
            >
              {profile?.role === "farmer" ? "Create Listing" : "Browse Crops"}
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {transactions.map((transaction) => (
              <div
                key={transaction.id}
                className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden"
              >
                <div className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-gray-900 mb-1">
                        {transaction.crop_name}
                        {transaction.variety && (
                          <span className="text-gray-600 font-normal">
                            {" "}
                            - {transaction.variety}
                          </span>
                        )}
                      </h3>
                      <div className="flex items-center space-x-4 text-sm text-gray-600">
                        <div className="flex items-center">
                          <User className="w-4 h-4 mr-1" />
                          {profile?.role === "farmer"
                            ? `Sold to ${transaction.buyer_name}`
                            : `Bought from ${transaction.farmer_name}`}
                        </div>
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1" />
                          {formatDateTime(transaction.created_at)}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center space-x-3">
                      <span
                        className={`px-3 py-1 text-sm font-medium rounded-full ${getStatusColor(
                          transaction.payment_status
                        )}`}
                      >
                        {transaction.payment_status}
                      </span>
                      {getStatusIcon(transaction.payment_status)}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                    <div>
                      <p className="text-sm text-gray-600">Quantity</p>
                      <p className="font-medium">{transaction.quantity} kg</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Price per kg</p>
                      <p className="font-medium">
                        {formatCurrency(transaction.final_price_per_kg)}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Total Amount</p>
                      <p className="font-medium text-lg">
                        {formatCurrency(transaction.total_amount)}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Transaction ID</p>
                      <p className="font-medium text-xs">#{transaction.id}</p>
                    </div>
                  </div>

                  {/* Payment Actions for Buyers */}
                  {profile?.role === "buyer" &&
                    transaction.payment_status === "PENDING" && (
                      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                        <div className="flex items-center justify-between">
                          <div>
                            <h4 className="font-medium text-yellow-800 mb-1">
                              Payment Required
                            </h4>
                            <p className="text-sm text-yellow-700">
                              Complete your payment to confirm this purchase
                            </p>
                          </div>
                          <button
                            onClick={() => handlePayment(transaction.id)}
                            disabled={payingTransaction === transaction.id}
                            className="bg-yellow-600 hover:bg-yellow-700 disabled:bg-gray-300 text-white px-6 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2"
                          >
                            <CreditCard className="w-4 h-4" />
                            <span>
                              {payingTransaction === transaction.id
                                ? "Processing..."
                                : "Pay Now"}
                            </span>
                          </button>
                        </div>
                      </div>
                    )}

                  {/* Payment Details */}
                  {transaction.payment_gateway_response && (
                    <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                      <h4 className="font-medium text-gray-900 mb-2">
                        Payment Details
                      </h4>
                      <div className="text-sm text-gray-600">
                        {(() => {
                          try {
                            const response = JSON.parse(
                              transaction.payment_gateway_response
                            );
                            return (
                              <div className="space-y-1">
                                <p>
                                  <strong>Payment ID:</strong>{" "}
                                  {response.payment_id}
                                </p>
                                <p>
                                  <strong>Status:</strong> {response.status}
                                </p>
                                <p>
                                  <strong>Amount:</strong>{" "}
                                  {formatCurrency(response.amount)}
                                </p>
                                <p>
                                  <strong>Timestamp:</strong>{" "}
                                  {formatDateTime(response.timestamp)}
                                </p>
                              </div>
                            );
                          } catch {
                            return <p>Payment details available</p>;
                          }
                        })()}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
