package com.sobrietychecker.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sobrietychecker.Status;
import com.sobrietychecker.android.alarms.AlarmClock;

public final class CheckinActivity extends Activity {
    private static final int CONTACT_PICKER_RESULT = 1001;

    private MessageStore mMessageStore = null;
    private SponsorStore mSponsorStore = null;

    private static int getStatusStringId(int status) {
        switch (status) {
        case Status.GOOD:
            return R.string.good;
        case Status.SLIPPING:
            return R.string.slipping;
        case Status.HELP_NOW:
            return R.string.help_now;
        default:
            throw new IllegalArgumentException();
        }
    }

    private void sendMessage(int status) {
        SmsManager sms = SmsManager.getDefault();
        String body = getString(getStatusStringId(status));
        String message = mMessageStore.getMessage(status);
        if (message != null) {
            body += ": " + message;
        }
        boolean sentOne = false;
        for (Sponsor sponsor: mSponsorStore.getItems()) {
            String phoneNumber = sponsor.getMobilePhoneNumber();
            if (phoneNumber != null) {
                sms.sendTextMessage(phoneNumber, null, body, null, null);
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
        mMessageStore = MessageStore.getInstance(this);
        mSponsorStore = SponsorStore.getInstance(this);
        setContentView(R.layout.main);
        findViewById(R.id.goodButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(Status.GOOD);
            }
        });
        findViewById(R.id.slippingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(Status.SLIPPING);
            }
        });
        findViewById(R.id.helpNowButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(Status.HELP_NOW);
            }
        });
        findViewById(R.id.pickSponsorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickSponsor();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            startActivity(new Intent(this, SetMessagesActivity.class));
            updateVisibility();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_alarms:
                startActivity(new Intent(this, AlarmClock.class));
                return true;
            case R.id.menu_item_set_messages:
                startActivity(new Intent(this, SetMessagesActivity.class));
                return true;
            case R.id.menu_item_sponsors:
                startActivity(new Intent(this, SponsorsActivity.class));
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
