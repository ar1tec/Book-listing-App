package com.example.mansi.booklisting;

import android.os.Parcel;
import android.os.Parcelable;


public class Books implements Parcelable {
    public static final Creator<Books> CREATOR = new Creator<Books>() {
        @Override
        public Books createFromParcel(Parcel in) {
            return new Books(in);
        }

        @Override
        public Books[] newArray(int size) {
            return new Books[size];
        }
    };
    private String mTitle = null;
    private String imageUrl = null;
    private String mLink = null;
    private String mauthors;

    public Books(String title, String authors, String imageLink, String link) {
        //initialise member variable with local variable of parametrised constructor
        mTitle = title;
        mauthors = authors;
        imageUrl = imageLink;
        mLink = link;
    }

    protected Books(Parcel in) {
        mTitle = in.readString();
        imageUrl = in.readString();
        mLink = in.readString();
        mauthors = in.readString();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getmLink() {
        return mLink;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getMauthors() {
        return mauthors;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mauthors);
        parcel.writeString(imageUrl);
        parcel.writeString(mLink);
    }
}
