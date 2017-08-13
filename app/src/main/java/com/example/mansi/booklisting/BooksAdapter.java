package com.example.mansi.booklisting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BooksAdapter extends ArrayAdapter<Books> {
    public BooksAdapter(Context context, ArrayList<Books> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        if (rootView == null) {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView authors = (TextView) rootView.findViewById(R.id.authors);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.book_image);

        Books currentbook = (Books) getItem(position);

        //if title is not blank in JSON response then display it
        if (currentbook.getmTitle() != null && currentbook.getmTitle() != "") {
            title.setText(currentbook.getmTitle());
        }
        //if authors is not blank in JSON response then display it
        if (currentbook.getMauthors() != null && currentbook.getMauthors() != "") {
            authors.setText(currentbook.getMauthors());
        }
        //if imageURL is not blank in JSON response then set that image otherwise set default image not found image from drawable
        if (currentbook.getImageUrl() != null && currentbook.getImageUrl() != "") {
            Glide.with(getContext())
                    .load(currentbook.getImageUrl()).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.image_not_found);
        }
        return rootView;
    }
}
