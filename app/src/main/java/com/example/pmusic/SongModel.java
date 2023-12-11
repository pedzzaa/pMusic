package com.example.pmusic;

import android.os.Parcel;
import android.os.Parcelable;

public class SongModel implements Parcelable {
    private String path;
    private String title;
    private String duration;
    private boolean isChecked;
    public int id;

    public SongModel(int id, String path, String title, String duration) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    protected SongModel(Parcel in) {
        id = in.readInt();
        path = in.readString();
        title = in.readString();
        duration = in.readString();
    }

    public static final Creator<SongModel> CREATOR = new Creator<SongModel>() {
        @Override
        public SongModel createFromParcel(Parcel in) {
            return new SongModel(in);
        }

        @Override
        public SongModel[] newArray(int size) {
            return new SongModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(path);
        parcel.writeString(title);
        parcel.writeString(duration);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public int getId() {
        return id;
    }

}