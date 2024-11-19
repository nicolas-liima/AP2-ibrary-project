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

	public int realizarEmprestimoFisico(String isbn, String username) throws SQLException {
	    // Verifica a existência do usuário e se ele está ativo
	    Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
	    if (usuario == null || !usuario.isUsuarioAtivo()) {
	        throw new RecursoNaoEncontradoException("Usuário não encontrado ou inativo.");
	    }

	    // Verifica a existência do livro e se ele está disponível na versão física
	    Livro livro = livroService.buscarLivroPorISBN(isbn);
	    if (livro == null || !livro.isLivroFisico()) {
	        throw new RecursoNaoEncontradoException("Livro físico não encontrado.");
	    }

	    // Verifica se o livro está disponível no estoque
	    if (!livro.isDisponivelParaEmprestimo()) {
	        throw new ObjetoDuplicadoException("Livro físico indisponível no estoque.");
	    }

	    // Verifica se o usuário já possui um empréstimo em aberto para o mesmo livro físico
	    Integer emprestimoAberto = dataRetriever.verificarEmprestimoAberto(usuario.getId(), livro.getId()); // true para físico
	    if (emprestimoAberto != null) {
	        throw new ObjetoDuplicadoException(
	                String.format("Usuário já possui um empréstimo em aberto para este livro físico. ID do empréstimo: %d", emprestimoAberto)
	            );   
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

	    // Cria o empréstimo físico
	    Emprestimo novoEmprestimo = new Emprestimo(livro, usuario, dataEmprestimo, dataDevolucaoPrevista, true, false);
	    boolean sucesso = dataInserter.inserirEmprestimo(novoEmprestimo);
	    if (!sucesso) {
	        throw new SQLException("Falha ao realizar o empréstimo físico.");
	    }

	    // Busca o ID correto do empréstimo inserido
	    Integer novoEmprestimoID = dataRetriever.verificarEmprestimoAberto(usuario.getId(), livro.getId());
	    if (novoEmprestimoID == null) {
	        throw new SQLException("Não foi possível encontrar o empréstimo após a inserção.");
	    }

	    logger.info("Empréstimo físico criado com sucesso. ID: {}", novoEmprestimoID);
	    return novoEmprestimoID;
	}
	
	public int realizarEmprestimoDigital(String isbn, String username) throws SQLException {
	    // Verifica a existência do usuário e se ele está ativo
	    Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
	    if (usuario == null || !usuario.isUsuarioAtivo()) {
	        throw new RecursoNaoEncontradoException("Usuário não encontrado ou inativo.");
	    }
	
	    // Verifica a existência do livro e se ele está disponível na versão digital
	    Livro livro = livroService.buscarLivroPorISBN(isbn);
	    if (livro == null || !livro.isLivroDigital()) {
	        throw new RecursoNaoEncontradoException("Livro digital não encontrado.");
	    }
	
	    // Verifica a quantidade de licenças disponíveis
	    if (livro.getQuantidadeLicencas() <= 0) {
	        throw new ObjetoDuplicadoException("Não há licenças disponíveis para o empréstimo deste livro digital.");
	    }
	
	    // Verifica se o usuário já possui um empréstimo do mesmo livro digital
	    Integer emprestimoExistente = dataRetriever.verificarEmprestimoDigital(usuario.getId(), livro.getId());
	    if (emprestimoExistente != null) {
	        throw new ObjetoDuplicadoException(
	                String.format("Usuário já possui um empréstimo em aberto para este livro digital. ID do empréstimo: %d", emprestimoExistente)
	        );
	    }
	
	    // Define a data de empréstimo e devolução efetiva (mesmo dia para fins de relatório)
	    LocalDate dataEmprestimo = LocalDate.now();
	    LocalDate dataDevolucaoPrevista = dataEmprestimo.plusDays(14);
	
	    // Reduz a quantidade de licenças disponíveis
	    livro.reduzirLicenca();  // Método que decrementa a quantidade de licenças
	    boolean livroAtualizado = dataInserter.atualizarLivro(isbn, livro);
	    if (!livroAtualizado) {
	        throw new SQLException("Falha ao atualizar o registro do livro digital.");
	    }
	
	    // Cria o empréstimo digital
	    Emprestimo novoEmprestimo = new Emprestimo(livro, usuario, dataEmprestimo, dataDevolucaoPrevista, false, true);
	    boolean sucesso = dataInserter.inserirEmprestimo(novoEmprestimo);
	    if (!sucesso) {
	        throw new SQLException("Falha ao realizar o empréstimo digital.");
	    }
	
	    // Busca o ID correto do empréstimo inserido
	    Integer novoEmprestimoID = dataRetriever.verificarEmprestimoDigital(usuario.getId(), livro.getId());
	    if (novoEmprestimoID == null) {
	        throw new SQLException("Não foi possível encontrar o empréstimo digital após a inserção.");
	    }
	
	    logger.info("Empréstimo digital criado com sucesso. ID: {}", novoEmprestimoID);
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

	public List<Emprestimo> listarEmprestimosDigital() throws SQLException {
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimosDigital();

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

	public List<Emprestimo> listarEmprestimosFisico() throws SQLException {
		List<Emprestimo> emprestimos = dataRetriever.listarEmprestimosFisico();

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

	    // Aumenta o estoque do livro ao ser devolvido
	    livro.aumentarEstoque();  // Aumenta o estoque do livro

	    // Atualiza o livro no banco de dados através do livroService
	    boolean estoqueAtualizado = livroService.atualizarLivro(livro.getIsbn(), livro);
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

}
