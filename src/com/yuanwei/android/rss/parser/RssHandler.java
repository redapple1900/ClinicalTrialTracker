package com.yuanwei.android.rss.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import android.net.Uri;
import android.util.Log;

import com.yuanwei.android.Constants;
import com.yuanwei.android.rss.domain.Article;
import com.yuanwei.android.rss.domain.RssChannel;

public class RssHandler extends DefaultHandler2 {

	// Feed and Article objects to use for temporary storage
	private RssChannel rsschannel;
	private String Tag;//
	private boolean isReadingItem;
	private Article currentArticle;
	private ArrayList<Article> articleList = new ArrayList<Article>();

	// Number of articles added so far
	private int articlesAdded = 0;

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 500;

	// Current characters being accumulated
	StringBuffer chars = new StringBuffer();

	public ArrayList<Article> getArticleList() {
		return articleList;
	}

	public RssChannel getChannel() {
		return rsschannel;
	}

	/*
	 * public RSSFeed getFeed(){ rss.setItemList(articleList); return rssFeed; }
	 */

	/*
	 * This method is called everytime a start element is found (an opening XML
	 * marker) here we always reset the characters StringBuffer as we are only
	 * currently interested in the the text values stored at leaf nodes
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		chars = new StringBuffer();
		if ("channel".equals(localName)) {
			rsschannel = new RssChannel();
			isReadingItem = false;
			if (Constants.LOGD)
				Log.e(Tag, "Reading Feed");
		} else if ("item".equals(localName)) {
			currentArticle = new Article();
			isReadingItem = true;
		}
		Tag = localName;
	}

	/*
	 * This method is called everytime an end element is found (a closing XML
	 * marker) here we check what element is being closed, if it is a relevant
	 * leaf node that we are checking, such as Title, then we get the characters
	 * we have accumulated in the StringBuffer and set the current Article's
	 * title to the value
	 * 
	 * If this is closing the "entry", it means it is the end of the article, so
	 * we add that to the list and then reset our Article object for the next
	 * one on the stream
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equalsIgnoreCase("channel")) {
			rsschannel.setTotal(articleList.size());
			SimpleDateFormat ma = new SimpleDateFormat(
					Constants.STORED_DATE_FORMAT, Locale.US);
			String pubDate = ma.format(new Date());
			rsschannel.setUpdateDate(pubDate);
		} else if (!isReadingItem) {
			// Reading Feed
			if (localName.equalsIgnoreCase("title")) {
				rsschannel.setTitle(chars.toString());
			} else if (localName.equalsIgnoreCase("pubDate")) {

				String rawdate = chars.toString();
				// String format = "EEE, dd MMM yyyy kk:mm:ss ZZZ";
				SimpleDateFormat df = new SimpleDateFormat(
						Constants.RSS_DATE_FORMAT, Locale.US);
				// String newformat = "yyyy-MM-dd,hh:mm:ss";
				SimpleDateFormat newdf = new SimpleDateFormat(
						Constants.STORED_DATE_FORMAT, Locale.US);
				Date pDate;
				String pubDate;

				try {
					pDate = df.parse(rawdate);
					pubDate = newdf.format(pDate);
				} catch (ParseException e) {
					Log.e("DATE PARSING", "Error parsing date..");
					pubDate = "";
				}
				if (Constants.LOGD)
					Log.d(rsschannel.getPubDate(), Tag);
				rsschannel.setPubDate(pubDate);
			} else if (localName.equalsIgnoreCase("description")) {
				rsschannel.setDescription(chars.toString());
			} else if (localName.equalsIgnoreCase("link")) {
				rsschannel.setUrl(Uri.parse((chars.toString())));
			}
		} else if (isReadingItem) {
			// Reading item
			if (localName.equalsIgnoreCase("title")) {
				currentArticle.setTitle(chars.toString());
			} else if (localName.equalsIgnoreCase("description")) {
				currentArticle.setDescription(chars.toString());
			} else if (localName.equalsIgnoreCase("pubDate")) {
				String rawdate = chars.toString();
				SimpleDateFormat df = new SimpleDateFormat(
						Constants.RSS_DATE_FORMAT, Locale.US);
				// String newformat = "yyyy-MM-dd,hh:mm:ss";
				SimpleDateFormat newdf = new SimpleDateFormat(
						Constants.STORED_DATE_FORMAT, Locale.US);
				Date pDate;
				String pubDate;

				try {
					pDate = df.parse(rawdate);
					pubDate = newdf.format(pDate);
				} catch (ParseException e) {
					Log.e("DATE PARSING", "Error parsing date..");
					pubDate = "";
				}
				currentArticle.setPubDate(pubDate);
			} else if (localName.equalsIgnoreCase("guid")) {
				currentArticle.setGuid(chars.toString());
			} else if (localName.equalsIgnoreCase("link")) {
				currentArticle.setUrl(Uri.parse(chars.toString()));
			} else if (localName.equalsIgnoreCase("content")) {
				currentArticle.setEncodedContent(chars.toString());
			} else if (localName.equalsIgnoreCase("entry")) {
			}

			// Check if looking for article, and if article is complete
			if (localName.equalsIgnoreCase("item")) {
				currentArticle.setRead(false);
				currentArticle.setOffline(false);
				// Set the last update time of the item
				SimpleDateFormat ma = new SimpleDateFormat(
						Constants.STORED_DATE_FORMAT, Locale.US);
				if (Constants.LOGD)
					Log.d(ma.format(new Date()), "Writing Update Time");
				currentArticle.setUpdateDate(ma.format(new Date()));
				
				articleList.add(currentArticle);

				currentArticle = new Article();

				// Lets check if we've hit our limit on number of articles
				articlesAdded++;
				
				if (Constants.LOGD)
				Log.d("" + articlesAdded, "add");
				if (articlesAdded >= ARTICLES_LIMIT) {
					throw new SAXException();
				}
			}
		}

	}

	/*
	 * This method is called when characters are found in between XML markers,
	 * however, there is no guarante that this will be called at the end of the
	 * node, or that it will be called only once , so we just accumulate these
	 * and then deal with them in endElement() to be sure we have all the text
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		chars.append(new String(ch, start, length));

	}
}