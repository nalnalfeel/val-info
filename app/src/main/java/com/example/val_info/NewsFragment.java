package com.example.val_info;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.val_info.database.InternalPreference;

public class NewsFragment extends Fragment {
    private InternalPreference mPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mPref = new InternalPreference(getContext());
        View parent = inflater.inflate(mPref.ADMIN ? R.layout.admin_list_user:R.layout.fragment_news, container, false);

        return parent;
    }
};
