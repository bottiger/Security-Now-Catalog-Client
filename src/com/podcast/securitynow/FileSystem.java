package com.podcast.securitynow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;

import sncatalog.shared.Serializer;

public class FileSystem {
	
	public static boolean save(Object o, File file) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			byte[] fileBytes = Serializer.byteSerialize(o);
			fos.write(fileBytes);
			fos.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static Object load(File file) {
		Object o = null;
		try {
			o = Serializer.deserialize(file);
		} catch (FileNotFoundException e) {
			return null;
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}

}
