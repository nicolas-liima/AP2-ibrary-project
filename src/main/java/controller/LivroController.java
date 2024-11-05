package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import model.Livro;
import service.LivroService;
import exceptions.RecursoNaoEncontradoException;

@RestController
@RequestMapping("/livros")
public class LivroController {
	
    private final LivroService livroService;

    @Autowired
    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listarTodosLivros() {
        List<Livro> livros = livroService.listarTodosLivros();
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Livro>> buscarLivrosPorCategoria(@PathVariable String categoria) {
        try {
            List<Livro> livros = livroService.buscarLivrosPorCategoria(categoria);
            return ResponseEntity.ok(livros);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Livro> buscarLivroPorISBN(@PathVariable String isbn) {
        try {
            Livro livro = livroService.buscarLivroPorISBN(isbn);
            return ResponseEntity.ok(livro);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/nome/{titulo}")
    public ResponseEntity<List<Livro>> buscarLivrosPorNome(@PathVariable String titulo) {
        try {
            List<Livro> livros = livroService.buscarLivrosPorTitulo(titulo);
            return ResponseEntity.ok(livros);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/autor/{autor}")
    public ResponseEntity<List<Livro>> buscarLivrosPorAutor(@PathVariable String autor) {
        try {
            List<Livro> livros = livroService.buscarLivrosPorAutor(autor);
            return ResponseEntity.ok(livros);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<String> salvarLivro(@RequestBody Livro livro) {
        boolean sucesso = livroService.salvarLivro(livro);
        return sucesso ? ResponseEntity.ok("Livro cadastrado com sucesso.") : ResponseEntity.badRequest().body("Erro ao cadastrar livro.");
    }

    @PutMapping
    public ResponseEntity<String> atualizarLivro(@RequestBody Livro livro) {
        boolean sucesso = livroService.atualizarLivro(livro);
        return sucesso ? ResponseEntity.ok("Livro atualizado com sucesso.") : ResponseEntity.badRequest().body("Erro ao atualizar livro.");
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<String> removerLivro(@PathVariable String isbn) {
        try {
            livroService.removerLivro(isbn);
            return ResponseEntity.ok("Livro removido com sucesso.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
