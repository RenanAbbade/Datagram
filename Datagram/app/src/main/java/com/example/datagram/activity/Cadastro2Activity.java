package com.example.datagram.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Membro;
import com.example.datagram.model.Pesquisador;
import com.example.datagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import com.google.firebase.database.DatabaseReference;

import org.w3c.dom.CDATASection;

import java.util.Calendar;


public class Cadastro2Activity extends AppCompatActivity {

    private EditText campoNome, campoSenha, campoEmail, campoDataNasc; //user comum
    private RadioButton radioSim, radioNao;//Verificador de membro ou pesquisador
    private EditText campoDataInicioTrabalho, campoInstituicao, campoFormacao, campoLinkCv, campoEstado; //pesquisador
    private EditText campoEscolaridade, campoCpf; //Membro
    private Button botaoFinalizarCadastro;
    private ProgressBar progressBar02;
    private Usuario usuario;
    private DatabaseReference usuariosRef;
    private String tipoUsuario = "Membro";
    public  EditText campoData;
    DatePickerDialog.OnDateSetListener setListener;
    //Obj autenticacao
    private FirebaseAuth autenticacao;

    //private DatabaseReference usuarioRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro02);

        inicializarComponentes();
        progressBar02.setVisibility(View.GONE);


        botaoFinalizarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Nome = campoNome.getText().toString();
                String Senha = campoSenha.getText().toString();
                String Email = campoEmail.getText().toString();
                String DataNascimento = campoDataNasc.getText().toString();

