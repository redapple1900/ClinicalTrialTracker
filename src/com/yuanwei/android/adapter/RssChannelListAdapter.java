package com.yuanwei.android.adapter;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.redapple.android.R;
import com.yuanwei.android.Constants;
import com.yuanwei.android.rss.domain.RssChannel;


public class RssChannelListAdapter extends ArrayAdapter<RssChannel> {

	public RssChannelListAdapter(Activity activity, List<RssChannel> RssChannels) {
		super(activity, 0, RssChannels);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		View rowView = inflater.inflate(R.layout.fragment_article_list, null);
		RssChannel RssChannel = getItem(position);
		

		TextView textView = (TextView) rowView.findViewById(R.id.article_title_text);
		textView.setText(RssChannel.getShortTitle());
		//textView.setText(RssChannel.getTag());
		//textView.setText(RssChannel.getTitle());
		
		TextView dateView = (TextView) rowView.findViewById(R.id.article_listing_smallprint);
		TextView tag = (TextView) rowView.findViewById(R.id.article_listing_tag);
		TextView total = (TextView) rowView.findViewById(R.id.article_listing_total);
		//String pubDate = RssChannel.getPubDate();
		//The following code does nothing but convert the date, which is totally unnecessary. 
		
	
		String format ="yyyy-MM-dd";
		SimpleDateFormat df = new SimpleDateFormat(Constants.STORED_DATE_FORMAT, Locale.US);

		SimpleDateFormat newdf = new SimpleDateFormat(format,Locale.US);
		Date pDate;
		String pubDate;
		
		try {
			pDate = df.parse(RssChannel.getUpdateDate());
			pubDate = "Last update:" + newdf.format(pDate) ;
		} catch (ParseException e) {
			if (Constants.LOGD)
			Log.e("DATE PARSING", "Error parsing date..");
			pubDate= "Last update:"+RssChannel.getUpdateDate();
		}		
		dateView.setText(pubDate);
		tag.setText(RssChannel.getTag());
		try{
			total.setText("Total£º"+RssChannel.getTotal());
		}catch(Exception e){
			total.setText("0");
		}
		

		

		return rowView;

	} 
}