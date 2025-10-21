package com.example.urbanroots;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class WatchProfileFragment extends Fragment {
    private static final String TAG = "WatchProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameText, emailText, locationText, farmerIdText;
    private ProgressBar progressBar;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameText = view.findViewById(R.id.name_text);
        emailText = view.findViewById(R.id.email_text);
        locationText = view.findViewById(R.id.location_text);
        farmerIdText = view.findViewById(R.id.farmer_id_text);
        progressBar = view.findViewById(R.id.progress_bar);

        // Fetch and display profile data
        loadProfileData();
    }

    private void loadProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded()) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show();
                // Optionally navigate to login screen
                // NavController navController = Navigation.findNavController(requireView());
                // navController.navigate(R.id.action_watchProfileFragment_to_loginFragment);
            }
            return;
        }

        String userEmail = currentUser.getEmail();
        emailText.setText(userEmail != null ? userEmail : "N/A");

        progressBar.setVisibility(View.VISIBLE);

        // Query Firestore for the user's profile data
        db.collection("farmers")
                .whereEqualTo("Email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming one document per user email
                        QuerySnapshot snapshot = queryDocumentSnapshots;
                        String name = snapshot.getDocuments().get(0).getString("Name");
                        String location = snapshot.getDocuments().get(0).getString("Location");
                        String farmerId = snapshot.getDocuments().get(0).getString("FarmerId");

                        nameText.setText(name != null ? name : "N/A");
                        locationText.setText(location != null ? location : "N/A");
                        farmerIdText.setText(farmerId != null ? farmerId : "N/A");
                    } else {
                        if (isAdded()) {
                            Toast.makeText(context, "Profile data not found", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    if (isAdded()) {
                        Toast.makeText(context, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "Error fetching profile data: ", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null; // Prevent memory leaks
    }
}