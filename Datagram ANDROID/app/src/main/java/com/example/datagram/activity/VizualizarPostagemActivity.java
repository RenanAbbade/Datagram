package com.example.datagram.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.datagram.R;
import com.example.datagram.model.Postagem;
import com.example.datagram.model.Usuario;

import de.hdodenhof.circleimageview.CircleImageView;

public class VizualizarPostagemActivity extends AppCompatActivity {

    private TextView textPerfilPostagem, TextQtdCurtidasPostagem, textDescricaoPostagem,
                     textVisualizarComentariosPostagem;
    private ImageView imagePostagemSelecionada;
    private CircleImageView imagePerfilPostagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vizualizar_postagem);

        inicializarComponentes();

        //Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Visualizar postagem");
        toolbar.setTitleTextColor(Color.argb(255,65,105,225));
        setSupportActionBar(toolbar);

        //recupera dados do perfilamigo
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            Postagem postagem = (Postagem) bundle.getSerializable("postagem");
            Usuario usuario = (Usuario) bundle.getSerializable("usuario");

            //exibe dados user
            Uri uri = Uri.parse(usuario.getCaminhoFoto());
            Glide.with(VizualizarPostagemActivity.this)
                    .load(uri)
                    .into(imagePerfilPostagem);
            textPerfilPostagem.setText(usuario.getNome());

            //exibe dados postagem
            Uri uriPostagem = Uri.parse(postagem.getCaminhoFoto());
            Glide.with(VizualizarPostagemActivity.this)
                    .load(uriPostagem)
                    .into(imagePostagemSelecionada);
            textDescricaoPostagem.setText(postagem.getDescricao());

        }
    }

    public void inicializarComponentes(){
        textPerfilPostagem = findViewById(R.id.textPerfilPostagem);
        TextQtdCurtidasPostagem = findViewById(R.id.textQtdCurtidas);
        textDescricaoPostagem = findViewById(R.id.textDescricaoPostagem);
        imagePostagemSelecionada = findViewById(R.id.ImagePostagemSelecionada);
        imagePerfilPostagem = findViewById(R.id.ImagePerfilPostagem);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }
}
