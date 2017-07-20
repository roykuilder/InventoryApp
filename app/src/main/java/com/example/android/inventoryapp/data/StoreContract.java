package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Kuilder on 19-07-17.
 */

public final class StoreContract {

    public void StoreContract(){
    }

    //Global variables
    //Authority
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    // Base content Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Path name
    public static final String PATH_ITEMS = "items";


    public static final class StoreEntry implements BaseColumns {

        // MIME type for list
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // MIME type for single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // base Uri for contacting the database
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // Table name for store items
        public final static String TABLE_NAME = "items";

        // ID number for store items
        public final static String _ID = BaseColumns._ID;

        /**
         * Column names for the items table
         */

        // name
        public final static String PRODUCT_NAME = "name";

        // quantity
        public final static String PRODUCT_QUANTITY = "quantity";

        // Price
        public final static String PRODUCT_PRICE = "price";

        // supplier webpage
        public final static String PRODUCT_SUPP_WEB = "supplierwebsite";







    }
}
