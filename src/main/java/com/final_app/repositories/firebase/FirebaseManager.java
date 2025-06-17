package com.final_app.repositories.firebase; // Or a more appropriate package like com.final_app.db or com.final_app.config

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseManager {

    private static final Logger log = LoggerFactory.getLogger(FirebaseManager.class);
    private static final String SERVICE_ACCOUNT_KEY_PATH = "serviceAccount.json"; // Path within resources folder

    private static Firestore db = null;
    private static boolean initialized = false;
    private static final Object lock = new Object();

    /**
     * Initializes the Firebase Admin SDK.
     * Reads credentials from the service account key file specified by SERVICE_ACCOUNT_KEY_PATH.
     * Should only be called once during application startup.
     */
    private static void initializeFirebase() {
        if (initialized) {
            return;
        }

        log.info("Initializing Firebase Admin SDK...");
        try (FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH)) {

            if (serviceAccount == null) {
                log.error("Service account key file not found at path: {}", SERVICE_ACCOUNT_KEY_PATH);
                throw new RuntimeException("Failed to find service account key file: " + SERVICE_ACCOUNT_KEY_PATH);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    //.setDatabaseUrl("https://languagelearningapp-5dd99-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    // Add .setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com") if using Realtime Database
                    .build();

            // Initialize the default app if it hasn't been initialized yet
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully.");
            } else {
                // Log if already initialized (e.g., in a framework context that might auto-init)
                log.warn("Firebase Admin SDK was already initialized.");
            }

            // Get Firestore instance from the default app
            db = FirestoreClient.getFirestore();
            initialized = true;

        } catch (IOException e) {
            log.error("Failed to read service account key or initialize Firebase Admin SDK.", e);
            throw new RuntimeException("Firebase initialization failed.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during Firebase initialization.", e);
            throw new RuntimeException("Firebase initialization failed unexpectedly.", e);
        }
    }

    /**
     * Gets the initialized Firestore instance.
     * Initializes Firebase Admin SDK on the first call if not already initialized.
     * This method is thread-safe.
     *
     * @return The Firestore database instance.
     * @throws RuntimeException if Firebase initialization fails.
     */
    public static Firestore getDb() {
        // Double-checked locking for thread safety and performance
        if (db == null) {
            synchronized (lock) {
                if (db == null) {
                    initializeFirebase();
                    if (db == null) {
                        // Should not happen if initializeFirebase throws exceptions correctly, but as a safeguard:
                        log.error("Firestore instance is null even after initialization attempt.");
                        throw new IllegalStateException("Firestore could not be initialized.");
                    }
                }
            }
        }
        return db;
    }

    public static FirebaseDatabase getFirebaseDatabase() {
        initializeFirebase();
        return FirebaseDatabase.getInstance();
    }

    // Optional: Add a shutdown hook or explicit close method if needed,
    // although typically the default app lives for the duration of the JVM.
    public static void close() {
        // FirebaseApp.getInstance().delete(); // Use with caution - deletes the app instance
        log.info("Firebase resources cleanup (if needed).");
        // Firestore itself doesn't have an explicit close(), relies on gRPC cleanup.
        initialized = false;
        db = null;
    }
}
