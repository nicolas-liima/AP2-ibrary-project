package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exceptions.AutenticacaoException;
import exceptions.RecursoNaoEncontradoException;
import model.Usuario;
import script.DataRetriever;

@Service
public class LoginService {

    private final DataRetriever dataRetriever;
    private final UsuarioService usuarioService;

    @Autowired
    public LoginService(DataRetriever dataRetriever, UsuarioService usuarioService) {
        this.dataRetriever = dataRetriever;
        this.usuarioService = usuarioService;
    }

    public Usuario login(String username, String senha) throws RecursoNaoEncontradoException, AutenticacaoException {
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username); 
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + username);
        }
        if (!usuario.validarSenha(senha)) {
            throw new AutenticacaoException("Credenciais inválidas para o usuário: " + username);
        }
        return usuario;
    }
}
