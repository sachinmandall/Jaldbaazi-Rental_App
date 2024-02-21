package com.example.jaldbaazi_theurgentrental;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private MaterialButton createAccountButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        createAccountButton = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.ProgressBar);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = getTextFromInputEditText(R.id.Email_edit_text);
                String password = getTextFromInputEditText(R.id.password_edit_text);
                String confirmPassword = getTextFromInputEditText(R.id.confirm_password_edit_text);

                clearErrors();
                signUpWithEmailPassword(email, password, confirmPassword);
            }
        });

        TextView loginTextView = findViewById(R.id.login_textview);

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLoginActivity();
            }
        });
    }

    private void signUpWithEmailPassword(String email, String password, String confirmPassword) {
        TextInputLayout emailInputLayout = findViewById(R.id.email_input_layout);
        TextInputLayout passwordInputLayout = findViewById(R.id.password_input_layout);
        TextInputLayout confirmPwdInputLayout = findViewById(R.id.confirm_password_input_layout);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            emailInputLayout.setError( "All fields are required");
            passwordInputLayout.setError("All fields are required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
         emailInputLayout.setError( "Enter a valid email address");
            return;
        }

        if (password.length() < 8|| !isStrongPassword(password)) {
           passwordInputLayout.setError( "Password requirements not met");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPwdInputLayout.setError( "Passwords do not match");
            return;
        }

        showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        sendVerificationEmail();
                    } else {
                        handleError(task.getException().getMessage());
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showVerificationDialog();
                        } else {
                            setError(R.id.email_input_layout, "Failed to send verification email: " + task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    private void showVerificationDialog() {
        showToast("Verification email sent. Please check your email.");
    }

    private void setError(int inputLayoutId, String error) {
        TextInputLayout inputLayout = findViewById(inputLayoutId);
        inputLayout.setError(error);
    }

    private void clearErrors() {
        setError(R.id.email_input_layout, "");
        setError(R.id.password_input_layout, "");
        setError(R.id.confirm_password_input_layout, "");
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createAccountButton.setText(show ? "" : "Create Account");
        createAccountButton.setEnabled(!show);
    }

    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleError(String errorMessage) {
        setError(R.id.confirm_password_input_layout, "Sign-up failed: " + errorMessage);
    }

    private void navigateToLoginActivity() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    private String getTextFromInputEditText(int editTextId) {
        return ((TextInputEditText) findViewById(editTextId)).getText().toString();
    }

    private boolean isStrongPassword(String password) {
        return password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+");
    }
}
