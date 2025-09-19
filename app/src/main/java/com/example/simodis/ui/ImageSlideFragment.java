package com.example.simodis.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.simodis.R;

public class ImageSlideFragment extends Fragment {

    private static final String ARG_IMAGE_RES_ID = "image_res_id";

    public static ImageSlideFragment newInstance(int imageResId) {
        ImageSlideFragment fragment = new ImageSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES_ID, imageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_slide, container, false);
        ImageView imageView = view.findViewById(R.id.slide_image);

        if (getArguments() != null) {
            int imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);
            imageView.setImageResource(imageResId);
        }

        return view;
    }
}
