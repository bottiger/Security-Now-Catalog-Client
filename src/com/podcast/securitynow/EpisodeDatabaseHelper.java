package com.podcast.securitynow;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EpisodeDatabaseHelper extends SQLiteOpenHelper {
    
    public static final String TABLE		= "episodes";
    
    public static final String EPISODE 		= "episode";
    public static final String TITLE 		= "title";
    public static final String LINK 		= "link";
    public static final String PUBDATE 		= "pubDate";
    public static final String DURATION 	= "duration";
    public static final String DESCRIPTION 	= "description";
    public static final String TRANSSCRIPT 	= "transscript";
    public static final String LISTENED 	= "listened";
    public static final String SHOWNOTES 	= "snownotes";
    
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "episodeStore.db";
    private static final String DICTIONARY_TABLE_NAME = "episodes";
    private static final String DICTIONARY_TABLE_CREATE =
                "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
                EPISODE + " INTEGER PRIMARY KEY, " +
                TITLE + " TEXT," +
                LINK + " TEXT," +
                DESCRIPTION + " TEXT," +
                TRANSSCRIPT + " TEXT," +
                PUBDATE + " INTEGER," +
                DURATION + "  INTEGER," +
                LISTENED + " INTEGER," + 
                SHOWNOTES + " TEXT" +
                ");";
    
    EpisodeDatabaseHelper(Context context) {
        super(context, getDataPath(), null, DATABASE_VERSION);
    }
    
    private static String getDataPath() {
    	String path = Config.appFolder + File.separator + DATABASE_NAME;
    	return path;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
