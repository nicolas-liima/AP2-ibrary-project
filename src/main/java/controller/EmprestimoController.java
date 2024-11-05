package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exceptions.RecursoNaoEncontradoException;
import model.Emprestimo;
import service.EmprestimoService;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @Autowired
    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping("/realizar")
    public ResponseEntity<Emprestimo> realizarEmprestimo(@RequestParam String isbn, @RequestParam String username) {
        try {
            Emprestimo emprestimo = emprestimoService.realizarEmprestimo(isbn, username);
            return ResponseEntity.ok(emprestimo); // Retorna o objeto Emprestimo criado
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.badRequest().body(null); // Retorna uma resposta 400 com corpo nulo
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Retorna uma resposta 400 com corpo nulo
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna uma resposta 500 em caso de erro no servidor
        }
    }

    @PostMapping("/devolver/{id}")
    public ResponseEntity<String> devolverEmprestimo(@PathVariable int id) {
        try {
            emprestimoService.devolverEmprestimo(id);
            return ResponseEntity.ok("Empréstimo devolvido com sucesso.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("ID inválido fornecido."); // Mensagem mais específica para ID inválido
        }
    }

    @GetMapping
    public ResponseEntity<List<Emprestimo>> listarTodosEmprestimos() {
        List<Emprestimo> emprestimos = emprestimoService.listarTodosEmprestimos();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<Emprestimo>> buscarEmprestimosPorUsername(@PathVariable String username) {
        try {
            List<Emprestimo> emprestimos = emprestimoService.buscarEmprestimosPorUsername(username);
            return ResponseEntity.ok(emprestimos);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.badRequest().body(null); // Retorna uma resposta 400 com corpo nulo
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Emprestimo> buscarEmprestimoPorId(@PathVariable int id) {
        try {
            Emprestimo emprestimo = emprestimoService.buscarEmprestimoPorId(id);
            return ResponseEntity.ok(emprestimo);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.badRequest().body(null); // Retorna uma resposta 400 com corpo nulo
        }
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Emprestimo>> listarEmprestimosAtivos() {
        List<Emprestimo> emprestimos = emprestimoService.listarEmprestimosAtivos();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/finalizados")
    public ResponseEntity<List<Emprestimo>> listarEmprestimosFinalizados() {
        List<Emprestimo> emprestimos = emprestimoService.listarEmprestimosFinalizados();
        return ResponseEntity.ok(emprestimos);
    }
}
