package service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
import model.Livro;
import script.DataInserter;
import script.DataRetriever;

@Service
public class LivroService {
    private final DataInserter dataInserter;
    private final DataRetriever dataRetriever;
    private static final Logger logger = LoggerFactory.getLogger(LivroService.class);

    @Autowired
    public LivroService(DataInserter dataInserter, DataRetriever dataRetriever) {
        this.dataInserter = dataInserter;
        this.dataRetriever = dataRetriever;
    }

    public List<Livro> buscarLivrosPorCategoria(String categoria) {
        List<Livro> livros = dataRetriever.buscarLivrosPorCategoria(categoria);
        if (livros.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum livro encontrado na categoria: " + categoria);
        }
        return livros;
    }

    public Livro buscarLivroPorISBN(String isbn) {
    	logger.info("Bucando Livro - ISBN: {}", isbn);
        Livro livro = dataRetriever.buscarLivroPorISBN(isbn);
        if (livro == null) {
        	logger.info("Não achou - ISBN: {}", isbn);
            throw new RecursoNaoEncontradoException("Livro não encontrado com ISBN: " + isbn);
        }
        logger.info("Achou Livro - ISBN: {}", isbn);
        return livro;
    }

    public List<Livro> buscarLivrosPorTitulo(String titulo) {
        List<Livro> livros = dataRetriever.buscarLivrosPorTitulo(titulo);
        if (livros.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum livro encontrado com o nome: " + titulo);
        }
        return livros;
    }

    public List<Livro> buscarLivrosPorAutor(String autor) {
        List<Livro> livros = dataRetriever.buscarLivrosPorAutor(autor);
        if (livros.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum livro encontrado do autor: " + autor);
        }
        return livros;
    }

    public List<Livro> listarTodosLivros() {
        return dataRetriever.listarTodos(Livro.class);
    }

    public boolean salvarLivro(Livro livro) {
        
        // Verifica se já existe um usuário com o mesmo username
        if (dataRetriever.buscarLivroPorISBN(livro.getIsbn()) != null) {
            throw new ObjetoDuplicadoException("ISBN já está cadastrado.");
        }

        return dataInserter.inserirLivro(livro);
    }
  
    
    public boolean atualizarLivro(String isbn, Livro livroAtualizado) {
        // Busca o livro existente com o isbn antigo
        Livro livroExistente = dataRetriever.buscarLivroPorISBN(isbn);
        if (livroExistente == null) {
            throw new RecursoNaoEncontradoException("Livro não encontrado.");
        }

        // Verifica se o novo isbn é diferente do atual e se já existe no banco
        if (!livroExistente.getIsbn().equals(livroAtualizado.getIsbn())
                && livroExisteComIsbn(livroAtualizado.getIsbn())) {
            throw new ObjetoDuplicadoException("ISBN já está em uso.");
        }

        // Atualiza o usuário no banco
        return dataInserter.atualizarLivro(isbn, livroAtualizado);
    }

    
    public boolean livroExisteComIsbn(String isbn) {
        return dataRetriever.buscarLivroPorISBN(isbn) != null;
    }

    

    public boolean removerLivro(String isbn) {
        Livro livro = buscarLivroPorISBN(isbn);
        return dataInserter.removerLivroPorIsbn(livro.getIsbn());
    }
}
