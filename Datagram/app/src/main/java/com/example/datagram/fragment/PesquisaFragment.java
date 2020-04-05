package com.example.datagram.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.datagram.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PesquisaFragment extends Fragment {

    //widget
    private SearchView searchViewPesquisa;
    private RecyclerView reacylerViewPesquisa;

    public PesquisaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_pesquisa, container, false);

        //sempre que trabalhamos com fragment, precisamos retornar a view em uma variavel
        //e assim conseguimos manipular os elementos da UI
        searchViewPesquisa = view.findViewById(R.id.searchViewPesquisa);
        reacylerViewPesquisa = view.findViewById(R.id.recyclerViewPesquisa);

        //Configurar searchview
        searchViewPesquisa.setQueryHint("Digite a sua busca");
        searchViewPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //aqui iremos buscar apenas quando o usuario pressionar enter
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            //aqui "nós" vamos dando resultados conforme o user digita
            @Override
            public boolean onQueryTextChange(String newText) {
//                Log.d("onQueryTextChange","texto digitado --> "+newText);
                return true;
            }
        });
        return view;
    }
}
