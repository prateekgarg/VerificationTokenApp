package com.garg.prateek.verificationtokenapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.garg.prateek.verificationtokenapp.fragments.First;
import com.garg.prateek.verificationtokenapp.fragments.Second;

/**
 * Created by dhruv on 10/12/14.
 */
public class FragmentPageAdapter extends FragmentStatePagerAdapter {

    public FragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new First();
            case 1:
                return new Second();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
