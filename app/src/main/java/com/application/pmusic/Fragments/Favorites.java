package com.application.pmusic.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.application.pmusic.Adapters.MainAdapter;
import com.application.pmusic.Database.AudioContract;
import com.application.pmusic.Database.SQLDatabase;
import com.application.pmusic.R;
import com.application.pmusic.Service.SongModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Favorites extends Fragment implements Loading {
    private final String TAG = "FAVORITES_FRAGMENT";
    private RecyclerView favoritesRecycleView;
    private TextView noFavoritesText;
    private SQLDatabase db;
    private ArrayList<SongModel> songs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        favoritesRecycleView = view.findViewById(R.id.favorites_recycle_view);
        noFavoritesText = view.findViewById(R.id.no_favorites);
        db = new SQLDatabase(getContext());
        songs = new ArrayList<>();

        displaySongs("");
        return view;
    }

    @Override
    public void displaySongs(String searchQuery) {
        Cursor cursor = searchQuery.isEmpty() ? db.readFavoriteSongs() : db.searchSongs(searchQuery);
        storeDataInArray(cursor);

        favoritesRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoritesRecycleView.setAdapter(new MainAdapter(getContext(), cursor));
        notifyAdapterForDataChanged();
    }

    @Override
    public void storeDataInArray(Cursor cursor) {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (cursor.getCount() == 0) {
                requireActivity().runOnUiThread(() -> {
                    noFavoritesText.setVisibility(View.VISIBLE);
                    favoritesRecycleView.setAdapter(null);
                });
            } else {
                requireActivity().runOnUiThread(() -> noFavoritesText.setVisibility(View.GONE));

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_TITLE));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_DATA));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_DURATION));

                    songs.add(new SongModel(id, path, title, duration));
                }
            }
        });
    }

    @Override
    public void onResume() {
        displaySongs("");
        notifyAdapterForDataChanged();
        super.onResume();
    }

    private void notifyAdapterForDataChanged(){
        try {
            requireActivity().runOnUiThread(Objects.requireNonNull(favoritesRecycleView.getAdapter())::notifyDataSetChanged);
        } catch (NullPointerException e){
            Log.d(TAG, "Probably the ID is null or the song doesn't exist! Error: \n" + e);
        }
    }
}