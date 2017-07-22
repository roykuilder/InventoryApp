package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventoryapp.data.StoreContract.StoreEntry;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Global variables
    private boolean changedData = false;
    private EditText editProductName;
    private EditText editPrice;
    private EditText editQuantity;
    private EditText editSupplier;
    private EditText editChangeQuantity;
    private int quantity;
    private Uri itemUri;
    boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        // Get intent that was used to open this activity
        Intent intent = getIntent();
        setTitle(R.string.activity_details_insert_mode);

        // Check is intent has Uri and set editMode to true to load the item data
        if (intent.hasExtra("storeItem")) {
            itemUri = Uri.parse(intent.getStringExtra("storeItem"));
            setTitle(R.string.activity_details_title);
            editMode = true;

            getLoaderManager().initLoader(0, null, this);
        }

        // Find all relevant views that we will need to read user input from
        editProductName = (EditText) findViewById(R.id.edit_product_name);
        editPrice = (EditText) findViewById(R.id.edit_price);
        editSupplier = (EditText) findViewById(R.id.edit_supplier);
        editQuantity = (EditText) findViewById(R.id.edit_quantity);
        editChangeQuantity = (EditText) findViewById(R.id.change_quantity);
        ImageButton emailSupplier = (ImageButton) findViewById(R.id.email_supplier);
        Button subtractButton = (Button) findViewById(R.id.change_q_min);
        Button addButton = (Button) findViewById(R.id.change_q_plus);

        // set OntouchListeners to keep track of information changes
        editProductName.setOnTouchListener(mTouchListener);
        editPrice.setOnTouchListener(mTouchListener);
        editSupplier.setOnTouchListener(mTouchListener);
        editQuantity.setOnTouchListener(mTouchListener);
        editChangeQuantity.setOnTouchListener(mTouchListener);

        //ClickListeners
        emailSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the product name and emailadres from the views
                String currentEmailAddress = editSupplier.getText().toString();
                String currentProduct = editProductName.getText().toString();

                // Create and send email Intent to mail the supplier
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                //TODO: email komt niet aan in email client
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{currentEmailAddress});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Product Order: " + currentProduct);

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the edit field is nog empty or null
                if (!editChangeQuantity.getText().toString().equals("")) {
                    int toAdd = Integer.parseInt(editChangeQuantity.getText().toString());
                    int newQuantity = quantity + toAdd;

                    // Check if the new quantity is allowed into the database
                    if (newQuantity > 0) {
                        ContentValues values = new ContentValues();
                        values.put(StoreEntry.PRODUCT_QUANTITY, newQuantity);

                        // send the update to the contentresolver
                        int response = getContentResolver().update(itemUri, values, null, null);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.negative_stock, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        subtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editChangeQuantity.getText().toString().equals("")) {
                    int toSubtract = Integer.parseInt(editChangeQuantity.getText().toString());
                    int newQuantity = quantity - toSubtract;

                    // Check if the new quantity is allowed into the database
                    if (newQuantity > 0) {
                        ContentValues values = new ContentValues();
                        values.put(StoreEntry.PRODUCT_QUANTITY, newQuantity);

                        // send the update to the contentresolver
                        int response = getContentResolver().update(itemUri, values, null, null);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.negative_stock, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    // Store is any changes are made
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            changedData = true;
            return false;
        }
    };

    // If unsaved data is present notifies the user with alert dialog
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_data);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.stay_here, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // if stay here is clicked do noting and dismiss dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Show the alert dialog to user
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        // If no data is changed return to previous activity as normal
        if (!changedData) {
            super.onBackPressed();
            return;
        }

        // If there is unsaved data. Warn the user about it through the dialog
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Method to save the information on screen and return to the main activity
     */
    private void saveItem() {
        // Clean up all the inputs before validating them
        String nameInput = editProductName.getText().toString().trim();
        String priceInput = editPrice.getText().toString().trim();
        String supplierInput = editSupplier.getText().toString().trim();
        String quantityInput = editQuantity.getText().toString().trim();

        // Check and inform the user of any missing data
        if (nameInput.equals("")) {
            Toast.makeText(DetailsActivity.this, R.string.no_product_name, Toast.LENGTH_LONG).show();
        } else if (priceInput.equals("")) {
            Toast.makeText(DetailsActivity.this, R.string.no_price, Toast.LENGTH_LONG).show();
        } else if (supplierInput.equals("")) {
            Toast.makeText(DetailsActivity.this, R.string.no_supplier_name, Toast.LENGTH_LONG).show();
        } else if (quantityInput.equals("")) {
            Toast.makeText(DetailsActivity.this, R.string.no_quantity, Toast.LENGTH_LONG).show();
        } else {

            // Create Values Object and insert all data from editText fields.
            ContentValues saveItem = new ContentValues();
            saveItem.put(StoreEntry.PRODUCT_NAME, nameInput);
            saveItem.put(StoreEntry.PRODUCT_PRICE, priceInput);
            saveItem.put(StoreEntry.PRODUCT_SUPP_WEB, supplierInput);
            saveItem.put(StoreEntry.PRODUCT_QUANTITY, quantityInput);

            // Depending on the mode. Save data to database
            if (editMode) {
                int updateResponse = getContentResolver().update(itemUri, saveItem, null, null);

                if (updateResponse == 1) {
                    Toast.makeText(getApplicationContext(), R.string.product_saved, Toast.LENGTH_LONG).show();
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.not_saved, Toast.LENGTH_LONG).show();
                }
            } else {
                Uri insertResponse = getContentResolver().insert(StoreEntry.CONTENT_URI, saveItem);
                NavUtils.navigateUpFromSameTask(this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Set the menu items in the app barr
        getMenuInflater().inflate(R.menu.menu_details_screen, menu);

        // Hide delete button when adding a new Product Item
        if (!editMode) {
            MenuItem menuItem = menu.findItem(R.id.menu_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Choose action based on the item clicked in the App Barr
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveItem();
                return true;
            case R.id.menu_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If there are no unsaved changes. Navigate up from this Activity
                if (!changedData) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                // If there are unsaved changes. Warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // If data does not need to be save. Navigate up from this Activity
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                // Show dialog to user
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Show dialog with the coice to delete or return
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteItem() {
        int deleted = getContentResolver().delete(itemUri, null, null);
        Toast toast = Toast.makeText(getApplicationContext(), R.string.delete_notification, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Create new CursorLoader to get data specified by the Uri
        return new CursorLoader(this, itemUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // if cursor is empty then do nothing.
        if (cursor.moveToFirst()) {

            // Get values from cursor
            String name = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_NAME));
            double price = cursor.getInt(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_PRICE));
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_QUANTITY));
            String site = cursor.getString(cursor.getColumnIndexOrThrow(StoreEntry.PRODUCT_SUPP_WEB));

            // Set text on editText fields
            editProductName.setText(name);
            editPrice.setText(Double.toString(price));
            editQuantity.setText(Integer.toString(quantity));
            editSupplier.setText(site);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
