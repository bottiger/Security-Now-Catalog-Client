package com.podcast.securitynow;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import sncatalog.shared.MobileEpisode;

import com.podcast.securitynow.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


public final class EpisodeActivity  extends Activity{
	
	static final String classIdentifier = "EpisodeActivity";
	static final String playButtonPause = "Pause";
	static final String playButtonPlay = "Play";
	
	private MediaPlayer mMp = new MediaPlayer();
	private File episodeFolder = new File(Environment.getExternalStorageDirectory(), "/sn");
	private EpisodeFetcher mFetcher;
	
	private Episode mEpisode = null;
	private SeekBar mProgressBar = null;
	
	private Button mPlayButton = null;
	private TextView mTitle = null;
	private TextView mDescription = null;
	
	private TextView mButton1 = null;
	private TextView mButton2 = null;
	private TextView mButton3 = null;
	private TextView mButton4 = null;
	
	private boolean isPlaying;
	private StreamingMediaPlayer audioStreamer = null;
	private TextView textStreamed;
	private Button streamButton;
	
	private LoadEpisode episodeLoader;
	private boolean playerStarted = false;
	private ProgressDialog progDialog;
	private Object mutex = new Object();
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.episode);
        // this.getExternalFilesDir(null)
        
        mProgressBar =  (SeekBar) findViewById(R.id.SeekBar01);
        mProgressBar.setOnSeekBarChangeListener(weightSeekBarListener);
        
        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.textarea);
        mPlayButton = (Button) findViewById(R.id.play);
        
        mButton1 = (TextView) findViewById(R.id.button1);
        mButton2 = (TextView) findViewById(R.id.button2);
        mButton3 = (TextView) findViewById(R.id.button3);
        mButton4 = (TextView) findViewById(R.id.button4);
        
        Bundle bun = getIntent().getExtras();
        mTitle.setText(bun.getString("title"));
        Spanned desc = Html.fromHtml(bun.getString("description"));
        mDescription.setText(desc);
        
        final Button button = (Button) findViewById(R.id.play);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	synchronized(mutex) {
            		try {
						mutex.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	//startSpinner();
            	MediaPlayer mp = audioStreamer.getMediaPlayer();
            	if (mp.isPlaying()) {
            		mp.pause();
            		mPlayButton.setText(playButtonPlay);
            	} else {
            		mp.start();
            		audioStreamer.startPlayProgressUpdater();
            		mPlayButton.setText(playButtonPause);
            	}
            	isPlaying = !isPlaying;
            }
        });
    }
	
	protected void startSpinner() {
		 progDialog = new ProgressDialog(this);
         progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         progDialog.setMessage("Loading...");
	}
	
	public void onStart() {
		super.onStart();
        mFetcher = new EpisodeFetcher(this.episodeFolder);
		
		Bundle bun = getIntent().getExtras();
		episodeLoader = new LoadEpisode();
		episodeLoader.execute(bun.getInt("episode"));
		
		//mEpisode = new Episode(mFetcher.getEpisode(bun.getInt("episode")));
		//mDescription.setText(mEpisode.getDescription());
	}
	
	private class LoadEpisode extends AsyncTask<Integer, Void, Boolean> {
        protected Boolean doInBackground(Integer... number) {
        	mEpisode = new Episode(mFetcher.getEpisode(number[0])); //
        	return true;
        }

        protected void onPostExecute(Boolean t) {
        	mDescription.setText(mEpisode.getDescription());
    		startStreamingAudio(mEpisode.getLink(), episodeFolder);
    		mPlayButton.setClickable(true);
    		playerStarted = true;
        }
    }
	
	public void button1ClickHandler(View v) {
		mDescription.setText(mEpisode.getDescription());
	}
	
	public void button2ClickHandler(View v) {
		mDescription.setText(mEpisode.getTransscript());
	}
	
	public void button3ClickHandler(View v) {
		TextView tv = (TextView) v;
		tv.setText("Test");
	}
	
	public void button4ClickHandler(View v) {
		TextView tv = (TextView) v;
		tv.setText("Test");
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

	private void startStreamingAudio(String url, File folder) {
    	try { 
    		if ( audioStreamer != null) {
    			audioStreamer.interrupt();
    		}
    		audioStreamer = new StreamingMediaPlayer(this, 
    											mPlayButton, 
    											streamButton,
    											mProgressBar,
    											folder);
    		audioStreamer.startStreaming(url);
    		mutex.notify();
    		//audioStreamer.startStreaming("http://www.pocketjourney.com/downloads/pj/tutorials/audio.mp3",1677, 214);
    		//streamButton.setEnabled(false);
    	} catch (IOException e) {
	    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
    	}
    	    	
    }
}
