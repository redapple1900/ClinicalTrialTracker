package com.yuanwei.android.rss.domain;

public class FeedMessage {
	private	String url;
	private boolean newChannel;
	private String newTrials;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String uri) {
		this.url = uri;
	}
	
	public boolean isNewChannel() {
		return newChannel;
	}

	public void setNewChannel(boolean newChannel) {
		this.newChannel = newChannel;
	}
	/*
	public boolean isNewTrials() {
		return newTrials;
	}
	*/

	public void setNewTrials(String newTrials) {
		this.newTrials = newTrials;
	}

	public String getTag() {
		// TODO Auto-generated method stub
		return newTrials;
	}
}
