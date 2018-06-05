package com.example.panea.newsfeed;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news articles data from USGS.
 */
public final class QueryUtils {

    // Tag for log messages
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the USGS dataset and return an {@link List< NewsArticle >} object to represent a list
     * of news articles
     */
    public static List<NewsArticle> fetchNewsData(String requestUrl){

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive an JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e){
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        // Extract relevant fields from the JSON response and reate an {@link NewsArticle} object
        List<NewsArticle> newsArticles = extractNewsArticles(jsonResponse);

        // Return the {@link NewsArticle}
        return newsArticles;
    }

    /**
     * Make a HTTP request to the given URL and return a String as the response
     */
    private static String makeHttpRequest(URL url) throws IOException{
        String jsonResponse = "";

        // If the URL is null the return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            }
        } catch (IOException e){
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results");
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (inputStream != null){
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null){
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();

    }


    /**
     * Returns new URL object from the given string URL
     */
    private static URL createUrl (String stringUrl){
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e){
            Log.e(LOG_TAG, "Error with creating URL", e);
        }
        return url;
    }

    /**
     * Return a list of {@link NewsArticle} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<NewsArticle> extractNewsArticles(String jsonResponse) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        // Create an empty ArrayList that we can start adding newsArticles to
        List<NewsArticle> newsArticles = new ArrayList<>();

        // Try to parse the jsonResponse. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Parse the jsonResponse string and
            // build up a list of NewsArticle objects with the corresponding data.
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONObject newsObj = baseJsonResponse.getJSONObject("response");
            JSONArray newsArray = newsObj.getJSONArray("results");

            for (int i=0; i < newsArray.length(); i++){
                JSONObject currentNewsStory = newsArray.getJSONObject(i);
                String newsTitle = currentNewsStory.getString("webTitle");
                String newsDate = currentNewsStory.getString("webPublicationDate");
                String url = currentNewsStory.getString("webUrl");
                // Create an array to access the author's name
                JSONArray tagsArray = currentNewsStory.getJSONArray("tags");

                String authorName;
                // Check if the author's name is present
                if (!(tagsArray.length() == 0)) {
                    JSONObject tagsObj = tagsArray.getJSONObject(0);
                    authorName = tagsObj.getString("webTitle");
                } else {
                    authorName = "";
                }
                // Create a new NewsArticle object with the new values
                // and add it to the List of articles
                NewsArticle newsArticle = new NewsArticle(authorName, newsTitle, newsDate, url);
                newsArticles.add(newsArticle);

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of newsArticles
        return (ArrayList<NewsArticle>) newsArticles;
    }

}
