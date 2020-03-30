package com.example.datagram.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.datagram.R;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    public void abrirCadastro(View view){

        Intent i= new Intent(LoginActivity.this, Cadastro1Activity.class);
        startActivity(i);


    }
}
