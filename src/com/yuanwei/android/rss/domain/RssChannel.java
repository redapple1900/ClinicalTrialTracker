package com.yuanwei.android.rss.domain;

import java.io.Serializable;
import android.net.Uri;

public class RssChannel implements Serializable {
	
	public static final String KEY = "Channel";
	private static final long serialVersionUID = 1L;
	private String title;
	private String pubDate;
	private String updateDate;
	private String tag;
	private Uri url;
	private String description;
	private int unread;
	private int total;
	private long dbId;
	private boolean newchannel;

	public String getTag(){
		if (null!=tag){
			return tag;	
		}else {
			String[] box1 = new String[3];
			box1=title.split("\\|", 2);
			String[] box =new String[2];
			box=box1[0].split("\\:",2);
			return box[1];
		}		
	}
	public String getShortTitle(){
		String[] box1 = new String[3];
		box1=title.split("\\|", 2);
		String[] box =new String[2];
		box=box1[0].split("\\:",2);
		return box[1];
	}
	
	public void setTag(String tag){
	this.tag=tag;	
	}
	
	public boolean isNewChannel(){
		return this.newchannel;
	}
	public void setNewChannel(boolean newchannel){
		this.newchannel=newchannel;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Uri getUrl() {
		return url;
	}

	public void setUrl(Uri uri) {
		this.url = uri;
	}

	public void setDescription(String description) {
		this.description = extractCData(description);
	}

	public String getDescription() {
		return description;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getPubDate() {
		return pubDate;
	}
	
	public void setUpdateDate(String UpdateDate) {
		this.updateDate = UpdateDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getTotal() {
		return this.total;
	}
	


	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	private String extractCData(String data){
		data = data.replaceAll("<!\\[CDATA\\[", "");
		data = data.replaceAll("\\]\\]>", "");
		return data;
	}

}