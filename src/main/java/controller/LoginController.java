package controller;

import model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import service.LoginService;
import exceptions.AutenticacaoException;
import exceptions.RecursoNaoEncontradoException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String senha) {
        try {
            Usuario usuario = loginService.login(username, senha);
            return ResponseEntity.ok(usuario);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(404).body("Usuário não encontrado: " + e.getMessage());
        } catch (AutenticacaoException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas ou Usuário inativo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}
