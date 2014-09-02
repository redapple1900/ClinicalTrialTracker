package com.yuanwei.android.rss;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
//import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

import com.yuanwei.android.BroadcastNotifier;
import com.yuanwei.android.Constants;
import com.yuanwei.android.db.DbAdapter;
import com.yuanwei.android.db.FeedProvider;
import com.yuanwei.android.rss.domain.Article;
import com.yuanwei.android.rss.domain.FeedMessage;
import com.yuanwei.android.rss.domain.RSSFeed;
import com.yuanwei.android.rss.domain.RssChannel;
import com.yuanwei.android.rss.parser.RssHandler;


public class RssDownloadingTask extends AsyncTask<FeedMessage, Void, List<RSSFeed>> {

	private ProgressDialog progress;
	private Context context;
	private ArrayList<Article> articles;
	private RssChannel rssChannel;
	private List<RSSFeed> rssFeedList;
    public static final String LOG_TAG = "RSSPullService";
    public static final String URL = "https://clinicaltrials.gov/ct2/search";

    // Defines and instantiates an object for handling status updates.
    private BroadcastNotifier mBroadcaster;
	//private ArticleListFragment articleListFrag;
	//private DrawPtrListActivity articleList;

	//public RssService(ArticleListFragment articleListFragment) {
	public RssDownloadingTask(Context context){
		//context=articleListFragment.getActivity();
		//context = articleList.getApplicationContext();
		this.context=context;
		mBroadcaster= new BroadcastNotifier(context);
		rssFeedList=new ArrayList<RSSFeed>();
		//articleListFrag = articleListFragment;
		progress = new ProgressDialog(context);
		progress.setMessage("Synchronizing...Press Back to Cancel");
	}
	
	public List<RSSFeed> getFeedList(){
		return this.rssFeedList;
	}

	@Override
	protected void onPreExecute() {
		if (Constants.LOGD)
		Log.e("ASYNC", "PRE EXECUTE");
		progress.show();
	}


