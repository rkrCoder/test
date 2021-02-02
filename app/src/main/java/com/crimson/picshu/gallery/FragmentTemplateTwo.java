package com.crimson.picshu.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crimson.picshu.R;


public class FragmentTemplateTwo extends Fragment {

    public FragmentTemplateTwo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_template_two,container,false);
        return rootView;
    }
}