import { useState } from "react";
import { useAuth } from "@/react-app/contexts/AuthContext";
import { X, Sprout, Shield, ArrowRight, Mail } from "lucide-react";

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function LoginModal({ isOpen, onClose }: LoginModalProps) {
  const { signInWithGoogle, signInWithEmail } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [mode, setMode] = useState<"google" | "email">("google");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    setIsLoading(true);
    try {
      await signInWithGoogle();
      onClose(); // Close modal after successful sign in
    } catch (error) {
      console.error("Login failed:", error);
      setIsLoading(false);
    }
  };

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await signInWithEmail(email, password);
      onClose(); // Close modal after successful sign in
    } catch (error) {
      console.error("Login failed:", error);
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl max-w-md w-full p-8 relative animate-in fade-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X className="w-6 h-6" />
        </button>

        {/* FarmDirect Branding */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Sprout className="w-8 h-8 text-white" />
          </div>
          <h2 className="text-2xl font-bold bg-gradient-to-r from-green-600 to-emerald-600 bg-clip-text text-transparent mb-2">
            Welcome to FarmDirect
          </h2>
          <p className="text-gray-600">
            Join the agricultural marketplace revolution
          </p>
        </div>

        {mode === "google" ? (
          <>
            {/* Security Notice */}
            <div className="bg-gray-50 rounded-xl p-4 mb-6">
              <div className="flex items-start space-x-3">
                <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Shield className="w-4 h-4 text-green-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900 text-sm mb-1">
                    Secure Authentication
                  </h3>
                  <p className="text-sm text-gray-600">
                    We use Google's secure authentication service to protect
                    your account. You'll be redirected to sign in with your
                    Google account.
                  </p>
                </div>
              </div>
            </div>

            {/* Benefits */}
            <div className="space-y-3 mb-8">
              <div className="flex items-center space-x-3">
                <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                <span className="text-sm text-gray-700">
                  Connect directly with farmers and buyers
                </span>
              </div>
              <div className="flex items-center space-x-3">
                <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                <span className="text-sm text-gray-700">
                  Transparent bidding and fair pricing
                </span>
              </div>
              <div className="flex items-center space-x-3">
                <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                <span className="text-sm text-gray-700">
                  Secure payment processing
                </span>
              </div>
            </div>

            {/* Login Buttons */}
            <div className="space-y-4">
              <button
                onClick={handleLogin}
                disabled={isLoading}
                className="w-full bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white px-6 py-4 rounded-xl font-semibold transition-all transform hover:scale-105 shadow-lg flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
              >
                {isLoading ? (
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  <>
                    <span>Continue with Google</span>
                    <ArrowRight className="w-5 h-5" />
                  </>
                )}
              </button>

              <button
                onClick={() => setMode("email")}
                className="w-full bg-white hover:bg-gray-50 text-green-600 border-2 border-green-600 px-6 py-4 rounded-xl font-semibold transition-all transform hover:scale-105 shadow-lg flex items-center justify-center space-x-2"
              >
                <Mail className="w-5 h-5" />
                <span>Sign In with Email</span>
              </button>
            </div>

            <p className="text-xs text-gray-500 text-center mt-4">
              By continuing, you agree to our Terms of Service and Privacy
              Policy
            </p>
          </>
        ) : (
          <>
            {/* Email Sign In Form */}
            <form onSubmit={handleEmailLogin} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  placeholder="Enter your email"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  placeholder="Enter your password"
                />
              </div>

              <div className="space-y-4">
                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white px-6 py-4 rounded-xl font-semibold transition-all transform hover:scale-105 shadow-lg flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                >
                  {isLoading ? (
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  ) : (
                    <>
                      <span>Sign In</span>
                      <ArrowRight className="w-5 h-5" />
                    </>
                  )}
                </button>

                <button
                  type="button"
                  onClick={() => setMode("google")}
                  className="w-full bg-gray-100 hover:bg-gray-200 text-gray-700 px-6 py-4 rounded-xl font-semibold transition-colors"
                >
                  Back
                </button>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
