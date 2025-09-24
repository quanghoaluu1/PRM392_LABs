package com.example.se183138.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.se183138.data.model.LoggedInUser;
import com.example.se183138.ui.login.Lab5SqliteHelper;

/**
 * Repository class responsible for managing user data operations
 * Handles registration, login, and email validation with SQLite database
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final Lab5SqliteHelper dbHelper;

    /**
     * Constructor to initialize the database helper
     * @param context Application context needed for database operations
     */
    public UserRepository(Context context) {
        dbHelper = new Lab5SqliteHelper(context);
    }

    /**
     * Registers a new user in the database
     * @param user LoggedInUser object containing email, password, and repassword
     * @return true if registration successful, false if email already exists or error occurs
     */
    public boolean register(LoggedInUser user){
        try {
            // Check if email already exists to prevent duplicates
            if (isEmailExits(user.getEmail())) {
                return false;
            }

            // Get writable database instance
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Prepare user data for insertion
            ContentValues values = new ContentValues();
            values.put(Lab5SqliteHelper.COLUMN_EMAIL, user.getEmail());
            values.put(Lab5SqliteHelper.COLUMN_PASS, user.getPassword());
            values.put(Lab5SqliteHelper.COLUMN_REPASS, user.getRepass());

            // Insert user data into database
            long result = db.insert(Lab5SqliteHelper.TABLE_USER, null, values);
            db.close();

            // Return true if insertion was successful (result != -1)
            return result != -1;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error registering user: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in register: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates user login credentials
     * @param email User's email address
     * @param pass User's password
     * @return true if login credentials are valid, false otherwise
     */
    public boolean login(String email, String pass){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            // Get readable database instance
            db = dbHelper.getReadableDatabase();

            // Define query parameters for user authentication
            String[] columns = {Lab5SqliteHelper.COLUMN_EMAIL};
            String selection = Lab5SqliteHelper.COLUMN_EMAIL + " = ? AND " + Lab5SqliteHelper.COLUMN_PASS + " = ?";
            String[] selectionArgs = {email, pass};

            // Execute query to find matching user credentials
            cursor = db.query(Lab5SqliteHelper.TABLE_USER, columns, selection, selectionArgs,
                    null, null, null);

            // Return true if user found (cursor has at least one row)
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error during login: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in login: " + e.getMessage());
            return false;
        } finally {
            // Always close resources to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Checks if an email address already exists in the database
     * @param email Email address to check
     * @return true if email exists, false otherwise
     */
    public boolean isEmailExits(String email){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            // Get readable database instance
            db = dbHelper.getReadableDatabase();

            // Define query parameters to search for email
            String[] columns = {Lab5SqliteHelper.COLUMN_EMAIL};
            String selection = Lab5SqliteHelper.COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            // Execute query to find email in database
            cursor = db.query(Lab5SqliteHelper.TABLE_USER, columns, selection, selectionArgs,
                    null, null, null);

            // Return true if email found (cursor has at least one row)
            return cursor.moveToFirst();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error checking email exists: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in isEmailExits: " + e.getMessage());
            return false;
        } finally {
            // Always close resources to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Closes the database helper to free up resources
     * Should be called when the repository is no longer needed
     */
    public void close() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            Log.e(TAG, "Error closing database: " + e.getMessage());
        }
    }
}
