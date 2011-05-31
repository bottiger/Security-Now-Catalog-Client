package com.podcast.securitynow;

import java.text.DecimalFormat;
import java.util.Arrays;

import sncatalog.shared.MobileEpisode;
import android.text.Spanned;

public class Episode extends MobileEpisode {
	
	private Spanned showNotes = null;

	public Episode(MobileEpisode me) {
		super(me.getEpisode().intValue(), 
				me.getTitle(), 
				me.getLink(), 
				me.getPubDate(), 
				me.getDescription(), 
				me.getTransscript(), 
				me.getDuration().intValue());
		// TODO Auto-generated constructor stub
	}
	
	public Spanned getShowNotes() {
		if (showNotes != null)
			return showNotes;
		return null;
	}

	public void setShowNotes(Spanned showNotes) {
		this.showNotes = showNotes;
	}

	public String getLink() {
		String url = "http://www.podtrac.com/pts/redirect.mp3/aolradio.podcast.aol.com/sn/sn*.mp3";
		url = url.replace("*", intToString(this.getEpisode().intValue(), 4));
		return url;
	}
	
	private static String intToString(int num, int digits) {
	    assert digits > 0 : "Invalid number of digits";

	    // create variable length array of zeros
	    char[] zeros = new char[digits];
	    Arrays.fill(zeros, '0');
	    // format number as String
	    DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

	    return df.format(num);
	}

}
