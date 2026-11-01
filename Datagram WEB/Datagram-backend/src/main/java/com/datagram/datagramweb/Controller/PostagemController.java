package com.datagram.datagramweb.Controller;
import com.datagram.datagramweb.api.ApiDtos.*;
import com.datagram.datagramweb.Service.*;
import com.datagram.datagramweb.Domain.Postagem;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.net.URI;
@RestController @RequestMapping("/post")
@org.springframework.transaction.annotation.Transactional
public class PostagemController {
 private final PostagemService posts;private final UsuarioService users;
 public PostagemController(PostagemService p,UsuarioService u){posts=p;users=u;}
 private PostView view(Postagem p){return PostView.of(p,users.authenticated().getId());}
 public record PostPage(List<PostView> content,boolean hasNext){}
 @GetMapping({"","/feed"}) public PostPage page(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) Integer autor){
  var slice=posts.page(autor,page,size);return new PostPage(slice.getContent().stream().map(this::view).toList(),slice.hasNext());
 }
 @GetMapping("/{id}") public List<PostView> byAuthor(@PathVariable Integer id){return posts.findAllbyAutorId(id).stream().map(this::view).toList();}
 @PostMapping("/insert") public ResponseEntity<PostView> create(@Valid @RequestBody PostInput d){var p=posts.insert(d,null);return ResponseEntity.created(URI.create("/post/"+p.getId())).body(view(p));}
 @PostMapping(value="/insert",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<PostView> createWithFile(@Valid @RequestPart("post") PostInput d,@RequestPart("file") MultipartFile f) throws java.io.IOException {
  var p=posts.insert(d,com.datagram.datagramweb.api.Uploads.pdf(f));return ResponseEntity.created(URI.create("/post/"+p.getId())).body(view(p));
 }
 @PutMapping("/{id}") public PostView update(@PathVariable Integer id,@Valid @RequestBody PostInput d){return view(posts.update(id,d));}
 @PutMapping("/{id}/like") public PostView like(@PathVariable Integer id){return view(posts.like(id,true));}
 @DeleteMapping("/{id}/like") public PostView unlike(@PathVariable Integer id){return view(posts.like(id,false));}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Integer id){posts.delete(id);}
 @GetMapping("/maisCurtido") public List<PostView> popular(){return posts.findPostsMaisCurtidos().stream().map(this::view).toList();}
 @GetMapping("/{id}/arquivo") public ResponseEntity<byte[]> file(@PathVariable Integer id){String v=posts.find(id).getArquivoPublicacao();
  if(v==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Arquivo não encontrado");
  return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=publicacao-"+id+".pdf")
    .body(Base64.getDecoder().decode(v.substring(v.indexOf(',')+1)));
 }
}
