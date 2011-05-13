package com.podcast.tests;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TestHttpConnection {
	
	private static final String mainURL = "http://media.grc.com/sn/sn-299.mp3";
	
	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
		HttpURLConnection cn = (HttpURLConnection) new URL(mainURL).openConnection();   
        cn.connect();   
        
        System.out.println(cn.getContentType());
        
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	System.out.println("Unable to create InputStream for mediaUrl:" + mainURL);
        }
        
        byte buf[] = new byte[16384];
        int totalBytesRead = 0, incrementalBytesRead = 0;
        do {
        	int numread = stream.read(buf);   
            if (numread <= 0)   
                break;   
            System.out.write(buf, 0, numread);
            totalBytesRead += numread;
            incrementalBytesRead += numread;
            int totalKbRead = totalBytesRead/1000;
            Thread.currentThread().sleep(1000);
        } while (true);
	}

}
