package sncatalog.shared;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.Serializable;

public class MobileEpisode implements Serializable, Comparable<MobileEpisode> {

	private Long Episode;
	private String Title;
	private String Link;
	private Date pubDate;
	private String Description;
	private String Transscript;
	private Long Duration;
	
	public MobileEpisode(int episode, String title, String link, 
			Date pubDate, String description, String transscript,
			int duration) {
		
		this.Episode = new Long(episode);
		this.Title = title;
		this.Link = link;
		this.Description = description;
		this.Transscript = transscript;
		this.pubDate = pubDate;
		this.Duration = new Long(duration);
	}

	public Long getEpisode() {
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

	public Long getDuration() {
		return Duration;
	}

	public void setEpisode(Long episode) {
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

	public void setDuration(Long duration) {
		Duration = duration;
	}

	@Override
	public int compareTo(MobileEpisode obj) {
		if (obj instanceof MobileEpisode) {
            
            MobileEpisode episode = (MobileEpisode) obj;
            if (this.Episode > episode.getEpisode())
                return 1;
            else if (this.Episode < episode.getEpisode())
                return -1;
        }
        return 0;
	}
	
	public boolean equals(Object obj) {
		if ( this == obj ) return true;
		if ( !(obj instanceof MobileEpisode) ) return false;
		MobileEpisode me = (MobileEpisode)obj;
		return me.getEpisode()==this.getEpisode();
	}
	
}
