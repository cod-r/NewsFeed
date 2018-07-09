package com.example.panea.newsfeed;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<NewsArticle>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LOG_TAG = MainActivity.class.getName();
    /**
     * URL for news articles data from the Guardian dataset
     */
    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search?";
    /**
     * Constant value for the news article loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWSARTICLE_LOADER_ID = 1;
    /**
     * Hold the state of the internet connection (true or false)
     */
    boolean isConnected;
    /**
     * Adapter for the list of news articles
     */
    private NewsArticleAdapter mAdapter;
    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;
    /**
     * ProgressBar that is displayed while the content is loading
     */
    private ProgressBar progressBar;

    /**
     * TextView that shows the selected news category
     */
    private TextView newsCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        // Find a reference to the {@link ListView} in the layout
        ListView newsArticleListView = (ListView) findViewById(R.id.list);

        newsArticleListView.setEmptyView(mEmptyStateTextView);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news article.
        newsArticleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current newsArticle that was clicked on
                NewsArticle newsArticle = mAdapter.getItem(position);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(newsArticle.getUrl())));
            }
        });

        // Create a new {@link ArrayAdapter} of news articles
        mAdapter = new NewsArticleAdapter(
                MainActivity.this, new ArrayList<NewsArticle>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsArticleListView.setAdapter(mAdapter);

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        // If there is a network connection, fetch data
        if (isConnected) {
            loaderManager.initLoader(NEWSARTICLE_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            progressBar = findViewById(R.id.loading_indicator);
            progressBar.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);

        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_category_key)) ||
                key.equals(getString(R.string.settings_select_page_key))) {
            // Clear the ListView as a new query will be kicked off
            mAdapter.clear();

            // Hide the empty state text view as the loading indicator will be displayed
            mEmptyStateTextView.setVisibility(View.GONE);

            // Show the loading indicator while new data is being fetched
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Restart the loader to requery the USGS as the query settings have been updated
            getLoaderManager().restartLoader(NEWSARTICLE_LOADER_ID, null, this);
        }
    }


    @Override
    public Loader<List<NewsArticle>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String page = sharedPrefs.getString(
                getString(R.string.settings_select_page_key),
                getString(R.string.settings_select_page_default));
        String category = sharedPrefs.getString(
                getString(R.string.settings_category_key),
                getString(R.string.settings_category_default));

        // Set the TextView to represent the current news category
        newsCategory = (TextView) findViewById(R.id.category_text_view) ;
        if (category.equals("politics")){
            newsCategory.setText(R.string.politics_section);
        } else if (category.equals("technology")){
            newsCategory.setText(R.string.technology_section);
        }

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value.
        uriBuilder.appendQueryParameter("section", category);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("page", page);
        uriBuilder.appendQueryParameter("page-size", "10");
        uriBuilder.appendQueryParameter("api-key", "a08f7bd9-71c3-4e8e-b590-fdb2829d80bd");

        // Return the completed uri
        return new NewsArticleLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(Loader<List<NewsArticle>> loader, List<NewsArticle> newsArticles) {
        // Clear the adapter of previous data
        mAdapter.clear();

        // If there is a valid list of {@link NewsArticle}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (newsArticles != null && !newsArticles.isEmpty()) {
            mAdapter.addAll(newsArticles);
        }

        // Set empty state text to display "No newsArticles found."
        mEmptyStateTextView.setText(R.string.no_news);

        // Hide the ProgressBar after the loading is finished
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<NewsArticle>> loader) {
        // Clear the adapter of previous data
        mAdapter.clear();
    }

    @Override
    // This method initialize the contents of the Activity's options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    // This method is called whenever an item in the options menu is selected.
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
