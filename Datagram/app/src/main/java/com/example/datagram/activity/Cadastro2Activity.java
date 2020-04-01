package com.example.datagram.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.datagram.R;
import com.example.datagram.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;

public class Cadastro2Activity extends AppCompatActivity {

    private EditText campoNome, campoCPF, campoEmail, campoDataNasc;
    private RadioButton radioSim, radioNao;
    private Button botaoProximo;
    private ProgressBar progressBar;
    private Usuario usuario;
    //Obj autenticacao
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro2);

    }

    public void inicializarComponentes(){


        radioSim = findViewById(R.id.radioButtonSim);
        radioNao = findViewById(R.id.radioButtonNao);

        if(radioSim.isChecked()){
            radioNao.setChecked(false);
        }if(radioNao.isChecked()){
            radioSim.setChecked(false);
        }

        //campoNome = findViewById(R.id.);
        campoCPF = findViewById(R.id.editCadastroSenha);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoDataNasc = findViewById((R.id.editCadastroDataNasc));
        botaoProximo = findViewById(R.id.buttonCadastroProximo);
        progressBar = findViewById(R.id.progressBar);

    }
}
