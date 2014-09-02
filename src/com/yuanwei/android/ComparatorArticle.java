package com.yuanwei.android;

import java.util.Comparator;

import com.yuanwei.android.rss.domain.Article;

public class ComparatorArticle implements Comparator<Article>{

	@Override
	public int compare(Article arg0, Article arg1) {
		Article a0=arg0;
		Article a1=arg1;
		
		int flag =a0.getUpdateDate().compareTo(a1.getUpdateDate());
		if (flag==0){
			return a0.getPubDate().compareTo(a1.getPubDate());
		}else {
			return flag;
		}
		
	}

}
