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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public final class SponsorStore {
    private final Context mContext;
    private final HashMap<String, Sponsor> mMap = new HashMap<String, Sponsor>();

    private SponsorStore(Context context) {
        mContext = context.getApplicationContext();
        try {
            FileInputStream fin = context.openFileInput("sponsors.json");
            if (fin == null) {
                return;
            }
            JsonReader in = new JsonReader(new InputStreamReader(new BufferedInputStream(fin), "UTF-8"));
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("items")) {
                    in.beginArray();
                    while (in.hasNext()) {
                        Sponsor item = Sponsor.fromJson(in, mContext);
                        if (!item.isValid()) {
                            continue;
                        }
                        mMap.put(item.contactUri, item);
                    }
                    in.endArray();
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            in.close();
        } catch (IOException e) {
        }
    }

    public int getItemCount() {
        return mMap.size();
    }

    public ArrayList<Sponsor> getItems() {
        ArrayList<Sponsor> items = new ArrayList<Sponsor>();
        for (Sponsor item: mMap.values()) {
            items.add(item);
        }
        Collections.sort(items, new Comparator<Sponsor>() {
            @Override
            public int compare(Sponsor a, Sponsor b) {
                return a.contactUri.compareTo(b.contactUri);
            }
        });
        return items;
    }

    public boolean has(String contactUri) {
        return mMap.containsKey(contactUri);
    }

    private void save() {
        try {
            JsonWriter out = new JsonWriter(new OutputStreamWriter(new BufferedOutputStream(mContext.openFileOutput("sponsors.json", Context.MODE_PRIVATE)), "UTF-8"));
            out.beginObject();
            out.name("items");
            out.beginArray();
            for (Sponsor item: mMap.values()) {
                item.writeJson(out);
            }
            out.endArray();
            out.endObject();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(Sponsor item) {
        mMap.put(item.contactUri, item);
        save();
    }

    public void remove(String contactUri) {
        mMap.remove(contactUri);
    }

    private static SponsorStore sInstance = null;
    public static SponsorStore getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SponsorStore(context);
        }
        return sInstance;
    }
}
