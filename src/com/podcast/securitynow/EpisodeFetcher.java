package com.podcast.securitynow;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import sncatalog.shared.*;

public class EpisodeFetcher {
	
	private static final String BASE_URL = "http://sn-catalog.appspot.com/";
	private File dataDir;

	public EpisodeFetcher(File dataDir) {
		this.dataDir = dataDir;
		if (!dataDir.exists())	dataDir.mkdir();
	}
	
	public ArrayList<MobileEpisode> getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
		URI url = new URI(BASE_URL + "lite-episode/new");
		return (ArrayList) getRemoteObject(url);
	}
	
	public ArrayList<MobileEpisode> getNew(ArrayList<MobileEpisode> current) {
		ArrayList<MobileEpisode> mes = null;
		try {
			mes = getEpisodes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int localHigh = current.size();
		for (int i = 0; i < mes.size(); i++) {
			ArrayList<MobileEpisode> a = new ArrayList();
			MobileEpisode e = mes.get(i);
			if (e.getEpisode().intValue() > localHigh) {
				a.add(e);
			}		
			a.addAll(current);
			current = a;
		}
		
		return current;
	}

	public Episode getEpisode(int episodeNumber) {
		if (this.episodeExistsOnDisk(episodeNumber)) {
			return this.loadFromDisk(episodeNumber);
		}
		try {
			String urlNumber;
			if (episodeNumber < 10) 
				urlNumber = "00" + Integer.toString(episodeNumber);
			else if (episodeNumber < 100) 
				urlNumber = "0" + Integer.toString(episodeNumber);
			else
				urlNumber = Integer.toString(episodeNumber);

			URI url = new URI(BASE_URL + "episode/" + urlNumber);

			MobileEpisode me = (MobileEpisode)getRemoteObject(url);
			Episode episode = new Episode(me);
			
			this.saveToDisk(episode);
			
			return episode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
	}
	
	public ArrayList<MobileEpisode> getAll() {
		URI url;
		ArrayList<MobileEpisode> mes = null;
		try {
			url = new URI(BASE_URL + "lite-episode/all");
			mes = (ArrayList<MobileEpisode>)getRemoteObject(url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mes;
	}
	
	private Object getRemoteObject(URI url) throws IOException, ClassNotFoundException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpUriRequest request = new HttpGet(url);
		request.addHeader("Accept-Encoding", "gzip");
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response = httpClient.execute(request, responseHandler);
		
		return (Object)Serializer.deserialize(response);
		
	}
	
	private boolean episodeExistsOnDisk(int number) {
		return episodeFile(number).exists();
	}
	
	private Episode loadFromDisk(int number) {
		File file = episodeFile(number);
		return (Episode) FileSystem.load(file);
	}
	
	private boolean saveToDisk(Episode episode) {
		File outputFile = episodeFile(episode.getEpisode().intValue());
		return FileSystem.save(episode, outputFile);
	}
	
	private File episodeFile(int number) {
		return new File(this.dataDir, number+".episode");
	}

}
