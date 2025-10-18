package com.example.urbanroots;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, locationEditText;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        emailEditText = view.findViewById(R.id.email_edit_text);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        locationEditText = view.findViewById(R.id.location_edit_text);
        registerButton = view.findViewById(R.id.register_button);
        progressBar = view.findViewById(R.id.progress_bar);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || location.isEmpty()) {
                if (isAdded()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (!password.equals(confirmPassword)) {
                if (isAdded()) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (password.length() < 6) {
                if (isAdded()) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            registerButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "Attempting to register with email: " + email);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "Registration task completed. Success: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            String farmerId = UUID.randomUUID().toString();
                            Map<String, Object> farmer = new HashMap<>();
                            farmer.put("FarmerId", farmerId);
                            farmer.put("Name", name);
                            farmer.put("Location", location);
                            farmer.put("Email", email);

                            db.collection("farmers").document(farmerId)
                                    .set(farmer)
                                    .addOnSuccessListener(aVoid -> {
                                        mAuth.getCurrentUser().sendEmailVerification()
                                                .addOnSuccessListener(aVoid1 -> {
                                                    if (isAdded()) {
                                                        Log.d(TAG, "Verification email sent successfully");
                                                        Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show();
                                                        NavController navController = Navigation.findNavController(view);
                                                        navController.navigate(R.id.action_registerFragment_to_loginFragment);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    if (isAdded()) {
                                                        Log.e(TAG, "Failed to send verification email: " + e.getMessage(), e);
                                                        Toast.makeText(context, "Failed to send verification email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded()) {
                                            Log.e(TAG, "Failed to save farmer data: " + e.getMessage(), e);
                                            Toast.makeText(context, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            if (isAdded()) {
                                String errorMessage = "Registration failed";
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    errorMessage = "Email already registered";
                                    Log.w(TAG, "Email already in use: " + email);
                                    mAuth.sendPasswordResetEmail(email)
                                            .addOnSuccessListener(aVoid -> {
                                                if (isAdded()) {
                                                    Log.d(TAG, "Password reset email sent for: " + email);
                                                    Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                if (isAdded()) {
                                                    Log.e(TAG, "Failed to send password reset email: " + e.getMessage(), e);
                                                    Toast.makeText(context, "Failed to send password reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else if (task.getException() instanceof FirebaseAuthException) {
                                    errorMessage = task.getException().getMessage();
                                    Log.e(TAG, "FirebaseAuthException: " + errorMessage, task.getException());
                                } else if (task.getException() != null) {
                                    errorMessage = task.getException().getMessage();
                                    Log.e(TAG, "Registration error: " + errorMessage, task.getException());
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (isAdded()) {
                            registerButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Log.e(TAG, "Registration task failed: " + e.getMessage(), e);
                            Toast.makeText(context, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            registerButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        });

        MaterialTextView loginText = view.findViewById(R.id.login_text);
        loginText.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}