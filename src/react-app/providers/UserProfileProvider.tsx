import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "@/react-app/contexts/AuthContext";
import { UserProfileContext } from "@/react-app/contexts/UserProfileContext";
import type { UserProfile } from "@/shared/types";
import { db } from "@/firebase";
import { doc, getDoc } from "firebase/firestore";

export function UserProfileProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { user } = useAuth();

  const refreshProfile = useCallback(async () => {
    if (!user) {
      setProfile(null);
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      const docRef = doc(db, "user_profiles", user.uid);
      const docSnap = await getDoc(docRef);
      if (docSnap.exists()) {
        setProfile(docSnap.data() as UserProfile);
      } else {
        setProfile(null);
      }
    } catch (error) {
      console.error("Failed to fetch profile:", error);
      setProfile(null);
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  useEffect(() => {
    refreshProfile();
  }, [user, refreshProfile]);

  const hasProfile = Boolean(profile);

  return (
    <UserProfileContext.Provider
      value={{ profile, isLoading, refreshProfile, hasProfile }}
    >
      {children}
    </UserProfileContext.Provider>
  );
}
