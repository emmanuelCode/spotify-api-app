package com.example.android.spotifytestapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * our adapter class will help us manage out data
 */

public class MusicListAdapter extends ArrayAdapter<MusicListItem> {



    public MusicListAdapter(Activity context, ArrayList<MusicListItem> musicListItem ){
        super(context,0,musicListItem);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View musicListView = convertView;
        if(musicListView == null) {
            musicListView = LayoutInflater.from(getContext()).inflate(R.layout.music_list_item, parent, false);

        }

        MusicListItem currentMusicItem = getItem(position);


        //link our MusiclistItems Views
        ImageView albumImage = (ImageView) musicListView.findViewById(R.id.image_album);
        TextView albumTitle = (TextView) musicListView.findViewById(R.id.album_name_text_view);
        TextView songTitle = (TextView) musicListView.findViewById(R.id.song_title_text_view);

        albumTitle.setText(currentMusicItem.getAlbumName());
        songTitle.setText(currentMusicItem.getSongTitle());

        //load the image using glide library
        Glide.with(albumImage.getContext())
                .load(currentMusicItem.getAlbumImageLink())
                .into(albumImage);






        return  musicListView;
    }
}
