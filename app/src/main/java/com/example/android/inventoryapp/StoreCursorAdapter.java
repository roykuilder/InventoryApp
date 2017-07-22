package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.StoreContract.StoreEntry;

public class StoreCursorAdapter extends CursorAdapter {

    /**
     * Constructor for a new StoreCursorAdapter
     */
    public StoreCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Create a new list item when the list is empty
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Puts the data from the cursor into the corresponding views
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final int rowId = cursor.getInt(cursor.getColumnIndexOrThrow(StoreEntry._ID));
        final int cursorPos = cursor.getPosition();

        // find views
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);

        // get information from cursor
        String itemName = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_NAME));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_PRICE));

        // set the retrieved data on the textViews
        nameTextView.setText(itemName);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);

        // find and set Onclicklistener on the main part of the listItem
        LinearLayout clickView = (LinearLayout) view.findViewById(R.id.info_part_list_item);

        clickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // intent to go to the detailpage for this specific item
                Uri itemUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, rowId);
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("storeItem", itemUri.toString());
                context.startActivity(intent);
            }
        });

        // set OnClickListener on the sell button
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // put the cursor at the right position and get quantity
                cursor.moveToPosition(cursorPos);
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_QUANTITY));

                // check if quantity is allowed
                if (quantity > 0) {
                    int q = quantity - 1;

                    // Store the new quantity in ContentValues
                    ContentValues values = new ContentValues();
                    values.put(StoreEntry.PRODUCT_QUANTITY, q);

                    // create contentUri
                    Uri uri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, rowId);
                    int response = context.getContentResolver().update(uri, values, null, null);
                } else {
                    // inform user of bad quantity
                    Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}