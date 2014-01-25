package com.sobrietychecker.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.view.View;

import com.sobrietychecker.android.alarms.AlarmClock;

public class CheckinActivity extends Activity {
    private static final int CONTACT_PICKER_RESULT = 1001;

    private SponsorStore mSponsorStore = null;

    private void sendMessage(int bodyStringId) {
        SmsManager sms = SmsManager.getDefault();
        String body = getString(R.string.message_body_prefix) + " " + getString(bodyStringId);
        boolean sentOne = false;
        for (Sponsor sponsor: mSponsorStore.getItems()) {
            String phoneNumber = sponsor.getMobilePhoneNumber();
            if (phoneNumber != null) {
                sms.sendTextMessage(phoneNumber, null, getString(R.string.message_body_prefix) + " " + getString(bodyStringId), null, null);
                sentOne = true;
            }
        }
        if (sentOne) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(getString(R.string.message_sent_prefix) + " " + body).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
    }

    private void pickSponsor() {
        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    private void updateVisibility() {
        boolean hasSponsor = mSponsorStore.getItemCount() > 0;
        findViewById(R.id.hasSponsor).setVisibility(hasSponsor ? View.VISIBLE : View.GONE);
        findViewById(R.id.noSponsor).setVisibility(!hasSponsor ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSponsorStore = SponsorStore.getInstance(this);
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
        findViewById(R.id.alarmsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckinActivity.this, AlarmClock.class));
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
            updateVisibility();
        }
    }
}
