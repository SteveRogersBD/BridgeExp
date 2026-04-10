package com.example.bridgeexp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bridgeexp.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.signupButton.setOnClickListener(v -> performSignUp());
        binding.loginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performSignUp() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.nameLayout.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            return;
        }

        binding.signupButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, name);
                    } else {
                        binding.signupButton.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name) {
        Map<String, Object> memory = new HashMap<>();
        memory.put("communication_style", "");
        memory.put("interests", "");
        memory.put("personality", "");
        memory.put("routines", "");
        memory.put("social", "");

        Map<String, Object> user = new HashMap<>();
        user.put("user_id", uid);
        user.put("name", name);
        user.put("memory", memory);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, ChatActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.signupButton.setEnabled(true);
                    Toast.makeText(SignUpActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
