package com.example.jaldbaazi_theurgentrental;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {
    // Declare FirebaseAuth instance
    private FirebaseAuth mAuth;
    // Declare UI elements
    private MaterialButton createAccountButton, continueWithGoogleButton;
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the specified layout file
        setContentView(R.layout.activity_sign_up);
        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Find UI elements by their respective IDs
        createAccountButton = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.ProgressBar);
        continueWithGoogleButton = findViewById(R.id.continue_with_google_button);

        // Configure Google Sign-In
        configureGoogleSignIn();

        // Set click listener for createAccountButton using lambda expression
        createAccountButton.setOnClickListener(view -> {
            // Get user input from text fields
            String email = getTextFromInputEditText(R.id.Email_edit_text);
            String password = getTextFromInputEditText(R.id.password_edit_text);
            String confirmPassword = getTextFromInputEditText(R.id.confirm_password_edit_text);

            // Clear any previous errors
            clearErrors();
            // Trigger the sign-up process
            signUpWithEmailPassword(email, password, confirmPassword);
        });

        // Set click listener for continueWithGoogleButton using lambda expression
        continueWithGoogleButton.setOnClickListener(view -> signInWithGoogle());

        // Find login TextView by its ID
        TextView loginTextView = findViewById(R.id.login_textview);

        // Set click listener for loginTextView using lambda expression
        loginTextView.setOnClickListener(v -> navigateToLoginActivity());
    }

    // Create a method to configure Google Sign-In
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272159282829-rbfq7po8bj8tvk5t96ofng3dh5euo3lo.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Create a method to handle Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Handle the result of the Google Sign-In
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }


    // Authenticate with Firebase using Google credentials
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showProgress(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        showToast("Google sign-in successful!");
                    } else {
                        // If sign in fails, display a message to the user.
                        showToast("Google sign-in failed.");
                    }
                });
    }

    // Handle the sign-up process with email and password
    private void signUpWithEmailPassword(String email, String password, String confirmPassword) {
        // Find TextInputLayouts by their respective IDs
        TextInputLayout emailInputLayout = findViewById(R.id.email_input_layout);
        TextInputLayout passwordInputLayout = findViewById(R.id.password_input_layout);
        TextInputLayout confirmPwdInputLayout = findViewById(R.id.confirm_password_input_layout);

        // Validate user inputs
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            emailInputLayout.setError("All fields are required");
            passwordInputLayout.setError("All fields are required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Enter a valid email address");
            return;
        }

        if (password.length() < 8 || !isStrongPassword(password)) {
            passwordInputLayout.setError("Password requirements not met");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPwdInputLayout.setError("Passwords do not match");
            return;
        }

        // Show progress bar and attempt to create user with email and password
        showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide progress bar
                    showProgress(false);

                    if (task.isSuccessful()) {
                        // If sign-up is successful, send verification email
                        sendVerificationEmail();
                    } else {
                        // If sign-up fails, handle the error
                        handleError(task.getException().getMessage());
                    }
                });
    }

    // Send a verification email to the user
    private void sendVerificationEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // If verification email is sent successfully, show a dialog
                            showVerificationDialog();
                        } else {
                            // If sending email fails, set an error on the email input layout
                            setError(R.id.email_input_layout, "Failed to send verification email: " + task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    // Show a verification dialog
    private void showVerificationDialog() {
        showToast("Verification email sent. Please check your email.");
    }

    // Set an error on a TextInputLayout
    private void setError(int inputLayoutId, String error) {
        TextInputLayout inputLayout = findViewById(inputLayoutId);
        inputLayout.setError(error);
    }

    // Clear errors from TextInputLayouts
    private void clearErrors() {
        setError(R.id.email_input_layout, "");
        setError(R.id.password_input_layout, "");
        setError(R.id.confirm_password_input_layout, "");
    }

    // Show or hide the progress bar and update the button text and state
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createAccountButton.setText(show ? "" : "Create Account");
        createAccountButton.setEnabled(!show);
    }

    // Show a Toast message
    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Handle errors during sign-up
    private void handleError(String errorMessage) {
        setError(R.id.confirm_password_input_layout, "Sign-up failed: " + errorMessage);
    }

    // Navigate to the login activity
    private void navigateToLoginActivity() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    // Get text from a TextInputEditText
    private String getTextFromInputEditText(int editTextId) {
        return ((TextInputEditText) findViewById(editTextId)).getText().toString();
    }

    // Check if a password is strong
    private boolean isStrongPassword(String password) {
        return password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+");
    }
}
