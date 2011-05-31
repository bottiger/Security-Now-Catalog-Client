package com.podcast.securitynow;

import java.io.File;

import android.os.Environment;

public class Config {
	
	private final static String FOLDER_NAME = "sn";
	
	// added a trailing slash
	public final static File appFolder = new File(Environment.getExternalStorageDirectory(), File.separator + FOLDER_NAME + File.separator);
	
}
