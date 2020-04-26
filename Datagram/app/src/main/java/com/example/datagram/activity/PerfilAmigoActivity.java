package com.example.datagram.activity;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.datagram.R;
import com.example.datagram.fragment.PerfilFragment;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference seguidoresRef;
    private ValueEventListener valueEventListenerPerfilAmigo;

    private String idUsuarioLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //configurcoes iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebase(); // <-- obtenho um objeto do banco
        usuariosRef = firebaseRef.child("usuarios"); // <-- acessa os users do banco
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuarioPorID();

        inicializaComponentes();

        //Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        toolbar.setTitleTextColor(Color.argb(255,65,105,225));
        setSupportActionBar(toolbar);

        //Configurando botão de voltar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //Recupera user selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");

            //Configurar nome do user na toolbar
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            addFotoPerfil();

            verificaSegueUserAmigo();
        }
    }

    private void inicializaComponentes(){
        imagePerfil = findViewById(R.id.imagePerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        textPublicacoes = findViewById(R.id.textViewNumPub);
        textSeguidores = findViewById(R.id.textViewNumSeguidores);
        textSeguindo = findViewById(R.id.textViewNumSeguindo);
        buttonAcaoPerfil.setText("Seguir");
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    private void verificaSegueUserAmigo(){
        DatabaseReference seguidorRef = seguidoresRef
                .child(idUsuarioLogado)
                .child(usuarioSelecionado.getId());
        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() { //forSingle recupera os dados apenas uma unica vez
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //verifica se existe dados dentro do userLogado se sim entao estou seguindo o usuarioSelecionado
                    habilitarBotaoSeguir(true);
                }else{
                    //nao esta seguindo
                    habilitarBotaoSeguir(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void habilitarBotaoSeguir(boolean segueUsuario){
        if(segueUsuario){
            buttonAcaoPerfil.setText(("Seguindo"));
        }else{
            buttonAcaoPerfil.setText(("Seguir"));
        }
    }

    //metodo executado sempre apos o onCreate no ciclo de vida de uma activity
    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosPerfilAmigo();
    }
    //caso o user saia da activity desativamos esta pagina(evento), economizando recursos do sistema
    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    private void recuperarDadosPerfilAmigo(){
        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId()); // <-- acesso o user por id, naquele que foi selecionado na busca
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
        new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class); // recupero nosso user
                String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());

                //configura valores recuperados, inserindo estes valores no perfil do usuario
                textPublicacoes.setText(postagens);
                textSeguindo.setText(seguindo);
                textSeguidores.setText(seguidores);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addFotoPerfil(){
        String caminhoFoto = usuarioSelecionado.getCaminhoFoto();
        if(caminhoFoto != null){
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(PerfilAmigoActivity.this)
                    .load(url)
                    .into(imagePerfil);
        }
    }
}
