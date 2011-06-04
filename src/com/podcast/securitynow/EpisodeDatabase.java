package com.podcast.securitynow;

import java.util.ArrayList;
import java.util.Date;

import sncatalog.shared.MobileEpisode;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public class EpisodeDatabase {
	
	private EpisodeDatabaseHelper db;
	private SQLiteDatabase writeDB;
	private SQLiteDatabase readDB;
	
	private final int LIMIT = 10; 
	
	EpisodeDatabase(Context context) {
		db = new EpisodeDatabaseHelper(context);
		this.writeDB = db.getWritableDatabase();
		this.readDB = db.getReadableDatabase();
	}
	
	/**
	 * @param episode
	 */
	public void addEpisode(Episode episode) {
	    int res = (int) writeDB.insert(EpisodeDatabaseHelper.TABLE, 
	    				null, 
	    				getEpisodeValues(episode)
	    			);
	    
	    if (res == -1)
	    	res = 0;
	}
	
	public void updateEpisode(Episode episode) {
		writeDB.update(EpisodeDatabaseHelper.TABLE, 
						getEpisodeValues(episode), 
						EpisodeDatabaseHelper.EPISODE+"=?", 
						new String[] {episode.getEpisode().toString()} 
		);
	}
	
	public long countEpisodes() {
		return DatabaseUtils.queryNumEntries(readDB,EpisodeDatabaseHelper.TABLE);
	}
	
	public Episode getEpisode(int number) {
		return getEpisodes(number, 1).get(0);
	}
	
	public ArrayList<Episode> getEpisodes(int number) {
		return getEpisodes(number, this.LIMIT);
	}
	
	public ArrayList<Episode> getRecentEpisodes() {
		return this.getEpisodes(0);
	}
	
	public String getProperty(int episodeNumber, String property) {
		String condition = EpisodeDatabaseHelper.EPISODE+" = ?";
		String[] column = {property};
		String[] conditionArg = {new Integer(episodeNumber).toString()};
		
		String q = SQLiteQueryBuilder.buildQueryString(false,
				EpisodeDatabaseHelper.TABLE, 
				column, 
				condition, 
				null, 
				null, 
				null, 
				"1");
		
		Cursor c = readDB.rawQuery(q, conditionArg);
		c.moveToFirst();
		
		String propValue = c.getString(c.getColumnIndex(property));
		return propValue;
	}
	
	public ArrayList<Episode> getEpisodes(int number, int limit) {
		String condition = EpisodeDatabaseHelper.EPISODE+" <= ?";
		//String[] condition = new String[1];
		//condition[0] = str;
		String[] conditionArgs = {new Integer(number).toString()}; 
		
		String[] tables = new String[1];
		tables[0] = EpisodeDatabaseHelper.TABLE;
		
		if (number == 0) {
			condition = null;
			conditionArgs = null;
		}
		
		//SQLiteQueryBuilder sqlqb = new SQLiteQueryBuilder();
		String q = SQLiteQueryBuilder.buildQueryString(false,
						EpisodeDatabaseHelper.TABLE, 
						null, 
						condition, 
						null, 
						null, 
						EpisodeDatabaseHelper.EPISODE + " DESC", 
						new Integer(limit).toString());
		
		Cursor c = readDB.rawQuery(q, conditionArgs);
		/*
		Cursor c = readDB.query(EpisodeDatabaseHelper.TABLE, 
								null, 
								condition, 
								conditionArgs, 
								null, 
								null, 
								EpisodeDatabaseHelper.EPISODE + " desc", 
								new Integer(limit).toString());
		*/
		int episode;
		String title;
		String link;
		Date pubDate;
		String description;
		String transscript;
		int duration;
		
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		
		
		c.moveToFirst();
        while (c.isAfterLast() == false) {
            //view.append("n" + c.getString(1));
        	episode = c.getInt(c.getColumnIndex(EpisodeDatabaseHelper.EPISODE));
        	title = c.getString(c.getColumnIndex(EpisodeDatabaseHelper.TITLE));
        	link = c.getString(c.getColumnIndex(EpisodeDatabaseHelper.LINK));
        	
        	int index = c.getColumnIndex(EpisodeDatabaseHelper.PUBDATE);
        	int pubDateInt = c.getInt(index);
        	transscript = c.getString(c.getColumnIndex(EpisodeDatabaseHelper.TRANSSCRIPT));
        	description = c.getString(c.getColumnIndex(EpisodeDatabaseHelper.DESCRIPTION));
        	duration = c.getInt(c.getColumnIndex(EpisodeDatabaseHelper.DURATION));

        	
        	pubDate = new Date(pubDateInt);
        	
    		MobileEpisode me = new MobileEpisode(episode,
    								title,
    								link,
    								pubDate,
    								description,
    								transscript,
    								duration);
    		
    		episodes.add(new Episode(me));
       	    c.moveToNext();
        }
        c.close();
		
		return episodes;
	}
	
	private ContentValues getEpisodeValues(Episode episode) {
		ContentValues values = new ContentValues();
		values.put(EpisodeDatabaseHelper.EPISODE, episode.getEpisode());
	    values.put(EpisodeDatabaseHelper.TITLE, episode.getTitle());
	    values.put(EpisodeDatabaseHelper.LINK, episode.getLink());
	    values.put(EpisodeDatabaseHelper.PUBDATE, episode.getPubDate().getTime());
	    values.put(EpisodeDatabaseHelper.DURATION, episode.getDuration());
	    values.put(EpisodeDatabaseHelper.DESCRIPTION, episode.getDescription());
	    values.put(EpisodeDatabaseHelper.TRANSSCRIPT, episode.getTransscript());
		return values;
	}

}
