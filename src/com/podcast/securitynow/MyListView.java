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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	
    @Override  
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.listview);
        
        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setOnClickListener(playButtonListener);
        
        mFetcher = new EpisodeFetcher(this.episodeFolder);
        mProgressBar =  (SeekBar) findViewById(R.id.SeekBar);
        
        this.database = new EpisodeDatabase(this);
        
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
    	/*SimpleAdapter adapter = new SimpleAdapter(
    			this, 
    			list,
    			R.layout.list_item,
    			new String[] {"subtitle", "title"},
    			new int[] {R.id.subtitle, R.id.title}
    			);
    	this.makeAdapter(episodes);
    	setListAdapter(adapter);*/
    	setListAdapter(new ContinuousEpisodeAdapter(episodes));
    }
    
    public void setAudioPlayer(StreamingMediaPlayer smp) {
    	this.audioStreamer = smp;
    }
 
    private ArrayList<Episode> getEpisodes() {
    	return this.database.getRecentEpisodes();
    }

    /*
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
	*/
    
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
    		if (e.getEpisode() == 298) {
    			// stop here
    			int j = 8;
    			j = j+1;
    		}
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
	
	
	class ContinuousEpisodeAdapter extends EndlessAdapter {
		private RotateAnimation rotate=null;
		
		ContinuousEpisodeAdapter(ArrayList<Episode> list) {
			super(new EpisodeAdapter(MyListView.this,
									R.layout.list_item,
									list));
			
			rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
																	0.5f, Animation.RELATIVE_TO_SELF,
																	0.5f);
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
			
			return(getWrappedAdapter().getCount()<75);
		}
		
		@Override
		protected void appendCachedData() {
			if (getWrappedAdapter().getCount()<75) {
				@SuppressWarnings("unchecked")
				EpisodeAdapter a=(EpisodeAdapter)getWrappedAdapter();
				
				
				Episode episode = a.getItem(a.getCount()-1);
				ArrayList<Episode> episodes = database.getEpisodes(episode.getEpisode().intValue()-1, 30);
				
				//for (int i=0;i<25;i++) { a.add(a.getCount()); }
				for (Episode ep : episodes)
					a.add(ep);
			}
		}
	}
	
	private class EpisodeAdapter extends ArrayAdapter<Episode> {
		
		private ArrayList<Episode> items;

	    public EpisodeAdapter(Context context, int textViewResourceId, ArrayList<Episode> items) {
	            super(context, textViewResourceId, items);
	            this.items = items;
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