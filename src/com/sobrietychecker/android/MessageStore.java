package com.sobrietychecker.android;

import android.content.Context;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import com.sobrietychecker.Status;

public final class MessageStore {
    private static final String FILE_NAME = "messages.json";

    private final Context mContext;
    private final HashMap<String, String> mMap = new HashMap<String, String>();

    private MessageStore(Context context) {
        mContext = context.getApplicationContext();
        try {
            FileInputStream fin = context.openFileInput(FILE_NAME);
            if (fin == null) {
                return;
            }
            JsonReader in = new JsonReader(new InputStreamReader(new BufferedInputStream(fin), "UTF-8"));
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                int status = Status.fromName(name);
                if (status == Status.INVALID) {
                    in.skipValue();
                } else {
                    String message = in.nextString();
                    mMap.put(name, message);
                }
            }
            in.endObject();
            in.close();
        } catch (IOException e) {
        }
    }

    public boolean hasMessage(int status) {
        String name = Status.toName(status);
        return mMap.containsKey(name) && mMap.get(name) != null;
    }

    public boolean hasAllMessages() {
        return hasMessage(Status.GOOD) && hasMessage(Status.SLIPPING) && hasMessage(Status.HELP_NOW);
    }

    public String getMessage(int status) {
        if (!hasMessage(status)) {
            return null;
        }
        return mMap.get(Status.toName(status));
    }

    private void save() {
        try {
            JsonWriter out = new JsonWriter(new OutputStreamWriter(new BufferedOutputStream(mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)), "UTF-8"));
            out.beginObject();
            for (String name: mMap.keySet()) {
                String message = mMap.get(name);
                if (message == null) {
                    continue;
                }
                out.name(name);
                out.value(message);
            }
            out.endObject();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMessage(int status, String message) {
        mMap.put(Status.toName(status), message);
    }

    private static MessageStore sInstance = null;
    public static MessageStore getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MessageStore(context);
        }
        return sInstance;
    }
}
