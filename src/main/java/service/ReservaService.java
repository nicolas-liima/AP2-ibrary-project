package service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
import model.Livro;
import model.Reserva;
import model.Usuario;
import script.DataInserter;
import script.DataRetriever;

@Service
public class ReservaService {

    private final DataInserter dataInserter;
    private final DataRetriever dataRetriever;
    private final UsuarioService usuarioService;
    private final EmprestimoService emprestimoService;
    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);

    @Autowired
    public ReservaService(DataInserter dataInserter, DataRetriever dataRetriever, UsuarioService usuarioService, EmprestimoService emprestimoService) {
        this.dataInserter = dataInserter;
        this.dataRetriever = dataRetriever;
        this.usuarioService = usuarioService;
        this.emprestimoService = emprestimoService;
    }

    public int realizarReserva(String isbn, String username) throws SQLException {
        // Verifica a existência do usuário e se ele está ativo
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        if (usuario == null || !usuario.isUsuarioAtivo()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado ou inativo.");
        }

        // Verifica a existência do livro
        Livro livro = dataRetriever.buscarLivroPorISBN(isbn);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro não encontrado.");
        }
        
        // Verifica se o usuário já possui um empréstimo em aberto para o livro
        Integer emprestimoAberto = dataRetriever.verificarEmprestimoAberto(usuario.getId(), livro.getId());
        if (emprestimoAberto != null) {
            throw new ObjetoDuplicadoException("Usuário já possui um empréstimo em aberto para este livro.");
        }

        // Verifica se o usuário já possui uma reserva para o mesmo livro
        Integer reservaAberta = dataRetriever.verificarReservaAberta(usuario.getId(), livro.getId());
        if (reservaAberta != null) {
            throw new ObjetoDuplicadoException("Usuário já possui uma reserva em aberto para este livro.");
        }

        // Verifica se o livro está disponível no estoque
        if (!livro.isDisponivelParaEmprestimo()) {
            throw new RecursoNaoEncontradoException("Livro não está disponível no estoque para reserva.");
        }

        // Reduz o estoque do livro
        livro.reduzirEstoque();

        // Atualiza o livro no banco de dados
        boolean estoqueAtualizado = dataInserter.atualizarLivro(isbn, livro);
        if (!estoqueAtualizado) {
            throw new SQLException("Falha ao atualizar o estoque do livro.");
        }

        // Cria a reserva com data de expiração fixa de 7 dias após a criação
        LocalDate dataExpiracao = LocalDate.now().plusDays(7); // Data de expiração = 7 dias após a criação
        Reserva novaReserva = new Reserva(livro, usuario, LocalDate.now(), dataExpiracao);

        // Insere a reserva no banco de dados
        boolean sucesso = dataInserter.inserirReserva(novaReserva);
        if (!sucesso) {
            throw new SQLException("Falha ao realizar a reserva.");
        }

        // Busca o ID correto da reserva inserida
        Integer novaReservaID = dataRetriever.verificarReservaAberta(usuario.getId(), livro.getId());
        if (novaReservaID == null) {
            throw new SQLException("Não foi possível encontrar a reserva após a inserção.");
        }

        logger.info("Reserva criada com sucesso. ID: {}", novaReservaID);

        // Retorna o ID da nova reserva
        return novaReservaID;
    }



    public List<Reserva> listarTodasReservas() {
        List<Reserva> reservas = dataRetriever.listarReservas();

        if (reservas == null || reservas.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhuma reserva encontrada.");
        }

        return reservas;
    }

    public List<Reserva> listarReservasPorUsername(String username) {
        List<Reserva> reservas = dataRetriever.listarReservasPorUsername(username);

        if (reservas == null || reservas.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhuma reserva encontrada.");
        }

        return reservas;
    }

    public Reserva buscarReservaPorId(int id) {
        Reserva reserva = dataRetriever.buscarReservaPorId(id);
        if (reserva == null) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada com ID: " + id);
        }
        return reserva;
    }

    public List<Reserva> listarReservasAtrasadas() throws SQLException {
        List<Reserva> reservas = dataRetriever.listarReservasAtrasadas();

        if (reservas == null || reservas.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhuma reserva encontrada.");
        }

        return reservas;
    }

    public void deletarReserva(int reservaId) throws SQLException {
        // Verifica a existência da reserva
        Reserva reserva = dataRetriever.buscarReservaPorId(reservaId);
        if (reserva == null) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada.");
        }

        // Verifica se o livro estava reservado e, em caso afirmativo, retorna o livro ao estoque
        Livro livro = reserva.getLivro();
        
        livro.aumentarEstoque();
        
        boolean estoqueAtualizado = dataInserter.atualizarLivro(livro.getIsbn(), livro);
        if (!estoqueAtualizado) {
            throw new SQLException("Falha ao atualizar o estoque do livro.");
        }
        
        // Exclui a reserva do banco
        boolean sucesso = dataInserter.removerReserva(reservaId);
        if (sucesso) {
            logger.info("Reserva excluída com sucesso. ID: {}", reservaId);
        } else {
            throw new SQLException("Falha ao excluir a reserva.");
        }
    }
    
    public int transformarReservaEmEmprestimo(int reservaId) throws SQLException {
        // Verifica a existência da reserva
        Reserva reserva = dataRetriever.buscarReservaPorId(reservaId);
        if (reserva == null) {
            throw new RecursoNaoEncontradoException("Reserva não encontrada.");
        }

        // Verifica se a data de hoje está dentro do período válido da reserva
        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(reserva.getDataExpiracao())) {
            throw new SQLException("A reserva expirou ou não está mais disponível para retirada.");
        }

        // Exclui a reserva
        deletarReserva(reservaId);

        // Realiza o empréstimo (transformando a reserva em empréstimo)
        int emprestimoId = emprestimoService.realizarEmprestimo(reserva.getLivro().getIsbn(), reserva.getUsuario().getUsername());
        return emprestimoId;
    }
    
}

