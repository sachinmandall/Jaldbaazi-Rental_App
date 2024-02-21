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

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private MaterialButton loginMaterialButton , continuewithGoogle;
    private ProgressBar progressBar;
    private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        loginMaterialButton = findViewById(R.id.login_account_button);
        continuewithGoogle=findViewById(R.id.continue_with_google_button);
        progressBar = findViewById(R.id.loginProgressBar);
        signup = findViewById(R.id.sign_up_Text);

        loginMaterialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = getTextFromInputEditText(R.id.email_edit_text);
                String password = getTextFromInputEditText(R.id.password_edit_text);

                clearErrors();
                loginWithEmailPassword(email, password);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUpActivity();
            }
        });
    }

    private void loginWithEmailPassword(String email, String password) {
        TextInputLayout emailInputLayout = findViewById(R.id.email_input_layout);
        TextInputLayout passwordInputLayout = findViewById(R.id.password_input_layout);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            emailInputLayout.setError("All fields are required");
            passwordInputLayout.setError("All fields are required");

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError( "Enter a valid email address");
            return;
        }

        showProgress(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            showToast("Login successful");
                            navigateToMainActivity();
                        } else {
                            showToast("Please verify your email address");
                        }
                    } else {
                        handleError(task.getException().getMessage());
                    }
                });
    }

    private void clearErrors() {
        setError(R.id.email_input_layout, "");
        setError(R.id.password_input_layout, "");
    }

    private void setError(int inputLayoutId, String error) {
        TextInputLayout inputLayout = findViewById(inputLayoutId);
        inputLayout.setError(error);
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginMaterialButton.setText(show ? " please wait " : "Login");
        loginMaterialButton.setEnabled(!show);
        continuewithGoogle.setOnClickListener(view -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }

    private void handleError(String errorMessage) {
        setError(R.id.email_input_layout, "Login failed: " + errorMessage);
        setError(R.id.password_input_layout, "");
    }

    private void navigateToSignUpActivity() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private String getTextFromInputEditText(int editTextId) {
        return ((TextInputEditText) findViewById(editTextId)).getText().toString();
    }
}