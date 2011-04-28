package com.example.android.skeletonapp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import sncatalog.webservice.MobileEpisode;

public class EpisodeFetcher {

	public EpisodeFetcher() {
	}

	public MobileEpisode getEpisode(int episodeNumber) {

		try {
			String urlNumber;
			if (episodeNumber < 10) 
				urlNumber = "00" + Integer.toString(episodeNumber);
			else if (episodeNumber < 100) 
				urlNumber = "0" + Integer.toString(episodeNumber);
			else
				urlNumber = Integer.toString(episodeNumber);

			URL url = new URL("http://www.grc.com/sn/sn-" + urlNumber + ".txt");
			url = new URL("https://sn-catalog.appspot.com/episode/144");

			URLConnection conn = url.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							conn.getInputStream()));
			String inputLine;
			String serializedObject = "";
			while ((inputLine = in.readLine()) != null) {
				serializedObject += inputLine;
			}

			byte[] serializedBytes = hexStringToByteArray(serializedObject);
			ByteArrayInputStream bis = new ByteArrayInputStream(serializedBytes);
			ObjectInputStream ois = new ObjectInputStream(bis); 
			MobileEpisode episode = null;

			episode = (sncatalog.webservice.MobileEpisode)ois.readObject();
			ois.close(); 
			return episode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
	}

	private byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

}
