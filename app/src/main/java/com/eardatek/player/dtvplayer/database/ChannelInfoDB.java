package com.eardatek.player.dtvplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.blazevideo.libdtv.ChannelInfo;
import com.eardatek.player.dtvplayer.system.DTVApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChannelInfoDB {
    public final static String TAG = "DTV/ChannelInfoDB";

    private static ChannelInfoDB instance;

    private SQLiteDatabase mDb;
    private final String DB_NAME = "DTV_DB_CHENGDU";
    private final int DB_VERSION = 10;

    private final String MEDIA_TABLE_NAME = "media_table";
    private final String MEDIA_TABLE_ID = "_id";
    private final String MEDIA_LOCATION = "location";
    private final String MEDIA_TITLE = "title";

    private ChannelInfoDB(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        this.mDb = helper.getWritableDatabase();
    }

    public  static ChannelInfoDB getInstance() {
        if (instance == null){
            synchronized (ChannelInfo.class){
                if (instance == null) {
                    instance = new ChannelInfoDB(DTVApplication.getAppContext());
                }
            }
        }
        return instance;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public SQLiteDatabase getWritableDatabase() {
            SQLiteDatabase db;
            try {
                return super.getWritableDatabase();
            } catch(SQLiteException e) {
                try {
                    db = SQLiteDatabase.openOrCreateDatabase(DTVApplication.getAppContext().getDatabasePath(DB_NAME), null);
                } catch(SQLiteException e2) {
                    Log.w(TAG, "SQLite database could not be created! Channel library cannot be saved.");
                    db = SQLiteDatabase.create(null);
                }
            }
            int version = db.getVersion();
            if (version != DB_VERSION) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, DB_VERSION);
                    }
                    db.setVersion(DB_VERSION);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            return db;
        }

        public void createMediaTableQuery(SQLiteDatabase db) {
            String query = "CREATE TABLE IF NOT EXISTS "
                    + MEDIA_TABLE_NAME + " ("
                    + MEDIA_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + MEDIA_LOCATION + " TEXT NOT NULL, "
                    + MEDIA_TITLE + " TEXT NOT NULL"
                    + ");";
            db.execSQL(query);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            createMediaTableQuery(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private int getID(String location, String title){
        Cursor c = mDb.query(MEDIA_TABLE_NAME,new String[]{"_id"},"location =? AND title=?",new String[]{location,title},null,null,null,null);
        if (c != null && c.moveToFirst()) //if the row exist then return the id
            return c.getInt(c.getColumnIndex("_id"));
        if (c != null)
            c.close();
        return -1;
    }

    public synchronized void insertOrUpdate(String location, String title){
        ContentValues values = new ContentValues();
        values.put(MEDIA_LOCATION,location);
        values.put(MEDIA_TITLE,title);

        int id = getID(location,title);
        if(id==-1)
            mDb.insert(MEDIA_TABLE_NAME, null, values);
        else
            mDb.update(MEDIA_TABLE_NAME, values, "_id=?", new String[]{Integer.toString(id)});
    }

    public void addChannelInfo(ChannelInfo media) {
//        mDb.replace(MEDIA_TABLE_NAME, MEDIA_LOCATION, values);
//        Log.i(TAG,"add chanel info");
        insertOrUpdate( media.getLocation(),media.getTitle());
    }

    public synchronized List<ChannelInfo> getAllChannelInfo()
    {
    	List<ChannelInfo> ret = new ArrayList<>();
        int chunk_count = 0;
        Cursor cursor = null;
        try{
            int CHUNK_SIZE = 500;
            cursor = mDb.rawQuery(String.format(Locale.US,
                    "SELECT %s,%s FROM %s LIMIT %d OFFSET %d",
                    MEDIA_TITLE,
                    MEDIA_LOCATION,
                    MEDIA_TABLE_NAME,
                    CHUNK_SIZE,
                    chunk_count * CHUNK_SIZE), null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String location = cursor.getString(1);
                    String serviceName = cursor.getString(0) ;
                    ChannelInfo media = new ChannelInfo(location, serviceName );

                    ret.add(media);

                } while (cursor.moveToNext());
                cursor.close();
            }

        }catch (IllegalArgumentException e){
            if (cursor != null)
                cursor.close();
            return null;
        }finally {
            if (cursor != null)
                cursor.close();
        }

    	
    	return ret;
    }

    public synchronized List<ChannelInfo> getAllVideoProgram(){
        List<ChannelInfo> videoList = new ArrayList<>();
        List<ChannelInfo> allList = getAllChannelInfo();
        for (ChannelInfo channelInfo : allList){
            String params[] = channelInfo.getLocation().split("-");
            int isradio = Integer.parseInt(params[4].substring(7));
            if (isradio != 1)
                videoList.add(channelInfo);
        }
        return videoList;
    }

    public synchronized List<ChannelInfo> getAllRadioProgram(){
        List<ChannelInfo> radioList = new ArrayList<>();
        List<ChannelInfo> allList = getAllChannelInfo();
        for (ChannelInfo channelInfo : allList){
            String params[] = channelInfo.getLocation().split("-");
            int isradio = Integer.parseInt(params[4].substring(7));
            if (isradio == 1)
                radioList.add(channelInfo);
        }

        return radioList;
    }

    public  synchronized ChannelInfo getChannelInfo(String location) {

        Cursor cursor = null;
        ChannelInfo media = null;

        try {
            cursor = mDb.query(
                MEDIA_TABLE_NAME,
                new String[] {
                        MEDIA_TITLE 
                },
                MEDIA_LOCATION + "=?",
                new String[] { location },
                null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                media = new ChannelInfo(location,
                        cursor.getString(0));
            }
        } catch(IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: the bind value at index 1 is null
            if (cursor != null)
                cursor.close();
            return null;
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }

        return media;
    }

    /**
     * Empty the database for debugging purposes
     */
    public synchronized void emptyDatabase() {
        mDb.delete(MEDIA_TABLE_NAME, null, null);
    }

    public synchronized int deleteChanelInfo(String title,String location){
        String whereClause = "location=?";
        String[] whereArgs = {location};
        return mDb.delete(MEDIA_TABLE_NAME,whereClause,whereArgs);
    }
}
