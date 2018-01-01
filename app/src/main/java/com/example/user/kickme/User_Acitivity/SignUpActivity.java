package com.example.user.kickme.User_Acitivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.kickme.MainActivity;
import com.example.user.kickme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/*
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
*/

/**
 * Created by poocoder on 11/16/17 and edited by smthls00 on 22/12/17.
 */

public class SignUpActivity extends Activity implements View.OnClickListener{
    EditText name, surname, email, password;
    String nameS, surnameS, emailS, passwordS;
    Double longitude, latitude;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference firebaseReference;
    FirebaseAuth.AuthStateListener mAuthListener;
    CheckBox checker;


    Button register;
    final static String TAG = "Login_Status";
    private static final int SIGNUP_SUCCESS = 0;

    //FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();


        name = findViewById(R.id.input_name);
        surname = findViewById(R.id.input_surname);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        register = findViewById(R.id.btn_signup);
        checker = findViewById(R.id.checkboxagree);




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    User userm = new User(nameS, surnameS, emailS, latitude, longitude);
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    firebaseReference = firebaseDatabase.getReference();

                    if(mAuth.getCurrentUser().getUid() != null) {
                        firebaseReference.child("user").child(mAuth.getCurrentUser().getUid()).setValue(userm);

                        Log.d("Firebase", "The account was created");
                    }else{
                        Toast.makeText(SignUpActivity.this, "Error while taking id.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        register.setOnClickListener(this);




        checker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                {
                   register.setVisibility(View.VISIBLE);
                }

                else
                {
                    register.setVisibility(View.INVISIBLE);
                }

            }
        });


    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_signup:
                if(!internet_connection()) {
                    Toast.makeText(getBaseContext(), "You have no internet connection, dude", Toast.LENGTH_SHORT).show();
                }
                else {
                    signIn();
                }
                break;
        }
    }


    public void signIn(){
        nameS = name.getText().toString();
        surnameS = surname.getText().toString();
        emailS = email.getText().toString();
        passwordS = password.getText().toString();
        latitude = null;
        longitude = null;

        if(validate())
        {
            mAuth.createUserWithEmailAndPassword(emailS, passwordS)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");

                            } else
                                {
                                // If sign in fails, display a message to the user.

                              //  Log.w(TAG, "createUserWithEmail:failure", task.getException());
                              //  Toast.makeText(SignUpActivity.this, "Authentication failed.",
                              //          Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            login();

        }
        else{
            onSignupFailed();
            Log.d(TAG, "Sign Up validation Fail");
        }
    }

    private void login(){

        mAuth.signInWithEmailAndPassword(emailS, passwordS)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            onSignupSuccess();

                            Toast.makeText(SignUpActivity.this, "Have Fun",
                                    Toast.LENGTH_SHORT).show();

                            // Start the Main Activity
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivityForResult(intent, SIGNUP_SUCCESS);
                            finish();
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        } else {
                            onSignupFailed();
                        }

                    }
                });
    }




    private void onSignupSuccess() {
        register.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), "login failed", Toast.LENGTH_LONG).show();

        register.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;


        if (nameS.isEmpty() || nameS.length() < 3) {
            name.setError("too short name");
            valid = false;
        } else {
            name.setError(null);
        }

        if (surnameS.isEmpty() || surnameS.length() < 3) {
            surname.setError("too short surname");
            valid = false;
        } else {
            surname.setError(null);
        }

        if (emailS.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailS).matches()) {
            email.setError("invalid email");
            valid = false;
        } else {
            email.setError(null);
        }

        if (passwordS.isEmpty() || passwordS.length() < 6 || passwordS.length() > 15) {
            password.setError("invalid password");
            valid = false;
        } else {
            password.setError(null);
        }


        return valid;
    }


    public boolean internet_connection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }
}
