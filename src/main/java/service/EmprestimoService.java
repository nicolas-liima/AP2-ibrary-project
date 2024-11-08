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
import model.Emprestimo;
import model.Livro;
import model.Usuario;
import script.DataInserter;
import script.DataRetriever;

@Service
public class EmprestimoService {

	private final DataInserter dataInserter;
	private final DataRetriever dataRetriever;
	private final UsuarioService usuarioService;
	private final LivroService livroService;
	private static final Logger logger = LoggerFactory.getLogger(EmprestimoService.class);

	@Autowired
	public EmprestimoService(DataInserter dataInserter, DataRetriever dataRetriever, UsuarioService usuarioService,
			LivroService livroService) {
		this.dataInserter = dataInserter;
		this.dataRetriever = dataRetriever;
		this.usuarioService = usuarioService;
		this.livroService = livroService;
	}

	public int realizarEmprestimo(String isbn, String username) throws SQLException {
		// Verifica a existência do usuário e se ele está ativo
		Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
		if (usuario == null || !usuario.isUsuarioAtivo()) {
			throw new RecursoNaoEncontradoException("Usuário não encontrado ou inativo.");
		}

		// Verifica a existência do livro
		Livro livro = livroService.buscarLivroPorISBN(isbn);
		if (livro == null) {
			throw new RecursoNaoEncontradoException("Livro não encontrado.");
		}

		// Verifica se o livro está disponível para empréstimo
		if (!livro.isDisponivelParaEmprestimo()) {
			throw new ObjetoDuplicadoException("Livro indisponível no estoque.");
		}

		// Verifica se o usuário já possui um empréstimo em aberto para o mesmo livro
		Integer emprestimoAberto = dataRetriever.verificarEmprestimoAberto(usuario.getId(), livro.getId());
		if (emprestimoAberto != null) {
			throw new ObjetoDuplicadoException("Usuário já possui um empréstimo em aberto para este livro.");
		}

		// Calcula a data de devolução prevista (14 dias após o empréstimo)
		LocalDate dataEmprestimo = LocalDate.now();
		LocalDate dataDevolucaoPrevista = dataEmprestimo.plusDays(14);

		// Reduz o estoque do livro
		livro.reduzirEstoque();
		boolean estoqueAtualizado = dataInserter.atualizarLivro(isbn, livro);
		if (!estoqueAtualizado) {
			throw new SQLException("Falha ao atualizar o estoque do livro.");
		}

		// Cria o empréstimo com a data de devolução prevista
		Emprestimo novoEmprestimo = new Emprestimo(livro, usuario, dataEmprestimo, dataDevolucaoPrevista);
		boolean sucesso = dataInserter.inserirEmprestimo(novoEmprestimo);
		if (sucesso) {
			logger.info("Empréstimo criado com sucesso.");
		} else {
			throw new SQLException("Falha ao realizar o empréstimo.");
		}

		// Busca o ID correto do empréstimo inserido
		Integer novoEmprestimoID = dataRetriever.verificarEmprestimoAberto(usuario.getId(), livro.getId());
		if (novoEmprestimoID == null) {
			throw new SQLException("Não foi possível encontrar o empréstimo após a inserção.");
		}

		logger.info("Empréstimo criado com sucesso. ID: {}", novoEmprestimoID);

		// Retorna o ID do novo empréstimo, após a execução completa
		return novoEmprestimoID;
	}

	public List<Emprestimo> listarTodosEmprestimos() {
		// Chama o DataRetriever para buscar todos os empréstimos
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimos();

		if (emprestimos == null || emprestimos.isEmpty()) {
			throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado.");
		}

		return emprestimos;
	}

	public List<Emprestimo> listarEmprestimosPorUsername(String username) {
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimosPorUsername(username);

		if (emprestimos == null || emprestimos.isEmpty()) {
			throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado.");
		}

		return emprestimos;
	}

	public Emprestimo buscarEmprestimoPorId(int id) {
		Emprestimo emprestimo = dataRetriever.buscarEmprestimoPorId(id);
		if (emprestimo == null) {
			throw new RecursoNaoEncontradoException("Empréstimo não encontrado com ID: " + id);
		}
		return emprestimo;
	}

	public List<Emprestimo> listarEmprestimosAtrasados() throws SQLException {
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimosAtrasados();

		if (emprestimos == null || emprestimos.isEmpty()) {
			throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado.");
		}

		return emprestimos;
	}

	public List<Emprestimo> listarEmprestimosAtivos() throws SQLException {
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimosAtivos();

		if (emprestimos == null || emprestimos.isEmpty()) {
			throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado.");
		}

		return emprestimos;
	}

	public List<Emprestimo> listarEmprestimosFinalizados() throws SQLException {
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimosFinalizados();

		if (emprestimos == null || emprestimos.isEmpty()) {
			throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado.");
		}

		return emprestimos;
	}

	public void devolverEmprestimo(int emprestimoId) throws SQLException {
		// Busca o empréstimo pelo ID
		Emprestimo emprestimo = dataRetriever.buscarEmprestimoPorId(emprestimoId);
		if (emprestimo == null) {
			throw new RecursoNaoEncontradoException("Empréstimo não encontrado.");
		}

		// Verifica se o empréstimo já foi devolvido
		if (emprestimo.getDataDevolucaoEfetiva() != null) {
			throw new IllegalStateException("Este empréstimo já foi devolvido.");
		}

		// Recupera o livro associado ao empréstimo
		Livro livro = emprestimo.getLivro();

		// Adiciona uma unidade ao estoque do livro
		livro.aumentarEstoque();
		boolean estoqueAtualizado = dataInserter.atualizarLivro(livro.getIsbn(), livro);
		if (!estoqueAtualizado) {
			throw new SQLException("Falha ao atualizar o estoque do livro.");
		}

		// Define a data de devolução efetiva para a data atual

		emprestimo.setDataDevolucaoEfetiva(LocalDate.now());

		// Atualiza o empréstimo no banco de dados
		boolean emprestimoAtualizado = dataInserter.devolverEmprestimo(emprestimo);
		if (!emprestimoAtualizado) {
			throw new SQLException("Falha ao atualizar o empréstimo.");
		}

		logger.info("Empréstimo devolvido com sucesso. ID: {}", emprestimoId);
	}

	public boolean removerEmprestimo(int id) {
		Emprestimo emprestimo = buscarEmprestimoPorId(id);
		return dataInserter.removerEmprestimo(emprestimo.getId());
	}

//    public List<Emprestimo> verificarAtrasados(List<Emprestimo> emprestimos) {
//        return emprestimos.stream().filter(Emprestimo::isAtrasado).toList();
//    }
}
