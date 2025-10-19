package com.example.urbanroots;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddCareTaskFragment extends Fragment {

    private EditText dueDateEditText, postalCodeEditText;
    private Spinner taskTypeSpinner;
    private Button addTaskButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;
    private String cropId; // Pass cropId from previous fragment

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_care_task, container, false);

        context = requireContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        dueDateEditText = view.findViewById(R.id.dueDateEditText);
        postalCodeEditText = view.findViewById(R.id.postalCodeEditText);
        taskTypeSpinner = view.findViewById(R.id.taskTypeSpinner);
        addTaskButton = view.findViewById(R.id.addTaskButton);

        // Populate task type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.task_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTypeSpinner.setAdapter(adapter);

        // Get cropId from arguments (e.g., passed from FindCropsFragment)
        if (getArguments() != null) {
            cropId = getArguments().getString("cropId");
        }

        addTaskButton.setOnClickListener(v -> addCareTask());

        return view;
    }

    private void addCareTask() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in to add a task", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = taskTypeSpinner.getSelectedItem().toString();
        String dueDate = dueDateEditText.getText().toString().trim();
        String postalCode = postalCodeEditText.getText().toString().trim();

        if (type.isEmpty() || dueDate.isEmpty() || cropId == null) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskId = db.collection("care_tasks").document().getId();
        CareTask careTask = new CareTask(taskId, type, cropId, user.getUid(), dueDate, postalCode, "Pending");

        db.collection("care_tasks").document(taskId).set(careTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_addCareTaskFragment_to_dashboardFragment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}