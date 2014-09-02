package com.yuanwei.android.adapter;



	


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.redapple.android.R;
import com.yuanwei.android.Constants;
import com.yuanwei.android.rss.domain.Article;
import com.yuanwei.android.util.DateConverter;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.graphics.PorterDuff;

public class ArticleExpandableListAdapter  extends BaseExpandableListAdapter {
 OnGroupClickListener mCallBack;	
 public ArrayList<Article> groupItem;//tempChild;
 public ArrayList<Article> Childtem = new ArrayList<Article>();
 public Article tempChild;
 public LayoutInflater minflater;
 public Context context;
// private int expandedArticle;


public ArticleExpandableListAdapter(Context context,ArrayList<Article> grList, ArrayList<Article> childItem) {
	
	  groupItem = grList;
	  this.Childtem = childItem;
	  this.context=context;
	  //expandedArticle=-1;
	 
	}

 public void setInflater(LayoutInflater mInflater, Context con) {
  this.minflater = mInflater;
  context = con;
 }
 public void getGroupData(ArrayList<Article> grList){
	 this.groupItem=grList;
 }
 public void getChildData(ArrayList<Article> childItem){
	 this.Childtem=childItem;
 }
 public final class GroupViewHolder{
	 public TextView title;
	// public TextView pubdate;
	 public TextView update;
	 public TextView arthur;
	 public CheckBox star;
	 public View read;
 }
 public interface OnGroupClickListener {
	 public void OnGroupClick(int id,String guid,String type);
 }
 public interface OnChildClickListener {
	 public void OnChildClick(int id,String link,String type);
 }

 @Override
 public Article getChild(int groupPosition, int childPosition) {
  return Childtem.get(groupPosition);
 }

 @Override
 public long getChildId(int groupPosition, int childPosition) {
  return 0;
 }

 @Override
 public View getChildView(int groupPosition, final int childPosition,
   boolean isLastChild, View convertView, ViewGroup parent) {
  tempChild = Childtem.get(groupPosition);
  TextView text = null;

  View share_layout;
  View copy_layout;
  View link_layout;
  LayoutInflater inflater = ((Activity) context).getLayoutInflater();
  if (convertView == null) {
   convertView = inflater.inflate(R.layout.child_view, null);
  }
  
  text = (TextView) convertView.findViewById(R.id.article_detail);

  share_layout=convertView.findViewById(R.id.layout_share);
  copy_layout=convertView.findViewById(R.id.layout_copy);
  link_layout=convertView.findViewById(R.id.layout_link);

  share_layout.setOnTouchListener(mOnTouchListener);
  link_layout.setOnTouchListener(mOnTouchListener);
  copy_layout.setOnTouchListener(mOnTouchListener);

  link_layout.setOnClickListener(mLinkOnClickListener);
  copy_layout.setOnClickListener(mCopyOnClickListener);
  share_layout.setOnClickListener(mShareOnClickListener);
  text.setText(Html.fromHtml(tempChild.getDescription()));
  
  /*
  convertView.setOnClickListener(new OnClickListener() {
   @Override
   public void onClick(View v) {
    Toast.makeText(context, tempChild.getDescription(),
      Toast.LENGTH_SHORT).show();
   }
  });
  */
  return convertView;
 }

 @Override
 public int getChildrenCount(int groupPosition) {
 //return  Childtem.get(groupPosition).size();
	 return 1;
 }

 @Override
 public Article getGroup(int groupPosition) {
	 if (groupItem!=null){
		 return groupItem.get(groupPosition);
		}else return null;
 }

 @Override
 public int getGroupCount() {
	if (groupItem!=null){
	 return groupItem.size();	
	}else return 0;
 
 }

 @Override
 public void onGroupCollapsed(int groupPosition) {
  super.onGroupCollapsed(groupPosition);
   
 }

 @Override
 public void onGroupExpanded(int groupPosition) {
  super.onGroupExpanded(groupPosition);
	 
  if (groupItem.get(groupPosition).isRead()==false){
	  //Mark the item read
	  if (Constants.LOGD)
	  Log.e(groupItem.get(groupPosition).getGuid(), "check");
	  groupItem.get(groupPosition).setRead(true);
	  
	  mCallBack=(OnGroupClickListener)((Activity)context);
	  mCallBack.OnGroupClick(groupPosition,groupItem.get(groupPosition).getGuid(),"read");
	  
	  //Only one groupview  is expanded at one time. If another groupview was expanded, the current one needs to be collapsed
	  /*
	  ExpandableListView mExpandableListVew=((ExpandableListActivity)context).getExpandableListView();
	  
	  if (expandedArticle>0&&mExpandableListVew.isGroupExpanded(expandedArticle)){
		  mExpandableListVew.collapseGroup(expandedArticle);
	  }
	  expandedArticle=groupPosition;
	  */
  }
 }

 @Override
 public long getGroupId(int groupPosition) {
  return 0;
 }

