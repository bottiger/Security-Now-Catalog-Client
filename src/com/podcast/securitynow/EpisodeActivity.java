package com.podcast.securitynow;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import sncatalog.shared.MobileEpisode;

import com.podcast.securitynow.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public final class EpisodeActivity  extends Activity{
	
	static final String classIdentifier = "EpisodeActivity";
	static final String playButtonPause = "Pause";
	static final String playButtonPlay = "Play";
	
	MediaPlayer mMp = new MediaPlayer();
	EpisodeFetcher mFetcher = new EpisodeFetcher();
	Episode mEpisode = null;
	
	Button mPlayButton = null;
	
	private boolean isPlaying;
	private StreamingMediaPlayer audioStreamer;
	private TextView textStreamed;
	private Button streamButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(classIdentifier, "Activity State: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.episode);
        
        
        mPlayButton = (Button) findViewById(R.id.play);
        textStreamed = (TextView) findViewById(R.id.streamtext);
        
        Bundle bun = getIntent().getExtras();
        int episodeNumber = bun.getInt("episode");
        mEpisode = new Episode(mFetcher.getEpisode(episodeNumber));
        startStreamingAudio(mEpisode.getLink());
        
        final Button button = (Button) findViewById(R.id.play);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (audioStreamer.getMediaPlayer().isPlaying()) {
            		audioStreamer.getMediaPlayer().pause();
            		mPlayButton.setText(playButtonPlay);
            	} else {
            		audioStreamer.getMediaPlayer().start();
            		audioStreamer.startPlayProgressUpdater();
            		mPlayButton.setText(playButtonPause);
            	}
            	isPlaying = !isPlaying;
            }
        });
    }

	private void startStreamingAudio(String url) {
    	try { 
    		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.SeekBar01);
    		if ( audioStreamer != null) {
    			audioStreamer.interrupt();
    		}
    		audioStreamer = new StreamingMediaPlayer(this,textStreamed, mPlayButton, streamButton,progressBar);
    		audioStreamer.startStreaming(url);
    		//audioStreamer.startStreaming("http://www.pocketjourney.com/downloads/pj/tutorials/audio.mp3",1677, 214);
    		//streamButton.setEnabled(false);
    	} catch (IOException e) {
	    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
    	}
    	    	
    }
}
