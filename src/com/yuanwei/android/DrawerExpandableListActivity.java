package com.yuanwei.android;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
//import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.redapple.android.R;
import com.yuanwei.android.Constants;
import com.yuanwei.android.adapter.ArticleExpandableListAdapter;
import com.yuanwei.android.adapter.RssChannelListAdapter;
import com.yuanwei.android.db.DbAdapter;
import com.yuanwei.android.db.FeedProvider;
import com.yuanwei.android.rss.RssDownloadingTask;
import com.yuanwei.android.rss.RssRefreshTask;
import com.yuanwei.android.rss.domain.Article;
import com.yuanwei.android.rss.domain.FeedMessage;
import com.yuanwei.android.rss.domain.RSSFeed;
import com.yuanwei.android.rss.domain.RssChannel;

/**
 * This example illustrates a common usage of the DrawerLayout widget in the
 * Android support library.
 * 
 * <p>
 * A DrawerLayout should be positioned at the top of your view hierarchy,
 * placing it below the action bar but above your content views. The primary
 * content should match_parent in both dimensions. Each drawer should define a
 * reasonable width and match_parent for height. Drawer views should be
 * positioned after the content view in your layout to preserve proper ordering.
 * </p>
 * 
 * <p>
 * When a navigation (left) drawer is present, the host activity should detect
 * presses of the action bar's Up affordance as a signal to open and close the
 * navigation drawer. Items within the drawer should fall into one of two
 * categories.
 * </p>
 * 
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic
 * policies as list or tab navigation in that a view switch does not create
 * navigation history. This pattern should only be used at the root activity of
 * a task, leaving some form of Up navigation active for activities further down
 * the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an
 * alternate parent for Up navigation. This allows a user to jump across an
 * app's navigation hierarchy at will. The application should treat this as it
 * treats Up navigation from a different task, replacing the current task stack
 * using TaskStackBuilder or similar. This is the only form of navigation drawer
 * that should be used outside of the root activity of a task.</li>
 * </ul>
 * 
 * <p>
 * Right side drawers should be used for actions, not navigation. This follows
 * the pattern established by the Action Bar that navigation should be to the
 * left and actions to the right. An action should be an operation performed on
 * the current contents of the window, for example enabling or disabling a data
 * overlay on top of the current content.
 * </p>
 */
