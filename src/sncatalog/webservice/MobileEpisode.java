package sncatalog.webservice;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.Serializable;

public class MobileEpisode implements Serializable {

	private int Episode;
	private String Title;
	private String Link;
	private Date pubDate;
	private String Description;
	private String Transscript;
	private int Duration;
	
	public MobileEpisode(int episode, String title, String link, 
			Date pubDate, String description, String transscript,
			int duration) {
		
		this.Episode = episode;
		this.Title = title;
		this.Link = link;
		this.Description = description;
		this.Transscript = transscript;
		this.pubDate = pubDate;
		this.Duration = duration;
	}

	public int getEpisode() {
		return Episode;
	}

	public String getTitle() {
		return Title;
	}

	public String getLink() {
		return Link;
	}

	public Date getPubDate() {
		return pubDate;
	}

	public String getDescription() {
		return Description;
	}

	public String getTransscript() {
		return Transscript;
	}

	public int getDuration() {
		return Duration;
	}

	public void setEpisode(int episode) {
		Episode = episode;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public void setLink(String link) {
		Link = link;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public void setTransscript(String transscript) {
		Transscript = transscript;
	}

	public void setDuration(int duration) {
		Duration = duration;
	}
	
}
