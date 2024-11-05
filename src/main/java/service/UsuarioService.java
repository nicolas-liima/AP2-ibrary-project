package service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
import model.Usuario;
import script.DataInserter;
import script.DataRetriever;

@Service
public class UsuarioService {
    private final DataInserter dataInserter;
    private final DataRetriever dataRetriever;
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    public UsuarioService(DataInserter dataInserter, DataRetriever dataRetriever) {
        this.dataInserter = dataInserter;
        this.dataRetriever = dataRetriever;
    }

    public List<Usuario> listarTodosUsuarios() {
        return dataRetriever.listarTodos(Usuario.class); // Retorna a lista de todos os usuários
    }

    public List<Usuario> buscarUsuariosPorNome(String nome) {
        List<Usuario> usuarios = dataRetriever.buscarUsuariosPorNome(nome);
        if (usuarios.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum usuário encontrado com o nome: " + nome);
        }
        return usuarios;
    }

    public Usuario buscarUsuarioPorUsername(String username) {
        Usuario usuario = dataRetriever.buscarUsuarioPorUsername(username);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + username);
        }
        return usuario;
    }

    public Usuario buscarUsuarioPorCpf(String cpf) {
        Usuario usuario = dataRetriever.buscarUsuarioPorCpf(cpf);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com CPF: " + cpf);
        }
        return usuario;
    }

    public boolean salvarUsuario(Usuario usuario) {
        if (!validarCPF(usuario.getCpf())) {
            logger.error("CPF inválido: {}", usuario.getCpf());
            throw new IllegalArgumentException("CPF inválido.");
        }

        // Verifica se já existe um usuário com o mesmo username
        if (dataRetriever.buscarUsuarioPorUsername(usuario.getUsername()) != null) {
            throw new ObjetoDuplicadoException("Username já está cadastrado.");
        }

        return dataInserter.inserirUsuario(usuario);
    }

    public boolean atualizarUsuario(Usuario usuario) {
        return dataInserter.atualizarUsuario(usuario);
    }

    public boolean removerUsuario(String username) {
        Usuario usuario = buscarUsuarioPorUsername(username);
        return dataInserter.removerUsuario(usuario);
    }

    public static boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", ""); // Remove caracteres não numéricos

        if (cpf.length() != 11) return false; // CPF deve ter 11 dígitos

        int soma = 0, peso = 10;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso--;
        }
        int primeiroDigitoVerificador = 11 - (soma % 11);
        if (primeiroDigitoVerificador >= 10) primeiroDigitoVerificador = 0;
        if (primeiroDigitoVerificador != Character.getNumericValue(cpf.charAt(9))) return false;

        soma = 0; peso = 11;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso--;
        }
        int segundoDigitoVerificador = 11 - (soma % 11);
        if (segundoDigitoVerificador >= 10) segundoDigitoVerificador = 0;
        return segundoDigitoVerificador == Character.getNumericValue(cpf.charAt(10));
    }
}
