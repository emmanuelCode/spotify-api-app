package com.example.android.spotifytestapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;



/**
 * Loader Class for background tasks
 */

public class MusicListItemLoader extends AsyncTaskLoader<List<MusicListItem>> {

    //log tag for debugging
    private static final String LOG_TAG = MusicListItemLoader.class.getSimpleName();

    //pass in the accessToken
    private String mAccessToken;

    public MusicListItemLoader(Context context, String accessToken) {
        super(context);

        //pass in the accessToken
        mAccessToken = accessToken;

    }

    /**
     * This method trigger automatically when init the loader in the PlayListActivity
     */
    @Override
    protected void onStartLoading() {
        forceLoad();//trigger the loader to start doing the background work
    }


    /**
     * load the list in the background thread
     *
     * @return the list of music items
     */
    @Override
    public List<MusicListItem> loadInBackground() {
        if (mAccessToken == null) {
            return null;
        }

        //slow down the background Thread
//        //To simulate a slow connection and test the loading indicator
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        return QueryUtils.fetchUserSavedTracks(mAccessToken);
    }
}
