package com.example.datagram.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.datagram.R;

public class FiltroActivity extends AppCompatActivity {

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        //inicializar componentes
        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);

        //recupera a img escolhida pelo user
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            byte[] dadoImagem = bundle.getByteArray("fotoEscolhida"); //recupero a imagem atraves da key definida no putExtra
            imagem = BitmapFactory.decodeByteArray(dadoImagem,0, dadoImagem.length); //comeco a decodificar do inicio ao fim da img
            imageFotoEscolhida.setImageBitmap(imagem);
        }
    }
}
