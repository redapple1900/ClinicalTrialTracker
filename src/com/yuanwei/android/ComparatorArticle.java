package com.yuanwei.android;

import java.util.Comparator;

import com.yuanwei.android.rss.domain.Article;

public class ComparatorArticle implements Comparator<Article>{

	@Override
	public int compare(Article a0, Article a1) {
		
		int flag =a1.getUpdateDate().compareTo(a0.getUpdateDate());
		
		if (flag==0) flag = a1.getPubDate().compareTo(a0.getPubDate());

		return flag;
		
		
	}

}
