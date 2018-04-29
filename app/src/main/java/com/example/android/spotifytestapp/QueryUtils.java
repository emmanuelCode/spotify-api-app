package com.example.android.spotifytestapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



/**
 * this class will help us parse our Json to get the appropriate value in the MusicListItem
 */

public final class QueryUtils {


    //log tag for debugging
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    //prepare our OkHttpClient and initialize it
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();


    // we don't want to initialize nothing we only want to hold static variables and method
    private QueryUtils(){


    }


    /**
     * fetch user saved tracks and parse the JSON
     * @param accessToken need a token to request tracks
     * @return an ArrayList of MusicListItem
     */
    public static ArrayList<MusicListItem> fetchUserSavedTracks(String accessToken) {

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/tracks")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();


        // prepare our response
        Response jsonResponse = null;
        String extractedJSON = "";

        try {
            //here there is no Call.enqueue as we are dealing with a long JSON response
            //execute a request call and store it to the response
            jsonResponse = mOkHttpClient.newCall(request).execute();

            //store it in a String
            extractedJSON = jsonResponse.body().string();


        } catch (IOException i) {
            Log.e(LOG_TAG, "Failed to fetch JSON data: " + i);

        }

        //extract the JSON data and store it into the MusicListItem ArrayList
        ArrayList<MusicListItem> musicListItems = extractMusicList(extractedJSON);

        return  musicListItems;
    }






    /**
     *
     * parse the JSON key element for our MusicListIte
     * @param jsonResponse the response given by OkHttp request
     * @return an ArrayList of MusicListItems
     */
    private static ArrayList<MusicListItem> extractMusicList(String jsonResponse) {

        ArrayList<MusicListItem> musicList = new ArrayList<MusicListItem>();


        // try to parse the json
        try {

            //root of the spotify json response
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);

            //look for the key tag item array where music info is store
            JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

            // here we loop into the array getting the fields we need for the earthquake data
            for (int i = 0; i < itemsArray.length(); i++) {

                String albumImageLink = "";
                String albumName = "";
                String songTitle = "";
                String mp3PreviewURL = "";


                //loop into "itemArray" ObjectKeyTag
                JSONObject items = itemsArray.getJSONObject(i);

                //look into "track" ObjectKeyTag
                JSONObject track = items.getJSONObject("track");

                //look into the "album" ObjectKeyTag
                JSONObject album = track.getJSONObject("album");

                //look into the "images" ArrayObjectKeyTag
                JSONArray images = album.getJSONArray("images");

                //get the imageObject of the album size:300 by 300 (chose the second element of the Array)
                JSONObject mediumSizeImage = images.getJSONObject(1);

                //get image link and store it
                albumImageLink = mediumSizeImage.getString("url");

                //get the album name
                albumName = album.getString("name");

                //get track name or songTitle
                songTitle = track.getString("name");

                //get the mp3 preview URL
                mp3PreviewURL = track.getString("preview_url");




                // add a new {@link MusicListItem} object with the albumImageLink, albumName, mp3 preview URL
                musicList.add(new MusicListItem(albumImageLink, albumName, songTitle, mp3PreviewURL));

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the MusicListItem JSON results", e);
        }


        return musicList;
    }


}
