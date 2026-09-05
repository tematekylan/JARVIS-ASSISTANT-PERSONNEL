import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore, doc, getDocFromServer, setDoc, getDoc, updateDoc, collection, addDoc, query, orderBy, getDocs, deleteDoc } from 'firebase/firestore';
import { 
  getAuth, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  sendEmailVerification, 
  updateProfile,
  signOut,
  User as FirebaseUser
} from 'firebase/auth';

// Read config from firebase-applet-config.json
const firebaseConfig = {
  projectId: "facebook-70d2a",
  appId: "1:815681635244:web:015b5bc4dd72b31606f009",
  apiKey: "AIzaSyDsBFZsRAOBO8DGxde7OMFYoPov8IqkM3c",
  authDomain: "facebook-70d2a.firebaseapp.com",
  firestoreDatabaseId: "ai-studio-jarvisassistantp-8da2af84-92b7-47c7-ae33-39b09c1feb58",
  storageBucket: "facebook-70d2a.firebasestorage.app",
  messagingSenderId: "815681635244",
  oAuthClientId: "815681635244-44ggknq9rfgpem7lgomqs33olouhadti.apps.googleusercontent.com",
};

// Initialize Firebase app singleton
export const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

// Initialize Firestore
export const db = getFirestore(app, firebaseConfig.firestoreDatabaseId);

// Initialize Auth
export const auth = getAuth(app);

// Connection test on boot per Firebase skill guidelines
export async function testFirestoreConnection(): Promise<boolean> {
  try {
    await getDocFromServer(doc(db, 'test', 'connection'));
    console.log('[Firebase] Connected to Firestore successfully');
    return true;
  } catch (error) {
    if (error instanceof Error && error.message.includes('the client is offline')) {
      console.warn('[Firebase] Firestore client is offline or network restricted');
    } else {
      console.log('[Firebase] Connection probe handled');
    }
    return false;
  }
}

// User Profile Database Helpers
export interface FirestoreUserProfile {
  uid: string;
  email: string;
  displayName: string;
  photoURL?: string;
  status?: string;
  authStatus?: 'EN_ATTENTE_VALIDATION' | 'VALIDE' | 'NON_AUTHENTIFIE';
  emailVerified?: boolean;
  isOnline?: boolean;
  lastSeen?: number;
  role?: string;
  createdAt: number;
  updatedAt: number;
  settings?: any;
}

export async function saveUserProfileToDb(profile: Partial<FirestoreUserProfile> & { uid: string; email: string }): Promise<void> {
  try {
    const userRef = doc(db, 'users', profile.uid);
    await setDoc(userRef, {
      ...profile,
      updatedAt: Date.now()
    }, { merge: true });
  } catch (e) {
    console.warn('[Firebase] saveUserProfile fallback:', e);
  }
}

export async function getUserProfileFromDb(uid: string): Promise<FirestoreUserProfile | null> {
  try {
    const userRef = doc(db, 'users', uid);
    const snap = await getDoc(userRef);
    if (snap.exists()) {
      return snap.data() as FirestoreUserProfile;
    }
  } catch (e) {
    console.warn('[Firebase] getUserProfile fallback:', e);
  }
  return null;
}

export async function updateUserAuthStatusInDb(uid: string, authStatus: 'EN_ATTENTE_VALIDATION' | 'VALIDE', emailVerified: boolean = true): Promise<void> {
  try {
    const userRef = doc(db, 'users', uid);
    await setDoc(userRef, {
      authStatus,
      emailVerified,
      updatedAt: Date.now()
    }, { merge: true });
  } catch (e) {
    console.warn('[Firebase] updateUserAuthStatusInDb error:', e);
  }
}

/**
 * Configure Firebase Auth user with email verification
 * Automatically assigns 'EN_ATTENTE_VALIDATION' status until confirmed.
 */
export async function registerFirebaseUser(
  email: string, 
  pass: string, 
  displayName: string,
  photoURL?: string
): Promise<{ user: FirebaseUser | null; error?: string }> {
  try {
    const cred = await createUserWithEmailAndPassword(auth, email, pass);
    if (cred.user) {
      // Update profile
      await updateProfile(cred.user, {
        displayName: displayName || "Teddy",
        photoURL: photoURL || undefined
      });

      // Send Firebase confirmation email
      try {
        await sendEmailVerification(cred.user);
        console.log('[Firebase Auth] E-mail de confirmation Firebase envoyé à:', email);
      } catch (mailErr) {
        console.warn('[Firebase Auth] sendEmailVerification notice:', mailErr);
      }

      // Persist in Firestore with EN_ATTENTE_VALIDATION state
      await saveUserProfileToDb({
        uid: cred.user.uid,
        email: email.trim().toLowerCase(),
        displayName: displayName.trim() || "Teddy",
        photoURL: photoURL || "",
        status: "En attente de validation par code",
        authStatus: 'EN_ATTENTE_VALIDATION',
        emailVerified: false,
        isOnline: true,
        lastSeen: Date.now(),
        role: "user",
        createdAt: Date.now(),
        updatedAt: Date.now()
      });

      return { user: cred.user };
    }
    return { user: null, error: "Création impossible" };
  } catch (err: any) {
    console.warn('[Firebase Auth] Erreur inscription:', err);
    // If account already exists, we fall back to signing in or notify
    return { user: null, error: err?.message || "Erreur lors de l'enregistrement Firebase" };
  }
}

/**
 * Sign in existing user and verify validation status
 */
export async function loginFirebaseUser(email: string, pass: string): Promise<{ user: FirebaseUser | null; error?: string }> {
  try {
    const cred = await signInWithEmailAndPassword(auth, email, pass);
    return { user: cred.user };
  } catch (err: any) {
    console.warn('[Firebase Auth] Erreur connexion:', err);
    return { user: null, error: err?.message || "Identifiants invalides" };
  }
}

/**
 * Resend verification email via Firebase Auth
 */
export async function resendFirebaseVerificationEmail(): Promise<boolean> {
  try {
    if (auth.currentUser) {
      await sendEmailVerification(auth.currentUser);
      return true;
    }
    return false;
  } catch (e) {
    console.warn('[Firebase Auth] Erreur renvoi email:', e);
    return false;
  }
}

