package com.example.it_webshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();

    EditText registerUserNameET;
    EditText registerEmailET;
    EditText registerPasswordET;
    EditText registerPasswordAgainET;
    RadioGroup accountTypeRG;

    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mauth = FirebaseAuth.getInstance();

        registerUserNameET = findViewById(R.id.usernameRegisterEditText);
        registerEmailET = findViewById(R.id.emailRegisterEditText);
        registerPasswordET = findViewById(R.id.passwordRegisterEditText);
        registerPasswordAgainET = findViewById(R.id.passwordAgainRegisterEditText);
        accountTypeRG = findViewById(R.id.accountTypeGroup);
    }

    public void register(View view) {

        String userName = registerUserNameET.getText().toString();
        String email = registerEmailET.getText().toString();
        String password = registerPasswordET.getText().toString();
        String passwordAgain = registerPasswordAgainET.getText().toString();

        if (!password.equals(passwordAgain)){
            Log.e(LOG_TAG, "Két jelszó nem egyezik meg!");
            Toast.makeText(this, "Két jelszó nem egyezik meg", Toast.LENGTH_SHORT).show();
            return;
        }

        int accountTypeId = accountTypeRG.getCheckedRadioButtonId();
        View radioButton = accountTypeRG.findViewById(accountTypeId);
        int id = accountTypeRG.indexOfChild(radioButton);
        String accountTypeString =  ((RadioButton) accountTypeRG.getChildAt(id)).getText().toString();

        mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(LOG_TAG, "Sikeres regisztráció");
                    goLogin();
                }else{
                    Log.d(LOG_TAG, "Sikertelen regisztráció:", task.getException());
                    if (task.getException().getClass() == FirebaseAuthWeakPasswordException.class){
                        Toast.makeText(RegisterActivity.this, "Jelszó legyen legalább 6 karakter hosszú", Toast.LENGTH_LONG).show();
                    } else if (task.getException().getClass() == FirebaseAuthInvalidCredentialsException.class) {
                        Toast.makeText(RegisterActivity.this, "Helytelen Email cím", Toast.LENGTH_LONG).show();
                    } else{
                        Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void goLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}