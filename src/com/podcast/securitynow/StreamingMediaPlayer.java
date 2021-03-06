package com.podcast.securitynow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * MediaPlayer does not yet support streaming from external URLs so this class provides a pseudo-streaming function
 * by downloading the content incrementally & playing as soon as we get enough audio in our temporary storage.
 */
public class StreamingMediaPlayer {

    private static final int INTIAL_KB_BUFFER =  96*10/8;//assume 96kbps*10secs/8bits per byte
    private static final String FILE_FOLDER = "files";
    private static final String STREAM_FOLDER = "tmp";
    
	private Boolean isDownloaded = false;
   
	private TextView streamTxt;
	private Button playButton;
	private SeekBar	progressBar;
	
	private File destinationFolder;
	private File tmpFolder;
	
	//  Track for display by progressBar
	private long mediaLengthInKb, mediaLengthInSeconds;
	private int totalKbRead = 0;
	
	// Create Handler to call View updates on the main UI thread.
	private final Handler handler = new Handler();
	private MediaPlayer mediaPlayer;
	private File downloadingMediaFile; 
	private boolean isInterrupted;
	private Context context;
	private int counter = 0;
	
	private Object bufferLock = new Object();
	private boolean waitForBuffer = true;
	
 	public StreamingMediaPlayer(Context  context, 
 								Button	playButton, 
 								Button	streamButton,
 								SeekBar	progressBar,
 								File destination,
 								TextView streamTxt) 
 	{
 		this.context = context;
		this.streamTxt = streamTxt;
		this.playButton = playButton;
		this.progressBar = progressBar;
		this.destinationFolder = new File(destination, FILE_FOLDER+"/");
		this.tmpFolder = new File(destination, STREAM_FOLDER+"/");
		
		if (!destinationFolder.exists())	destinationFolder.mkdir();
		if (!tmpFolder.exists())	tmpFolder.mkdir();
	}
 	
 	public void updateUI(SeekBar progressBar, Button playButton) {
 		this.progressBar = progressBar;
 		this.playButton = playButton;
 		setPlayButtonText();
 	}
 	
 	public void playButton() {
 		if (mediaPlayer.isPlaying()) {
 			mediaPlayer.pause();
 		} else {
 			mediaPlayer.start();
 			startPlayProgressUpdater();
 		}
 		setPlayButtonText();
 			
 	}
 	
 	private void setPlayButtonText() {
 		if (mediaPlayer.isPlaying())
 			playButton.setText(R.string.pause);
 		else
 			playButton.setText(R.string.play);
 			
 	}
	
