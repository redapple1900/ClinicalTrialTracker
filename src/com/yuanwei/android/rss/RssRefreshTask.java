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

import com.yuanwei.android.BroadcastNotifier;
import com.yuanwei.android.Constants;
import com.yuanwei.android.db.DbAdapter;
import com.yuanwei.android.db.FeedProvider;
import com.yuanwei.android.rss.domain.Article;
import com.yuanwei.android.rss.domain.FeedMessage;
import com.yuanwei.android.rss.domain.RSSFeed;
import com.yuanwei.android.rss.domain.RssChannel;
import com.yuanwei.android.rss.parser.RssHandler;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class RssRefreshTask  extends AsyncTask<FeedMessage, Void, List<RSSFeed>>{
	/*
	public RssDownloadingService(Context context) {
		super("DownloadingService");
		// TODO Auto-generated constructor stub
	}
	*/
	public RssRefreshTask(Context context){
		//context=articleListFragment.getActivity();
		//context = articleList.getApplicationContext();
		this.context=context;
		mBroadcaster= new BroadcastNotifier(context);
		rssFeedList=new ArrayList<RSSFeed>();

	}
	

	private Context context;
	public static final String TAG = "Downloading Service";

	public static final String URL = "http://www.clinicaltrials.gov";
	// An ID used to post the notification.
	public static final int NOTIFICATION_ID = 2;

	private List<RSSFeed> rssFeedList;

	private BroadcastNotifier mBroadcaster;
    
    /*
	@Override
	protected void onHandleIntent(Intent intent) {
		// BEGIN_INCLUDE(service_onhandle)
		int count = 0;
		boolean connection_flag = true;
		
		mBroadcaster= new BroadcastNotifier(context);

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		NetworkInfo mMobile = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mMobile!=null&&mMobile.isAvailable())
			connection_flag=mMobile.isConnected()&&mWifi.isConnected();
		else connection_flag=mWifi.isConnected();

		mFeedProvider = new FeedProvider(this);
		mFeedProvider.openToRead();
		channelList = mFeedProvider.getAllChannelsByDate();
		mFeedProvider.close();
		
		deleteOutdatedArticles(channelList);
		
		if (connection_flag&&isConnectedToServer(URL, 5 * 1000)) {


			rssFeedList = downloadFeedFromUrl(channelList);
			if (rssFeedList != null)
				count = syncDatabaseWithFeed(rssFeedList);

			// If the app got new feeds from one or more channels, it will send
			// a notification to the user. Otherwise, it
			// remains silent.
			if (0 != count) {
				//sendNotification(getString(R.string.string_synchronized)
						//+ count);
				mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_UPDATED);
				if (Constants.LOGD)
					Log.i(TAG, "New item");
			} else {
				if (Constants.LOGD)
					mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_NON_UPDATE);
				if (Constants.LOGD)
					Log.i(TAG, "No new Item. :-(");
			}
		}else {
			if (Constants.LOGD)
				mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_FAILED);
		}


		// Release the wake lock provided by the BroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
		// END_INCLUDE(service_onhandle)
	}*/


	private ArrayList<Article> articles;
	private RssChannel rssChannel;

    public static final String LOG_TAG = "RSSPullService";


    // Defines and instantiates an object for handling status updates.


	@Override
	protected void onPreExecute() {
		if (Constants.LOGD)
		Log.e("ASYNC", "PRE EXECUTE");
		//TODO
	}


	@Override
	protected void onPostExecute(final List<RSSFeed> rssFeedList) {
		if (Constants.LOGD)
		Log.e("ASYNC", "POST EXECUTE");
		//Changed @articleListFrag.getActivity() to @context 
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*
				 * @Param sync  represents the number of items refreshed.
				 */
				int sync=0;
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
									sync++;
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
				}
				if (rssFeedList!=null&&sync>0)
				 mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_UPDATED);	
				else if (rssFeedList!=null&&sync==0)
				mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_NON_UPDATE);
				else;
			}
		});
		super.onPostExecute(rssFeedList);
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
		
		if (rssFeedList!=null){
			
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


