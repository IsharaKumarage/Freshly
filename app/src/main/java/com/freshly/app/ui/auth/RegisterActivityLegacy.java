package com.freshly.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.freshly.app.R;
import com.freshly.app.ui.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import com.freshly.app.ui.onboarding.PartnerIntroActivity;

public class RegisterActivityLegacy extends AppCompatActivity {

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnRegister;
    private MaterialTextView tvLogin;
    private LinearLayout cardConsumer;
    private LinearLayout cardFarmer;
    private CheckBox cbTerms;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String selectedRole = "CONSUMER"; // or FARMER

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initViews();
        setupClicks();
        highlightRole();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        cardConsumer = findViewById(R.id.cardConsumer);
        cardFarmer = findViewById(R.id.cardFarmer);
        cbTerms = findViewById(R.id.cbTerms);
    }

    private void setupClicks() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish()); // go back to LoginActivity

        cardConsumer.setOnClickListener(v -> { selectedRole = "CONSUMER"; highlightRole(); });
        cardFarmer.setOnClickListener(v -> { selectedRole = "FARMER"; highlightRole(); });
    }

    private void attemptRegister() {
        String name = getText(etFullName);
        String email = getText(etEmail);
        String phone = getText(etPhone);
        String password = getText(etPassword);
        String confirm = getText(etConfirmPassword);

        if (TextUtils.isEmpty(name)) { etFullName.setError(getString(R.string.full_name)); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError(getString(R.string.email)); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError(getString(R.string.password)); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); return; }
        if (!TextUtils.equals(password, confirm)) { etConfirmPassword.setError("Passwords do not match"); return; }
        if (!cbTerms.isChecked()) { Toast.makeText(this, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show(); return; }

        btnRegister.setEnabled(false);
        btnRegister.setText(R.string.loading);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser() != null ? result.getUser().getUid() : null;
                    if (uid == null) {
                        onError("User ID is null");
                        return;
                    }

                    Map<String, Object> user = new HashMap<>();
                    user.put("id", uid);
                    user.put("fullName", name);
                    user.put("email", email);
                    user.put("phoneNumber", phone);
                    user.put("userType", selectedRole);
                    user.put("profileImageUrl", "");
                    user.put("address", "");
                    user.put("city", "");
                    user.put("state", "");
                    user.put("zipCode", "");
                    user.put("createdAt", System.currentTimeMillis());
                    user.put("isActive", true);

                    firestore.collection("users").document(uid).set(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                                if ("FARMER".equals(selectedRole)) {
                                    startActivity(new Intent(this, PartnerIntroActivity.class));
                                    finish();
                                } else {
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> onError(e.getMessage()));
                })
                .addOnFailureListener(e -> onError(e.getMessage()));
    }

    private String getText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void onError(String msg) {
        Toast.makeText(this, msg != null ? msg : getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
        btnRegister.setEnabled(true);
        btnRegister.setText(R.string.register);
    }

    private void highlightRole() {
        // Simple visual cue: selected role card uses splash_background, other uses gray
        if ("CONSUMER".equals(selectedRole)) {
            cardConsumer.setBackgroundResource(R.drawable.splash_background);
            cardFarmer.setBackgroundColor(0xFFF7F7F7);
        } else {
            cardFarmer.setBackgroundResource(R.drawable.splash_background);
            cardConsumer.setBackgroundColor(0xFFF7F7F7);
        }
    }
}
