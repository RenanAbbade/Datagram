package com.example.datagram.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.helper.ValidaCPF;
import com.example.datagram.helper.ValidaIdade;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Cadastro2Activity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText campoNome, campoSenha, campoEmail, campoDataNasc; //user comum
    private RadioButton radioSim, radioNao;//Verificador de membro ou pesquisador
    private EditText campoDataInicioTrabalho, campoInstituicao, campoFormacao, campoLinkCv; Spinner spinnerCampoUF,spinnerCampoMunicipio; String pesquisadorUF,pesquisadorMunicipio;//pesquisador
    private EditText  campoCpf; Spinner spinnerEscolaridade; String membroEscolaridade;//Membro
    private Button botaoFinalizarCadastro;
    private ProgressBar progressBar02;

    //Aux
    private String tipoUsuario = "Membro";
    DatePickerDialog.OnDateSetListener setDataNascListener, setDataTrabListener;


    //Obj autenticacao
    private FirebaseAuth autenticacao;

    //private DatabaseReference usuarioRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro02);

        inicializarComponentes();

        CriacaoSpinnerFormacao(spinnerEscolaridade,tipoUsuario);
        //campoEstado
        UfApiTask UfService = new UfApiTask();
        //Chamando API do IBGE para consulta de estado/municipio
        UfService.execute();

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
                    String Formacao = membroEscolaridade;
                    String LinkCv = campoLinkCv.getText().toString();
                    String Estado = pesquisadorUF;
                    String Municipio = pesquisadorMunicipio;

                    if(!Nome.isEmpty()) {
                        if(!Senha.isEmpty()) {
                            if(!Email.isEmpty()){
                                if(!DataNascimento.isEmpty()){
                                    if(ValidaIdade.isGreatherThan18(DataNascimento)){
                                        if(ValidaIdade.isGreatherThan18(DataNascimento,DataInicioTrabalho)){
                                            if(!Instituicao.isEmpty()){
                                                if(!Formacao.equalsIgnoreCase("Escolaridade")){
                                                    if(!LinkCv.isEmpty()){
                                                        if(!Estado.equalsIgnoreCase("UF")){
                                                            if(!Municipio.equalsIgnoreCase("Municipio")){

                                                                Pesquisador pesquisador = new Pesquisador(Nome,Senha,Email,DataNascimento,DataInicioTrabalho,Estado,Municipio,Instituicao,Formacao,LinkCv);

                                                                try {
                                                                    cadastrarUsuario(pesquisador);

                                                                }catch (Exception e){
                                                                    e.getMessage();
                                                                    e.printStackTrace();
                                                                }
                                                            }else{
                                                                Toast.makeText(Cadastro2Activity.this, "Escolha um municipio válido!", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }else{
                                                            Toast.makeText(Cadastro2Activity.this, "Escolha um UF válido!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }else{
                                                        Toast.makeText(Cadastro2Activity.this, "Preencha o Link do CV!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    Toast.makeText(Cadastro2Activity.this, "Escolha uma formação válida!", Toast.LENGTH_SHORT).show();
                                                }

                                            }else{
                                                Toast.makeText(Cadastro2Activity.this, "Preencha o nome da instituição", Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            Toast.makeText(Cadastro2Activity.this, "Preencha a data em que iniciou os trabalhos com Data Science corretamente", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(Cadastro2Activity.this, "É necessário ter mais de 18 anos para inscrever-se!", Toast.LENGTH_SHORT).show();
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
                    String Escolaridade = membroEscolaridade;
                    String CPF = campoCpf.getText().toString();

                    if (!Nome.isEmpty()) {
                        if (!Senha.isEmpty()) {
                            if (!Email.isEmpty()) {
                                if (!DataNascimento.isEmpty()) {
                                    if(ValidaIdade.isGreatherThan18(DataNascimento)){
                                        if(!Escolaridade.equalsIgnoreCase("Escolaridade")){
                                            if(!CPF.isEmpty()){
                                                //if(!CPF.contains(".")){
                                                    if(ValidaCPF.isCPF(CPF)){
                                                        Membro membro = new Membro(Nome,Senha,Email,DataNascimento,Escolaridade,CPF);

                                                        try {
                                                            campoCpf.setText(ValidaCPF.imprimeCPF(CPF));
                                                            cadastrarUsuario(membro);

                                                        }catch (Exception e){
                                                            e.getMessage();
                                                            e.printStackTrace();
                                                        }

                                                    }else {
                                                        Toast.makeText(Cadastro2Activity.this, "Digite um CPF válido!", Toast.LENGTH_SHORT).show();
                                                    }
                                               // }else {
                                                   // Toast.makeText(Cadastro2Activity.this, "Preencha o campo CPF sem os pontos!", Toast.LENGTH_SHORT).show();
                                                //}
                                            }else {
                                                Toast.makeText(Cadastro2Activity.this, "Preencha o campo CPF!", Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            Toast.makeText(Cadastro2Activity.this, "Preencha o campo Escolaridade!", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(Cadastro2Activity.this, "É necessário ter mais de 18 anos para inscrever-se!", Toast.LENGTH_SHORT).show();
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
            //campoFormacao.setVisibility(View.VISIBLE);
            campoLinkCv.setVisibility(View.VISIBLE);
            //Colocando os campos de membro como invisiveis, caso o usuário mude de ideia.
            spinnerEscolaridade.setVisibility(View.VISIBLE);
            campoCpf.setVisibility(View.GONE);
            spinnerCampoUF.setVisibility(View.VISIBLE);
            spinnerCampoMunicipio.setVisibility(View.VISIBLE);
            CriacaoSpinnerFormacao(spinnerEscolaridade,tipoUsuario);

        }else if(radioNao.isPressed()){
            radioSim.setChecked(false);
            tipoUsuario = "Membro";
            //Colocando os campos de membro visiveis
            spinnerEscolaridade.setVisibility(View.VISIBLE);
            campoCpf.setVisibility(View.VISIBLE);
            //Colocando os campos de Pesquisador como invisiveis, caso o usuário mude de ideia.
            campoDataInicioTrabalho.setVisibility(View.GONE);
            campoInstituicao.setVisibility(View.GONE);
            campoFormacao.setVisibility(View.GONE);
            campoLinkCv.setVisibility(View.GONE);
            spinnerCampoUF.setVisibility(View.GONE);
            spinnerCampoMunicipio.setVisibility(View.GONE);
            CriacaoSpinnerFormacao(spinnerEscolaridade,tipoUsuario);

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
            spinnerCampoUF = findViewById(R.id.editCadastro02Estado);
            spinnerCampoMunicipio = findViewById(R.id.editCadastro02Municipio);

            //Membro
            campoCpf = findViewById(R.id.editCadastroCPF);
            spinnerEscolaridade = findViewById(R.id.editCadastroEscolaridade);


            //AcessoriosTela
            botaoFinalizarCadastro = findViewById(R.id.buttonCadastro02Finalizar);
            progressBar02 = findViewById(R.id.progressBar02);

            campoNome.requestFocus();

        }

        public void validaDataNasc(View v){

            Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            campoDataNasc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog datePickerDialog;
                    datePickerDialog = new DatePickerDialog(
                            Cadastro2Activity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, setDataNascListener, year, month, day);
                    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    datePickerDialog.show();
                }
            });

            setDataNascListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    month = month +1;
                    String date = day+"/"+month+"/"+year;
                    campoDataNasc.setText(date);
                }
            };
        }

    public void validaDataTrab(View v){

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        campoDataInicioTrabalho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog;
                datePickerDialog = new DatePickerDialog(
                        Cadastro2Activity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, setDataTrabListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        setDataTrabListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month +1;
                String date = day+"/"+month+"/"+year;
                campoDataInicioTrabalho.setText(date);
            }
        };
    }

        //Reusar para lista de estados/uf
     public void CriacaoSpinnerFormacao(Spinner spinner, String tipoUsuario){
         List<String> Formacoes;
        if(tipoUsuario.equalsIgnoreCase("Pesquisador")){
            Formacoes = new ArrayList<String>(){
                {
                    add("Escolaridade");
                    add("Doutorado/Pós-Doutorado(PhD)");
                    add("Mestrado (Msc)");
                    add("Graduação");
                }
            };
        }else{
            Formacoes = new ArrayList<String>(){
                {
                    add("Escolaridade");
                    add("Doutorado/Pós-Doutorado(PhD)");
                    add("Mestrado (Msc)");
                    add("Graduação");
                    add("Ensino Médio");
                    add("Ensino Fundamental");
                    add("Não frequentei");
                }
        };}

         ArrayAdapter<String> spinnerEscolaridadeMembro = new ArrayAdapter<String>(this, R.layout.spinner_item,Formacoes);
         spinnerEscolaridadeMembro.setDropDownViewResource(R.layout.spinner_dropdown_item);
         spinner.setAdapter(spinnerEscolaridadeMembro);
         spinner.setOnItemSelectedListener(this);
     }

    public void CriacaoSpinnerUF(Spinner spinner, List<String> s){
        s.add(0,"UF");
        ArrayAdapter<String> spinnerUfs = new ArrayAdapter<String>(this, R.layout.spinner_item,s);
        spinnerUfs.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerUfs);
        spinner.setOnItemSelectedListener(this);
    }

    public void CriacaoSpinnerMunicipio(Spinner spinner, List<String> s){
        s.add(0,"Municipio");
        ArrayAdapter<String> spinnerMunicipio = new ArrayAdapter<String>(this, R.layout.spinner_item,s);
        spinnerMunicipio.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerMunicipio);
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getItemAtPosition(0).toString() == "Escolaridade"){
            membroEscolaridade = parent.getItemAtPosition(position).toString();
            if(!membroEscolaridade.equalsIgnoreCase("Escolaridade"))
                Toast.makeText(parent.getContext(),membroEscolaridade,Toast.LENGTH_SHORT).show();

        }if(parent.getItemAtPosition(0).toString() == "UF"){
            pesquisadorUF = parent.getItemAtPosition(position).toString();
            if(!pesquisadorUF.equalsIgnoreCase("UF")){
                Toast.makeText(parent.getContext(),pesquisadorUF,Toast.LENGTH_SHORT).show();
            //A api de escolha de municipio só é consumida se um UF válido é escolhido
            MunicipioApiTask municipioApiTask = new MunicipioApiTask();
            municipioApiTask.execute("http://ibge.herokuapp.com/municipio/?val="+pesquisadorUF);
            }


        }if (parent.getItemAtPosition(0).toString() == "Municipio"){
            pesquisadorMunicipio = parent.getItemAtPosition(position).toString();
            if(!pesquisadorMunicipio.equalsIgnoreCase("Municipio") )
                Toast.makeText(parent.getContext(),pesquisadorMunicipio,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if(parent.getItemAtPosition(0).toString() == "Escolaridade"){
            membroEscolaridade = parent.getItemAtPosition(0).toString();
        }if(parent.getItemAtPosition(0).toString() == "UF"){
            pesquisadorUF = parent.getItemAtPosition(0).toString();
        }if (parent.getItemAtPosition(0).toString() == "Municipio"){
            pesquisadorMunicipio = parent.getItemAtPosition(0).toString();
        }
    }

    class UfApiTask extends AsyncTask<Void, Void, List<String> > {//Recebo void pois já tenho o atribulo da URL como estatico, o processamento é void, e retorno String
        //Poderia receber atributos pelo método execute -> UfApiService.execute(URL)

        private String URL_WS = "http://ibge.herokuapp.com/estado/UF";
        public  List<String> ListOfUfs = new ArrayList<String>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            //Pego a URL passada no parametro de chamada da API
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            StringBuffer stringBuffer = null;
            ArrayList<String> ufList = new ArrayList<String>();

            try {
                URL url = new URL(URL_WS);//Cast de String para URL
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();//Cria a requisicao //O tipo HttpUrlConnection permite que recuperemos os

                inputStream = conexao.getInputStream();//Recuperando os dados devolvidos pelo servidor em bytes[]

                inputStreamReader = new InputStreamReader(inputStream);//Le os dados em bytes e decodifica para caracteres

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//Leitura dos caracteres decodificados pelo InputStreamReader

                String linha = "";

                stringBuffer = new StringBuffer();

                while (( linha = bufferedReader.readLine()) != null ){//readLine lerá todas as linhas do bufferedReader, quando acabar retorna null
                    stringBuffer.append(linha);
                }

                Map<String, String> retMap = new Gson().fromJson(stringBuffer.toString(), new TypeToken<HashMap<String, String>>() {}.getType());

                for (String sigla_key : retMap.keySet()) {
                    //String codigo_value = retMap.get(sigla_key);
                    //Adiciona a uf a um list
                    ufList.add(sigla_key);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //return ufList;
            return ufList;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            super.onPostExecute(s);

            Collections.sort(s);

            CriacaoSpinnerUF(spinnerCampoUF, s);
        }
    }

    class MunicipioApiTask extends AsyncTask<String, Void, List<String> > {//Recebo void pois já tenho o atribulo da URL como estatico, o processamento é void, e retorno String
        //Poderia receber atributos pelo método execute -> UfApiService.execute(URL)

        private String URL_WS = "http://ibge.herokuapp.com/estado/UF";
        public  List<String> ListOfUfs = new ArrayList<String>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            //Pego a URL passada no parametro de chamada da API
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            StringBuffer stringBuffer = null;
            ArrayList<String> MunicipioList = new ArrayList<String>();
            String url_municipios = strings[0];//Para pegar o parametro passado no .execute()

            try {
                URL url = new URL(url_municipios);//Cast de String para URL
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();//Cria a requisicao //O tipo HttpUrlConnection permite que recuperemos os

                inputStream = conexao.getInputStream();//Recuperando os dados devolvidos pelo servidor em bytes[]

                inputStreamReader = new InputStreamReader(inputStream);//Le os dados em bytes e decodifica para caracteres

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//Leitura dos caracteres decodificados pelo InputStreamReader

                String linha = "";

                stringBuffer = new StringBuffer();

                while (( linha = bufferedReader.readLine()) != null ){//readLine lerá todas as linhas do bufferedReader, quando acabar retorna null
                    stringBuffer.append(linha);
                }

                Map<String, String> retMap = new Gson().fromJson(stringBuffer.toString(), new TypeToken<HashMap<String, String>>() {}.getType());

                for (String sigla_key : retMap.keySet()) {
                    //String codigo_value = retMap.get(sigla_key);
                    //Adiciona o municipio a um list
                    MunicipioList.add(sigla_key);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //return ufList;
            return MunicipioList;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            super.onPostExecute(s);

            Collections.sort(s);

            CriacaoSpinnerMunicipio(spinnerCampoMunicipio, s);
        }
    }


}
