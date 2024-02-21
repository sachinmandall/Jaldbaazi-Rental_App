package com.example.jaldbaazi_theurgentrental.UserDashBoard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.jaldbaazi_theurgentrental.R;

public class HomeFragment extends Fragment {

    Button click;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the button
//        click = view.findViewById(R.id.your_button_id); // Replace 'your_button_id' with the actual ID of your button

        // Set click listener or any other operations you want to perform with the button

        return view;
    }
}
