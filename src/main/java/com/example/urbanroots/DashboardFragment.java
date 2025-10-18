package com.example.urbanroots;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView cropsRecyclerView;
    private CropAdapter cropAdapter;
    private List<Crop> cropList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        context = requireContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        cropsRecyclerView = view.findViewById(R.id.cropsRecyclerView);
        cropsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cropList = new ArrayList<>();
        cropAdapter = new CropAdapter(cropList);
        cropsRecyclerView.setAdapter(cropAdapter);

        fetchCrops();

        MaterialButton adminPanelButton = view.findViewById(R.id.adminPanelButton);
        MaterialButton addCropButton = view.findViewById(R.id.addCropButton);
        isAdmin(isAdmin -> {
            if (isAdmin) {
                adminPanelButton.setVisibility(View.VISIBLE);
                addCropButton.setVisibility(View.GONE); // Hide for admins to avoid confusion
                adminPanelButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_dashboardFragment_to_adminFragment);
                });
            } else {
                adminPanelButton.setVisibility(View.GONE);
                addCropButton.setVisibility(View.VISIBLE); // Show for non-admins
                addCropButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_dashboardFragment_to_findCropsFragment);
                });
            }
        });

        return view;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}