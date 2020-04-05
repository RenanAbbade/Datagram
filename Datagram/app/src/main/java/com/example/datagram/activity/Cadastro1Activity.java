package com.example.datagram.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

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
        System.out.println("ON CREATE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro1);

        inicializarComponentes();

        //Cadastrar User
        progressBar.setVisibility(View.GONE);//Escondendo a ProgressBar

        botaoProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Nome = campoNome.getText().toString();
                String Senha = campoSenha.getText().toString();
                String Email = campoEmail.getText().toString();
                String DataNascimento = campoDataNasc.getText().toString();


                if(!Nome.isEmpty()){
                    if(!Email.isEmpty()){
                        if(!Senha.isEmpty()){
                            if(!DataNascimento.isEmpty()){
                                //Cadastro usuario
                                //if(tipoUsuario.equalsIgnoreCase("Pesquisador")){
                                    usuario = new Usuario(Nome,Senha,Email,DataNascimento);
                                //}
                                //else {
                                    //usuario = new Membro(Nome,Senha,Email,DataNascimento);
                                //}

                                try {
                                    cadastrarUsuario(usuario);
                                }catch (Exception e){
                                    e.getMessage();
                                    e.printStackTrace();
                                }


                            }else{
                                Toast.makeText(Cadastro1Activity.this, "Preencha a Data de Nascimento!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(Cadastro1Activity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
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

        }else if(radioNao.isPressed()){
            radioSim.setChecked(false);
            tipoUsuario = "Membro";
        }else{
            tipoUsuario = "Membro";
        }
    }


    //Método resp. por cadastrar user com email e senha
    public void cadastrarUsuario(final Usuario usuario){
        System.out.println("Método cadastrar");
        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(),
                usuario.getSenha()).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ //Este Listener refere-se a tratativa de erros referente a tentativa de cadastrar um usuario - Verifica se a task executou com sucesso

                            try{//Salvando dados do user no FIREBASE

                                progressBar.setVisibility(View.GONE);
                                //Pegando o id do usuário gerado pelo firebase
                                String idUsuario = task.getResult().getUser().getUid();
                                //Salvando novo usuário no firecloud
                                usuario.setId(idUsuario);

                                usuario.salvar();

                                //salvar dados no profile do firebase

                                UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                                Toast.makeText(Cadastro1Activity.this,
                                        "Cadastro com sucesso",
                                        Toast.LENGTH_SHORT).show();

                                //O usuário será enviado para o proximo passo.
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                finish();

                            }catch (Exception e){
                                e.printStackTrace();
                            }






                        }else{//Tratamento da excessão ao criar user/Cenário falha
                            progressBar.setVisibility(View.GONE);

                            String erroExcecao = "";

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                erroExcecao = "Digite uma senha mais forte!";
                            } catch (FirebaseAuthInvalidCredentialsException e){
                                erroExcecao = "Por facor, digite um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                erroExcecao = "Este e-mail já foi cadastrado";
                            }catch (Exception e){
                                erroExcecao = "Ao cadastrar o usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(Cadastro1Activity.this,
                                    "Erro: " + erroExcecao,
                                    Toast.LENGTH_SHORT).show();
                            //O erroExcecao será transmitido no PopUp Toast
                        }

                    }
                }
        )
        ;
    }

    public void inicializarComponentes(){


        radioSim = findViewById(R.id.radioButtonSim);
        radioNao = findViewById(R.id.radioButtonNao);

        campoNome = findViewById(R.id.editCadastroNome);
        campoSenha = findViewById(R.id.editCadastroSenha);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoDataNasc = findViewById((R.id.editCadastroDataNasc));
        botaoProximo = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);

        campoNome.requestFocus();

    }
}
