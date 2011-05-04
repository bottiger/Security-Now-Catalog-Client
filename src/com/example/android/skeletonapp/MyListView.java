package com.example.android.skeletonapp;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sncatalog.shared.MobileEpisode;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.app.ListActivity; 
 
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
	String[] episodes = {};
 
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
 
        setListAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, episodes));
    }    
 
    private String[] getEpisodes() throws IOException, ClassNotFoundException, URISyntaxException {
    	EpisodeFetcher ef = new EpisodeFetcher();
    	
    	ArrayList<MobileEpisode> lme = ef.getEpisodes();
		String[] episodes = new String[lme.size()];
		
		int i = 0;
		for(MobileEpisode me : lme) {
			episodes[i] = me.getTitle();
			i++;
		}
		return episodes;
	}

	public void onListItemClick(
    ListView parent, View v,
    int position, long id) 
    {   
        Toast.makeText(this, 
            "You have selected " + episodes[position], 
            Toast.LENGTH_SHORT).show();
    }  
	
	
}