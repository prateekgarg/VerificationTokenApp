package com.garg.prateek.verificationtokenapp.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.garg.prateek.verificationtokenapp.R;

/**
 * Created by dhruv on 10/12/14.
 */
public class First extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.first, container, false);
    }
}
