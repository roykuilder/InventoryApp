package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.StoreContract.StoreEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Global variable
    private StoreCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        // get ListView object
        ListView mainList = (ListView) findViewById(R.id.list_view);

        // add empty view to the list
        View emptyView = findViewById(R.id.empty_view);
        mainList.setEmptyView(emptyView);

        // create new adapter Object
        adapter = new StoreCursorAdapter(this, null);

        // set adapter on list view
        mainList.setAdapter(adapter);

        // set ItemClickListener on listItems
        // Take the id of the listItem and send it with intent to details activity
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri itemUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("storeItem", itemUri.toString());
                startActivity(intent);
            }
        });

        // get LoaderManager and initialize the loader. this will populate the list when the app is
        // opened with existing data in the database
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * makes options menu appear
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    /**
     * Handles the clicking of option menu items
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all:
                showDeleteConfirmationDialog();
                // Deletes all data from the database
                int deleted = getContentResolver().delete(StoreEntry.CONTENT_URI, null, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create loader to get the cursor on a background thread
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(this, StoreEntry.CONTENT_URI, null, null, null, null);
    }

    /**
     * Is called when loader has recieved the cursor. Change the cursor on the adapter
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    /**
     * When data is no longer needed. Changes cursor to null
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        // Show dialog with the choice to delete
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_entries);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // When delete is confirmed this sends the command to the resolver
                int deleted = getContentResolver().delete(StoreEntry.CONTENT_URI, null, null);
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
}