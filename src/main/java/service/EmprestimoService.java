package service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public EmprestimoService(DataInserter dataInserter, DataRetriever dataRetriever, UsuarioService usuarioService, LivroService livroService) {
        this.dataInserter = dataInserter;
        this.dataRetriever = dataRetriever;
        this.usuarioService = usuarioService;
        this.livroService = livroService;
    }

    public Emprestimo realizarEmprestimo(String isbn, String username) {
        logger.info("Iniciando empréstimo para username: " + username + " e ISBN: " + isbn);
        logger.info("Parâmetros recebidos - ISBN: {}, Username: {}", isbn, username);


        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        if (usuario == null) {
            logger.warn("Usuário não encontrado: " + username);
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + username);
        } 
        logger.info("Livro - ISBN: {}", isbn);
        // Buscar o livro pelo ISBN
        Livro livro = livroService.buscarLivroPorISBN(isbn);
        if (livro == null || livro.getQuantidadeEstoque() <= 0) {
        	logger.info("Não achou Quantidade Livro - ISBN: {}", isbn);
            throw new RecursoNaoEncontradoException("Livro não disponível em estoque.");
        }

        logger.info("começa emprestimo Livro - ISBN: {}", livro);
        // Criar o empréstimo com a data atual e a data de devolução
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate dataDevolucaoPrevista = dataEmprestimo.plusDays(14); // Padrão de 14 dias
        Emprestimo emprestimo = new Emprestimo(livro, usuario, dataEmprestimo, dataDevolucaoPrevista);
        logger.info("Emprestimo criado");
        // Inserir o empréstimo no banco de dados
        if (dataInserter.inserirEmprestimo(emprestimo)) {
        	logger.info("inserindo EMprestimo, {}", emprestimo);
            // Reduzir o estoque do livro em 1
            livro.setQuantidadeEstoque(livro.getQuantidadeEstoque() - 1);
            logger.info("Atualizando estoque do livro...");
            livroService.atualizarLivro(isbn, livro);
            logger.info("Estoque do livro atualizado.");
 // Atualiza o livro passando o ISBN da URL e o livro atualizado
            logger.info("Empréstimo realizado com sucesso.");
            return emprestimo; // Retorna o empréstimo criado
        } else {
            logger.info("Erro ao realizar empréstimo.");
            throw new RuntimeException("Erro ao realizar empréstimo."); // Lançar exceção em caso de erro
        }
    }



    public void devolverEmprestimo(int emprestimoId) {
        if (!dataInserter.devolverEmprestimo(emprestimoId)) {
            throw new RecursoNaoEncontradoException("Empréstimo não encontrado com ID: " + emprestimoId);
        }
    }
    public boolean removerEmprestimo(int id) {
        Emprestimo emprestimo = buscarEmprestimoPorId(id);
        return dataInserter.removerEmprestimo(emprestimo.getId());
    }

    public List<Emprestimo> listarTodosEmprestimos() {
        return dataRetriever.listarTodos(Emprestimo.class);
    }

    public List<Emprestimo> buscarEmprestimosPorUsername(String username) {
        List<Emprestimo> emprestimos = dataRetriever.buscarEmprestimosPorUsername(username);
        if (emprestimos.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado para o usuário: " + username);
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

    public List<Emprestimo> listarEmprestimosAtivos() {
        return dataRetriever.listarEmprestimosAtivos();
    }

    public List<Emprestimo> listarEmprestimosFinalizados() {
        return dataRetriever.listarEmprestimosFinalizados();
    }

    public List<Emprestimo> verificarAtrasados(List<Emprestimo> emprestimos) {
        return emprestimos.stream().filter(Emprestimo::isAtrasado).toList();
    }
    
//    public Emprestimo detalharEmprestimo(int emprestimoId) {
//        // Busca o empréstimo pelo ID, mas sem incluir dados detalhados do usuário e livro.
//        Emprestimo emprestimo = buscarEmprestimoPorId(emprestimoId);
//        
//        if (emprestimo == null) {
//            throw new RecursoNaoEncontradoException("Empréstimo não encontrado para o ID: " + emprestimoId);
//        }
//
//        // Busca o livro relacionado ao empréstimo usando o ID do livro
//        Livro livro = buscarLivroPorId(emprestimo.getLivro());
//        if (livro == null) {
//            throw new RecursoNaoEncontradoException("Livro não encontrado para o ID: " + emprestimo.getLivro());
//        }
//
//        // Busca o usuário relacionado ao empréstimo usando o ID do usuário
//        Usuario usuario = usuarioService.buscarUsuarioPorId(emprestimo.getUsuarioId());
//        if (usuario == null) {
//            throw new RecursoNaoEncontradoException("Usuário não encontrado para o ID: " + emprestimo.getUsuarioId());
//        }
//
//        // Associa o livro e o usuário ao empréstimo
//        emprestimo.setLivro(livro);
//        emprestimo.setUsuario(usuario);
//
//        return emprestimo;
//    }

	private Livro buscarLivroPorId(Livro livro) {
		// TODO Auto-generated method stub
		return null;
	}

    
    
}
