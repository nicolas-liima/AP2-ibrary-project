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
    private static final Logger logger = LoggerFactory.getLogger(EmprestimoService.class);

    @Autowired
    public EmprestimoService(DataInserter dataInserter, DataRetriever dataRetriever) {
        this.dataInserter = dataInserter;
        this.dataRetriever = dataRetriever;
    }

    public Emprestimo realizarEmprestimo(String isbn, String username) {
        Livro livro = dataRetriever.buscarLivroPorISBN(isbn);
        if (livro == null || livro.getQuantidadeEstoque() <= 0) {
            throw new RecursoNaoEncontradoException("Livro não disponível em estoque.");
        }

        Usuario usuario = dataRetriever.buscarUsuarioPorUsername(username);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }

        // Criar o empréstimo
        LocalDate dataEmprestimo = LocalDate.now();
        LocalDate dataDevolucaoPrevista = dataEmprestimo.plusDays(14); // Padrão de 14 dias
        Emprestimo emprestimo = new Emprestimo(livro, usuario, dataEmprestimo, dataDevolucaoPrevista);
        if (dataInserter.inserirEmprestimo(emprestimo)) {
            // Diminuir o estoque do livro
            livro.setQuantidadeEstoque(livro.getQuantidadeEstoque() - 1);
            dataInserter.atualizarLivro(livro);
            logger.info("Empréstimo realizado com sucesso.");
            return emprestimo; // Retorna o empréstimo criado
        } else {
            logger.error("Erro ao realizar empréstimo.");
            throw new RuntimeException("Erro ao realizar empréstimo."); // Lançar exceção em caso de erro
        }
    }


    public void devolverEmprestimo(int emprestimoId) {
        if (!dataInserter.devolverEmprestimo(emprestimoId)) {
            throw new RecursoNaoEncontradoException("Empréstimo não encontrado com ID: " + emprestimoId);
        }
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
}
