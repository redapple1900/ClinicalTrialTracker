package com.yuanwei.android.rss.domain;

import java.io.Serializable;
import android.net.Uri;

public class Article implements Serializable {
	
	public static final String KEY = "ARTICLE";

	private static final long serialVersionUID = 1L;
	private String guid;
	private String title;
	private String description;
	private String pubDate;
	private String updateDate;
	private String feed;
	private Uri url;
	private String encodedContent;
	private boolean read;
	private boolean star;
	private boolean offline;
	private long dbId;
	
	public String getTag(){
			
			String[] box1 = new String[3];
			box1=feed.split("\\|", 2);
			String[] box =new String[2];
			box=box1[0].split("\\:",2);
			return box[1].trim();
				
	}
	


	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
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

	public String getFeed() {
		return this.feed;
	}

	public void setFeed(String feed) {
		this.feed = feed;
	}

	public void setEncodedContent(String encodedContent) {
		this.encodedContent = extractCData(encodedContent);
	}

	public String getEncodedContent() {
		return encodedContent;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}
	
	public void setStar(boolean star){
		this.star=star;
	}
	public boolean isStar(){
		return star;
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