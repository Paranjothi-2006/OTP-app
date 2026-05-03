package com.example.otp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etPhoneNumber, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextInputLayout layoutOtp;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        layoutOtp = findViewById(R.id.layoutOtp);
        progressBar = findViewById(R.id.progressBar);

        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                etPhoneNumber.setError("Enter phone number");
                return;
            }

            // Automatically format to E.164
            if (!phoneNumber.startsWith("+")) {
                if (phoneNumber.length() == 10) {
                    phoneNumber = "+91" + phoneNumber; // Defaulting to India (+91)
                } else {
                    phoneNumber = "+" + phoneNumber;
                }
            }

            sendVerificationCode(phoneNumber);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                etOtp.setError("Enter OTP");
                return;
            }
            verifyCode(code);
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    progressBar.setVisibility(View.GONE);
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String s,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    progressBar.setVisibility(View.GONE);
                    verificationId = s;
                    
                    // Show OTP fields
                    layoutOtp.setVisibility(View.VISIBLE);
                    btnVerifyOtp.setVisibility(View.VISIBLE);
                    btnSendOtp.setVisibility(View.GONE);
                    
                    Toast.makeText(MainActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode(String code) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Verification Successful!", Toast.LENGTH_SHORT).show();
                        // Redirect to next activity if needed
                    } else {
                        Toast.makeText(MainActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}