

package com.yuanwei.android;

import java.util.Locale;

/**
 *
 * Constants used by multiple classes in this package
 */
public final class Constants {

    // Set to true to turn on verbose logging
    public static final boolean LOGV = false;
    
    // Set to true to turn on debug logging
    public static final boolean LOGD = false;

    // Custom actions
    
    public static final String ACTION_VIEW_IMAGE =
            "com.redapple.android.ACTION_VIEW_IMAGE";

    public static final String ACTION_ZOOM_IMAGE =
            "com.redapple.android.ACTION_ZOOM_IMAGE";
    
    // Defines a custom Intent action
    public static final String BROADCAST_ACTION = "com.redapple.android.BROADCAST";
    

    // Fragment tags
    public static final String PHOTO_FRAGMENT_TAG =
            "com.redapple.android.PHOTO_FRAGMENT_TAG";
    
    public static final String THUMBNAIL_FRAGMENT_TAG =
            "com.redapple.android.THUMBNAIL_FRAGMENT_TAG";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = "com.redapple.android.STATUS";

    // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = "com.redapple.android.LOG";
    
    public static final String EXTENDED_FEED_URL = "com.redapple.android.FEED";
    
    public static final String STORED_DATE_FORMAT = "yyyy-MM-dd,HH:mm:ss";
    
    public static final String RSS_DATE_FORMAT = "EEE, dd MMM yyyy kk:mm:ss ZZZ";
    
    // Defines the key for storing fullscreen state
    public static final String EXTENDED_FULLSCREEN =
            "com.redapple.android.EXTENDED_FULLSCREEN";

    /*
     * A user-agent string that's sent to the HTTP site. It includes information about the device
     * and the build that the device is running.
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android "
            + android.os.Build.VERSION.RELEASE + ";"
            + Locale.getDefault().toString() + "; " + android.os.Build.DEVICE
            + "/" + android.os.Build.ID + ")";

    // Status values to broadcast to the Activity

    // The download is starting
    public static final int STATE_ACTION_STARTED = 0;
    
    // The background thread is connecting to the RSS feed
    public static final int STATE_ACTION_CONNECTING = 1;

    // The background thread is parsing the RSS feed
    public static final int STATE_ACTION_PARSING = 2;

    // The background thread is writing data to the content provider
    public static final int STATE_ACTION_WRITING = 3;

    // The background thread is done
    public static final int STATE_ACTION_COMPLETE = 4;
   
    //The download will not start since the server is not reachable;
    public static final int STATE_ACTION_FAILED = 5;
    
    public static final int STATE_ACTION_UPDATED =6;
    
    public static final int STATE_ACTION_NON_UPDATE=7;

    // The background thread is doing logging
    public static final int STATE_LOG = -1;
    
    
    // The download is starting
    public static final int FEED_ACTION_ABORTED = 10;
    
    // The background thread is connecting to the RSS feed
    public static final int FEED_ACTION_STARTED = 11;

    // The background thread is parsing the RSS feed
    public static final int FEED_ACTION_SETTING_TARGET = 12;

    // The background thread is writing data to the content provider
    public static final int FEED_ACTION_DONE = 13;
    
    public static final int FEED_ACTION_SETTING_FILTER = 14;

    public static final CharSequence BLANK = " ";

	public static final String EXTENDED_FEED_TAG = "com.redapple.android.TAG";
	public static final String TAG_TO_BE_REMOVED = "a";

	public static final int MAXIMAM_DAYS_ALLOWED = 14;
	
	public static final String SHARED_PREFERENCES="com.redapple.android.sharedpreferences";

	protected static final String UPDATE_MODE = "update mode";

	protected static final String UPDATE_HOUR = "hour";

	protected static final String UPDATE_MINUTE = "minute";

	protected static final int ALARM_START = 20;
	
	protected static final int ALARM_CANCEL=21;

	
}
