package com.example.datagram.model;

/*
   MODELO POSTAGEM
   postagens
       id_user
           id_postagem_firebase
               descricao
               caminhoFoto
               idUsuario
    */

import com.example.datagram.helper.ConfiguracaoFirebase;
import com.example.datagram.helper.UsuarioFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Postagem implements Serializable {
    private String id;
    private String idUsuario;
    private String descricao;
    private String caminhoFoto;

    public Postagem() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        String idPostagem = postagemRef.push().getKey(); // <-- gera automaticamente e recupera um id de postagem para o user
        setId(idPostagem);
    }

    public boolean salvar(DataSnapshot seguidoresSnapshot){
        Map objeto = new HashMap();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        String idSeguidor = seguidoresSnapshot.getKey();

        //referencia para postagem
        //alem disso estamos aplicando a tecnica de espalhamento, salvando um objeto em dois lugares (postagens e feed)
        String combinaId = "/" + getIdUsuario() + "/" + getId();
        objeto.put("/postagens"+combinaId,this); // <-- salvando a postagem dentro de postagens =  /postagens/id_user/id_postagem

        for(DataSnapshot seguidores : seguidoresSnapshot.getChildren()){
            /*
            ESTRUTURA FEED
                id_seguidor --> id_user logado
                    id_postagem
                        postagem(ou varias postagens) por jamilton
             */

            //monta obj para salvar
            Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("fotoPostagem",getCaminhoFoto());
            dadosSeguidor.put("descricaoPostagem",getDescricao());
            dadosSeguidor.put("idPostagem",getId());
            dadosSeguidor.put("nomeUsuario",usuarioLogado.getNome());
            dadosSeguidor.put("fotoUsuario",usuarioLogado.getCaminhoFoto());

            String idsAtualizacao = "/" + idSeguidor + "/" + getId();
            objeto.put("/feed"+idsAtualizacao,dadosSeguidor); // <-- salvando a postagem dentro do feed
        }

        firebaseRef.updateChildren(objeto); // joga os filhos ^^ dentro da raiz do database
        return true;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}
