package com.example.datagram.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.datagram.R;
import com.example.datagram.activity.EditarPerfilActivity;
import com.example.datagram.activity.PerfilAmigoActivity;
import com.example.datagram.adapter.AdapterGrid;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Postagem;
import com.example.datagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {
    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    private GridView gridViewPerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private Usuario usuarioLogado;
    private String identificadorUsuario;

    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private DatabaseReference postagensUsuarioRef;
    private StorageReference storageRef;

    private AdapterGrid adapterGrid;
    private ValueEventListener valueEventListenerPerfil;



    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_perfil, container, false);
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //configurcoes iniciais
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase(); // <-- obtenho um objeto do banco
        usuariosRef = firebaseRef.child("usuarios"); // <-- acessa os users do banco

        //Configurar referencia de postagens do usuario
        postagensUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());

        //initi componente
        inicializaComponentes(view);

        //config foto de perfil
        addFotoPerfil();

        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(intent);
            }
        });

        //INITI imageLoader
        inicializarImageLoader();

        //carega fotos post
        carregarFotosPostagem();


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarDadosUsuarioLogado();

    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }

    public void inicializarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void inicializaComponentes(View view){
        gridViewPerfil = view.findViewById(R.id.GridViewPerfil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);
        textPublicacoes = view.findViewById(R.id.textViewNumPub);
        textSeguidores = view.findViewById(R.id.textViewNumSeguidores);
        textSeguindo = view.findViewById(R.id.textViewNumSeguindo);
        buttonAcaoPerfil.setText("Editar Perfil");
    }

    public void carregarFotosPostagem(){
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
                    urlFotos.add(postagem.getCaminhoFoto());
                }

                int qtdPostagem = urlFotos.size();
                textPublicacoes.setText((String.valueOf(qtdPostagem)));

                //configurar adapter
                adapterGrid = new AdapterGrid(getActivity(),R.layout.grid_postagem,urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addFotoPerfil(){
        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if(caminhoFoto != null){
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(PerfilFragment.this)
                    .load(url)
                    .into(imagePerfil);
        }
    }

    private void recuperarDadosUsuarioLogado(){
        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId()); // <-- acesso o user por id, naquele que foi selecionado na busca
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(
                new ValueEventListener(){
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class); // recupero nosso user
                        //String postagens = String.valueOf(usuario.getPostagens());
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());

                        //configura valores recuperados, inserindo estes valores no perfil do usuario
                        //textPublicacoes.setText(postagens);
                        textSeguindo.setText(seguindo);
                        textSeguidores.setText(seguidores);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