public class DrawerExpandableListActivity extends ExpandableListActivity implements
		ArticleExpandableListAdapter.OnGroupClickListener {
	private boolean unreadonly;
	
	private boolean star;

	private Button button_addfeed;

	private Button button_showallitems;
	
	private Button button_staritems;

	private DrawerLayout mDrawerLayout;
	// this is the layout inside mDrawerLayout
	private LinearLayout mDrawer;

	private ListView mDrawerList;

	private ActionBarHelper mActionBar;

	private ActionBarDrawerToggle mDrawerToggle;

	public static final String CLASS_TAG = "MainActivity";

	public static final String URL = "https://www.clinicaltrials.gov";



	private ArticleExpandableListAdapter mAdapter;
	private RssChannelListAdapter mDrawerAdapter;
	/*
	 * Construct a BroadcastReceiver to monitor the status of downloading
	 */
	private DownloadStateReceiver mDownloadStateReceiver;



	private List<RssChannel> channelList;

	private ArrayList<Article> articleList, itemList;

	private Map<String, RSSFeed> feedmap = new HashMap<String, RSSFeed>();

	private RssChannel currentChannel;
	
	private int currentChannelPosition = -1;

	private RssDownloadingTask rssService;
	
	private FeedProvider mFeedProvider;


	private DbAdapter dba;

	AlarmReceiver alarm = new AlarmReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_layout);
		if (savedInstanceState == null) {
			unreadonly = false;
			star=false;
			currentChannelPosition = -1;
		} else {
			star=savedInstanceState.getBoolean("star");
			unreadonly = savedInstanceState.getBoolean("unreadonly");
			currentChannelPosition = savedInstanceState
					.getInt("currentChannelPosition");
			this.invalidateOptionsMenu();
		}
		// If both databases have been instantiated,the activity should
		// undergone configuration change.
		/*
		 * Load @channelList from local database
		 */
		mFeedProvider = new FeedProvider(this);
		mFeedProvider.openToRead();
		channelList = mFeedProvider.getAllChannelsByDate();
		
		mFeedProvider.close();

		articleList = new ArrayList<Article>();
		/*
		 * 
		 */
		if (channelList != null) {
			if (null == itemList) {
				/*
				 * Initiate the instance of @itemList
				 */
				//itemList = new ArrayList<Article>();
				if (Constants.LOGD)
					Log.d("Initiate articleList", CLASS_TAG);
				/*
				 * Load @articleList from local database
				 */
				dba = new DbAdapter(this);
				dba.openToRead();
				for (RssChannel r : channelList) {
					RSSFeed feed = new RSSFeed();// Put all feeds in a hashmap
													// for quick access
					feed.setRssChannel(r);
					feed.setItemList(dba.getFeedListing(r.getTitle()));
					feedmap.put(r.getTitle(), feed);
				}

				itemList = dba.getAllArticlesByDate();
				dba.close();

				String condition =null;
				try{
					currentChannel=channelList.get(currentChannelPosition);
					condition = currentChannel.getTitle();
				}catch(Exception e){
					if (Constants.LOGD)
						Log.e("Try adding feed", "IndexOutOfBound");
				}
				try {
					articleList.addAll(getArticleList(condition,
							unreadonly, star, false, feedmap, itemList));
				} catch (Exception e) {
					if (Constants.LOGD)
						Log.e("Try adding feed", "Empty List");
				}
				/*
				 * If the activity is restarted, load the previous channel
				 */
				/*
				if (currentChannelPosition >= 0) {
					currentChannel = channelList
							.get((int) currentChannelPosition);
				}

				if (null != currentChannel) {
					if (!unreadonly) 
						// articleList.clear();
							try{
								articleList.addAll(feedmap.get(
										currentChannel.getTitle()).getItemList());
							}catch(Exception e){
								if (Constants.LOGD)
									Log.e("error","Empty List");
							}

					 else {
						List<Article> unreadItemList = new ArrayList<Article>();
						for (Article a : feedmap.get(currentChannel.getTitle())
								.getItemList())
							if (!a.isRead())
								unreadItemList.add(a);
						// articleList.clear();
						if (unreadItemList != null)
							articleList.addAll(unreadItemList);
					}
					/*
					 * if (null != (feedmap.get(currentChannel.getTitle())
					 * .getItemList())) {
					 * articleList.addAll(feedmap.get(currentChannel.getTitle())
					 * .getItemList()); }
					 */
				/*
				} else {
					if (!unreadonly)
						articleList.addAll(itemList);
					else {
						List<Article> unreadItemList = new ArrayList<Article>();
						for (Article a : itemList)
							if (!a.isRead())
								unreadItemList.add(a);
						articleList.addAll(unreadItemList);
					}
				}
				/*
				 * if (unreadonly) { List<Article> toDeleteList = new
				 * ArrayList<Article>(); int i = 0; for (Article a :
				 * articleList) { if (a.isRead() == true) { toDeleteList.add(a);
				 * i++;
				 * 
				 * } } Log.d(i + "", CLASS_TAG); if (null != toDeleteList &&
				 * toDeleteList.size() != 0) {
				 * articleList.removeAll(toDeleteList); Log.d("removeall",
				 * CLASS_TAG); } }
				 */
			}
		}

		mAdapter = new ArticleExpandableListAdapter(this, articleList,
				articleList);
		// alarm.setAlarm(this);

		mDrawerList = (ListView) findViewById(R.id.start_drawer);

		mDrawerAdapter = new RssChannelListAdapter(this, channelList);
		mDrawerList.setAdapter(mDrawerAdapter);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawer = (LinearLayout) findViewById(R.id.start_drawer_layout);

		// mContent = (TextView) findViewById(R.id.content_text);

		mDrawerLayout.setDrawerListener(new DemoDrawerListener());
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mActionBar = createActionBarHelper();
		mActionBar.init();

		// ActionBarDrawerToggle provides convenient helpers for tying together
		// the
		// prescribed interactions between a top-level sliding drawer and the
		// action bar.
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close);

		button_addfeed = (Button) findViewById(R.id.button_addfeed1);
		button_addfeed.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//
				RssDialogManager manager = new RssDialogManager(
						DrawerExpandableListActivity.this);
				// manager.showAddFeedDialog();
				manager.showSimpleSearchDialog(DrawerExpandableListActivity.this);
				star=false;
			}
		});
		button_showallitems = (Button) findViewById(R.id.button_showallitem);
		button_showallitems.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showAllItems();
				
			}
		});

		//mExpandableListView = (ExpandableListView) findViewById(android.R.id.list);
		
		//mExpandableListView.setAdapter(mAdapter);
		
		button_staritems=(Button)findViewById(R.id.button_showstar1);
		button_staritems.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showStarItems();
			}
		});

		// Set a listener to be invoked when the list should be refreshed.
		/*
		 * mPullRefreshListView.setOnRefreshListener(new
		 * OnRefreshListener<ExpandableListView>() {
		 * 
		 * @Override public void onRefresh(PullToRefreshBase<ExpandableListView>
		 * refreshView) { // Do work to refresh the list here. new
		 * RssService(DrawPtrListActivity.this).execute(BLOG_URL);
		 * 
		 * } });
		 */
		setListAdapter(mAdapter);
		registerForContextMenu(mDrawerList);

		/*
		 * Creates an intent filter for DownloadStateReceiver that intercepts
		 * broadcast Intents
		 */

		// The filter's action is BROADCAST_ACTION
		IntentFilter statusIntentFilter = new IntentFilter(
				Constants.BROADCAST_ACTION);

		// The filter's action is BROADCAST_ACTION

		// Sets the filter's category to DEFAULT
		statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

		// Instantiates a new DownloadStateReceiver
		if (null == mDownloadStateReceiver) {
			mDownloadStateReceiver = new DownloadStateReceiver();
		}

		// Registers the DownloadStateReceiver and its intent filters
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mDownloadStateReceiver, statusIntentFilter);

		// this.setTheme(android.R.style.Theme_Holo_Light_Panel);
	}

	protected void showAllItems() {
		// TODO Auto-generated method stub
		// Synchronized all items with the database.
		/*
		 * dba.openToRead(); articleList.clear();
		 * articleList.addAll(dba.getAllArticlesByDate()); dba.close();
		 */
		if (!unreadonly) {
			articleList.clear();
			articleList.addAll(itemList);
		} else {

			List<Article> unreadItemList = new ArrayList<Article>();
			for (Article a : itemList)
				if (!a.isRead())
					unreadItemList.add(a);
			articleList.clear();
			if (unreadItemList != null)
				articleList.addAll(unreadItemList);

		}
		// Sort the data
		// Collections.sort(articleList, mComparatorArticle);
		mAdapter.notifyDataSetChanged();
		mActionBar.setTitle(getTitle());
		currentChannel = null;
		currentChannelPosition = -1;
		star=false;
		mDrawerLayout.closeDrawer(mDrawer);

	}
	protected void showStarItems(){
		List<Article> starItemList = new ArrayList<Article>();
		for (Article a : itemList)
			if (a.isStar())
				starItemList.add(a);
		articleList.clear();
		if (starItemList != null)
			articleList.addAll(starItemList);
		
		mAdapter.notifyDataSetChanged();
		mActionBar.setTitle(getTitle());
		currentChannel = null;
		currentChannelPosition = -1;
		mDrawerLayout.closeDrawer(mDrawer);
		
	}

	/*
	 * TODO how to make a status table? public
	 */

	public List<Article> getArticleList(String condition, boolean unread,
			boolean star, boolean recent, Map<String, RSSFeed> map,
			List<Article> list) {
		List<Article> result = new ArrayList<Article>();
		if (list == null || map == null){
			
			return null;
		
		}
		if (star){
			List<Article> starItemList = new ArrayList<Article>();
			for (Article a : result)
				if (a.isStar())
					starItemList.add(a);
			return starItemList;
		}
		
		
		
		if (condition == null) {
			result = list;
		} else {
			result = map.get(condition).getItemList();
		}
		if (result==null)
			return result;
			
		
		if (unread) {
			List<Article> readItemList = new ArrayList<Article>();
			for (Article a : result)
				if (a.isRead())
					readItemList.add(a);
			if (readItemList.size()>0)
				result.removeAll(readItemList);
		}
			
		if (recent) {
			//List<Article> recentItemList = new ArrayList<Article>();
			/*
			for (Article a : result) {
				// if (a.isStar())
				// recentItemList.add(a);
			}
			*/
			/*
			if (recentItemList != null) {
				result.clear();
				result.addAll(recentItemList);
			}
			*/
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		if (unreadonly) {

			inflater.inflate(R.menu.readallmenu, menu);
		} else {

			inflater.inflate(R.menu.refreshmenu, menu);
		}
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Change the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		if (unreadonly) {
			menu.clear();
			inflater.inflate(R.menu.readallmenu, menu);
		} else {
			menu.clear();
			inflater.inflate(R.menu.refreshmenu, menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Forget the meaning
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		Log.d("ActionBar", "Loading Item");
		int id = item.getItemId();
		switch (id) {// Begin include:Switch
		case R.id.actionbar_refresh1:
			// TODO This piece must have space to improve @07.21.2014
			
			if (null != currentChannel) {
				
				FeedMessage message = new FeedMessage();
				message.setUrl(currentChannel.getUrl().toString());
				message.setNewChannel(false);
				message.setNewTrials(currentChannel.getTag());

				new RssRefreshTask(DrawerExpandableListActivity.this).execute(message);
			} else {
				FeedMessage[] link = new FeedMessage[channelList.size()];
				int count = 0;
				for (RssChannel r : channelList) {
					FeedMessage message = new FeedMessage();
					message.setUrl(r.getUrl().toString());
					message.setNewChannel(false);
					message.setNewTrials(r.getTag());
					
					link[count++] = message;
				}
				new RssRefreshTask(DrawerExpandableListActivity.this).execute(link);
			}
			
			break;
		case R.id.actionbar_markunread1:
			if (!unreadonly) {
				unreadonly = true;
				this.invalidateOptionsMenu();

				/*
				 * articleList.clear(); if (null!=(feedmap.get(currentChannel
				 * .getTitle()).getItemList())){ articleList.addAll(feedmap.get
				 * (currentChannel.getTitle()).getItemList()); for (Article
				 * a:articleList){ if (a.isRead()){ articleList.remove(a); } }
				 * if (null==articleList){
				 * Toast.makeText(getApplicationContext(),
				 * "All items have been read", Toast.LENGTH_LONG).show(); } }
				 */
				List<Article> unreadItemList = new ArrayList<Article>();

				for (Article a : articleList) {
					if (!a.isRead()) {
						unreadItemList.add(a);
					}
				}
				articleList.clear();
				if (unreadItemList != null)
					articleList.addAll(unreadItemList);

				if (null == articleList || articleList.size() == 0) {
					Toast.makeText(getApplicationContext(),
							"All items have been read", Toast.LENGTH_LONG)
							.show();
				}
				mAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "Show unread items only", Toast.LENGTH_LONG).show();

			}
			break;
		case R.id.actionbar_read1:
			if (unreadonly) {
				unreadonly = false;
				this.invalidateOptionsMenu();
				if (null != currentChannel) {
					articleList.clear();
					if (null != feedmap.get(currentChannel.getTitle())
							.getItemList())
						articleList.addAll(feedmap.get(
								currentChannel.getTitle()).getItemList());
					
				} else {
					/*
					 * articleList.clear(); for (RssChannel r : channelList) {
					 * articleList.addAll(feedmap.get(r.getTitle())
					 * .getItemList()); }
					 */
					articleList.clear();
					articleList.addAll(itemList);
					// Collections.sort(articleList, mComparatorArticle);				
				}
				mAdapter.notifyDataSetChanged();
				
			}
			break;
		case R.id.actionbar_markallread1:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Mark all items read")
					.setIcon(R.drawable.alert_dialog_icon)
					.setMessage(
							"All items under this section will be marked as read. This action can not be undone.Are you sure?");
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (null != articleList && articleList.size() > 0) {
								for (Article a : articleList) {
									a.setRead(true);
									dba.openToWrite();
									dba.markAsRead(a.getGuid());
									dba.close();
								}
							}
							mAdapter.notifyDataSetChanged();

						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.show();
			break;

		case R.id.actionbar_alarm:
			RssDialogManager manager= new RssDialogManager(this);
			manager.showAlarmDialog(DrawerExpandableListActivity.this);

			break;
		case R.id.actionbar_help:
			RssDialogManager help= new RssDialogManager(this);
			help.showHelpDialog(DrawerExpandableListActivity.this);
			break;
		case R.id.actionbar_contact:
			 Intent i = new Intent(Intent.ACTION_SEND);  
				//i.setType("text/plain"); //
				i.setType("message/rfc822"); // 
				i.putExtra(Intent.EXTRA_EMAIL, new String[]{"yuanwei@sas.upenn.edu"});  
				i.putExtra(Intent.EXTRA_SUBJECT,"FeedBack");				
				startActivity(Intent.createChooser(i, "Send Email to Arthor"));
			break;

		}// End include:Switch
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Called whenever we call invalidateOptionsMenu()
	 * 
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { // If the nav
	 * drawer is open, hide action items related to the content view boolean
	 * drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
	 * menu.findItem(R.id.action_websearch).setVisible(!drawerOpen); return
	 * super.onPrepareOptionsMenu(menu); }
	 */

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { int id =
	 * item.getItemId(); if (id == R.id.actionbar_add) { addList(); return true;
	 * } return false; }
	 * 
	 * private void addList(){
	 * 
	 * }
	 */
	/*
	 * @Override public void onCreateContextMenu(ContextMenu arg0, View arg1,
	 * ContextMenuInfo arg2) { AdapterContextMenuInfo info =
	 * (AdapterContextMenuInfo) arg2;
	 * 
	 * Log.d("123", "123"); arg0.add(Menu.NONE, Menu.FIRST, Menu.NONE,
	 * "Delete"); arg0.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE,
	 * "Mark As Read"); super.onCreateContextMenu(arg0, arg1, info); }
	 */
	 @Override
		public void onCreateContextMenu(ContextMenu arg0, View arg1,
				ContextMenuInfo arg2) {
			super.onCreateContextMenu(arg0, arg1, arg2);
		arg0.add(Menu.NONE,Menu.FIRST,Menu.NONE,"Remove this Feed");
		}
	 //Super Important, Sounds like the reason is that do not return super.OnContextItemSelected.
	 @Override
	public boolean onContextItemSelected(MenuItem menuItem){
		 AdapterContextMenuInfo info = (AdapterContextMenuInfo) (menuItem)
		          .getMenuInfo();
		switch((menuItem).getItemId()){
		case Menu.FIRST:
			final String title =(channelList.get(info.position)).getTitle();
			List<Article> removeList=new ArrayList<Article>();
			try{
				removeList.addAll(feedmap.get(title).getItemList());
				articleList.removeAll(removeList);
				itemList.removeAll(removeList);
				dba.openToWrite();
				for (Article a:removeList)
					dba.deleteByGuid(a.getGuid());
				dba.close();
				
			}catch(Exception e){
				if (Constants.LOGD)
					Log.e("Delete channel","Empty list");
			}
			feedmap.remove(title);			
			
			mFeedProvider.openToWrite();
			mFeedProvider.deleteChannel(title);
			mFeedProvider.close();
			channelList.remove(info.position);
			mDrawerAdapter.notifyDataSetChanged();	
			mAdapter.notifyDataSetChanged();
			break;
		}
		return true;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("unreadonly", unreadonly);
		outState.putBoolean("star",star);
		outState.putLong("currentChannelPosition", currentChannelPosition);
	}

	/*
	 * This callback is invoked when the system is about to destroy the
	 * Activity.
	 */
	@Override
	public void onDestroy() {

		// If the DownloadStateReceiver still exists, unregister it and set it
		// to null
		if (mDownloadStateReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mDownloadStateReceiver);
			mDownloadStateReceiver = null;
		}
		// Must always call the super method at the end.
		super.onDestroy();
	}

	/**
	 * This list item click listener implements very simple view switching by
	 * changing the primary content text. The drawer is closed when a selection
	 * is made.
	 */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			currentChannel = channelList.get(position);
			/*
			 * if (!unreadonly) { articleList.clear(); if (null !=
			 * (feedmap.get(channelList.get(position).getTitle())
			 * .getItemList()))
			 * articleList.addAll(feedmap.get(currentChannel.getTitle())
			 * .getItemList()); } else { List<Article> unreadItemList = new
			 * ArrayList<Article>(); if (null !=
			 * (feedmap.get(channelList.get(position).getTitle())
			 * .getItemList())) for (Article a :
			 * feedmap.get(currentChannel.getTitle()) .getItemList()) if
			 * (!a.isRead()) unreadItemList.add(a); articleList.clear(); if
			 * (unreadItemList != null) articleList.addAll(unreadItemList); }
			 */
			articleList.clear();
			try {
				articleList.addAll(getArticleList(currentChannel.getTitle(),
						unreadonly, star, false, feedmap, itemList));
			} catch (Exception e) {
				if (Constants.LOGD)
					Log.e("Try adding feed", "Empty List");
			}
			/*
			 * articleList.clear(); if (null !=
			 * (feedmap.get(channelList.get(position).getTitle())
			 * .getItemList())) { articleList.addAll(feedmap.get(
			 * channelList.get(position).getTitle()).getItemList()); } if
			 * (unreadonly) { List<Article> toDeleteList = new
			 * ArrayList<Article>(); int i = 0; for (Article a : articleList) {
			 * if (a.isRead() == true) { toDeleteList.add(a); i++;
			 * 
			 * } } Log.d(i + "", CLASS_TAG); if (null != toDeleteList &&
			 * toDeleteList.size() != 0) { articleList.removeAll(toDeleteList);
			 * Log.d("removeall", CLASS_TAG); } }
			 */
			// Collections.sort(articleList, mComparatorArticle);

			Log.d(channelList.get(position).getTitle(), "search by feed");
			mAdapter.notifyDataSetChanged();
			mActionBar.setTitle(channelList.get(position).getShortTitle());
			mDrawerLayout.closeDrawer(mDrawer);
			star=false;
			currentChannel = channelList.get(position);
			currentChannelPosition = position;

		}
	}

	/**
	 * A drawer listener can be used to respond to drawer events such as
	 * becoming fully opened or closed. You should always prefer to perform
	 * expensive operations such as drastic relayout when no animation is
	 * currently in progress, either before or after the drawer animates.
	 * 
	 * When using ActionBarDrawerToggle, all DrawerLayout listener methods
	 * should be forwarded if the ActionBarDrawerToggle is not used as the
	 * DrawerLayout listener directly.
	 */
	private class DemoDrawerListener implements DrawerLayout.DrawerListener {
		@Override
		public void onDrawerOpened(View drawerView) {
			mDrawerToggle.onDrawerOpened(drawerView);
			mActionBar.onDrawerOpened();
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			mDrawerToggle.onDrawerClosed(drawerView);
			mActionBar.onDrawerClosed();
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			mDrawerToggle.onDrawerStateChanged(newState);
		}
	}

	/**
	 * Create a compatible helper that will manipulate the action bar if
	 * available.
	 */
	private ActionBarHelper createActionBarHelper() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new ActionBarHelperICS();
		} else {
			return new ActionBarHelper();
		}
	}

	/**
	 * Stub action bar helper; this does nothing.
	 */
	private class ActionBarHelper {
		public void init() {
		}

		public void onDrawerClosed() {
		}

		public void onDrawerOpened() {
		}

		public void setTitle(CharSequence title) {
		}
	}

	/**
	 * Action bar helper for use on ICS and newer devices.
	 */
	private class ActionBarHelperICS extends ActionBarHelper {
		private final ActionBar mActionBar;
		private CharSequence mDrawerTitle;
		private CharSequence mTitle;

		ActionBarHelperICS() {
			mActionBar = getActionBar();
		}

		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		@Override
		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
			mTitle = mDrawerTitle = getTitle();
		}

		/**
		 * When the drawer is closed we restore the action bar state reflecting
		 * the specific contents in view.
		 */
		@Override
		public void onDrawerClosed() {
			super.onDrawerClosed();
			mActionBar.setTitle(mTitle);
		}

		/**
		 * When the drawer is open we set the action bar to a generic title. The
		 * action bar should only contain data relevant at the top level of the
		 * nav hierarchy represented by the drawer, as the rest of your content
		 * will be dimmed down and non-interactive.
		 */
		@Override
		public void onDrawerOpened() {
			super.onDrawerOpened();
			mActionBar.setTitle(mDrawerTitle);
		}

		@Override
		public void setTitle(CharSequence title) {
			mTitle = title;
		}
	}

	private class DownloadStateReceiver extends BroadcastReceiver {

		private DownloadStateReceiver() {

			// prevents instantiation by other packages.
		}

		/**
		 * 
		 * This method is called by the system when a broadcast Intent is
		 * matched by this class' intent filters
		 * 
		 * @param context
		 *            An Android context
		 * @param intent
		 *            The incoming broadcast Intent
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			/*
			 * Gets the status from the Intent's extended data, and chooses the
			 * appropriate action
			 */
			switch (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS,
					Constants.STATE_ACTION_COMPLETE)) {
			/*
			 * Deal with alarm
			 */
			case Constants.ALARM_START:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "Start Alarm");
				alarm.setAlarm(DrawerExpandableListActivity.this);
				break;
			case Constants.ALARM_CANCEL:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "Cancel Alarm");
				alarm.cancelAlarm(DrawerExpandableListActivity.this);
				break;
			case Constants.STATE_ACTION_FAILED:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "State: Failed)");
				
				Toast.makeText(DrawerExpandableListActivity.this, "Unable to connect server. Please check your connection", Toast.LENGTH_SHORT).show();
				
				break;

			// Logs "started" state
			case Constants.STATE_ACTION_STARTED:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "State: STARTED");
				
				break;
			// Logs "connecting to network" state
			case Constants.STATE_ACTION_CONNECTING:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "State: CONNECTING");
				
				break;
			// Logs "parsing the RSS feed" state
			case Constants.STATE_ACTION_PARSING:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "State: PARSING");
				
				break;
			// Logs "Writing the parsed data to the content provider" state
			case Constants.STATE_ACTION_WRITING:
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "State: WRITING");
				
				break;
			// Starts displaying data when the RSS download is complete
			case Constants.STATE_ACTION_COMPLETE:
				// Logs the status
				// TODO To make the code more readable;
				if (Constants.LOGD) 

					Log.d(CLASS_TAG, "State: COMPLETE");
				

				List<RSSFeed> rssFeedList = rssService.getFeedList();
				if (rssFeedList!=null)
				for (RSSFeed rssFeed : rssFeedList) {
					try {
						if (rssFeed.getChannel().isNewChannel()) {
							channelList.add(rssFeed.getChannel());
							feedmap.put(rssFeed.getChannel().getTitle(),
									rssFeed);
							articleList.clear();
							articleList.addAll(rssFeed.getItemList());
							itemList.addAll(rssFeed.getItemList());

						} else {
							List<Article> updatelist = rssFeed.getItemList();
							Iterator<Article> iterator = updatelist.iterator();
							while (iterator.hasNext()) {
								if (iterator.next().getFeed()
										.equals(Constants.TAG_TO_BE_REMOVED))
									iterator.remove();
							}
							feedmap.get(rssFeed.getChannel().getTitle())
									.getItemList().addAll(0, updatelist);
							articleList.addAll(0, updatelist);
							itemList.addAll(0, updatelist);
						}
					} catch (Exception e) {
						if (Constants.LOGD)
							Log.e("Try adding feed", "Empty List");
					}

					// }
				}

				// Begin: remove read items if @unread only is toggled

				if (unreadonly) {
					List<Article> toDeleteList = new ArrayList<Article>();

					for (Article a : articleList) {
						if (a.isRead()) {
							toDeleteList.add(a);

						}
					}

					if (null != toDeleteList && toDeleteList.size() != 0) {
						articleList.removeAll(toDeleteList);
						Log.d("removeall", CLASS_TAG);
					}
				}
				// End:remove read items if @unread only is toggled
				// if (null!=articleList)
				// Collections.sort(articleList, mComparatorArticle);
				mFeedProvider = new FeedProvider(DrawerExpandableListActivity.this);
				mFeedProvider.openToRead();
				channelList.clear();
				channelList.addAll(mFeedProvider.getAllChannelsByDate());
				mFeedProvider.close();
				// Bug: {@code RssAsyncTask} will not refresh the latest update
				// time of each channel.
				mAdapter.notifyDataSetChanged();
				mDrawerAdapter.notifyDataSetChanged();
				break;
			case Constants.STATE_ACTION_UPDATED:
					Toast.makeText(getApplicationContext(), "Synchronization completed", Toast.LENGTH_SHORT).show();
					itemList.clear();
					articleList.clear();
					feedmap.clear();

						/*
						 * Load @articleList from local database
						 */
						dba = new DbAdapter(DrawerExpandableListActivity.this);
						
						dba.openToRead();
						if (channelList!=null)
						for (RssChannel r : channelList) {
							RSSFeed feed = new RSSFeed();// Put all feeds in a hashmap
															// for quick access
							feed.setRssChannel(r);
							feed.setItemList(dba.getFeedListing(r.getTitle()));
							feedmap.put(r.getTitle(), feed);
						}

						itemList = dba.getAllArticlesByDate();
						dba.close();

						String condition =null;
						try{
							currentChannel=channelList.get(currentChannelPosition);
							condition = currentChannel.getTitle();
						}catch(Exception e){
							if (Constants.LOGD)
								Log.e("Try adding feed", "IndexOutOfBound");
						}
						try {
							articleList.addAll(getArticleList(condition,
									unreadonly, star, false, feedmap, itemList));
						} catch (Exception e) {
							if (Constants.LOGD)
								Log.e("Try adding feed", "Empty List");
						}
						mAdapter.notifyDataSetChanged();

				break;
			case Constants.STATE_ACTION_NON_UPDATE:
				if (Constants.LOGD) 
					Log.d(CLASS_TAG, "State: No update synchronized");
				Toast.makeText(getApplicationContext(), "Latest update synchronized",Toast.LENGTH_SHORT).show();
				break;
			case Constants.FEED_ACTION_ABORTED:
				if (Constants.LOGD) 
					Log.d(CLASS_TAG, "State: User aborted)");
				break;
			case Constants.FEED_ACTION_STARTED:
				if (Constants.LOGD) {

					Log.d(CLASS_TAG, "State: User starts to input feed)");
				}
				break;
			case Constants.FEED_ACTION_SETTING_FILTER:
				if (Constants.LOGD) {

					Log.d(CLASS_TAG,
							"State: User starts to set the filter.Ex: Phase or Type)");
				}
				String url = intent.getStringExtra(Constants.EXTENDED_FEED_URL);
				String tag = intent.getStringExtra(Constants.EXTENDED_FEED_TAG);
				RssDialogManager manager = new RssDialogManager(
						DrawerExpandableListActivity.this);
				manager.showConditionFilterDialog(DrawerExpandableListActivity.this,
						url, tag);
				break;
			case Constants.FEED_ACTION_SETTING_TARGET:
				if (Constants.LOGD) {

					Log.d(CLASS_TAG,
							"State: User starts to set the target.Ex:Intervention=Rituxmab)");
				}
				break;
			case Constants.FEED_ACTION_DONE:
				if (Constants.LOGD) {

					Log.d(CLASS_TAG, "State: finished)");
				}
				String url2 = intent
						.getStringExtra(Constants.EXTENDED_FEED_URL);
				String tag2 = intent
						.getStringExtra(Constants.EXTENDED_FEED_TAG);
				mDrawerLayout.closeDrawer(mDrawer);

				rssService = new RssDownloadingTask(DrawerExpandableListActivity.this);
				FeedMessage message = new FeedMessage();
				message.setUrl(url2);
				message.setNewChannel(true);
				message.setNewTrials(tag2);
				rssService.execute(message);// Broadcast receiver will refresh
											// the
				// content
				mAdapter.notifyDataSetChanged();
				mDrawerAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	}


	@Override
	public void OnGroupClick(int id, String guid, String type) {
		if (type.equalsIgnoreCase("read")) {
			Log.e("CHANGE", "Changing to read: ");
			// mark article as read in the database

			dba.openToWrite();
			dba.markAsRead(guid);
			dba.close();
			// Change the View
			mAdapter.notifyDataSetChanged();
		} else if (type.equalsIgnoreCase("unread")) {
			Log.e("CHANGE", "Changing to read: ");
			// mark article as read in the database

			dba.openToWrite();
			dba.markAsUnread(guid);
			dba.close();
			// Change the View
			mAdapter.notifyDataSetChanged();
		} else if (type.equalsIgnoreCase("star")) {
			if (Constants.LOGD)
			Log.e("CHANGE", "Label STAR ");
			// mark article as read in the database
			
			dba.openToWrite();
			dba.markStar(guid);
			dba.close();
			// Change the View
			mAdapter.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), "This item is highlighted", Toast.LENGTH_SHORT).show();
		} else if (type.equalsIgnoreCase("unstar")) {
			Log.e("CHANGE", "Cancel STAR ");
			// mark article as read in the database

			dba.openToWrite();
			dba.deMarkStar(guid);
			dba.close();
			// Change the View
			mAdapter.notifyDataSetChanged();
		} else if (type.equalsIgnoreCase("link")) {
			Log.e("CHANGE", "Jump to URI ");
			// Use browser to link out the external resource on website
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(guid));
			startActivity(i);
		} else if (type.equalsIgnoreCase("share")) {
			Log.e("CHANGE", "share link ");
			ShareCompat.IntentBuilder.from(this).setType("text/html")
					.setChooserTitle("Share the link By...")
					.setText(guid.toString()).startChooser();
		} else if (type.equalsIgnoreCase("copy")) {
			Log.e("CHANGE", "copy content");
			ShareCompat.IntentBuilder.from(this).setType("text/html")
					.setChooserTitle("Copy content By...")
					.setText(guid.toString()).startChooser();
		}

	}

}
