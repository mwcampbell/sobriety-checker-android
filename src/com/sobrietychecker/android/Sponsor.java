package com.sobrietychecker.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class Sponsor {
    private final Context mContext;

    public final String contactUri;

    public Sponsor(Context context, String contactUri) {
        mContext = context;
        this.contactUri = contactUri;
    }

    public String getDisplayName() {
        Uri contactUriObject = Uri.parse(contactUri);
        String contactId = contactUriObject.getLastPathSegment();
        Cursor cursor = mContext.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] {contactId}, null);
        String displayName = null;
        if (cursor.moveToFirst()) {
            int displayNameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
            displayName = cursor.getString(displayNameIdx);
        }
        return displayName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public String getMobilePhoneNumber() {
        Uri contactUriObject = Uri.parse(contactUri);
        String contactId = contactUriObject.getLastPathSegment();
        Cursor cursor = mContext.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] {contactId}, null);
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

    public boolean isValid() {
        return getMobilePhoneNumber() != null;
    }

    public void writeJson(JsonWriter out) throws IOException {
        out.beginObject();
        out.name("contactUri");
        out.value(contactUri);
        out.endObject();
    }

    public static Sponsor fromJson(JsonReader in, Context context) throws IOException {
        String contactUri = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("contactUri")) {
                contactUri = in.nextString();
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        if (contactUri == null) {
            throw new IllegalArgumentException();
        }
        return new Sponsor(context, contactUri);
    }
}
