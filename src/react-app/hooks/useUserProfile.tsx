import { useContext } from "react";
import { UserProfileContext } from "@/react-app/contexts/UserProfileContext";
import type { UserProfileContextValue } from "@/react-app/contexts/UserProfileContext";

export function useUserProfile(): UserProfileContextValue {
  const context = useContext(UserProfileContext);
  if (!context) {
    throw new Error("useUserProfile must be used within UserProfileProvider");
  }
  return context;
}
