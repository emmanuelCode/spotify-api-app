package com.example.android.spotifytestapp;

/**
 * MusicListItem blueprint class that will hold our values from the JSON parse
 */


public class MusicListItem {

    private String mAlbumImageLink, mAlbumName,mSongTitle, mMp3Streamlink;


    /**
     *
     * @param albumImageLink  album image
     * @param albumName album name
     * @param mp3Streamlink stream link
     * @param songTitle title of the song
     */

    public MusicListItem(String albumImageLink, String albumName, String songTitle , String mp3Streamlink){

        mAlbumImageLink = albumImageLink;
        mAlbumName = albumName;
        mSongTitle = songTitle;
        mMp3Streamlink = mp3Streamlink;

    }

    public String getAlbumImageLink() {
        return mAlbumImageLink;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getSongTitle() {
        return mSongTitle;
    }

    public String getMp3Streamlink() {
        return mMp3Streamlink;
    }


}
