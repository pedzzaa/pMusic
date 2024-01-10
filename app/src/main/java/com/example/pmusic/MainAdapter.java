package com.example.pmusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>{
    Context context;
    Cursor cursor;
    MyMediaPlayer myMediaPlayer;

    public MainAdapter(Context context, Cursor cursor, MyMediaPlayer myMediaPlayer) {
        this.context = context;
        this.cursor = cursor;
        this.myMediaPlayer = myMediaPlayer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        cursor.moveToPosition(position);

        int id = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry._ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_TITLE));

        holder.titleTextView.setText(title);

        if (myMediaPlayer != null && myMediaPlayer.getCurrentSongId() == id) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.selected_song));
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.default_card_color));
        }

        holder.itemView.setOnClickListener(view -> {
            int selectedPosition = holder.getAdapterPosition();
            SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            preferences.edit().putInt("selectedPosition", selectedPosition).apply();

            ((Music) context).setResourcesWithMusic(id);

            Intent intent = new Intent(context, MusicPlayer.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("currentSong", id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView card;
        TextView titleTextView;
        ImageView iconImageView;
        public ViewHolder(View itemView){
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
            card = itemView.findViewById(R.id.song_tab);
        }
    }
}