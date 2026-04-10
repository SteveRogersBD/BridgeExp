package com.example.bridgeexp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bridgeexp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, ChatActivity.class));
            finish();
        }

        binding.loginButton.setOnClickListener(v -> performLogin());
        binding.signupRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            return;
        }

        binding.loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, ChatActivity.class));
                        finish();
                    } else {
                        binding.loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
