import { useEffect } from "react";
import { useNavigate } from "react-router";
import { Sprout } from "lucide-react";

export default function AuthCallback() {
  const navigate = useNavigate();

  useEffect(() => {
    // Firebase handles the authentication callback automatically
    // Just redirect to home after a short delay
    const timer = setTimeout(() => {
      navigate("/");
    }, 2000);

    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 to-emerald-50 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin mb-6">
          <Sprout className="w-12 h-12 text-green-600 mx-auto" />
        </div>
        <h2 className="text-2xl font-semibold text-gray-900 mb-2">
          Completing Sign In...
        </h2>
        <p className="text-gray-600">
          Please wait while we set up your account
        </p>
      </div>
    </div>
  );
}
