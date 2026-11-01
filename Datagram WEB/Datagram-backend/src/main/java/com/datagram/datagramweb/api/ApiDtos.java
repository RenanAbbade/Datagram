package com.datagram.datagramweb.api;
import com.datagram.datagramweb.Domain.*;
import jakarta.validation.constraints.*;
import java.util.Set;

public final class ApiDtos {
 private ApiDtos() {}
 public record Login(@NotBlank @Email String email, @NotBlank String senha) {}
 public record Register(@NotBlank @Size(max=120) String nome, @NotBlank @Email String email,
   @NotBlank @Size(min=8,max=72) String senha, @NotBlank String dataNasc, String cpf,
   @NotBlank String tipoUsuario, String escolaridade, String estado, String municipio,
   String instituicao, String linkCv, String dataInicio) {}
 public record Profile(@NotBlank @Size(max=120) String nome, @Size(max=120) String escolaridade,
   @Size(max=2) String estado, @Size(max=120) String municipio, @Size(max=200) String instituicao,
   @Size(max=1000) String linkCv, @Size(max=30) Set<@Size(max=80) String> interesses) {}
 public record PostInput(@NotBlank @Size(max=200) String titulo, @Size(max=300) String subtitulo,
   @NotBlank @Size(max=20000) String conteudo, @Size(max=2000) String url,
   String tipoPostagem, @Size(max=30) Set<@Size(max=80) String> palavrasChave) {}
 public record UserView(Integer id, String nome, String email, String tipoUsuario,
   String escolaridade, String estado, String municipio, String instituicao, String linkCv,
   String fotoPerfil, Set<String> interesses, int seguidores, int seguindo, int posts) {
   public static UserView of(Usuario u, boolean own) {
    String photo = u.getFotoPerfil() == null ? null : "/api/user/"+u.getId()+"/foto?v="+Integer.toHexString(u.getFotoPerfil().hashCode());
    return new UserView(u.getId(),u.getNome(),own?u.getEmail():null,u.getTipoUsuario(),u.getEscolaridade(),
      u.getEstado(),u.getMunicipio(),u.getInstituicao(),u.getLinkCv(),photo,Set.copyOf(u.getInteresses()),
      u.getIdsSeguidores().size(),u.getIdsSeguindo().size(),u.getPosts());
   }
 }
 public record PostView(Integer id, UserView autor, String titulo, String subtitulo, String conteudo,
   String date, int curtida, boolean curtido, String url, String tipoPostagem,
   Set<String> palavrasChave, boolean temArquivo) {
  public static PostView of(Postagem p, Integer current) {
   return new PostView(p.getId(),UserView.of(p.getAutor(),false),p.getTitulo(),p.getSubtitulo(),p.getConteudo(),
    p.getDate(),p.getIdsCurtida().size(),p.getIdsCurtida().contains(current),p.getUrl(),p.getTipoPostagem(),
    Set.copyOf(p.getPalavrasChave()),p.getArquivoPublicacao()!=null);
  }
 }
 public record Session(String token, UserView usuario) {}
}
