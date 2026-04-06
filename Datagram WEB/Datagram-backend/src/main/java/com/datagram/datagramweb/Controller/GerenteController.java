package com.datagram.datagramweb.Controller;
import java.util.List;
import com.datagram.datagramweb.Domain.Usuario;
import com.datagram.datagramweb.Service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/gerente")
public class GerenteController {

    private final UsuarioService service;

    public GerenteController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/mediaSeguidores")
    public ResponseEntity<Integer> getNumMediaSeguidores() {
        return ResponseEntity.ok().body(service.numeroMedioSeguidores());
    }

    @GetMapping("/maisConectados")
    public ResponseEntity<List<Usuario>> getMaisConectados() {
        return ResponseEntity.ok().body(service.membrosMaisConectados());
    }

}