    /**  
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */  
    public void startStreaming(final String mediaUrl) throws IOException {
    	
    	//this.mediaLengthInKb = mediaLengthInKb;
    	//this.mediaLengthInSeconds = mediaLengthInSeconds;
    	this.mediaLengthInKb = 500;
    	this.mediaLengthInSeconds = 500;
    	
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadAudioIncrement(mediaUrl);
	            } catch (IOException e) {
	            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
	            	return;
	            }   
	        }   
	    };
	    //synchronized(bufferLock) {
	    	new Thread(r).start();
	    //}
    }
    
    /**  
     * Download the url stream to a temporary location and then call the setDataSource  
     * for that local file
     */  
    public void downloadAudioIncrement(String mediaUrl) throws IOException {
    	Log.e("URL", mediaUrl);
    	HttpURLConnection cn = (HttpURLConnection) new URL(mediaUrl).openConnection();   
        cn.connect();   
        
        Log.e("content-type: ", cn.getContentType()+"");
        
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(getClass().getName(), "Unable to create InputStream for mediaUrl:" + mediaUrl);
        }
        
        Log.e("content-type: ", cn.getContentType()+"");
        Log.e("server: ", cn.getHeaderField("server")+"");
        Log.e("content-length: ", cn.getContentLength()+"");
        this.mediaLengthInKb = cn.getContentLength() / 1024;
        this.mediaLengthInSeconds = this.mediaLengthInKb * 8 / 64;
        
		//downloadingMediaFile = new File(context.getCacheDir(),"downloadingMedia_" + (counter++) + ".dat");
		downloadingMediaFile = new File(this.tmpFolder,"downloadingMedia_" + (counter++) + ".dat");
		FileOutputStream out = new FileOutputStream(downloadingMediaFile);   
        byte buf[] = new byte[16384];
        int totalBytesRead = 0, incrementalBytesRead = 0;
        do {
        	int numread = stream.read(buf);   
            if (numread <= 0)   
                break;   
            out.write(buf, 0, numread);
            totalBytesRead += numread;
            incrementalBytesRead += numread;
            totalKbRead = totalBytesRead/1000;
            float displayBytes = Math.round((float)totalBytesRead/1024/1024*100);
            //Log.e("MegaBytesRead: ",displayBytes/100+"");
            testMediaBuffer();
           	updateProgressBar();
        } while (validateNotInterrupted());   

       	stream.close();
        if (validateNotInterrupted()) {
	       	downloadCompleted();
        }
    }  

    public MediaPlayer getMediaPlayer() {

    	if (waitForBuffer) { 
    		synchronized(bufferLock) {
    			try {
    				bufferLock.wait();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}

    	return mediaPlayer;
	}
    
    public Boolean isDownloaded() {
    	return this.isDownloaded;
    }
    
    public void isDownloaded(Boolean downloaded) {
    	this.isDownloaded = downloaded;
    }
	
    public void startPlayProgressUpdater() {
    	float progress = (((float)mediaPlayer.getCurrentPosition()/1000)/(float)mediaLengthInSeconds);
    	progressBar.setProgress((int)(progress*100));
    	
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	startPlayProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,1000);
    	}
    }    
    
    public void interrupt() {
    	playButton.setEnabled(false);
    	isInterrupted = true;
    	validateNotInterrupted();
    }
    
    public void seekTo(float position) {
    	int maxPos = mediaPlayer.getDuration();
    	mediaPlayer.seekTo((int)(position*maxPos));
    }
    
    private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
				//mediaPlayer.release();
			}
			return false;
		} else {
			return true;
		}
    }

    
    /**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */  
    private void  testMediaBuffer() {
	    Runnable updater = new Runnable() {
	        public void run() {
	            if (mediaPlayer == null) {
	            	//  Only create the MediaPlayer once we have the minimum buffered data
	            	if ( totalKbRead >= INTIAL_KB_BUFFER) {
	            		try {
		            		startMediaPlayer();
	            		} catch (Exception e) {
	            			Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
	            		}
	            	}
	            } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
	            	//  NOTE:  The media player has stopped at the end so transfer any existing buffered data
	            	//  We test for < 1second of data because the media player can stop when there is still
	            	//  a few milliseconds of data left to play
	            	transferBufferToMediaPlayer();
	            }
	        }
	    };
	    handler.post(updater);
    }
    
    private void startMediaPlayer() {
        try {   
        	//File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter) + ".dat");
        	File bufferedFile = new File(this.tmpFolder,"playingMedia" + (counter) + ".dat");
        	moveFile(downloadingMediaFile,bufferedFile);
    		
        	Log.e("Player",bufferedFile.length()+"");
        	Log.e("Player",bufferedFile.getAbsolutePath());
        	
        	// Added
        	FileInputStream fileInputStream = new FileInputStream(bufferedFile.getAbsolutePath());
        	
    		mediaPlayer = new MediaPlayer();
        	mediaPlayer.setDataSource(fileInputStream.getFD());
        	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		mediaPlayer.prepare();
    		synchronized (bufferLock) { //
    			bufferLock.notify();
    			waitForBuffer = false;
    		}
        	fireDataPreloadComplete();
        	
        } catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlaer.", e);
        	return;
        }   
    }
    
    /**
     * Transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */  
    private void transferBufferToMediaPlayer() {
	    try {
	    	/* 
	    	 * Copy the current data into a "bufferedFile" which can
	    	 * then be fed into the mediaplayer
	    	 */
	    	
        	//File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter) + ".dat");
        	File bufferedFile = new File(this.tmpFolder,"playingMedia" + (counter++) + ".dat");
        	copyFile(downloadingMediaFile,bufferedFile);
        	
	    	// Determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
	    	int curPosition = 0;
	    	boolean wasPlaying = false;
	    	if (mediaPlayer != null) {
	    		wasPlaying = mediaPlayer.isPlaying();
	    		curPosition = mediaPlayer.getCurrentPosition();
	    		mediaPlayer.pause();
	    	}
	    	
        	/*
        	 * Create a inputStream from the bufferedFile and feed it to
        	 * the mediaplayer
        	 */
        	String newFilePath = bufferedFile.getAbsolutePath();
	    	FileInputStream fileInputStream = new FileInputStream(newFilePath);
	    	
			mediaPlayer = new MediaPlayer();
    		mediaPlayer.setDataSource(fileInputStream.getFD());
    		//mediaPlayer.setAudioStreamType(AudioSystem.STREAM_MUSIC);
    		mediaPlayer.prepare();
    		mediaPlayer.seekTo(curPosition);
    		
	    	/*
        	 * Delete the old "bufferedFile
        	 */
	    	File oldBufferedFile = new File(this.tmpFolder,"playingMedia" + (counter) + ".dat");
	    	if (oldBufferedFile.exists())
	    		oldBufferedFile.delete();
    		
    		//  Restart if at end of prior beuffered content or mediaPlayer was previously playing.  
    		//	NOTE:  We test for < 1second of data because the media player can stop when there is still
        	//  a few milliseconds of data left to play
    		boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
        	if (wasPlaying || atEndOfFile){
        		mediaPlayer.start();
        	}
		}catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }
    
    private void updateProgressBar() {
		Runnable updater = new Runnable() {
	        public void run() {
	        	//textStreamed.setText((CharSequence) (totalKbRead + " Kb read"));
	    		float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);
	    		progressBar.setSecondaryProgress((int)(loadProgress*100));
	    		}
	    };
	    handler.post(updater);
    }
    
    /**
     * We have preloaded enough content and started the MediaPlayer so update the buttons & progress meters.
     */
    private void fireDataPreloadComplete() {
    	Runnable updater = new Runnable() {
	        public void run() {
	    		//mediaPlayer.start();
	    		startPlayProgressUpdater();
	        	//playButton.setEnabled(true);
	        	//streamButton.setEnabled(false);
	        }
	    };
	    handler.post(updater);
    }

    private void downloadCompleted() {
		Runnable updater = new Runnable() { 
			public void run() {
				isDownloaded(true);
   	        	transferBufferToMediaPlayer();
	        	//textStreamed.setText((CharSequence) ("Audio full loaded: " + totalKbRead + " Kb read"));
	        }
	    };
	    handler.post(updater);
    }

    
	private void moveFile(File	oldLocation, File	newLocation)
	throws IOException {

		if ( oldLocation.exists( )) {
			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
            try {
		        byte[]  buff = new byte[8192];
		        int numChars;
		        while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
		        	writer.write( buff, 0, numChars );
      		    }
            } catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if ( reader != null ){
                    	writer.close();
                        reader.close();
                    }
                } catch( IOException ex ){
				    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
				}
            }
        } else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
        }
	}

	private static void copyFile(File sourceFile, File destFile)
	throws IOException {
		if (!sourceFile.exists()) {
			return;
		}
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		if (destination != null) {
			destination.close();
		}

	}
}
