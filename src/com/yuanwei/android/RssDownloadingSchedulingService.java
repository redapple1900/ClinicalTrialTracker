package com.yuanwei.android;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.redapple.android.R;
import com.yuanwei.android.db.DbAdapter;
import com.yuanwei.android.db.FeedProvider;
import com.yuanwei.android.rss.domain.Article;
import com.yuanwei.android.rss.domain.RSSFeed;
import com.yuanwei.android.rss.domain.RssChannel;
import com.yuanwei.android.rss.parser.RssHandler;
import com.yuanwei.android.util.DateConverter;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class RssDownloadingSchedulingService extends IntentService {
	public RssDownloadingSchedulingService() {
		super("SchedulingService");
	}

	public static final String TAG = "Scheduling Downloading Service";

	public static final String URL = "http://www.clinicaltrials.gov";
	// An ID used to post the notification.
	public static final int NOTIFICATION_ID = 1;

	private List<RssChannel> channelList;

	private List<RSSFeed> rssFeedList;

	private FeedProvider mFeedProvider;

	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	@Override
	protected void onHandleIntent(Intent intent) {
		// BEGIN_INCLUDE(service_onhandle)
		int count = 0;
		boolean connection_flag = true;

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
				sendNotification(getString(R.string.string_synchronized)
						+ count);
				// TODO How to show tell the user which channel get updated??
				if (Constants.LOGD)
					Log.i(TAG, "New item");
			} else {
				if (Constants.LOGD)
					sendNotification(getString(R.string.string_failed));
				if (Constants.LOGD)
					Log.i(TAG, "No new Item. :-(");
			}
		}else {
			if (Constants.LOGD)
				sendNotification("No internet connection");
		}


		// Release the wake lock provided by the BroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
		// END_INCLUDE(service_onhandle)
	}

	private List<RSSFeed> downloadFeedFromUrl(List<RssChannel> list) {
		if (list == null || list.size() == 0)
			return null;
		String urlArray[] = new String[list.size()];
		int i = 0;
		for (RssChannel r : list) {
			urlArray[i] = r.getUrl().toString();
			i++;
		}
		List<RSSFeed> result = new ArrayList<RSSFeed>();
		for (String feed : urlArray) {
			URL url = null;
			try {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				url = new URL(feed);
				RssHandler rh = new RssHandler();

				xr.setContentHandler(rh);
				xr.parse(new InputSource(url.openStream()));
				if (Constants.LOGD)
					Log.e("Service", "PARSING FINISHED");
				List<Article> articles = rh.getArticleList();
				RssChannel rssChannel = rh.getChannel();
				RSSFeed rssFeed = new RSSFeed();
				rssFeed.setItemList(articles);
				rssFeed.setRssChannel(rssChannel);
				result.add(rssFeed);

			} catch (IOException e) {
				if (Constants.LOGD)
					Log.e("RSS Handler IO",
							e.getMessage() + " >> " + e.toString());
				return null;

			} catch (SAXException e) {
				if (Constants.LOGD)
					Log.e("RSS Handler SAX", e.toString());
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				if (Constants.LOGD)
					Log.e("RSS Handler Parser Config", e.toString());
				return null;
			}
		}

		return result;
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
	private void deleteOutdatedArticles(List<RssChannel> list){
		if (list==null||list.size()==0) return;
		else {
			List<Article> articlelist;
			SimpleDateFormat format = new SimpleDateFormat(
					Constants.STORED_DATE_FORMAT, Locale.US);
			FeedProvider mFeedProvider = new FeedProvider(this);
			if (list!=null&&list.size()>0)
			for (RssChannel r : list) {
				DbAdapter dba=new DbAdapter(this);
				dba.openToRead();
				articlelist=dba.getFeedListing(r.getTitle());
				dba.close();
				int total=r.getTotal();
				if (articlelist!=null&&articlelist.size()>0)
				for (Article a:articlelist){
					if (DateConverter.getDateGap(a.getUpdateDate(), format)>=Constants.MAXIMAM_DAYS_ALLOWED){
						dba.openToWrite();
						dba.deleteById(a.getDbId());
						dba.close();
						total--;
					}
				}//End for (Article a articlelist)				
				mFeedProvider.openToWrite();
				mFeedProvider.setTotalArticles(r.getTitle(), total);
				mFeedProvider.close();
			}
		}
	}
	private int syncDatabaseWithFeed(List<RSSFeed> rssFeedList) {
		int newfeed = 0;
		
		for (RSSFeed rssFeed : rssFeedList) {
			if (rssFeed.getItemList() == null) {
				Log.e("ASYNC", "Empty");
			} else {
				int total=0;
				for (Article a : rssFeed.getItemList()) {
					if (Constants.LOGD)
						Log.d("DB", "Searching DB for GUID: " + a.getGuid());
					DbAdapter dba = new DbAdapter(this);
					dba.openToRead();
					Article fetchedArticle = dba.getBlogListing(a.getGuid());
					dba.close();
					if (fetchedArticle == null) {
						newfeed++;
						total++;
						if (Constants.LOGD)
						Log.d("DB",
								"Found entry for first time: " + a.getTitle());
						dba = new DbAdapter(this);
						dba.openToWrite();
						dba.insertBlogListing(a.getGuid(), a.getTitle(), a
								.getPubDate(), a.getUpdateDate(), a
								.getDescription(), a.getUrl(), rssFeed
								.getChannel().getTitle());
						dba.close();
					}/*
					 * else { a.setDbId(fetchedArticle.getDbId());
					 * a.setTitle(fetchedArticle.getTitle());
					 * a.setPubDate(fetchedArticle.getPubDate());
					 * a.setUpdateDate(fetchedArticle.getUpdateDate());
					 * a.setDescription(fetchedArticle.getDescription());
					 * a.setUrl(fetchedArticle.getUrl());
					 * a.setFeed(fetchedArticle.getFeed());
					 * a.setOffline(fetchedArticle.isOffline());
					 * a.setRead(fetchedArticle.isRead());
					 * a.setStar(fetchedArticle.isStar()); }
					 */
				}
				FeedProvider mFeedProvider = new FeedProvider(this);

				RssChannel channel = rssFeed.getChannel();
				mFeedProvider.openToWrite();
				mFeedProvider.setLastUpdate(channel.getTitle(),
						channel.getUpdateDate());
				mFeedProvider.setTotalArticles(channel.getTitle(), channel.getTotal()+total);
				mFeedProvider.close();

			}
		}

		return newfeed;
	}

	// Post a notification indicating new feeds are available;
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, DrawerExpandableListActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.rssicon)
				.setContentTitle(getString(R.string.app_name))
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
/*
	private String readIt(InputStream stream) throws IOException {

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		for (String line = reader.readLine(); line != null; line = reader
				.readLine())
			builder.append(line);
		reader.close();
		return builder.toString();
	}
}
*/