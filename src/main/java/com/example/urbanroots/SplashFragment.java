package com.example.urbanroots;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class SplashFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Delayed navigation to loginFragment
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_splashFragment_to_loginFragment);
            }
        }, 2000); // 2-second delay
    }
}