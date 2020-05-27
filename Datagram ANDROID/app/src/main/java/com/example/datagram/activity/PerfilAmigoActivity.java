package com.example.datagram.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.datagram.R;
import com.example.datagram.adapter.AdapterGrid;
import com.example.datagram.fragment.PerfilFragment;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Postagem;
import com.example.datagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;

    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference postagensUsuarioRef;

    private ValueEventListener valueEventListenerPerfilAmigo;

    private String idUsuarioLogado;
    private List<Postagem> postagens;


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

            //Configurar referencia de postagens do usuario
            postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());

            //Configurar nome do user na toolbar
            getSupportActionBar().setTitle(usuarioSelecionado.getNome());

            addFotoPerfil();

            inicializarImageLoader();

            carregarFotosPostagem();

            //aqui saberemos qual dos itens de postagen foi selecionado
            gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Postagem postagem = postagens.get(position);
                    Intent i = new Intent(getApplicationContext(),VizualizarPostagemActivity.class);
                    i.putExtra("postagem",postagem);
                    i.putExtra("usuario",usuarioSelecionado);
                    startActivity(i);

                }
            });
        }
    }

    private void inicializaComponentes(){
        gridViewPerfil = findViewById(R.id.GridViewPerfil);
        imagePerfil = findViewById(R.id.imagePerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        textPublicacoes = findViewById(R.id.textViewNumPub);
        textSeguidores = findViewById(R.id.textViewNumSeguidores);
        textSeguindo = findViewById(R.id.textViewNumSeguindo);
        buttonAcaoPerfil.setText("Carregando");
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

    /*
    Instancia a UniversalImageLoader passando uma configuracao inicial Obrigatorio! para seu uso
    alem disso fazemos algumas configs a mais relacionado ao memorycache, para um carregamento mais rapido das img's
     */
    public void inicializarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
			.build();
        ImageLoader.getInstance().init(config);
    }

    public void carregarFotosPostagem(){
        postagens = new ArrayList<>();
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Configurar o tamanho do grid
                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels; // <-- recupero a largura que temos na tela pelo aparelho do user
                int tamanhoImagem = tamanhoGrid/3; // <-- divido por 3 pois na minha gridView foi definido 3 fotos por linha
                gridViewPerfil.setColumnWidth(tamanhoImagem);
                
                List<String> urlFotos = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    postagens.add(postagem); // <--array de postagens
                    urlFotos.add(postagem.getCaminhoFoto()); // <--array de urlfotos
                }

                int qtdPostagem = urlFotos.size();
                textPublicacoes.setText((String.valueOf(qtdPostagem)));

                //configurar adapter
                adapterGrid = new AdapterGrid(getApplicationContext(),R.layout.grid_postagem,urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarDadosUsuarioLogado(){
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //como precisamos de mais dados alem do que temos no usuariosRef, precisamos de um obj do tipo Usuario
                //para entao conseguir acesso aos dados de postagen,seguidor...
                usuarioLogado = dataSnapshot.getValue(Usuario.class);
                verificaSegueUserAmigo();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void verificaSegueUserAmigo(){
        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);
        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() { //forSingle recupera os dados apenas uma unica vez
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //verifica se existe dados dentro do userLogado se sim entao estou seguindo o usuarioSelecionado
                    habilitarBotaoSeguir(true);
                }else{
                    //nao esta seguindo
                    habilitarBotaoSeguir(false);
                    //add evento para seguir
                    buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            salvarSeguidor(usuarioLogado, usuarioSelecionado);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo){
        /*
        ESTRUTURA REFERENTE A LOGICA DE SEGUIR
        seguidores
        id_jamilton(amigoSEGUIDO)
            id_logado
                dados_LOGADO
         */
        HashMap<String,Object> dadosUserLogado = new HashMap<>();
        dadosUserLogado.put("nome",uLogado.getNome());
        dadosUserLogado.put("caminhoFoto",uLogado.getCaminhoFoto());

        DatabaseReference seguidorRef = seguidoresRef
                .child(uAmigo.getId())
                .child(uLogado.getId());
                seguidorRef.setValue(dadosUserLogado);

                //alterar botao para seguindo
                buttonAcaoPerfil.setText("Seguindo");
                //depois de seguir o user, o botao de seguir e desabilitado
                buttonAcaoPerfil.setOnClickListener(null);

                //incrementar seguindo do user logado
                int qtdSeguindo = uLogado.getSeguindo()+1;
                HashMap<String,Object> dadosSeguindo = new HashMap<>();
                dadosSeguindo.put("seguindo",qtdSeguindo);
                DatabaseReference usuarioSeguindo = usuariosRef.child(uLogado.getId());
                usuarioSeguindo.updateChildren(dadosSeguindo);

                //incrementar seguidores do amigos
                int qtdSeguidores = uAmigo.getSeguidores()+1;
                HashMap<String,Object> dadosSeguidores = new HashMap<>();
                dadosSeguidores.put("seguidores",qtdSeguidores);
                DatabaseReference usuarioSeguidores = usuariosRef.child(uAmigo.getId());
                usuarioSeguidores.updateChildren(dadosSeguidores);
    }

    private void habilitarBotaoSeguir(boolean segueUsuario){
        if(segueUsuario){
            buttonAcaoPerfil.setText(("Seguindo"));
        }else{
            buttonAcaoPerfil.setText(("Seguir"));
        }
    }

    //metodo executado sempre apos o onCreate no ciclo de vida de uma activity, com isso
    //mantemos os dados sempre o mais atualizado possivel.
    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosPerfilAmigo();
        recuperarDadosUsuarioLogado();

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
//                String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());

                //configura valores recuperados, inserindo estes valores no perfil do usuario
//                textPublicacoes.setText(postagens);
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