 @Override
 public View getGroupView(final int groupPosition, final boolean isExpanded,
   View convertView, ViewGroup parent) {
	 GroupViewHolder holder=null;
  if (convertView == null) {
	  LayoutInflater inflater = ((Activity)context).getLayoutInflater();
   convertView = inflater.inflate(R.layout.group_view, null);
   holder=new GroupViewHolder();
   holder.title=(TextView)convertView.findViewById(R.id.article_title_text);
   //holder.pubdate=(TextView)convertView.findViewById(R.id.article_listing_smallprint);

   holder.update=(TextView)convertView.findViewById(R.id.article_listing_update);
   holder.arthur=(TextView)convertView.findViewById(R.id.article_listing_arthur);
   holder.read=convertView.findViewById(R.id.article_readmarker);
   holder.star=(CheckBox)convertView.findViewById(R.id.btn_star);
   holder.star.setTag(groupPosition);
   convertView.setTag(holder);
  }else{
	  holder=(GroupViewHolder) convertView.getTag();
  }
  holder.title.setText(groupItem.get(groupPosition).getTitle());
  //holder.pubdate.setText("Published by:"+groupItem.get(groupPosition).getPubDate());
	
	SimpleDateFormat newdf = new SimpleDateFormat(Constants.STORED_DATE_FORMAT,
			Locale.US);
	Date pDate;
	try {
		pDate = newdf.parse(groupItem.get(groupPosition).getUpdateDate());
		holder.update.setText("Last update:"+DateConverter.getDateDifference(pDate));
	} catch (ParseException e) {
		Log.e("DATE PARSING", "Error parsing date..");
		holder.update.setText("Last update:Unknown");
	}
  //holder.update.setText("Last update:"+groupItem.get(groupPosition).getUpdateDate());
  holder.arthur.setText(groupItem.get(groupPosition).getTag());
  /*
   * The Android API provides the OnCheckedChangeListener interface
   * and its onCheckedChanged(CompoundButton buttonView, boolean
   * isChecked) method. Unfortunately, this implementation suffers
   * from a big problem: you can't determine whether the checking
   * state changed from code or because of a user action. As a result
   * the only way we have is to prevent the CheckBox from callbacking
   * our listener by temporary removing the listener.
   */
  holder.star.setOnCheckedChangeListener(null);
  holder.star.setChecked(groupItem.get(groupPosition).isStar());
  holder.star.setOnCheckedChangeListener(mStarCheckedChanceChangeListener);
  
  
  if (groupItem.get(groupPosition).isRead()){
	  holder.read.setVisibility(View.INVISIBLE);
	  
  }else {
	  holder.read.setVisibility(View.VISIBLE);
  }

  
	LinearLayout row = (LinearLayout) convertView.findViewById(R.id.article_group_row_layout);
	row.setBackgroundColor(Color.WHITE);
	holder.title.setTypeface(Typeface.DEFAULT_BOLD);
  /*
  if (groupItem.get(groupPosition).isRead()==false){
		LinearLayout row = (LinearLayout) convertView.findViewById(R.id.article_group_row_layout);
		row.setBackgroundColor(Color.WHITE);
		holder.title.setTypeface(Typeface.DEFAULT_BOLD);
	}else{
		LinearLayout row = (LinearLayout) convertView.findViewById(R.id.article_group_row_layout);
		row.setBackgroundColor(Color.parseColor("#E6E6E6"));
		holder.title.setTypeface(Typeface.DEFAULT);
	}
	*/
  return convertView;
 }

 @Override
 public boolean hasStableIds() {
  return false;
 }

 @Override
 public boolean isChildSelectable(int groupPosition, int childPosition) {
  return false;
 }
 private OnTouchListener mOnTouchListener = new OnTouchListener(){


        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView) v.findViewById(R.id.image);
                    //overlay is black with transparency of 0x77 (119)
                    view.getDrawable().setColorFilter(0x99000000,PorterDuff.Mode.SRC_ATOP);
                    Log.d("check"," "+view.getId());
                    view.invalidate();
                    TextView text =(TextView)v.findViewById(R.id.text);
                    text.setBackgroundColor(Color.LTGRAY);
                    text.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    ImageView view = (ImageView) v.findViewById(R.id.image);
                    //clear the overlay
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    TextView text =(TextView)v.findViewById(R.id.text);
                    text.setBackgroundColor(Color.TRANSPARENT);
                    text.invalidate();
                	
