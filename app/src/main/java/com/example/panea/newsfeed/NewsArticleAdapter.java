package com.example.panea.newsfeed;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class NewsArticleAdapter extends ArrayAdapter<NewsArticle> {
    public NewsArticleAdapter(Activity context, List<NewsArticle> newsArticles) {
        super(context, 0, newsArticles);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        NewsArticle currentNewsArticle = getItem(position);

        // Set the author name to the TextView
        TextView newsAuthorTextView = (TextView) convertView.findViewById(R.id.author);
        // If there is no author then hide the text view
        if ("".equals(currentNewsArticle.getAuthorName())){
            newsAuthorTextView.setVisibility(View.GONE);
        } else {
            newsAuthorTextView.setText("by " + currentNewsArticle.getAuthorName());
        }
        // Set the news title to the TextView
        TextView newsTitleTextView = (TextView) convertView.findViewById(R.id.news_tite);
        newsTitleTextView.setText(currentNewsArticle.getNewsTitle());


        TextView newsDateTextView = (TextView) convertView.findViewById(R.id.date_published);

        // Save the date into a string variable
        String newsDate = currentNewsArticle.getNewsDate();

        // Check if the date String is empty
        if ( !(TextUtils.isEmpty(newsDate)) ) {
            // Split the date into two parts to use only the date without the time
            String[] split = newsDate.split("T");
            // Set the date to the TextView
            newsDateTextView.setText(split[0]);

        } else {
            // If the date string is empty hide the view
            newsDateTextView.setVisibility(View.GONE);
        }

        return convertView;


    }
}
