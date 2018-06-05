package com.example.panea.newsfeed;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

public class NewsArticleLoader extends AsyncTaskLoader<List<NewsArticle>> {
    private String url;

    /**
     * Constructs a new {@link NewsArticleLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public NewsArticleLoader(Context context, String url){
        super(context);
        this.url = url;

    }
    /**
     * This is on a background thread.
     */
    @Override
    public List<NewsArticle> loadInBackground() {
        if (url == null) {
            return null;
        }
        // Perform the HTTP request for news article data and process the response
        List<NewsArticle> newsArticles = QueryUtils.fetchNewsData(url);
        return newsArticles;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
