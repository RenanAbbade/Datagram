package com.example.datagram.activity;

import android.graphics.Color;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Usuario;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView imageEditarPerfil;
    private TextView textAlterarFoto;
    private TextInputEditText editNomePerfil, editEmailPerfil;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //configuracoes iniciais
        //aqui ja teremos o usuario com seus dados, nome, email, photo...
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Editar perfil");
        toolbar.setTitleTextColor(Color.argb(255,65,105,225));
        setSupportActionBar(toolbar);

        //Configurando botão de voltar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //botão que permite substituir o botão de voltar default por outro que foi inserido no projeto.
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //Inicializando o componente
        inicializarComponentes();

        //Recuperar os dados do usuário
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        editNomePerfil.setText(usuarioPerfil.getDisplayName());
        editEmailPerfil.setText(usuarioPerfil.getEmail());

        //salvar alteracoes do nome
        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeAtualizado = editNomePerfil.getText().toString();
                UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado);
                usuarioLogado.setNome(nomeAtualizado);
                usuarioLogado.atualizar();

                Toast.makeText(EditarPerfilActivity.this,
                        "Dados alterados com sucesso!",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void inicializarComponentes(){
        imageEditarPerfil = findViewById(R.id.imagePerfil);
        textAlterarFoto = findViewById(R.id.textAlterarFoto);
        editNomePerfil = findViewById(R.id.textAlterarNome);
        editEmailPerfil = findViewById(R.id.textAlterarEmail);
        buttonSalvarAlteracoes = findViewById(R.id.buttonSalvarAlteracoes);
        editEmailPerfil.setFocusable(false);//Faz com que o user não altere o campo
    }
    @Override
    //metodo responsavel pelo botao voltar
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }
}
