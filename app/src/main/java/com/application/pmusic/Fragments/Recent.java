package com.application.pmusic.Fragments;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.application.pmusic.Adapters.MainAdapter;
import com.application.pmusic.Database.AudioContract;
import com.application.pmusic.Database.SQLDatabase;
import com.application.pmusic.R;
import com.application.pmusic.Service.MusicService;
import com.application.pmusic.Service.SongModel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Recent extends Fragment implements Loading {
    private final String TAG = "RECENT_FRAGMENT";
    private RecyclerView recentRecyclerView;
    private TextView noSongsText;
    private ArrayList<SongModel> songs;
    private SQLDatabase db;

    public Recent(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        recentRecyclerView = view.findViewById(R.id.recent_recycle_view);
        noSongsText = view.findViewById(R.id.no_songs);
        db = new SQLDatabase(getContext());
        songs = new ArrayList<>();

        while(MusicService.isLoading){
            requireActivity().runOnUiThread(() -> noSongsText.setVisibility(View.VISIBLE));
        }

        displaySongs("");
        return view;
    }

    @Override
    public void displaySongs(String searchQuery) {
        Cursor cursor = searchQuery.isEmpty() ? db.readAllMainTableData() : db.searchSongs(searchQuery);
        storeDataInArray(cursor);

        recentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentRecyclerView.setAdapter(new MainAdapter(getContext(), cursor));
        notifyAdapterForDataChanged();
    }

    @Override
    public void storeDataInArray(Cursor cursor) {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (cursor.getCount() == 0) {
                requireActivity().runOnUiThread(() -> {
                    noSongsText.setVisibility(View.VISIBLE);
                    recentRecyclerView.setAdapter(null);
                });
            } else {
                requireActivity().runOnUiThread(() -> noSongsText.setVisibility(View.GONE));

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

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        notifyAdapterForDataChanged();
        super.onResume();
    }

    private void notifyAdapterForDataChanged(){
        try {
            requireActivity().runOnUiThread(Objects.requireNonNull(recentRecyclerView.getAdapter())::notifyDataSetChanged);
        } catch (NullPointerException e){
            Log.d(TAG, "Probably the ID is null or the song doesn't exist! Error: \n" + e);
        }
    }
}