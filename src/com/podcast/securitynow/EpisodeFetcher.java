package com.podcast.securitynow;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import sncatalog.shared.MobileEpisode;
import sncatalog.shared.Serializer;

public class EpisodeFetcher {
	
	private static final String BASE_URL = "http://sn-catalog.appspot.com/";
	private EpisodeDatabase episodeDatabase;

	public EpisodeFetcher(EpisodeDatabase database) {
		this.episodeDatabase = database;
	}
	
	public ArrayList<Episode> getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
		URI url = new URI(BASE_URL + "lite-episode/new");
		ArrayList<MobileEpisode> mes = (ArrayList<MobileEpisode>) getRemoteObject(url);
		ArrayList<Episode> es = new ArrayList<Episode>();
		for (MobileEpisode me : mes)
			es.add(new Episode(me));
		return es;
	}
	
	public ArrayList<Episode> getNew(ArrayList<Episode> current) {
		ArrayList<Episode> es = null;
		try {
			es = getEpisodes();
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
		for (int i = 0; i < es.size(); i++) {
			ArrayList<Episode> a = new ArrayList();
			Episode e = es.get(i);
			if (e.getEpisode().intValue() > localHigh) {
				a.add(e);
			}		
			a.addAll(current);
			current = a;
		}
		
		return current;
	}
	
	public Episode getEpisode(int episodeNumber) {
		return getEpisode(episodeNumber, false);
	}

	public Episode getEpisode(int episodeNumber, boolean getRemote) {
		Episode ep = this.episodeDatabase.getEpisode(episodeNumber);
		if (ep != null && !getRemote) {
			return ep;
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
			
			//this.saveToDisk(episode);
			this.episodeDatabase.updateEpisode(episode);
			
			return episode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
	}
	
	public ArrayList<Episode> getAll() {
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
		ArrayList<Episode> es = new ArrayList<Episode>();
		for (MobileEpisode me : mes)
			es.add(new Episode(me));
		
		return es;
	}
	
	private Object getRemoteObject(URI url) throws IOException, ClassNotFoundException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpUriRequest request = new HttpGet(url);
		request.addHeader("Accept-Encoding", "gzip");
		
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String response = httpClient.execute(request, responseHandler);
		
		return (Object)Serializer.deserialize(response);
		
	}

}
