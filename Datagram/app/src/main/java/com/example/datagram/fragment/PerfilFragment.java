package com.example.datagram.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.datagram.R;
import com.example.datagram.activity.EditarPerfilActivity;

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

    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_perfil, container, false);
        //Configurações dos componentes

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        gridViewPerfil = view.findViewById(R.id.GridViewPerfil);
        progressBar = view.findViewById(R.id.progressBarPefil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        textPublicacoes = view.findViewById(R.id.textViewNumPub);
        textSeguidores = view.findViewById(R.id.textViewNumSeguidores);
        textSeguindo = view.findViewById(R.id.textViewNumSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);

        //Abre edição de perfil
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(intent);

            }
        });

        return view;
    }
}
