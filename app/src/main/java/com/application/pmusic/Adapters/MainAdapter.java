package com.application.pmusic.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.application.pmusic.Database.AudioContract;
import com.application.pmusic.Player.MusicPlayer;
import com.application.pmusic.R;
import com.application.pmusic.Service.MusicService;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private final Context context;
    private final Cursor cursor;
    private final String fragmentIdentifier;

    public MainAdapter(Context context, Cursor cursor, String fragmentIdentifier){
        this.context = context;
        this.cursor = cursor;
        this.fragmentIdentifier = fragmentIdentifier;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycle_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        int id = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract._ID));
        String songTitle = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_TITLE));
        holder.titleTextView.setText(songTitle);

        holder.songCard.setCardBackgroundColor(ContextCompat.getColor(context,
                MusicService.getCurrentSongId() == id ? R.color.selected_song : R.color.default_card_color));

        holder.songCard.setOnClickListener(click -> {
            Intent intent = new Intent(context, MusicPlayer.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("fragment", fragmentIdentifier);
            intent.putExtra("songId", id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView songCard;
        public TextView titleTextView;
        public ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            songCard = itemView.findViewById(R.id.song_tab);
            titleTextView = itemView.findViewById(R.id.music_title_text);
        }
    }
}