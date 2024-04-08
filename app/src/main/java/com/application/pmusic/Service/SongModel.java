package com.application.pmusic.Service;

public class SongModel {
    private String title;
    private boolean isFavorite;
    private final String path;
    private final String duration;
    private final int id;

    public SongModel(int id, String path, String title, String duration) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFavorite(boolean isChecked) {
        this.isFavorite = isChecked;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}