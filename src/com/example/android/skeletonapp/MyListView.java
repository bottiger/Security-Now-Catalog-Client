package com.example.android.skeletonapp;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import sncatalog.shared.MobileEpisode;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
 
public class MyListView extends ListActivity 
{
	/*
    String[] presidents = {
            "Dwight D. Eisenhower",
            "John F. Kennedy",
            "Lyndon B. Johnson",
            "Richard Nixon",
            "Gerald Ford",
            "Jimmy Carter",
            "Ronald Reagan",
            "George H. W. Bush",
            "Bill Clinton",
            "George W. Bush",
            "Barack Obama",
            ef.getEpisode(200).getTitle()
    };
    */
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
 
    private ArrayList<MobileEpisode> getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
    	EpisodeFetcher ef = new EpisodeFetcher();
    	return ef.getEpisodes();
	}
    
    private void makeAdapter(ArrayList<MobileEpisode> me) {
    	for (MobileEpisode e : me) {
    		HashMap<String,String> map = new HashMap<String,String>();
    		map.put("number", e.getLink());
    		map.put("title", e.getTitle());
    		list.add(map);
    	}
    }

	public void onListItemClick(
    ListView parent, View v,
    int position, long id) 
    {   
        Toast.makeText(this, 
            "You have selected ",
            Toast.LENGTH_SHORT).show();
    }  
	
	
}