package com.podcast.securitynow;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;


public final class EpisodeActivity  extends Activity{
	
	private static final String classIdentifier = "EpisodeActivity";
	private static final String playButtonPause = "Pause";
	private static final String playButtonPlay = "Play";
	
	private static final String LOADING = "Loading...";
	
	private File episodeFolder = Config.appFolder;
	private EpisodeFetcher mFetcher;
	
	private Episode mEpisode = null;
	private SeekBar mProgressBar = null;
	private Spanned mShowNotes = null;
	
	private Button mPlayButton = null;
	private TextView mTitle = null;
	private TextView mDescription = null;
	private ScrollView mScrollView = null;
	
	private LinkedList<TableRow> mButtons = new LinkedList<TableRow>();
	
	private boolean isPlaying;
	private StreamingMediaPlayer audioStreamer = null;
	private TextView mStreamTxt;
	private Button streamButton;
	
	private LoadEpisode episodeLoader;
	private boolean playerStarted = false;
	private ProgressDialog progDialog;
	private Object mutex = new Object();
	private boolean waitForBuffer = true;
	
	private MyListView lv;
	
	private EpisodeDatabase database;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.episode);
        // this.getExternalFilesDir(null)
        
        this.database = new EpisodeDatabase(this);
        mFetcher = new EpisodeFetcher(this.database);

        mProgressBar =  (SeekBar) findViewById(R.id.SeekBar);
        mProgressBar.setOnSeekBarChangeListener(weightSeekBarListener);
        
        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.textarea);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        
        //mStreamTxt = (TextView) findViewById(R.id.streamtxt);
        
        TableRow mButton1 = (TableRow) findViewById(R.id.row1);
        TableRow mButton2 = (TableRow) findViewById(R.id.row2);
        TableRow mButton3 = (TableRow) findViewById(R.id.row3);
        
        mButtons.add(mButton1);
        mButtons.add(mButton2);
        mButtons.add(mButton3);//
        
        Bundle bun = getIntent().getExtras();
        mTitle.setText(bun.getString("title"));
        
        // is this needed?
        Spanned desc = Html.fromHtml(bun.getString("description"));
        mDescription.setText(desc);
        
        mPlayButton = (Button) findViewById(R.id.play);
        mPlayButton.setOnClickListener(playButtonListener);
		
		int episodeNumber = bun.getInt("episode");
		Episode episode = database.getEpisode(episodeNumber);
		
		if (episode != null) {
			mEpisode = episode;
			mDescription.setText(mEpisode.getDescription());
		}
		
		episodeLoader = new LoadEpisode();
		episodeLoader.execute(episodeNumber);
		
	}
	
	private class LoadEpisode extends AsyncTask<Integer, Void, Boolean> {
        protected Boolean doInBackground(Integer... number) {
        	if (!mEpisode.isComplete()) {
        		mEpisode = new Episode(mFetcher.getEpisode(number[0], true)); //
        		database.updateEpisode(mEpisode);
        	}
        	return true;
        }

        protected void onPostExecute(Boolean t) {
        	if (mDescription.getText().equals(""))
        		mDescription.setText(mEpisode.getDescription());
        	
    		startStreamingAudio(mEpisode.getLink(), episodeFolder);
    		mPlayButton.setClickable(true);
    		playerStarted = true;
    		
    		if (mEpisode.getShowNotes().toString().equals("")) {
    			LoadShowNotes showNotesLoader = new LoadShowNotes();
    			showNotesLoader.execute(mEpisode.getEpisode().intValue());
    		}
        }
    }
	
	private class LoadShowNotes extends AsyncTask<Integer, Void, Spanned> {
        protected Spanned doInBackground(Integer... number) {
        	ShowNote sn = new ShowNote(number[0]);
        	mShowNotes = sn.text;
        	return mShowNotes;
        }

        protected void onPostExecute(Spanned showNotes) {
        	mEpisode.setShowNotes(showNotes);
        	database.updateEpisode(mEpisode);
        }
    }
	
	public void buttonClickHandler(View v) {
		final int id = v.getId();
		
		for (TableRow tr : mButtons) {
			if (id == tr.getId())
				activateButton(tr);
			else
				resetButton(tr);
		}
		
		if (id == R.id.row1) {
			mDescription.setText(mEpisode.getDescription());
		} else if (id == R.id.row2) {
			String t = mEpisode.getTransscript();
			//System.out.print(t);
			mDescription.setText(t.toString());
		} else if (id == R.id.row3) {
			mDescription.setText(mShowNotes);
		}
	}
	
	private void resetButton(TableRow row) {
		Resources resource = getApplicationContext().getResources();
		row.setBackgroundColor(Color.TRANSPARENT); //make the background transparent
		TextView tv = (TextView)row.getChildAt(0);
		tv.setTextColor(resource.getColor(R.color.white));
		//tv.setTextColor(resource.getColor(R));
	}
	
	private void activateButton(TableRow row) {
		Resources resource = getApplicationContext().getResources();
		row.setBackgroundColor(resource.getColor(R.color.buttonBackground));
		TextView tv = (TextView)row.getChildAt(0);
		tv.setTextColor(R.color.black);
	}
	
	private View.OnClickListener playButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if (waitForBuffer) {
        		synchronized(mutex) {
        			try {
        				mutex.wait();
        			} catch (InterruptedException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        		}
        	}

        	//startSpinner();
        	MediaPlayer mp = audioStreamer.getMediaPlayer();
        	audioStreamer.playButton();
        	isPlaying = !isPlaying;
        }
    };
	
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

	private void startStreamingAudio(String url, File folder) {
    	try { 
    		if ( audioStreamer != null) {
    			audioStreamer.interrupt();
    		}
    		audioStreamer = new StreamingMediaPlayer(this, 
    											mPlayButton, 
    											streamButton,
    											mProgressBar,
    											folder,
    											mStreamTxt);
    		audioStreamer.startStreaming(url);
    		
    		synchronized(mutex) {
    			mutex.notify();
    			this.waitForBuffer = false;
    		}
    		
    		// lv is null
    		SecurityNow sn = (SecurityNow)this.getApplication();
    		sn.smp = audioStreamer;
//    			mutex = new Object();
    		//audioStreamer.startStreaming("http://www.pocketjourney.com/downloads/pj/tutorials/audio.mp3",1677, 214);
    		//streamButton.setEnabled(false);
    	} catch (IOException e) {
	    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
    	}
    	    	
    }
}