                	v.performClick();
                	break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v.findViewById(R.id.image);
                    //clear the overlay
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    TextView text =(TextView)v.findViewById(R.id.text);
                    text.setBackgroundColor(Color.TRANSPARENT);
                    text.invalidate();
                    break;
                }
            }
            return true;
        }
 };
	private View.OnClickListener mLinkOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (v != null) {
				final int position = ((ExpandableListActivity) context)
						.getExpandableListView().getPositionForView(v) - 1;
				Log.d("" + position, "checkposition");
				final long packedPosition = ((ExpandableListActivity) context)
						.getExpandableListView().getExpandableListPosition(
								position);
				Log.d("" + packedPosition, "checkposition");
				((ExpandableListActivity) context).getExpandableListView();

				// final int packedPosition =
				// ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
				final int groupPosition = ExpandableListView
						.getPackedPositionGroup(packedPosition);
				if (groupPosition != AdapterView.INVALID_POSITION) {

					mCallBack = (OnGroupClickListener) ((Activity) context);
					mCallBack.OnGroupClick(groupPosition,
							groupItem.get(groupPosition).getUrl().toString(),
							"link");

				}
			}
		}
	};
	private View.OnClickListener mShareOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (v != null) {
				final int position = ((ExpandableListActivity) context)
						.getExpandableListView().getPositionForView(v) - 1;//Give the position of parent view
				Log.d("" + position, "checkposition");
				final long packedPosition = ((ExpandableListActivity) context)
						.getExpandableListView().getExpandableListPosition(
								position);
				Log.d("" + packedPosition, "checkposition");
				((ExpandableListActivity) context).getExpandableListView();

				// final int packedPosition =
				// ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
				final int groupPosition = ExpandableListView
						.getPackedPositionGroup(packedPosition);
				if (groupPosition != AdapterView.INVALID_POSITION) {

					mCallBack = (OnGroupClickListener) ((Activity) context);
					mCallBack.OnGroupClick(groupPosition,
							groupItem.get(groupPosition).getUrl().toString(),
							"Share");

				}
			}
		}
	};
	private View.OnClickListener mCopyOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (v != null) {
				final int position = ((ExpandableListActivity) context)
						.getExpandableListView().getPositionForView(v) - 1;//Give the position of parent view
				Log.d("" + position, "checkposition");
				final long packedPosition = ((ExpandableListActivity) context)
						.getExpandableListView().getExpandableListPosition(
								position);
				Log.d("" + packedPosition, "checkposition");
				((ExpandableListActivity) context).getExpandableListView();

				// final int packedPosition =
				// ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
				final int groupPosition = ExpandableListView
						.getPackedPositionGroup(packedPosition);
				if (groupPosition != AdapterView.INVALID_POSITION) {

					mCallBack = (OnGroupClickListener) ((Activity) context);
					mCallBack.OnGroupClick(groupPosition,
							groupItem.get(groupPosition).getDescription().toString(),
							"copy");

				}
			}
		}
	};
 private OnCheckedChangeListener mStarCheckedChanceChangeListener = new OnCheckedChangeListener() {
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	 if (buttonView!=null){
    		 final int position = ((ExpandableListActivity)context).getExpandableListView().getPositionForView(buttonView);
    		 Log.d(""+position,"checkposition");
    		 final long packedPosition= ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
    		 Log.d(""+packedPosition,"checkposition");
    		//((ExpandableListActivity)context).getExpandableListView();
    		
			// final int packedPosition = ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
    		 final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
    		 
             if (groupPosition != AdapterView.INVALID_POSITION) {
                 getGroup(groupPosition).setStar(isChecked);
                 //Change the View
                 if (isChecked){
                  mCallBack=(OnGroupClickListener)((Activity)context);
               	  mCallBack.OnGroupClick(groupPosition,groupItem.get(groupPosition).getGuid(),"star");	
                 }else {
                  mCallBack=(OnGroupClickListener)((Activity)context);
               	  mCallBack.OnGroupClick(groupPosition,groupItem.get(groupPosition).getGuid(),"unstar");	
                 }        	 
             } 
             
    	 }
        
     }
 };
 /*
 private OnCheckedChangeListener mReadCheckedChanceChangeListener = new OnCheckedChangeListener() {
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	 if (buttonView!=null){
    		 final int position = ((ExpandableListActivity)context).getExpandableListView().getPositionForView(buttonView);
    		 Log.d(""+position,"checkposition");
    		 final long packedPosition= ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
    		 Log.d(""+packedPosition,"checkposition");
    		//((ExpandableListActivity)context).getExpandableListView();
    		
			// final int packedPosition = ((ExpandableListActivity)context).getExpandableListView().getExpandableListPosition(position);
    		 final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
    		 
             if (groupPosition != ExpandableListView.INVALID_POSITION) {
                 getGroup(groupPosition).setRead(isChecked);
                 //Change the View
                 if (isChecked){
                  mCallBack=(OnGroupClickListener)((Activity)context);
               	  mCallBack.OnGroupClick(groupPosition,groupItem.get(groupPosition).getGuid(),"read");	
                 }else {
                  mCallBack=(OnGroupClickListener)((Activity)context);
               	  mCallBack.OnGroupClick(groupPosition,groupItem.get(groupPosition).getGuid(),"unread");	
                 }
           	 
             } 
    	 }
        
     }
 };
 */

}