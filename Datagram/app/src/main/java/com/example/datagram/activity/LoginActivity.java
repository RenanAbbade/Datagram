package com.example.datagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;

    private Button botaoLogin;

    private ProgressBar progressBar;
    
    private Usuario usuario;

    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verificarUsuarioLogado();//Para o caso de o usuário ter fechado o app sem desconectar-se

        inicializarComponenetes();


        progressBar.setVisibility(View.GONE);
//Realiza logon do user
        botaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Email = campoEmail.getText().toString();

                String Senha = campoSenha.getText().toString();

                if(!Email.isEmpty())
                {
                    if(!Senha.isEmpty()){
                        
                        usuario = new Usuario();
                        
                        usuario.setEmail(Email);
                        
                        usuario.setSenha(Senha);
                        
                        validarLogin(usuario);

                    }else{
                        Toast.makeText(LoginActivity.this, "Preencha o campo senha!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Preencha o campo email!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(autenticacao.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    private void validarLogin(final Usuario usuario) {
        progressBar.setVisibility(View.VISIBLE);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    Toast.makeText(LoginActivity.this,
                            "Bem vindo ao DataGram, "+usuario.getEmail(),
                            Toast.LENGTH_SHORT).show();
                    finish();

                }else{
                    Toast.makeText(LoginActivity.this,
                            "Erro ao realizar login!",
                            Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    public void abrirCadastro(View view){

        Intent i= new Intent(LoginActivity.this, Cadastro1Activity.class);
        startActivity(i);
    }

    public void inicializarComponenetes(){

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
        botaoLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressLogin);

        campoEmail.requestFocus();
    }
}
