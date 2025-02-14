package com.example.realestate.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.realestate.R;
import com.example.realestate.databinding.ActivityLoginPhoneBinding;
import com.example.realestate.utils.MyUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {
    private ActivityLoginPhoneBinding binding;
    private static final String TAG = "LOGIN_PHONE_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.phoneInputRl.setVisibility(VISIBLE);
        binding.otpInputRl.setVisibility(GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallBack();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(forceResendingToken);
            }
        });

        binding.verifyOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = binding.otpEt.getText().toString().trim();

                if (otp.isEmpty()){
                    binding.otpEt.setError("Enter OTP");
                    binding.otpEt.requestFocus();
                }else if (otp.length() < 6){
                    binding.otpEt.setError("OTP length must be 6 characters");
                    binding.otpEt.requestFocus();
                }else {
                    // data is valid, start verification
                    verifyPhoneNumberWithCode(otp);
                }
            }
        });

    }

    private String phoneCode = "",phoneNumber = "", phoneNumberWithCode = "";

    private void validateData(){
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode + phoneNumber;

        Log.d(TAG, "validateData: Phone Code: "+phoneCode);
        Log.d(TAG, "validateData: Phone Number: "+phoneNumber);
        Log.d(TAG, "validateData: Phone Number with Code: "+phoneNumberWithCode);

        if (phoneCode.isEmpty()){
            binding.phoneNumberEt.setError("Enter Phone Number");
            binding.phoneNumberEt.requestFocus();
        }else {
            startPhoneNumberVerification();
        }

    }

    private void startPhoneNumberVerification(){

        progressDialog.setMessage("Sending OTP to "+phoneNumberWithCode);
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        // Setup Phone Auth Options with phone number, time out, callback etc.
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .build();

        // Start phone verification with PhoneAuthOptions
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token){
        progressDialog.setMessage("Resending OTP to "+phoneNumberWithCode);
        progressDialog.show();

        // Setup Phone Auth Options with phone number, time out, callback etc.
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .setForceResendingToken(token)
                .build();

        // Start phone verification with PhoneAuthOptions
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String otp){

        Log.d(TAG, "verifyPhoneNumberWithCode: OTP: "+otp);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,otp);
        signInWithPhoneAuthCredential(credential);

    }

    private void phoneLoginCallBack(){
        Log.d(TAG, "phoneLoginCallBack: ");
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                Log.d(TAG, "onCodeSent: ");

                mVerificationId = verificationId;
                forceResendingToken = token;

                progressDialog.dismiss();

                binding.phoneInputRl.setVisibility(GONE);

                binding.otpInputRl.setVisibility(VISIBLE);

                MyUtils.toast(LoginPhoneActivity.this,"OTP sent to "+phoneNumberWithCode);

                binding.loginPhoneLabelTv.setText("PLease type verification code sent to "+phoneNumberWithCode);

            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: ");
                signInWithPhoneAuthCredential( phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: "+e.getMessage());

                progressDialog.dismiss();
                MyUtils.toast(LoginPhoneActivity.this,"FAiled to verify due to "+e.getMessage());


            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){

        Log.d(TAG, "signInWithPhoneAuthCredential: ");

        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: ");
                        // SignIn Success, lets check if the user is new (New Account Register) or existing (Existing Login)
                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            Log.d(TAG, "onSuccess: New User, Account created...");
                            // New User, Account created. Lets save user info to firebase realtime database
                            updateUserInfo();
                            
                        }else {
                            Log.d(TAG, "onSuccess: Existing User Logged In...");
                            startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e );
                        progressDialog.dismiss();
                        MyUtils.toast(LoginPhoneActivity.this,"Login failed due to "+e.getMessage());
                    }
                });

    }

    private void updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ");

        progressDialog.setMessage("Saving User Info...");
        progressDialog.show();

        long timestamp = MyUtils.timestamp();
        String registeredUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",registeredUserUid);
        hashMap.put("email","");
        hashMap.put("name","");
        hashMap.put("timestamp",timestamp);
        hashMap.put("phoneCode",""+phoneCode);
        hashMap.put("phoneNumber",""+phoneNumber);
        hashMap.put("profileImageUrl","");
        hashMap.put("dob","");
        hashMap.put("userType",""+MyUtils.USER_TYPE_PHONE);
        hashMap.put("token","");


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registeredUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: User info saved...");
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(LoginPhoneActivity.this,"Failed to save due to "+e.getMessage());
                    }
                });

    }

}