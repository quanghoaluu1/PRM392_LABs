package com.example.se183138.activity.lab5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database helper class for managing user authentication database
 * Handles database creation, upgrades, and table structure definition
 */
public class Lab5SqliteHelper extends SQLiteOpenHelper {
    private static final String TAG = "Lab5SqliteHelper";

    // Database configuration constants
    private static final String DATABASE_NAME = "SE183138.db";
    private static final int DATABASE_VERSION = 2;

    // Table and column name constants for user data
    public static final String TABLE_USER = "Tbl_user";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASS = "pass";
    public static final String COLUMN_REPASS = "repass";

    /**
     * Constructor initializes the database helper with predefined settings
     * @param context Application context needed for database operations
     */
    public Lab5SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time
     * Creates the user table with email as primary key and password fields
     * @param db SQLiteDatabase instance to execute SQL commands on
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        try {
            // SQL statement to create user table
            // email: primary key field for unique user identification
            // pass: user's password field
            // repass: password confirmation field (stored for validation purposes)
            String CREATE_TABLE = "create table IF NOT EXISTS " + TABLE_USER + "(" +
                     COLUMN_EMAIL + " text primary key ," +
                     COLUMN_PASS + " text," +
                     COLUMN_REPASS + " text)";

            // Execute the table creation command
            db.execSQL(CREATE_TABLE);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error creating database table: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in onCreate: " + e.getMessage());
        }
    }

    /**
     * Called when the database needs to be upgraded (version number increased)
     * Drops existing table and recreates it to ensure clean schema
     * @param db SQLiteDatabase instance to execute SQL commands on
     * @param oldVersion Previous version number of the database
     * @param newVersion New version number of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Drop existing table to start fresh (note: this will lose all data)
            db.execSQL("DROP TABLE IF EXISTS Tbl_user");

            // Recreate the table with current schema
            onCreate(db);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in onUpgrade: " + e.getMessage());
        }
    }
}
