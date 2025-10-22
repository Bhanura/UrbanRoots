package com.example.urbanroots;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText nameEditText, locationEditText, farmerIdEditText;
    private TextView emailText;
    private ProgressBar progressBar;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating fragment_edit_profile");
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Initializing EditProfileFragment");

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameEditText = view.findViewById(R.id.name_edit_text);
        emailText = view.findViewById(R.id.email_text);
        locationEditText = view.findViewById(R.id.location_edit_text);
        farmerIdEditText = view.findViewById(R.id.farmer_id_edit_text);
        progressBar = view.findViewById(R.id.progress_bar);
        MaterialButton saveButton = view.findViewById(R.id.save_profile);

        // Set up save button click listener
        saveButton.setOnClickListener(v -> saveProfileData());

        // Fetch and display current profile data
        loadProfileData();
    }

    private void loadProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "loadProfileData: No user logged in");
            if (isAdded()) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show();
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_editProfileFragment_to_loginFragment);
            }
            return;
        }

        String userEmail = currentUser.getEmail();
        Log.d(TAG, "loadProfileData: Fetching data for email: " + userEmail);
        emailText.setText(userEmail != null ? userEmail : "N/A");

        progressBar.setVisibility(View.VISIBLE);

        // Query Firestore for the user's profile data
        db.collection("farmers")
                .whereEqualTo("Email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Firestore query completed. Documents found: " + queryDocumentSnapshots.size());
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming one document per user email
                        String name = queryDocumentSnapshots.getDocuments().get(0).getString("Name");
                        String location = queryDocumentSnapshots.getDocuments().get(0).getString("Location");
                        String farmerId = queryDocumentSnapshots.getDocuments().get(0).getString("FarmerId");

                        Log.d(TAG, "Data retrieved - Name: " + name + ", Location: " + location + ", FarmerId: " + farmerId);
                        nameEditText.setText(name != null ? name : "");
                        locationEditText.setText(location != null ? location : "");
                        farmerIdEditText.setText(farmerId != null ? farmerId : "");
                    } else {
                        Log.w(TAG, "No profile data found for email: " + userEmail);
                        if (isAdded()) {
                            Toast.makeText(context, "Profile data not found", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching profile data: ", e);
                    if (isAdded()) {
                        Toast.makeText(context, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "saveProfileData: No user logged in");
            if (isAdded()) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show();
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_editProfileFragment_to_loginFragment);
            }
            return;
        }

        String userEmail = currentUser.getEmail();
        String name = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String farmerId = farmerIdEditText.getText().toString().trim();

        // Basic validation
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Update Firestore document
        db.collection("farmers")
                .whereEqualTo("Email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Update existing document
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("farmers").document(documentId)
                                .update(
                                        "Name", name,
                                        "Location", location,
                                        "FarmerId", farmerId
                                )
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "Profile updated successfully");
                                    if (isAdded()) {
                                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_LONG).show();
                                        NavController navController = Navigation.findNavController(requireView());
                                        navController.navigate(R.id.action_editProfileFragment_to_profileFragment);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Error updating profile: ", e);
                                    if (isAdded()) {
                                        Toast.makeText(context, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // No document found, create a new one
                        db.collection("farmers")
                                .add(new FarmerProfile(name, userEmail, location, farmerId))
                                .addOnSuccessListener(documentReference -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "Profile created successfully");
                                    if (isAdded()) {
                                        Toast.makeText(context, "Profile created successfully", Toast.LENGTH_LONG).show();
                                        NavController navController = Navigation.findNavController(requireView());
                                        navController.navigate(R.id.action_editProfileFragment_to_profileFragment);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Error creating profile: ", e);
                                    if (isAdded()) {
                                        Toast.makeText(context, "Failed to create profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error querying profile: ", e);
                    if (isAdded()) {
                        Toast.makeText(context, "Failed to query profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up");
        context = null; // Prevent memory leaks
    }

    // Helper class to store profile data
    public static class FarmerProfile {
        public String Name;
        public String Email;
        public String Location;
        public String FarmerId;

        public FarmerProfile(String name, String email, String location, String farmerId) {
            this.Name = name;
            this.Email = email;
            this.Location = location;
            this.FarmerId = farmerId;
        }
    }
}