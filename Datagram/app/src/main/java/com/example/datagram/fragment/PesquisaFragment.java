package com.example.datagram.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.datagram.R;
import com.example.datagram.adapter.AdapterPesquisa;
import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PesquisaFragment extends Fragment {

    //widget
    private SearchView searchViewPesquisa;
    private RecyclerView recylerViewPesquisa;

    private List<Usuario> listaUsuarios;
    private DatabaseReference usuariosRef;
    private AdapterPesquisa adapterPesquisa;

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
        recylerViewPesquisa = view.findViewById(R.id.recyclerViewPesquisa);

        //configuracoes iniciais
        listaUsuarios = new ArrayList<>();
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");

        //configurar RecyclerView
        recylerViewPesquisa.setHasFixedSize(true);
        recylerViewPesquisa.setLayoutManager(new LinearLayoutManager(getActivity()));

        //configrar adapter
        adapterPesquisa = new AdapterPesquisa(listaUsuarios,getActivity());
        recylerViewPesquisa.setAdapter(adapterPesquisa);

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
                String  textoDigitado = newText.toUpperCase();
                pesquisarUsuarios(textoDigitado);
                return true;
            }
        });
        return view;
    }

    private void pesquisarUsuarios(String texto){
        listaUsuarios.clear();

        //pesquisa user caso tenha texto na busca
        if(texto.length() > 0){
            Query query = usuariosRef.orderByChild("nome")
                    .startAt(texto)
                    .endAt(texto+"\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    listaUsuarios.clear();

                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        listaUsuarios.add(ds.getValue(Usuario.class)); //recuperamos o user do fb
                    }
                    adapterPesquisa.notifyDataSetChanged(); //notificamos ao adapter as mudancas
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
