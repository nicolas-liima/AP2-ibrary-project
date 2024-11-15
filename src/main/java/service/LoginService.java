package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exceptions.AutenticacaoException;
import exceptions.RecursoNaoEncontradoException;
import model.Usuario;

@Service
public class LoginService {

    private final UsuarioService usuarioService;

    @Autowired
    public LoginService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Usuario login(String username, String senha) throws RecursoNaoEncontradoException, AutenticacaoException {
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + username);
        }
        
        if (!usuario.isUsuarioAtivo()) {
            throw new AutenticacaoException(username);
        }

        if (!usuario.validarSenha(senha)) {
            throw new AutenticacaoException(username);
        }
        
        return usuario;
    }
}
