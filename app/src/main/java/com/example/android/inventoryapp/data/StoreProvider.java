package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.data.StoreContract.StoreEntry;

public class StoreProvider extends ContentProvider {

    // Global variables
    private StoreDbHelper storeDbHelper;

    // Values to use in Uri matcher
    public static final int ITEMS = 1;
    public static final int ITEMS_ID = 2;

    // URI matcher to match the incoming uri
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Store Uri values to match with the constants
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_ITEMS + "/#", ITEMS_ID);
    }

    /**
     * onCreate: make new StoreDbHelper Object.
     */
    @Override
    public boolean onCreate() {
        storeDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    /**
     * Perform main query on database.
     * read only
     * case for the whole database and a single item
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = storeDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Match the incoming Uri with the Uri matcher
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Uri for complete database
                // return all the rows with the selected projection
                cursor = database.query(StoreEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEMS_ID:
                // Uri for a single item
                // selection and selectionArgs are made from the uri
                selection = StoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // query the database with incoming params
                cursor = database.query(StoreEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a new Product into the database
     */
    private Uri insertItem(Uri uri, ContentValues values) {

        // check for bad values
        String name = values.getAsString(StoreEntry.PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String supplierWebSite = values.getAsString(StoreEntry.PRODUCT_SUPP_WEB);
        if (supplierWebSite == null) {
            throw new IllegalArgumentException("Product requires a website");
        }

        int quantity = values.getAsInteger(StoreEntry.PRODUCT_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Product quantity cannot be below 0");
        }

        // get writable database
        SQLiteDatabase database = storeDbHelper.getWritableDatabase();

        // insert new item into database with ContentValues
        long id = database.insert(StoreEntry.TABLE_NAME, null, values);

        // Log when insert failed
        if (id == -1) {
            Log.e("insert() StoreProvider:", "Failed to insert row for " + uri);
            return null;
        }

        // notify changelistener to reload cursor
        getContext().getContentResolver().notifyChange(uri, null);

        // return the Uri with the new ID of the inserted item
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update a single Item
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(StoreEntry.PRODUCT_NAME)) {
            String name = values.getAsString(StoreEntry.PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires name");
            }
        }

        if (values.containsKey(StoreEntry.PRODUCT_SUPP_WEB)) {
            if (values.getAsString(StoreEntry.PRODUCT_SUPP_WEB) == null) {
                throw new IllegalArgumentException("Product requires webpage");
            }
        }

        // Match Uri to take corresponding action
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                //update all items
                return updateItem(uri, values, selection, selectionArgs);
            case ITEMS_ID:
                // update a single items with id
                selection = StoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // get writable database
        SQLiteDatabase database = storeDbHelper.getWritableDatabase();

        // update the database
        int response = database.update(StoreEntry.TABLE_NAME, values, selection, selectionArgs);

        // if data is update. notify the cursor to reload its data.
        if (response > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return response;
    }

    /**
     * Delete data according to input
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = storeDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows
                int deletedItems = database.delete(StoreEntry.TABLE_NAME, selection, selectionArgs);
                if (deletedItems != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return deletedItems;
            case ITEMS_ID:
                // Delete a single row specified by Uri
                selection = StoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int deleted = database.delete(StoreEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * getType to handle MIME requests
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return StoreEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return StoreEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
