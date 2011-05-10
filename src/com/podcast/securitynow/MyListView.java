package com.podcast.securitynow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import com.podcast.securitynow.R;

import sncatalog.shared.MobileEpisode;
import sncatalog.shared.Serializer;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class MyListView extends ListActivity 
{

	ArrayList<MobileEpisode> episodes = null;
	static final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
 
    @Override  
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.listview);
        
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
		
		SimpleAdapter adapter = new SimpleAdapter(
		this,
		list,
		R.layout.list_item,
		new String[] {"number", "title"},
		new int[] {R.id.number, R.id.title}
		);
		this.makeAdapter(episodes);
		setListAdapter(adapter);
 
        //setListAdapter(new ArrayAdapter<String>(this,
        //    android.R.layout.simple_list_item_1, episodes));
        //setListAdapter(new ArrayAdapter<String>(this,
        //       R.layout.list_item, episodes));
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
    	
    	return (ArrayList<MobileEpisode>) Serializer.deserialize(fileContent.toString());
    	
  
    	// For remote
    	//EpisodeFetcher ef = new EpisodeFetcher();
    	//return ef.getEpisodes();
	}
    
    private void makeAdapter(ArrayList<MobileEpisode> me) {
    	for (MobileEpisode e : me) {
    		HashMap<String,String> map = new HashMap<String,String>();
    		map.put("number", e.getLink());
    		map.put("episode", e.getEpisode().toString());
    		map.put("title", e.getTitle());
    		list.add(map);
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
		viewEpisode(Integer.parseInt((String)selection.get("episode")));
    }  
	
	private void viewEpisode(int episode) {
		Intent i = new Intent(this, EpisodeActivity.class);
		Bundle bundle = new Bundle(); //bundle is like the letter
		bundle.putInt("episode", episode); //arg1 is the keyword of the txt, arg2 is the txt 
		i.putExtras(bundle);//actually it's bundle who carries the content u wanna pass
		startActivity(i);
	}
	
	
}