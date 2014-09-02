package com.yuanwei.android.rss.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Vector;

public class RSSFeed {
	private int itemcount = 0;// 计算列表数目
	private List<Article> itemList;// 声明一个RSSItem类型的集合对象itemList，用于描述item列表
	private RssChannel rssChannel;
	
	public RSSFeed() {
		//itemList = new Vector<Article>(0);// 构造函数初始化 itemList
		 itemList=new ArrayList<Article>();
		 rssChannel=new RssChannel();
	}
	//private Map<String,String> mMap =new HashMap<String,String>();


	/**
	 * 该方法负责讲一个RSSItem加入到RSSFeed类里
	 * 
	 * @param item
	 * @return
	 */
	public int addItem(Article article) {
		
		itemList.add(article);
		itemcount++;
		System.out.println("Total"+itemcount);
		return itemcount;
		
	}

	/*public int addChannel(RSSChannel rssChannel)
	{
		rssChannels.add(rssChannel);
		itemcount++;
		return itemcount;
	}	
*/	

/**
	 * 该方法是用来在activity中根据索引获取点击的item的描述的，在activity的跳转中会用到
	 * 
	 * @param location
	 * @return
	 */
	public Article getItem(int location) {
		return itemList.get(location);
	}
	public RssChannel getChannel(){
		return this.rssChannel;
	}
	/**
	 * 生成ListView中需要的列表数据
	 * @return
	 */
	public List<Map<String, Article>> getAllItemsForListView()
	{
	
		List<Map<String,Article>> dataList=new ArrayList<Map<String,Article>>();
		for (int i = 0; i < itemList.size(); i++) {
			Map<String,Article> item=new HashMap<String, Article>();
			item.put("item", itemList.get(i));
			dataList.add(item);
		}
		return dataList;
	}
	public int getItemcount() {
		return itemcount;
	}

	public void setItemcount(int itemcount) {
		this.itemcount = itemcount;
	}

	public List<Article> getItemList() {
		return itemList;
	}

	public void setItemList(List<Article> itemList) {
		this.itemList = itemList;
	}
	public void setRssChannel(RssChannel rssChannel){
		this.rssChannel=rssChannel;
	}
	
}

