package com.podcast.securitynow;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.podcast.securitynow.R;

import sncatalog.shared.MobileEpisode;
import sncatalog.shared.Serializer;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class MyListView extends ListActivity 
{

	private static final String EPISODE_LIST = "all-lite.dat";
	
	ArrayList<MobileEpisode> episodes = null;
	static final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	
	private File episodeFolder = new File(Environment.getExternalStorageDirectory(), "/sn/");
	private File liteEpisodeFile = new File(episodeFolder, EPISODE_LIST);
	
	private EpisodeFetcher mFetcher;
	private final Handler handler = new Handler();
	
    @Override  
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.listview);
        
        mFetcher = new EpisodeFetcher(this.episodeFolder);
        
        try {
			episodes = this.getEpisodes();
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
		
		if (episodes == null)
			episodes = mFetcher.getAll();
		
		if (liteEpisodeFile.exists() && liteEpisodeFile.canRead()) {
			episodes = (ArrayList<MobileEpisode>)FileSystem.load(liteEpisodeFile);
			makeList(episodes);
			new DownloadNewEpisodesTask().execute(episodes);
		} else {
			makeList(episodes);
			new DownloadAllEpisodesTask().execute(null);
		}
		
    }    
    
    public void makeList(ArrayList<MobileEpisode> mes) {
    	Collections.sort(mes);
    	Collections.reverse(mes);
    	SimpleAdapter adapter = new SimpleAdapter(
    			this,
    			list,
    			R.layout.list_item,
    			new String[] {"number", "title"},
    			new int[] {R.id.number, R.id.title}
    			);
    	this.makeAdapter(mes);
    	setListAdapter(adapter);
    }
 
    @SuppressWarnings("unchecked")
	private ArrayList<MobileEpisode> getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
    	InputStream ins = getResources().openRawResource(R.raw.episodes);
    	
    	BufferedReader r = new BufferedReader(new InputStreamReader(ins));
    	StringBuilder fileContent = new StringBuilder();
    	String line;
    	while ((line = r.readLine()) != null) {
    	    fileContent.append(line);
    	}
    	String content = fileContent.toString();
    	if (!content.equals(""))
    		return (ArrayList<MobileEpisode>) Serializer.deserialize(content);
    	else
    		return null;
	}
    
    private class DownloadNewEpisodesTask extends AsyncTask<ArrayList<MobileEpisode>, Void, ArrayList<MobileEpisode>> {
        protected ArrayList<MobileEpisode> doInBackground(ArrayList<MobileEpisode>... params) {
            return mFetcher.getNew(episodes);
        }

        protected void onPostExecute(ArrayList<MobileEpisode> mes) {
        	updateEpisodeList(mes);
        }
    }
    
    private class DownloadAllEpisodesTask extends AsyncTask<Void, Void, ArrayList<MobileEpisode>> {
        protected ArrayList<MobileEpisode> doInBackground(Void... params) {
            return mFetcher.getAll();
        }

        protected void onPostExecute(ArrayList<MobileEpisode> mes) {
        	updateEpisodeList(mes); //
        }
    }
    
    private void updateEpisodeList(ArrayList<MobileEpisode> mes) {
    	episodes = mes;
    	makeList(episodes);
    	FileSystem.save(episodes, liteEpisodeFile);
    }
    
    private void makeAdapter(ArrayList<MobileEpisode> me) {
    	list.clear();
    	HashMap<Long, Boolean> hm = new HashMap<Long, Boolean>();
    	for (int i = 0; i < me.size(); i++) {
    		MobileEpisode e = me.get(i);
    		if (e.getEpisode() == 298) {
    			// stop here
    			int j = 8;
    			j = j+1;
    		}
    		if (!hm.containsKey(e.getEpisode())) {
    			hm.put(e.getEpisode(), true);
    			HashMap<String,String> map = new HashMap<String,String>();
    			map.put("index", i+"");
    			map.put("number", e.getLink());
    			map.put("episode", e.getEpisode().toString());
    			map.put("title", e.getTitle());
    			list.add(map);
    		}
    	}
    }

	public void onListItemClick(
    ListView parent, View v,
    int position, long id) 
    {   
		HashMap selection = (HashMap)getListView().getItemAtPosition(position);
        //Toast.makeText(this, 
        //    "You have selected: " + selection.get("episode"), 
        //    Toast.LENGTH_SHORT).show();	
		String index = (String)selection.get("index");
		viewEpisode(Integer.parseInt(index));
    }  
	
	private void viewEpisode(int episodeIndex) {
		MobileEpisode me = episodes.get(episodeIndex);
		Intent i = new Intent(this, EpisodeActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("episode", me.getEpisode().intValue());
		bundle.putString("title", me.getTitle());
		bundle.putString("description", me.getDescription());
		
		i.putExtras(bundle);
		startActivity(i);
	}
	
	
}