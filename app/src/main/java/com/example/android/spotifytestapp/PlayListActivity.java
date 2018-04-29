package com.example.android.spotifytestapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.TextView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * I implemented a loader in this class as AsyncTask isn't good with screenRotation as it create new instance and waste resource
 */
public class PlayListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MusicListItem>> {

    //log tag
    private static final String LOG_TAG = PlayListActivity.class.getSimpleName();

    //create an id for the loader useful when you have multiple loader
    private static final int MUSIC_LIST_ITEM_LOADER_ID = 1;

    //for playing preview song from spotify api
    private MediaPlayer mMediaPlayer;

    //for AudioFocus for when we have a call for example
    private AudioManager mAudioManager;

    //TODO review those method in the android documentation
    //this listen for audio focus changes
    private AudioManager.OnAudioFocusChangeListener mAudioManagerListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {


            //if focus lost for a short time(AUDIOFOCUS_LOSS_TRANSIENT) or we receive notification lower the volume(AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange ==
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                //pause the playback
                mMediaPlayer.pause();
                // set seeker to 0
                mMediaPlayer.seekTo(0);

                //if another app is playing sound(AUDIOFOCUS_LOSS) release the media
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {


                releaseMediaPlayer();

                //our app  gained audio focus, so we can play it music again.
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                // so here we resume playback, because our app hold the Audio Focus again!
                mMediaPlayer.start();
            }

        }
    };


    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        //release the player if once completed
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };


    //our media playblack UI Object
    private MediaPlayBack mMediaPlayBack;

    //token to be store and use later
    private String mAccessToken;

    //prepare our adapter
    private MusicListAdapter mMusicListAdapter;

    //a text view to show whether we have no music or no internet connection
    private TextView mEmptyStateTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        // set out texView for the empty state
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);

        //get the audio system service for handle media app
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        //get the intent from the last activity and get the token
        Intent I = getIntent();
        mAccessToken = I.getStringExtra("token");



        //TODO: set it to the real user name
        getSupportActionBar().setTitle("User" + " Saved Tracks");


        // initialize an adapter and pass an empty ArrayList of MusicListItem
        mMusicListAdapter = new MusicListAdapter(this, new ArrayList<MusicListItem>());


        //TODO set up an emptyView and a loading indicator
        //link our listView
        ListView playListView = (ListView) findViewById(R.id.music_list);

        //set the empty view on the listView if there no data to be shown
        playListView.setEmptyView(mEmptyStateTextView);



        //set our adapter
        playListView.setAdapter(mMusicListAdapter);

        //set the ListView click Listeners
        playListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MusicListItem currentMusicListItem = mMusicListAdapter.getItem(position);

                //release the mediaplayer if we play/click a new sound
                releaseMediaPlayer();



                // Request audio focus for playback
                int result = mAudioManager.requestAudioFocus(mAudioManagerListener,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request focus for a short amount of time
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);


                //if granted access play our media
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                    //TODO fix this method for better playback (works but still crash)
                    playMusic(currentMusicListItem.getMp3Streamlink());


                }


            }
        });


        // REMEMBER TO NOT FORGET THE ACCESS NETWORK STATE PERMISSION TO CHECK INTERNET CONNECTIVITY
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        // if networkInfo is not empty and network is connected then perform step...
        if (networkInfo != null && networkInfo.isConnected()) {

            // get our loader Manager
            LoaderManager loaderManager = getLoaderManager();

            // initialize the loader and specify an id for reuse
            loaderManager.initLoader(MUSIC_LIST_ITEM_LOADER_ID, null, this);




        } else {

            //set the loading indicator to gone if we have no connection
            //so that it will not overlap the view when showing "no internet connection"
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            //set the text to no internet connection if we aren't connected
            mEmptyStateTextView.setText("no internet connection");
        }


    }


    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    /**
     *create the loader
     */
    @Override
    public Loader<List<MusicListItem>> onCreateLoader(int i, Bundle bundle) {
        return new MusicListItemLoader(this, mAccessToken);
    }

    /**
     * once the loader finished complete some tasks
     */
    @Override
    public void onLoadFinished(Loader<List<MusicListItem>> loader, List<MusicListItem> musicListItems) {

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);


        // Set empty state text to display "No music tracks found"
        // this TextView is attached to the setEmptyView()
        // here we just preparing our text to show up if the view is really empty
        mEmptyStateTextView.setText("No music tracks found");



        // Clear the adapter of previous data
        mMusicListAdapter.clear();


        if (musicListItems != null && !musicListItems.isEmpty()) {
            mMusicListAdapter.addAll(musicListItems);//here you can comment out the adapter to test if the empty view message will be shown or to see the progress indicator
        }

    }

    /**
     *
     * once the loader reset we clean the data
     */
    @Override
    public void onLoaderReset(Loader<List<MusicListItem>> loader) {

        mMusicListAdapter.clear();

    }


    /**
     * TODO this method is a bit unstable must fix it for better playback experience
     * play our music
     * @param url the String of the current music link we want to play
     */
    public void playMusic(String url) {

        //instanciate a new player
        mMediaPlayer = new MediaPlayer();


        //set up our MediaPlayer UI and set our anchor view
        mMediaPlayBack = new MediaPlayBack(this, mMediaPlayer, findViewById(R.id.music_list));


        // set audioStream type
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {

            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepare();

        } catch (IllegalArgumentException | IOException e) {
            Log.e(LOG_TAG, "failed to set data source: " + e);

        }


        //start the music
        mMediaPlayer.start();

        //this method set a listener to the player to know if were done playing
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);


    }


    //TODO arrange this method: link to {@link #playMusic} witch is not perfect

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {

        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null) {

            //release the mediaPlayer
            mMediaPlayer.release();

            // set the media player back to null. .
            mMediaPlayer = null;

            //abandon Audio focus
            mAudioManager.abandonAudioFocus(mAudioManagerListener);

            //empty the MediaPlayer (this is not a good solution only temporary )
            mMediaPlayBack = null;



        }
    }





}
