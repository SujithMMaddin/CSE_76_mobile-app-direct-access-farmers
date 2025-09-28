import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyBxczHU-2IrwXm7vADULDBb1gHA_V4SFhY",
  authDomain: "farmdirect-7c6f8.firebaseapp.com",
  projectId: "farmdirect-7c6f8",
  storageBucket: "farmdirect-7c6f8.firebasestorage.app",
  messagingSenderId: "725451486867",
  appId: "1:725451486867:web:83ebf4289e87384193ff66",
  measurementId: "G-GYYKH2PLZN",
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
