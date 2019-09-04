package com.example.android.myapplication;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;
    public MarkerAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        if(!marker.getTitle().equals("Your Position")) {
            SpannableStringBuilder builder = new SpannableStringBuilder();

            View view = context.getLayoutInflater().inflate(R.layout.infowindow, null);

            TextView Name = (TextView) view.findViewById(R.id.Name);
            TextView category = (TextView) view.findViewById(R.id.Category);
            TextView id = (TextView) view.findViewById(R.id.ID);

            Name.setTypeface(null, Typeface.BOLD);
            category.setTypeface(null, Typeface.BOLD);
            id.setTypeface(null, Typeface.BOLD);

            final ImageView image = (ImageView) view.findViewById(R.id.Image);
            if (marker.getTitle() != null) {
                String redName = "Name : ";
                SpannableString redSpannableName = new SpannableString(redName);
                redSpannableName.setSpan(new ForegroundColorSpan(Color.RED), 0, redName.length(), 0);
                builder.append(redSpannableName);
                Name.setText(builder, TextView.BufferType.SPANNABLE);
                Name.append(marker.getTitle());
                builder.clear();
            } else {
                Name.setText("No Title");
            }
            if (marker.getSnippet() != null) {
                String snippet = marker.getSnippet();
                String words[] = snippet.split("#");

                //RED CATEGORY
                String redCategory = "Category : ";
                SpannableString redSpannableCategory = new SpannableString(redCategory);
                redSpannableCategory.setSpan(new ForegroundColorSpan(Color.RED), 0, redCategory.length(), 0);
                builder.append(redSpannableCategory);
                category.setText(builder, TextView.BufferType.SPANNABLE);
                category.append(words[0]);
                builder.clear();

                //RED ID
                String redID = "ID : ";
                SpannableString redSpannableID = new SpannableString(redID);
                redSpannableID.setSpan(new ForegroundColorSpan(Color.RED), 0, redID.length(), 0);
                builder.append(redSpannableID);
                id.setText(builder, TextView.BufferType.SPANNABLE);
                id.append(words[2]);
                builder.clear();
                if (!words[1].equals("Not Exist")) {
                    final Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            image.setImageBitmap(bitmap);

                            if (marker != null && marker.isInfoWindowShown()) {

                                marker.hideInfoWindow();

                                marker.showInfoWindow();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };
                    Picasso.with(context)
                            .load(words[1])
                            .resize(500, 500)
                            .centerCrop().into(target);
                    image.setVisibility(View.VISIBLE);
                } else {
                    category.setText(words[0] + "\nImage Does Not Exist");
                }
            } else {
                if (marker.getTitle().equals("Your Position")) {
                } else
                    category.setText("No Category");
            }
            return view;
        }
        else{
            View view = context.getLayoutInflater().inflate(R.layout.user_position, null);
            TextView name = (TextView) view.findViewById(R.id.Name);
            name.setText(marker.getTitle());
            return name;
        }

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;

    }

}
