package com.podcast.securitynow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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

import sncatalog.shared.*;

public class EpisodeFetcher {

	public EpisodeFetcher() {
	}
	
	public ArrayList<MobileEpisode> getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
		URI url = new URI("https://sn-catalog.appspot.com/lite-episode/new/");
		return (ArrayList) getRemoteObject(url);
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

			URI url = new URI("https://sn-catalog.appspot.com/episode/" + urlNumber);

			MobileEpisode episode = (MobileEpisode)getRemoteObject(url);
			/*
			byte[] serializedBytes = toByteArray(serializedObject);
			ByteArrayInputStream bis = new ByteArrayInputStream(serializedBytes);
			ObjectInputStream ois = new ObjectInputStream(bis); 

			episode = (MobileEpisode)ois.readObject();
			ois.close(); 
			return episode;
			*/
			////////episode = (MobileEpisode) sncatalog.shared.Serializer.deserialize(serializedObject);
			return episode;
			//@SuppressWarnings("unchecked")
			//ArrayList<MobileEpisode> mes = (ArrayList<MobileEpisode>) sncatalog.shared.Serializer.deserialize(serializedObject);
			//return mes;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
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
