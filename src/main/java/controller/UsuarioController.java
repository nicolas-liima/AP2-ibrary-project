package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
import model.Usuario;
import service.UsuarioService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodosUsuarios() {
        List<Usuario> usuarios = usuarioService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Usuario>> buscarUsuariosPorNome(@PathVariable String nome) {
        try {
            List<Usuario> usuarios = usuarioService.buscarUsuariosPorNome(nome);
            return ResponseEntity.ok(usuarios);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Usuario> buscarUsuarioPorUsername(@PathVariable String username) {
        try {
            Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
            return ResponseEntity.ok(usuario);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<List<Usuario>> buscarUsuarioPorCpf(@PathVariable String cpf) {
        try {
        	List<Usuario> usuarios = usuarioService.buscarUsuarioPorCpf(cpf);
            return ResponseEntity.ok(usuarios);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<String> salvarUsuario(@RequestBody Usuario usuario) {
        try {
            usuarioService.salvarUsuario(usuario);
            return ResponseEntity.ok("Usuário cadastrado com sucesso.");
        } catch (ObjetoDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar usuário.");
        }
    }

    @PutMapping("/{username}")
    public ResponseEntity<String> atualizarUsuario(@PathVariable String username, @RequestBody Usuario usuario) {
        try {
            // Verifica se o novo username é válido
            boolean usuarioAtualizado = usuarioService.atualizarUsuario(username, usuario);

            if (usuarioAtualizado) {
                return ResponseEntity.ok("Usuário atualizado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ObjetoDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar usuário.");
        }
    }


    @DeleteMapping("/{username}")
    public ResponseEntity<String> removerUsuario(@PathVariable String username) {
        try {
            boolean removido = usuarioService.removerUsuario(username);
            return removido ? ResponseEntity.ok("Usuário removido com sucesso.") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
    }
}
