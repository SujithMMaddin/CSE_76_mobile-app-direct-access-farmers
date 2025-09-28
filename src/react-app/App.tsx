import { BrowserRouter as Router, Routes, Route } from "react-router";
import { AuthProvider } from "@/react-app/contexts/AuthContext";
import HomePage from "@/react-app/pages/Home";
import AuthCallbackPage from "@/react-app/pages/AuthCallback";
import DashboardPage from "@/react-app/pages/Dashboard";
import ProfileSetupPage from "@/react-app/pages/ProfileSetup";
import CropDetailPage from "@/react-app/pages/CropDetail";
import CreateCropPage from "@/react-app/pages/CreateCrop";
import TransactionsPage from "@/react-app/pages/Transactions";
import AdminDashboard from "@/react-app/pages/AdminDashboard";
import { UserProfileProvider } from "@/react-app/providers/UserProfileProvider";

export default function App() {
  return (
    <AuthProvider>
      <UserProfileProvider>
        <Router>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/auth/callback" element={<AuthCallbackPage />} />
            <Route path="/profile-setup" element={<ProfileSetupPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/crops/:id" element={<CropDetailPage />} />
            <Route path="/create-crop" element={<CreateCropPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Router>
      </UserProfileProvider>
    </AuthProvider>
  );
}