	@Override
	protected void onPostExecute(final List<RSSFeed> rssFeedList) {
		if (Constants.LOGD)
		Log.e("ASYNC", "POST EXECUTE");
		//Changed @articleListFrag.getActivity() to @context 
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (rssFeedList!=null)
				for (RSSFeed rssFeed:rssFeedList){
					if (rssFeed.getItemList()==null){
						if (Constants.LOGD)
						Log.e("ASYNC", "Empty");
					}else {
						//Iterate each article, check if it is new article or existed.
						int num=0;
						for (Article a : rssFeed.getItemList()){
							if (Constants.LOGD)
							Log.d("DB", "Searching DB for GUID: " + a.getGuid());
							DbAdapter dba = new DbAdapter(context);
							dba.openToRead();
								Article fetchedArticle = dba.getBlogListing(a
										.getGuid());
								dba.close();
								if (fetchedArticle == null) {
									if (Constants.LOGD)
										Log.d("DB",
												"Found entry for first time: "
														+ a.getTitle());

									a.setFeed(rssFeed.getChannel().getTitle());
									dba = new DbAdapter(context);
									dba.openToWrite();
									dba.insertBlogListing(a.getGuid(),
											a.getTitle(), a.getPubDate(),
											a.getUpdateDate(),
											a.getDescription(), a.getUrl(),
											rssFeed.getChannel().getTitle());
									dba.close();
								} else {
									a.setFeed(Constants.TAG_TO_BE_REMOVED);
									num++;
								}
							}
							if (rssFeed.getChannel().isNewChannel()) {
								if (Constants.LOGD)
									Log.d("DB",
											"Insert the Feed for the first time");
								FeedProvider mFeedProvider = new FeedProvider(
										context);
								mFeedProvider.openToWrite();
								RssChannel channel = rssFeed.getChannel();
								mFeedProvider.insertFeed(channel.getTitle(),
										channel.getPubDate(),
										channel.getDescription(),
										channel.getUrl(), channel.getTag(),
										channel.getUpdateDate(),
										channel.getTotal());
								mFeedProvider.close();
							} else {
								if (Constants.LOGD)
									Log.d("DB", "Refresh the update time");
								FeedProvider mFeedProvider = new FeedProvider(
										context);
								mFeedProvider.openToWrite();
								RssChannel channel = rssFeed.getChannel();
								SimpleDateFormat ma = new SimpleDateFormat(
										Constants.STORED_DATE_FORMAT, Locale.US);
								String pubDate = ma.format(new Date());
								// To fix the bug that the lasted update date
								// can't be refreshed 07.07.2014 @Yuanwei
								// Bug seems fixed 07.18.2014@Yuanwei
								mFeedProvider.setLastUpdate(channel.getTitle(),
										pubDate);
								mFeedProvider.setTotalArticles(
										channel.getTitle(),
										 channel.getTotal()*2-num);
								mFeedProvider.close();
							}
						/*
						FeedProvider mFeedProvider = new FeedProvider(context);
						mFeedProvider.openToRead();
						RssChannel fetchedChannel = mFeedProvider.getFeed(rssFeed.getChannel().getUrl().toString());
						mFeedProvider.close();
						*/
						/*
						if (fetchedChannel==null){
							if (Constants.LOGD)
							Log.d("DB","Insert the Feed for the first time");

							RssChannel channel=rssFeed.getChannel();
							mFeedProvider= new FeedProvider(context);
							mFeedProvider.openToWrite();
							mFeedProvider.insertFeed(channel.getTitle(),channel.getPubDate(),channel.getDescription(),channel.getUrl(),channel.getTag(),channel.getUpdateDate(),channel.getTotal());
							mFeedProvider.close();
						}else {
							RssChannel channel=rssFeed.getChannel();
							    mFeedProvider.openToWrite();
							    mFeedProvider.setLastUpdate(channel.getDbId(), channel.getUpdateDate());
							    mFeedProvider.close();	
							    rssFeed.setRssChannel(null);
						}
						*/
					}
				}
				 mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_COMPLETE);			
			}
		});
		//PullToRefreshExpandableListView mPullRefreshListView=(PullToRefreshExpandableListView) ((Activity)context).findViewById(R.id.pull_refresh_expandable_list);
		//mPullRefreshListView.onRefreshComplete();
		super.onPostExecute(rssFeedList);
		progress.dismiss();
	}


	@Override
	protected  List<RSSFeed> doInBackground(FeedMessage... messages) {
		// If the server is not reachable, send a broadcast to the activity.
		if (!isConnectedToServer(URL, 5 * 1000)) {
			mBroadcaster
					.broadcastIntentWithState(Constants.STATE_ACTION_FAILED);
			return null;
		}// Otherwise start the download.
		else
			mBroadcaster
					.broadcastIntentWithState(Constants.STATE_ACTION_STARTED);
		// Iterate the channel list.
		for (FeedMessage feed : messages) {
			URL url = null;
			mBroadcaster
					.broadcastIntentWithState(Constants.STATE_ACTION_CONNECTING);
			try {

				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				url = new URL(feed.getUrl());
				RssHandler rh = new RssHandler();

				xr.setContentHandler(rh);
				xr.parse(new InputSource(url.openStream()));
				if (Constants.LOGD)
				Log.e("ASYNC", "PARSING FINISHED");
				articles = rh.getArticleList();
				
				rssChannel = rh.getChannel();
				
				rssChannel.setNewChannel(feed.isNewChannel());
				rssChannel.setTag(feed.getTag());
				
				RSSFeed rssFeed = new RSSFeed();
				rssFeed.setItemList(articles);
				rssFeed.setRssChannel(rssChannel);

				rssFeedList.add(rssFeed);

			} catch (IOException e) {
				Log.e("RSS Handler IO", e.getMessage() + " >> " + e.toString());
				return null;
			} catch (SAXException e) {
				Log.e("RSS Handler SAX", e.toString());
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				Log.e("RSS Handler Parser Config", e.toString());
				return null;
			}

		}
		return rssFeedList;
	}
	
	

	
	
	private boolean isConnectedToServer(String url, int timeout) {
		try {
			URL myUrl = new URL(url);
			URLConnection connection = myUrl.openConnection();
			connection.setConnectTimeout(timeout);
			connection.connect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
/*
private RssFeed getFeed(String urlString) {
	try {
		URL url = new URL(urlString);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		
		RssHandler rssHandler = new RssHandler();
		xmlReader.setContentHandler(rssHandler);
		
		InputSource is = new InputSource(url.openStream());
		System.out.println(is.toString());
		xmlReader.parse(is);
		
		return rssHandler.getFeed();
	}catch (Exception e) {
		return null;
	}
	*/
