package com.sobrietychecker.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public final class SponsorsActivity extends Activity {
    private static final int CONTACT_PICKER_RESULT = 1001;

    private ListView mMainView;
    private SponsorStore mSponsorStore;
    private ArrayList<Sponsor> mSponsors = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sponsors);
        mSponsorStore = SponsorStore.getInstance(this);
        mMainView = (ListView)findViewById(R.id.sponsors_list);
        mMainView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                deleteItem(arg2);
            }
        });
        findViewById(R.id.addSponsorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSponsor();
            }
        });
    }

    private void loadList() {
        mSponsors = mSponsorStore.getItems();
        ArrayAdapter<Sponsor> adapter = new ArrayAdapter<Sponsor>(this, android.R.layout.simple_list_item_1, mSponsors);
        mMainView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadList();
    }

    private void deleteItem(final int index) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_sponsor))
                .setMessage(getString(R.string.delete_sponsor_confirm))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                mSponsorStore.remove(mSponsors.get(index).contactUri);
                                loadList();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addSponsor() {
        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == CONTACT_PICKER_RESULT) {
            String contactUri = data.getData().toString();
            Sponsor sponsor = new Sponsor(this, contactUri);
            if (!sponsor.isValid()) {
                new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(getString(R.string.no_mobile_phone_number)).setCancelable(true).setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                return;
            }
            mSponsorStore.add(sponsor);
            loadList();
        }
    }
}