                if(tipoUsuario.equalsIgnoreCase("Pesquisador")){
                    String DataInicioTrabalho = campoDataInicioTrabalho.getText().toString();
                    String Instituicao = campoInstituicao.getText().toString();
                    String Formacao = campoFormacao.getText().toString();
                    String LinkCv = campoLinkCv.getText().toString();
                    String Estado = campoEstado.getText().toString();

                    if(!Nome.isEmpty()) {
                        if(!Senha.isEmpty()) {
                            if(!Email.isEmpty()){
                                if(!DataNascimento.isEmpty()){
                                    if(!DataInicioTrabalho.isEmpty()){
                                        if(!Instituicao.isEmpty()){
                                            if(!Formacao.isEmpty()){
                                                if(!LinkCv.isEmpty()){
                                                    if(!Estado.isEmpty()){

                                                        Pesquisador pesquisador = new Pesquisador(Nome,Senha,Email,DataNascimento,DataInicioTrabalho,Estado,Instituicao,Formacao,LinkCv);

                                                        try {
                                                            cadastrarUsuario(pesquisador);

                                                        }catch (Exception e){
                                                            e.getMessage();
                                                            e.printStackTrace();
                                                        }

                                                    }else{
                                                        Toast.makeText(Cadastro2Activity.this, "Preencha o campo Estado/Cidade!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    Toast.makeText(Cadastro2Activity.this, "Preencha o Link do CV!", Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                Toast.makeText(Cadastro2Activity.this, "Preencha o campo Formação!", Toast.LENGTH_SHORT).show();
                                            }

                                        }else{
                                            Toast.makeText(Cadastro2Activity.this, "Preencha o nome da instituição", Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        Toast.makeText(Cadastro2Activity.this, "Preencha a data em que iniciou os trabalhos com Data Science", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                Toast.makeText(Cadastro2Activity.this, "Preencha a Data de Nascimento!", Toast.LENGTH_SHORT).show();
                            }
                            }else{
                                Toast.makeText(Cadastro2Activity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
                            }
                        }else
                            {
                            Toast.makeText(Cadastro2Activity.this, "Preencha a Senha!", Toast.LENGTH_SHORT).show();
                            }
                    }else{
                        Toast.makeText(Cadastro2Activity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                    }



                }else {
                    String Escolaridade = campoEscolaridade.getText().toString();
                    String CPF = campoCpf.getText().toString();

                    if (!Nome.isEmpty()) {
                        if (!Senha.isEmpty()) {
                            if (!Email.isEmpty()) {
                                if (!DataNascimento.isEmpty()) {
                                    if(!Escolaridade.isEmpty()){
                                        if(!CPF.isEmpty()){
                                            Membro membro = new Membro(Nome,Senha,Email,DataNascimento,Escolaridade,CPF);

                                            try {
                                                cadastrarUsuario(membro);

                                            }catch (Exception e){
                                                e.getMessage();
                                                e.printStackTrace();
                                            }

                                        }else {
                                            Toast.makeText(Cadastro2Activity.this, "Preencha o campo CPF!", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(Cadastro2Activity.this, "Preencha o campo Escolaridade!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Cadastro2Activity.this, "Preencha a Data de Nascimento!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(Cadastro2Activity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Cadastro2Activity.this, "Preencha a Senha!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Cadastro2Activity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    public void cadastrarUsuario(final Usuario usuario){

        progressBar02.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(),
                usuario.getSenha()).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ //Este Listener refere-se a tratativa de erros referente a tentativa de cadastrar um usuario - Verifica se a task executou com sucesso

                            try{//Salvando dados do user no FIREBASE

                                progressBar02.setVisibility(View.GONE);
                                //Pegando o id do usuário gerado pelo firebase
                                String idUsuario = task.getResult().getUser().getUid();
                                //Salvando novo usuário no firecloud
                                usuario.setId(idUsuario);

                                usuario.salvar();

                                UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                                Toast.makeText(Cadastro2Activity.this,
                                        "Cadastro com sucesso",
                                        Toast.LENGTH_SHORT).show();

                                //No cenário de sucesso, o usuário será enviado para o menu da aplicação.

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));

                                finish();

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{//Tratamento da excessão ao criar user/Cenário falha
                            progressBar02.setVisibility(View.GONE);

                            String erroExcecao = "";

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                erroExcecao = "Digite uma senha mais forte!";
                            } catch (FirebaseAuthInvalidCredentialsException e){
                                erroExcecao = "Por favor, digite um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                erroExcecao = "Este e-mail já foi cadastrado";
                            }catch (Exception e){
                                erroExcecao = "Ao cadastrar o usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(Cadastro2Activity.this,
                                    "Erro: " + erroExcecao,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void checkButton(View v){
        if(radioSim.isPressed()){
            radioNao.setChecked(false);
            tipoUsuario = "Pesquisador";
            //Colocando os campos de pesquisador visiveis.
            campoDataInicioTrabalho.setVisibility(View.VISIBLE);
            campoInstituicao.setVisibility(View.VISIBLE);
            campoFormacao.setVisibility(View.VISIBLE);
            campoLinkCv.setVisibility(View.VISIBLE);
            campoEstado.setVisibility(View.VISIBLE);
            //Colocando os campos de membro como invisiveis, caso o usuário mude de ideia.
            campoEscolaridade.setVisibility(View.GONE);
            campoCpf.setVisibility(View.GONE);

        }else if(radioNao.isPressed()){
            radioSim.setChecked(false);
            tipoUsuario = "Membro";
            //Colocando os campos de membro visiveis
            campoEscolaridade.setVisibility(View.VISIBLE);
            campoCpf.setVisibility(View.VISIBLE);
            //Colocando os campos de Pesquisador como invisiveis, caso o usuário mude de ideia.
            campoDataInicioTrabalho.setVisibility(View.GONE);
            campoInstituicao.setVisibility(View.GONE);
            campoFormacao.setVisibility(View.GONE);
            campoLinkCv.setVisibility(View.GONE);
            campoEstado.setVisibility(View.GONE);

        }
    }
        private void inicializarComponentes () {
            //radio
            radioNao = findViewById(R.id.radioButtonCadastroNao);
            radioSim = findViewById(R.id.radioButtonCadastroSim);

            //usuário comum - abstração
            campoNome = findViewById(R.id.editCadastroNome);
            campoSenha = findViewById(R.id.editCadastroSenha);
            campoDataNasc = findViewById(R.id.editCadastroDataNasc);
            campoEmail = findViewById(R.id.editCadastroEmail);

            //Pesquisador
            campoDataInicioTrabalho = findViewById(R.id.editCadastro02DataTrabalho);
            campoInstituicao = findViewById(R.id.editCadastro02Instituicao);
            campoFormacao = findViewById(R.id.editCadastro02Formacao);
            campoLinkCv = findViewById(R.id.editCadastro02CV);
            campoEstado = findViewById(R.id.editCadastro02Estado);

            //Membro
            campoCpf = findViewById(R.id.editCadastroCPF);
            campoEscolaridade = findViewById(R.id.editCadastroEscolaridade);

            //Acessorios tela
            botaoFinalizarCadastro = findViewById(R.id.buttonCadastro02Finalizar);
            progressBar02 = findViewById(R.id.progressBar02);

            campoNome.requestFocus();
        }

        public void validaData(View v){
            campoData = campoDataNasc;

            if(campoDataInicioTrabalho.isPressed()){
                campoData = campoDataInicioTrabalho;
            }

            Calendar calendar = Calendar.getInstance();

            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            campoData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            Cadastro2Activity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,setListener, year,month,day);
                    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    datePickerDialog.show();
                }
            });


            setListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    month = month +1;
                    String date = day+"/"+month+"/"+year;
                    campoData.setText(date);
                }
            };

        }

}
