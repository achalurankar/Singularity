package com.android.singularity.util;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

public class Loader {

    // method to toggle loader while api callouts
    public static void toggleLoading(View context, int loaderId, int containerId) {
        View loader = context.findViewById(loaderId);
        View container = context.findViewById(containerId);
        if(loader.getVisibility() == View.INVISIBLE) {
            loader.setVisibility(View.VISIBLE);
            loader.setAlpha(1);
            container.setAlpha(0.4f);
        } else {
            loader.setVisibility(View.INVISIBLE);
            container.setAlpha(1);
        }
    }
}
