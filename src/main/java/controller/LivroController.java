package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
import model.Livro;
import service.LivroService;

@CrossOrigin(origins = "http://localhost:3000")
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

    @GetMapping("/titulo/{titulo}")
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
        try {
            livroService.salvarLivro(livro);
            return ResponseEntity.ok("Livro cadastrado com sucesso.");
        } catch (ObjetoDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar livro.");
        }
    }


    @PutMapping("/{isbn}")
    public ResponseEntity<String> atualizarLivro(@PathVariable String isbn, @RequestBody Livro livroAtualizado) {
        try {
            // Chama o serviço para atualizar o livro com o ISBN fornecido
            boolean livroAtualizadoComSucesso = livroService.atualizarLivro(isbn, livroAtualizado);

            if (livroAtualizadoComSucesso) {
                return ResponseEntity.ok("Livro atualizado com sucesso.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado.");
            }
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ObjetoDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar livro.");
        }
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
