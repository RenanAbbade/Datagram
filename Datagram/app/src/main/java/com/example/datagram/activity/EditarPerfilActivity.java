package com.example.datagram.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datagram.R;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.example.datagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView imageEditarPerfil;
    private TextView textAlterarFoto;
    private TextInputEditText editNomePerfil, editEmailPerfil;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //configuracoes iniciais
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

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

        textAlterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i,SELECAO_GALERIA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;

            try{
                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localimagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localimagemSelecionada);
                         break;
                }

                //caso user tenha escolhido uma imagem
                if(imagem != null){
                    //configurar imagem na tela
                    imageEditarPerfil.setImageBitmap(imagem);

                    //recuperar dados da img para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                    byte [] dadosImagem = baos.toByteArray();

                    //salvar imagem no firebase
                    StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child("<id-usuario>.jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "erro ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    ;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
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
