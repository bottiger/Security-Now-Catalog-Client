package com.podcast.securitynow;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import sncatalog.shared.MobileEpisode;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.commonsware.cwac.endless.EndlessAdapter;


public class MyListView extends ListActivity 
{

	private static final String EPISODE_LIST = "all-lite.dat";
	
	ArrayList<Episode> episodes = null;
	static final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	
	private EpisodeDatabase database; 
	
	private File episodeFolder = new File(Environment.getExternalStorageDirectory(), "/sn/");
	private File liteEpisodeFile = new File(episodeFolder, EPISODE_LIST);
	
	private EpisodeFetcher mFetcher;
	private final Handler handler = new Handler();
	
	private StreamingMediaPlayer audioStreamer = null;
	private SeekBar mProgressBar;
	private Button mPlayButton;
	
	private View mEpisodeList;
	private View mPlayer;
	
    @Override  
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.listview);
        Log.d("Test", "Hello There - this is running");
        
        this.database = new EpisodeDatabase(this);
        
        mEpisodeList = findViewById(android.R.id.list);
        mPlayer = findViewById(R.id.player);
        
        mEpisodeList.setPadding(0, 0, 0, 0);
        mPlayer.setVisibility(8); // 8 = GONE
        
        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setOnClickListener(playButtonListener);
        
        mFetcher = new EpisodeFetcher(this.database);
        mProgressBar =  (SeekBar) findViewById(R.id.SeekBar);
        
		episodes = this.getEpisodes();
		
		if (episodes == null)
			episodes = mFetcher.getAll();
		else
			makeList(episodes);
		
		new DownloadNewEpisodesTask().execute(null);
		
    }
    
    public void onResume() {
    	super.onResume();
    	SecurityNow sn = (SecurityNow)this.getApplication();
    	if (sn.smp != null) {
    		mEpisodeList.setPadding(0, 0, 0, 50);
    		mPlayer.setVisibility(0);
    		this.audioStreamer = sn.smp;
    		mProgressBar =  (SeekBar) findViewById(R.id.SeekBar);
    		mPlayButton = (Button) findViewById(R.id.play);
    		this.audioStreamer.updateUI(mProgressBar, mPlayButton);
    	}
    }
    
    SeekBar.OnSeekBarChangeListener weightSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

		  public void onProgressChanged(SeekBar seekBar, int progress,
		    boolean fromTouch) {
		  }

		  // When the user touches the seekbar
		  public void onStartTrackingTouch(SeekBar seekBar) {
		  }

		  // When the user stops touching the seekbar
		  public void onStopTrackingTouch(SeekBar seekBar) {
			  if (audioStreamer.isDownloaded()) {
				  float seekBarProgress = (float)seekBar.getProgress() / (float)seekBar.getMax();
				  //progressBar.setProgress((int)seekBarProgress*progressBar.getMax());
				  audioStreamer.seekTo(seekBarProgress);
			  }
		  }
	};
    
    private View.OnClickListener playButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if (audioStreamer != null)
        		audioStreamer.playButton();
        }
    };
    
    public void makeList(ArrayList<Episode> episodes) {
    	Collections.sort(episodes);
    	Collections.reverse(episodes);
    	
    	ListAdapter lad = getListAdapter();
    	if (lad != null) {
    		int count = lad.getCount();
    		for (int i = 0; i < count-1; i++) {
    			Episode episode = (Episode) lad.getItem(i);
    			if (episodes.contains(episode))
    				episodes.remove(episode); //
    		}
    	}

    	setListAdapter(new ContinuousEpisodeAdapter(episodes));
    }
    
    public void setAudioPlayer(StreamingMediaPlayer smp) {
    	this.audioStreamer = smp;
    }
 
    private ArrayList<Episode> getEpisodes() {
    	return this.database.getRecentEpisodes();
    }
    
    private class DownloadNewEpisodesTask extends AsyncTask<ArrayList<Episode>, Void, ArrayList<Episode>> {
        protected ArrayList<Episode> doInBackground(ArrayList<Episode>... params) {
            return mFetcher.getNew(episodes);
        }

        protected void onPostExecute(ArrayList<Episode> es) {
        	updateEpisodeList(es);
        	if (database.countEpisodes() > 200) {
    			//new DownloadMissingEpisodesTask().execute(null);
    		} else 
    			new DownloadAllEpisodesTask().execute(null);
        }
    }
    
    private class DownloadAllEpisodesTask extends AsyncTask<Void, Void, ArrayList<Episode>> {
        protected ArrayList<Episode> doInBackground(Void... params) {
        	ArrayList<Episode> episodes = mFetcher.getAll();
        	for (Episode episode : episodes)
        		database.addEpisode(episode);
        	return episodes;
        }

        protected void onPostExecute(ArrayList<Episode> es) {
        	updateEpisodeList(es); //
        }
    }
    
    private class DownloadMissingEpisodesTask extends AsyncTask<Void, Void, ArrayList<Episode>> {
        protected ArrayList<Episode> doInBackground(Void... params) {
        	ArrayList<Episode> episodes = mFetcher.getAll();
        	for (Episode episode : episodes)
        		database.addEpisode(episode);
        	return episodes;
        }

        protected void onPostExecute(ArrayList<Episode> es) {
        	updateEpisodeList(es); //
        }
    }
    
    private void updateEpisodeList(ArrayList<Episode> es) {
    	episodes = es;
    	makeList(episodes);
    }
    
    private void makeAdapter(ArrayList<Episode> me) {
    	list.clear();
    	HashMap<Long, Boolean> hm = new HashMap<Long, Boolean>();
    	for (int i = 0; i < me.size(); i++) {
    		Episode e = me.get(i);
    		if (!hm.containsKey(e.getEpisode())) {
    			hm.put(e.getEpisode(), true);
    			HashMap<String,String> map = new HashMap<String,String>();
    			map.put("index", i+"");
    			map.put("subtitle", generateSubtitle(e));
    			map.put("episode", e.getEpisode().toString());
    			map.put("title", e.getTitle());
    			list.add(map);
    		}
    	}
    }
    
    private String generateSubtitle(Episode e) {
    	String source = "Episode: ";
    	source += "<b>" + e.getEpisode() + "</b> ";
    	source += "Duration: ";
    	source += "<b>"+ e.getDuration() +" min</b>";
    	return Html.fromHtml(source).toString();
    }

	public void onListItemClick(
    ListView parent, View v,
    int position, long id) 
    {   
		Episode chosenEpisode = (Episode)getListView().getItemAtPosition(position);
        //Toast.makeText(this, 
        //    "You have selected: " + selection.get("episode"), 
        //    Toast.LENGTH_SHORT).show();	
		viewEpisode(chosenEpisode);
    }  
	
	private void viewEpisode(Episode episode) {
		Intent i = new Intent(this, EpisodeActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("episode", episode.getEpisode().intValue());
		bundle.putString("title", episode.getTitle());
		bundle.putString("description", episode.getDescription());
		
		i.putExtras(bundle);
		startActivity(i);
	}
	
	
	class ContinuousEpisodeAdapter extends EndlessAdapter {
		
		private static final int MAX_EPISODES = 400;
		private RotateAnimation rotate=null;
		
		ContinuousEpisodeAdapter(ArrayList<Episode> list) {
			super(new EpisodeAdapter(MyListView.this,
									R.layout.list_item,
									list));
			
			rotate=new RotateAnimation(0f, 
										360f, 
										Animation.RELATIVE_TO_SELF,
										0.5f, 
										Animation.RELATIVE_TO_SELF,
										0.5f
									);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);
		}
		
		@Override
		protected View getPendingView(ViewGroup parent) {
			View row=getLayoutInflater().inflate(R.layout.list_item, null);
			
			View child=row.findViewById(R.id.title);
			
			child.setVisibility(View.GONE);
			
			child=row.findViewById(R.id.throbber);
			child.setVisibility(View.VISIBLE);
			child.startAnimation(rotate);
			
			return(row);
		}
		
		@Override
		protected boolean cacheInBackground() {
			//SystemClock.sleep(10000);				// pretend to do work
			
			return(getWrappedAdapter().getCount()<MAX_EPISODES);
		}
		
		@Override
		protected void appendCachedData() {
			if (getWrappedAdapter().getCount()<MAX_EPISODES) {
				@SuppressWarnings("unchecked")
				EpisodeAdapter a=(EpisodeAdapter)getWrappedAdapter();
				
				int count = a.getCount()-1;
				
				if (count != -1) {
					Episode episode = a.getItem(count);
					ArrayList<Episode> episodes = database.getEpisodes(episode.getEpisode().intValue()-1, 30);

					//for (int i=0;i<25;i++) { a.add(a.getCount()); }
					for (Episode ep : episodes)
						if (!a.containsEpisode(ep))
							a.add(ep);
				}
			}
		}
	}
	
	private class EpisodeAdapter extends ArrayAdapter<Episode> {
		
		private ArrayList<Episode> items;

	    public EpisodeAdapter(Context context, int textViewResourceId, ArrayList<Episode> items) {
	            super(context, textViewResourceId, items);
	            this.items = items;
	    }
	    
	    public boolean containsEpisode(Episode episode) {
	    	return this.items.contains(episode);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	            View v = convertView;
	            if (v == null) {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate(R.layout.list_item, null);
	            }
	            Episode episode = items.get(position);
	            if (episode != null) {
	                    TextView listTitle = (TextView) v.findViewById(R.id.title);
	                    TextView listSubTitle = (TextView) v.findViewById(R.id.subtitle);
	                    if (listTitle != null) {
	                          listTitle.setText(episode.getTitle());                            }
	                    if(listSubTitle != null){
	                          listSubTitle.setText(generateSubtitle(episode));
	                    }
	            }
	            return v;
	    }

	}
	
}