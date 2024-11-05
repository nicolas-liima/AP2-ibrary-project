package service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exceptions.RecursoNaoEncontradoException;
import model.Livro;
import script.DataInserter;
import script.DataRetriever;

@Service
public class LivroService {
    private final DataInserter dataInserter;
    private final DataRetriever dataRetriever;

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
        Livro livro = dataRetriever.buscarLivroPorISBN(isbn);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro não encontrado com ISBN: " + isbn);
        }
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
        return dataInserter.inserirLivro(livro);
    }

    public boolean atualizarLivro(Livro livro) {
        return dataInserter.atualizarLivro(livro);
    }

    public boolean removerLivro(String isbn) {
        Livro livro = buscarLivroPorISBN(isbn);
        return dataInserter.removerLivroPorIsbn(livro.getIsbn());
    }
}
