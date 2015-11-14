package com.bob.musicaudio.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/7/12.
 */
public class ArtistInfo implements Parcelable {
    public static final String KEY_ARTIST_NAME="artist";
    public static final String KEY_NUMBER_OF_TRACKS="number_of_tracks";

    public String artist_name;
    public int number_of_tracks;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle=new Bundle();
        bundle.putString(KEY_ARTIST_NAME,artist_name);
        bundle.putInt(KEY_NUMBER_OF_TRACKS, number_of_tracks);
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator<ArtistInfo> CREATOR=new Parcelable.Creator<ArtistInfo>(){
        @Override
        public ArtistInfo createFromParcel(Parcel source) {
            ArtistInfo info=new ArtistInfo();
            Bundle bundle=source.readBundle();
            info.artist_name=bundle.getString(KEY_ARTIST_NAME);
            info.number_of_tracks=bundle.getInt(KEY_NUMBER_OF_TRACKS);
            return info;
        }

        @Override
        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };

}
