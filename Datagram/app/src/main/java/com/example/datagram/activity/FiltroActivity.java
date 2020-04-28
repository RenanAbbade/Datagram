package com.example.datagram.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Postagem;
import com.example.datagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;


public class FiltroActivity extends AppCompatActivity {

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private String idUsuarioLogado;
    private TextInputEditText textDescricaoFiltro;
    private Usuario usuarioLogado;
    private ProgressBar progressBar;
    private boolean estaCarregando;

    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;

    private DataSnapshot seguidoresSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        //config inicias
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuarioPorID();
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");
        firebaseRef = ConfiguracaoFirebase.getFirebase();

        //inicializar componentes
        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);
        progressBar = findViewById(R.id.progressFiltro);

        //recuperar dados user logado
        recuperarDadosPostagem();

        //Configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Publicacao");
        toolbar.setTitleTextColor(Color.argb(255,65,105,225));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //recupera a img escolhida pelo user
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            byte[] dadoImagem = bundle.getByteArray("fotoEscolhida"); //recupero a imagem atraves da key definida no putExtra
            imagem = BitmapFactory.decodeByteArray(dadoImagem,0, dadoImagem.length); //comeco a decodificar do inicio ao fim da img
            imageFotoEscolhida.setImageBitmap(imagem);
        }
    }

    private void carregando(Boolean status){
        if(status){
            estaCarregando = true;
            progressBar.setVisibility(View.VISIBLE);
        }else{
            estaCarregando = false;
            progressBar.setVisibility(View.GONE);
        }

    }

    private void recuperarDadosPostagem(){
        carregando(true);
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usuarioLogado = dataSnapshot.getValue(Usuario.class);
                carregando(false);

                //recuperar seguidores
                final DatabaseReference seguidoresRef = firebaseRef
                        .child("seguidores")
                        .child(idUsuarioLogado);
                seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //iremos recuperar os seguidores
                        seguidoresSnapshot = dataSnapshot;

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void publicarPostagem(){
        if(estaCarregando){
            Toast.makeText(FiltroActivity.this,
                    "Carregando dados, aguarde!",
                    Toast.LENGTH_LONG).show();
        }else{

            final Postagem postagem = new Postagem();
            postagem.setIdUsuario(idUsuarioLogado);
            postagem.setDescricao(textDescricaoFiltro.getText().toString());

            //recuperar dados da img para o firebase
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
            byte [] dadosImagem = baos.toByteArray();

            //salvar img no storage
            StorageReference storageReference = ConfiguracaoFirebase.getFirebaseStorage();
            StorageReference imagemRef = storageReference.child("imagens").child("postagens").child(postagem.getId()+".jpeg");
            UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FiltroActivity.this,
                            "erro ao salvar imagem, tente novamente!",
                            Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //recuperar local da foto
                    Uri url = taskSnapshot.getDownloadUrl();
                    postagem.setCaminhoFoto(url.toString());

                    int qtdPostagem = usuarioLogado.getPostagens()+1;
                    usuarioLogado.setPostagens(qtdPostagem);
                    usuarioLogado.atualizarQtdPostagem();

                    //salvar postagem
                    if(postagem.salvar(seguidoresSnapshot)){
                        Toast.makeText(FiltroActivity.this,
                                "Sucesso ao salvar postagem ",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }
    }

    //irei inserir o "Publicar" no componente atual
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //verifico e o item do menu "Publicar" foi clicado
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_salvar_postagem :
                publicarPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
