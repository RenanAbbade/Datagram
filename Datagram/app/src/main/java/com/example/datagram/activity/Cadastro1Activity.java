package com.example.datagram.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.model.Membro;
import com.example.datagram.model.Pesquisador;
import com.example.datagram.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;

public class Cadastro1Activity extends AppCompatActivity {

    private EditText campoNome, campoSenha, campoEmail, campoDataNasc;
    private RadioButton radioSim, radioNao;
    private Button botaoProximo;
    private ProgressBar progressBar;
    private Usuario usuario;
    private String tipoUsuario;
    //Obj autenticacao
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro1);

        inicializarComponentes();

        //Cadastrar User
        progressBar.setVisibility(View.GONE);//Escondendo a ProgressBar
        botaoProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);//Quando o user clicar no cadastrar, a progressbar aparecera
                String Nome = campoNome.getText().toString();
                String CPF = campoSenha.getText().toString();
                String Email = campoEmail.getText().toString();
                String DataNascimento = campoDataNasc.getText().toString();


                if(!Nome.isEmpty()){
                    if(!Email.isEmpty()){
                        if(!CPF.isEmpty()){
                            if(!DataNascimento.isEmpty()){
                                //Cadastro usuario
                                if(tipoUsuario.equalsIgnoreCase("Pesquisador")){
                                    usuario = new Pesquisador(Nome,CPF,Email,DataNascimento);
                                }
                                else {
                                    usuario = new Membro(Nome,CPF,Email,DataNascimento);
                                }
                                cadastrarUsuario(usuario);

                            }else{
                                Toast.makeText(Cadastro1Activity.this, "Preencha a Data de Nascimento!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(Cadastro1Activity.this, "Preencha o CPF!", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(Cadastro1Activity.this, "Preencha o Email!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Cadastro1Activity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void checkButton(View v){
        if(radioSim.isPressed()){
            radioNao.setChecked(false);
            tipoUsuario = "Pesquisador";

        }if(radioNao.isPressed()){
            radioSim.setChecked(false);
            tipoUsuario = "Membro";
        }
    }


    //Método resp. por cadastrar user com email e senha
    public void cadastrarUsuario(Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //autenticacao.createUserWithEmailAndPassword(usuario.getEmail());

    }

    public void inicializarComponentes(){


        radioSim = findViewById(R.id.radioButtonSim);
        radioNao = findViewById(R.id.radioButtonNao);

        campoNome = findViewById(R.id.editCadastroNome);
        campoSenha = findViewById(R.id.editCadastroSenha);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoDataNasc = findViewById((R.id.editCadastroDataNasc));
        botaoProximo = findViewById(R.id.buttonCadastroProximo);
        progressBar = findViewById(R.id.progressBar);

    }
}
