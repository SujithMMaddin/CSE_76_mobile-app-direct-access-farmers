import { createContext } from "react";
import type { UserProfile } from "@/shared/types";

export interface UserProfileContextValue {
  profile: UserProfile | null;
  isLoading: boolean;
  refreshProfile: () => Promise<void>;
  hasProfile: boolean;
}

export const UserProfileContext = createContext<UserProfileContextValue | null>(
  null
);
