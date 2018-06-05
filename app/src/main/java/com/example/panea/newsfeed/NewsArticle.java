package com.example.panea.newsfeed;


public class NewsArticle {
    private String authorName;
    private String newsTitle;
    private String url;
    private String newsDate;

    NewsArticle(String authorName, String newsTitle, String newsDate, String url){
        this.authorName = authorName;
        this.newsTitle = newsTitle;
        this.url = url;
        this.newsDate = newsDate;
    }

    public String getAuthorName(){
        return authorName;
    }


    public String getNewsTitle(){
        return newsTitle;
    }

    public String getNewsDate (){
        return newsDate;
    }

    public String getUrl (){
        return url;
    }

}
