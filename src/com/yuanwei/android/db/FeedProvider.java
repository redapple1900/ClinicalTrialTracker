package com.yuanwei.android.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import com.yuanwei.android.rss.domain.RssChannel;

public class FeedProvider{
	
	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_TITLE = "title";
	public static final String KEY_PUBDATE = "date";
	public static final String KEY_UPDATE = "lastupdate";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_URI = "uri";
	public static final String KEY_TAG = "tag";
	public static final String KEY_READ = "read";
	public static final String KEY_TOTAL = "total";    
	
	private static final String DATABASE_NAME = "ClinicalTrialsChannel";
	private static final String DATABASE_TABLE = "ClinicalTrialsChannelList";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE_LIST_TABLE = "create table " + DATABASE_TABLE + " (" + 
																KEY_ROWID +" integer primary key autoincrement, "+ 
																KEY_TITLE + " text not null, " +
																KEY_PUBDATE + " text not null, " +
																KEY_UPDATE + " text not null, " +
																KEY_DESCRIPTION + " text not null, " +
																KEY_URI + " text not null, " +
																KEY_TAG + " text, " +
																KEY_READ + " integer not null, " + 
																KEY_TOTAL + " integer not null);";


	private SQLiteHelper sqLiteHelper;
	private SQLiteDatabase sqLiteDatabase;
	private Context context;

	public FeedProvider(Context c){
		context = c;
	}

	public FeedProvider openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this; 
	}

	public synchronized FeedProvider openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getWritableDatabase();
		return this; 
	}

	public void close(){
		sqLiteHelper.close();
	}

	public class SQLiteHelper extends SQLiteOpenHelper {
		public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_LIST_TABLE);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE );
            onCreate(db);
		}
	}

    public long insertFeed(String title,String pubDate,String description,Uri uri,String tag,String lastupdate, int total) {
    	
        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_PUBDATE, pubDate);
        initialValues.put(KEY_DESCRIPTION, description);
        initialValues.put(KEY_TAG, tag);
        initialValues.put(KEY_URI, uri.toString());
        initialValues.put(KEY_UPDATE,lastupdate);
        //TODO with read & total
        initialValues.put(KEY_READ, 0);
        initialValues.put(KEY_TOTAL, total);

        return sqLiteDatabase.insert(DATABASE_TABLE, null, initialValues);
    }
    
    public RssChannel getFeed(String url) throws SQLException {
        Cursor mCursor =
        		sqLiteDatabase.query(true, DATABASE_TABLE, new String[] {
                		KEY_ROWID,
                		KEY_TITLE,
                		KEY_PUBDATE,
                		KEY_UPDATE,
                		KEY_DESCRIPTION,
                        KEY_URI,
                        KEY_TAG,
                		KEY_READ,
                		KEY_TOTAL
                		}, 
                		KEY_URI + "= '" + url+ "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null && mCursor.getCount() > 0) {
        	mCursor.moveToFirst();
        	RssChannel a = cursorToRssChannel(mCursor);
   			return a;
        }
        return null;
    }
    public RssChannel getFeed(long id) throws SQLException {
        Cursor mCursor =
        		sqLiteDatabase.query(true, DATABASE_TABLE, new String[] {
                		KEY_ROWID,
                		KEY_TITLE,
                		KEY_PUBDATE,
                		KEY_UPDATE,
                		KEY_DESCRIPTION,
                        KEY_URI,
                        KEY_TAG,
                		KEY_READ,
                		KEY_TOTAL
                		}, 
                		KEY_ROWID + "= '" + id+ "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null && mCursor.getCount() > 0) {
        	mCursor.moveToFirst();
        	RssChannel a = cursorToRssChannel(mCursor);
   			return a;
        }
        return null;
    }
    public List<RssChannel> getAllChannelsByDate() {
        List<RssChannel> RssChannels = new ArrayList<RssChannel>();

        Cursor mCursor = sqLiteDatabase.query(DATABASE_TABLE,
        		 new String[] {
        		KEY_ROWID,
        		KEY_TITLE,
        		KEY_PUBDATE,
        		KEY_UPDATE,
        		KEY_DESCRIPTION,
                KEY_URI,
                KEY_TAG,
        		KEY_READ,
        		KEY_TOTAL
        		}, null, null, null, null,KEY_PUBDATE+" desc");

        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
          RssChannel Rsschannel = cursorToRssChannel(mCursor);
          RssChannels.add(Rsschannel);
          mCursor.moveToNext();
        }
        // make sure to close the mCursor
        mCursor.close();
        return RssChannels;
      }
    public boolean setLastUpdate(String title,String date){
    	ContentValues cv =new ContentValues();
    	cv.put(KEY_UPDATE, date);
    	return sqLiteDatabase.update(DATABASE_TABLE, cv, KEY_TITLE+"='"+title+"'", null)>0;
    }
    public boolean setTotalArticles(String title,int num){
    	ContentValues cv =new ContentValues();
    	cv.put(KEY_TOTAL, num);
    	return sqLiteDatabase.update(DATABASE_TABLE, cv, KEY_TITLE+"='"+title+"'", null)>0;
    }
    public boolean deleteChannel(String title){
    	
    	return sqLiteDatabase.delete(DATABASE_TABLE, KEY_TITLE+"='"+title+"'", null)>0;
    }
    private RssChannel cursorToRssChannel(Cursor mCursor) {
    	RssChannel a = new RssChannel();
    		a.setDbId(mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)));
			a.setTitle(mCursor.getString(mCursor.getColumnIndex(KEY_TITLE)));
			a.setPubDate(mCursor.getString(mCursor.getColumnIndex(KEY_PUBDATE)));
			a.setDescription(mCursor.getString(mCursor.getColumnIndex(KEY_DESCRIPTION)));
			a.setUrl(Uri.parse(mCursor.getString(mCursor.getColumnIndex(KEY_URI))));
			a.setTag(mCursor.getString(mCursor.getColumnIndex(KEY_TAG)));		
			a.setUpdateDate(mCursor.getString(mCursor.getColumnIndex(KEY_UPDATE)));
			a.setTotal(mCursor.getInt(mCursor.getColumnIndex(KEY_TOTAL)));
        return a;
      }




}
