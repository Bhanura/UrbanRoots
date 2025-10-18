package com.example.urbanroots;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AdminFragment extends Fragment implements CropAdapter.OnCropActionListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText cropNameEditText, priceEditText, descriptionEditText, statusEditText;
    private MaterialButton addCropButton, updateCropButton;
    private ProgressBar progressBar;
    private RecyclerView cropsRecyclerView;
    private CropAdapter cropAdapter;
    private List<Crop> cropList;
    private Context context;
    private Crop selectedCrop;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
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
        updateCropButton = view.findViewById(R.id.updateCropButton);
        progressBar = view.findViewById(R.id.progressBar);
        cropsRecyclerView = view.findViewById(R.id.cropsRecyclerView);

        cropList = new ArrayList<>();
        cropAdapter = new CropAdapter(cropList, true, this);
        cropsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cropsRecyclerView.setAdapter(cropAdapter);

        // Handle edit navigation from FindCropsFragment
        Bundle args = getArguments();
        if (args != null && args.containsKey("cropId")) {
            selectedCrop = new Crop();
            selectedCrop.setCropId(args.getString("cropId"));
            selectedCrop.setCropName(args.getString("cropName"));
            selectedCrop.setPrice(args.getDouble("price"));
            selectedCrop.setDescription(args.getString("description"));
            selectedCrop.setStatus(args.getString("status"));
            cropNameEditText.setText(selectedCrop.getCropName());
            priceEditText.setText(String.valueOf(selectedCrop.getPrice()));
            descriptionEditText.setText(selectedCrop.getDescription());
            statusEditText.setText(selectedCrop.getStatus());
            addCropButton.setVisibility(View.GONE);
            updateCropButton.setVisibility(View.VISIBLE);
        }

        // Check admin status
        isAdmin(isAdmin -> {
            if (!isAdmin) {
                Toast.makeText(context, "Access denied: Admins only", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_adminFragment_to_dashboardFragment);
                return;
            }

            fetchCrops();
            addCropButton.setOnClickListener(v -> addCrop());
            updateCropButton.setOnClickListener(v -> updateCrop());
        });
    }
    private void isAdmin(OnAdminCheckListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            listener.onResult(false);
            return;
        }
        db.collection("admins").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> listener.onResult(documentSnapshot.exists()))
                .addOnFailureListener(e -> listener.onResult(false));
    }

    private interface OnAdminCheckListener {
        void onResult(boolean isAdmin);
    }

    private void fetchCrops() {
        db.collection("crops").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cropList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Crop crop = document.toObject(Crop.class);
                        crop.setCropId(document.getId());
                        cropList.add(crop);
                    }
                    cropAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error fetching crops: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                        fetchCrops();
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

    private void updateCrop() {
        if (selectedCrop == null) {
            Toast.makeText(context, "No crop selected", Toast.LENGTH_SHORT).show();
            return;
        }

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

        updateCropButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> cropData = new HashMap<>();
        cropData.put("cropName", cropName);
        cropData.put("price", price);
        cropData.put("description", description);
        cropData.put("status", status);

        db.collection("crops").document(selectedCrop.getCropId()).set(cropData)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Crop updated successfully", Toast.LENGTH_SHORT).show();
                        fetchCrops();
                        clearInputFields();
                        addCropButton.setVisibility(View.VISIBLE);
                        updateCropButton.setVisibility(View.GONE);
                        selectedCrop = null;
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error updating crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        updateCropButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onEdit(Crop crop) {
        selectedCrop = crop;
        cropNameEditText.setText(crop.getCropName());
        priceEditText.setText(String.valueOf(crop.getPrice()));
        descriptionEditText.setText(crop.getDescription());
        statusEditText.setText(crop.getStatus());
        addCropButton.setVisibility(View.GONE);
        updateCropButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDelete(Crop crop) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("seeds").whereEqualTo("cropId", crop.getCropId()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (isAdded()) {
                            Toast.makeText(context, "Cannot delete crop with associated seeds", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        db.collection("crops").document(crop.getCropId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (isAdded()) {
                                        Toast.makeText(context, "Crop deleted successfully", Toast.LENGTH_SHORT).show();
                                        fetchCrops();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) {
                                        Toast.makeText(context, "Error deleting crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnCompleteListener(task -> {
                                    if (isAdded()) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error checking dependencies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void clearInputFields() {
        cropNameEditText.setText("");
        priceEditText.setText("");
        descriptionEditText.setText("");
        statusEditText.setText("Available");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}