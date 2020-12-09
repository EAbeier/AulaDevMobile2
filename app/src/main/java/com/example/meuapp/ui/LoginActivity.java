package com.example.meuapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meuapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "loginFIREBASE";
    EditText mEmail, mPassword;
    Button mLoginBtn;
    ProgressBar progressBar;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mEmail =  findViewById(R.id.inEmail);
        mPassword =  findViewById(R.id.inPass);
        mLoginBtn = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progress_bar_logar);
        mAuth = FirebaseAuth.getInstance();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String senha = mPassword.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email é necessário");
                    return;
                }
                if(TextUtils.isEmpty(senha)){
                    mPassword.setError("Senhas incompativeis ou em branco");
                    return;
                }

                if(senha.length()<6){
                    mPassword.setError("Senha precisa ter 6 ou mais caracteres");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logado Com Sucesso",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),ListaFilmesActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this, "Falha ao cadastrar!"
                                            +task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }


}