package com.example.urbanroots;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddCropFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText cropNameEditText, priceEditText, descriptionEditText, statusEditText;
    private MaterialButton addCropButton;
    private ProgressBar progressBar;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_crop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cropNameEditText = view.findViewById(R.id.cropNameEditText);
        priceEditText = view.findViewById(R.id.priceEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        statusEditText = view.findViewById(R.id.statusEditText);
        addCropButton = view.findViewById(R.id.addCropButton);
        progressBar = view.findViewById(R.id.progressBar);

        isAdmin(isAdmin -> {
            if (!isAdmin) {
                Toast.makeText(context, "Access denied: Admins only", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_addCropFragment_to_dashboardFragment);
                return;
            }
            addCropButton.setOnClickListener(v -> addCrop());
        });
    }

    private void isAdmin(OnAdminCheckListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            listener.onResult(false);
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences("UrbanRoots", Context.MODE_PRIVATE);
        boolean cachedAdmin = prefs.getBoolean("isAdmin_" + user.getUid(), false);
        if (cachedAdmin) {
            listener.onResult(true);
            return;
        }
        db.collection("admins").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isAdmin = documentSnapshot.exists();
                    prefs.edit().putBoolean("isAdmin_" + user.getUid(), isAdmin).apply();
                    listener.onResult(isAdmin);
                })
                .addOnFailureListener(e -> listener.onResult(false));
    }

    private interface OnAdminCheckListener {
        void onResult(boolean isAdmin);
    }

    private void addCrop() {
        String cropName = cropNameEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String status = statusEditText.getText().toString().trim();

        if (cropName.isEmpty() || priceStr.isEmpty() || description.isEmpty() || status.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        addCropButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> cropData = new HashMap<>();
        cropData.put("cropName", cropName);
        cropData.put("price", price);
        cropData.put("description", description);
        cropData.put("status", status);

        db.collection("crops").add(cropData)
                .addOnSuccessListener(documentReference -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Crop added successfully", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_addCropFragment_to_dashboardFragment);
                        clearInputFields();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error adding crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        addCropButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void clearInputFields() {
        cropNameEditText.setText("");
        priceEditText.setText("");
        descriptionEditText.setText("");
        statusEditText.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}