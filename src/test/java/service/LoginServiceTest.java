package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import exceptions.RecursoNaoEncontradoException;
import model.Usuario;
import script.DataRetriever;

public class LoginServiceTest {

    @Mock
    private DataRetriever dataRetriever; // Moca a DataRetriever

    @InjectMocks
    private LoginService loginService; // Classe em teste

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa os mocks
    }

    @Test
    public void testLoginSuccess() throws RecursoNaoEncontradoException {
        // Arrange
        String username = "Guilherme";
        String senha = "testPass";
        Usuario expectedUser = new Usuario(1, username, senha, Usuario.TipoUsuario.CLIENTE,true , "Test User", "12345678901","email test", "Endereco teste", "115524");

        when(dataRetriever.buscarUsuarioAtivoPorUsernameESenha(username, senha)).thenReturn(expectedUser); // Define o comportamento do mock

        // Act
        Usuario actualUser = loginService.login(username, senha);

        // Assert
        assertEquals(expectedUser, actualUser); // Verifica se o usuário retornado é o esperado
    }

    @Test
    public void testLoginFailure() {
        // Arrange
        String username = "testUser";
        String senha = "wrongPass";

        when(dataRetriever.buscarUsuarioAtivoPorUsernameESenha(username, senha)).thenReturn(null); // Define o comportamento do mock para falha

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> loginService.login(username, senha), "Usuário não encontrado ou senha incorreta");
    }
}
