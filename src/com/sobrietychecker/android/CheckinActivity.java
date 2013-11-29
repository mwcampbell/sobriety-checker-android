package com.sobrietychecker.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;

public class CheckinActivity extends Activity {
    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final String PREF_SPONSOR_CONTACT_URI = "sponsor_contact_uri";

    private SharedPreferences mPrefs;

    private String getMobilePhoneNumber(Uri contactUri) {
        String contactId = contactUri.getLastPathSegment();
        Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] {contactId}, null);
        String phoneNumber = null;
        if (cursor.moveToFirst()) {
            do {
                int typeIdx = cursor.getColumnIndex(Phone.TYPE);
                int type = cursor.getInt(typeIdx);
                if (type == Phone.TYPE_MOBILE) {
                    int phoneIdx = cursor.getColumnIndex(Phone.DATA);
                    phoneNumber = cursor.getString(phoneIdx);
                    break;
                }
            } while (cursor.moveToNext());
        }
        return phoneNumber;
    }

    private void sendMessage(int bodyStringId) {
        Uri contactUri = Uri.parse(mPrefs.getString(PREF_SPONSOR_CONTACT_URI, null));
        String phoneNumber = getMobilePhoneNumber(contactUri);
        if (phoneNumber != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", getString(R.string.message_body_prefix) + " " + getString(bodyStringId));
            startActivity(intent);
            finish();
            return;
        }
    }

    private void pickSponsor() {
        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    private void updateVisibility() {
        boolean hasSponsor = mPrefs.contains(PREF_SPONSOR_CONTACT_URI);
        findViewById(R.id.hasSponsor).setVisibility(hasSponsor ? View.VISIBLE : View.GONE);
        findViewById(R.id.noSponsor).setVisibility(!hasSponsor ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.main);
        findViewById(R.id.goodButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(R.string.good);
            }
        });
        findViewById(R.id.slippingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(R.string.slipping);
            }
        });
        findViewById(R.id.helpNowButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(R.string.help_now);
            }
        });
        findViewById(R.id.pickSponsorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickSponsor();
            }
        });
        updateVisibility();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == CONTACT_PICKER_RESULT) {
            Uri contactUri = data.getData();
            if (getMobilePhoneNumber(contactUri) == null) {
                new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(getString(R.string.no_mobile_phone_number)).setCancelable(true).setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
                return;
            }
            mPrefs.edit().putString(PREF_SPONSOR_CONTACT_URI, contactUri.toString()).commit();
            updateVisibility();
        }
    }
}
