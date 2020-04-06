package com.example.datagram.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.datagram.R;
import com.example.datagram.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/*
O Adapter permite o acesso aos itens de dados. O Adapter também é responsável por fazer
uma exibição para cada item no conjunto de dados. ... Um Adapter é uma classe de Android
que intermedia a criação de listas, e também lida com outros tipos de componentes,
como os tipos Spinner, GridView, Gallery e AutoCompleteTextView
 */

public class AdapterPesquisa extends RecyclerView.Adapter<AdapterPesquisa.MyViewHolder> {

    private List<Usuario> listaUsuario;
    private Context context;

    public AdapterPesquisa(List<Usuario> listaUsuario, Context context) {
        this.listaUsuario = listaUsuario;
        this.context = context;
    }

    @Override
    //aqui criamos o layout referente a imagem e nome dos users, onde iremos retornar nossa lista de users
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pesquisa_user,parent,false);
        return new MyViewHolder(itemLista);
    }

    //aqui iremos exibir os itens
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Usuario usuario = listaUsuario.get(position);
        holder.nome.setText(usuario.getNome());
        if(usuario.getCaminhoFoto() != null){
            Uri url = Uri.parse(usuario.getCaminhoFoto());
            Glide.with(context).load(url).into((holder.foto));
        }else{
            holder.foto.setImageResource(R.drawable.avatar);
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuario.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView foto;
        TextView nome;

        public MyViewHolder(View itemView) {
            super(itemView);

            foto = itemView.findViewById((R.id.imageFotoPesquisa));
            nome = itemView.findViewById((R.id.textNomePesquisa));

        }
    }
}
