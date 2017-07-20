package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
     * Puts the date from the cursor into the corresponding views
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        String itemName = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_NAME));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_PRICE));

        nameTextView.setText(itemName);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);
    }
}