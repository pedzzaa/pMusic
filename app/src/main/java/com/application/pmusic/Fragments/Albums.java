package com.application.pmusic.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.application.pmusic.R;

public class Albums extends Fragment {
    public RecyclerView albumsRecyclerView;
    public TextView noAlbumsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        albumsRecyclerView = view.findViewById(R.id.albums_recycle_view);
        noAlbumsText = view.findViewById(R.id.no_albums);

        return view;
    }

}