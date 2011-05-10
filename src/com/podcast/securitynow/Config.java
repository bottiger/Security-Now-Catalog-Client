package com.podcast.securitynow;

import java.io.File;

import android.content.Context;

public class Config {
	
	private static Context context;
	public static File DATA_DIR;
	
	public static void setContext(Context c) {
		context = c;
		DATA_DIR = c.getExternalFilesDir(null);
		
	}

}
