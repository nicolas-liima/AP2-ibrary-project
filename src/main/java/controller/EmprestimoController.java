package controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exceptions.RecursoNaoEncontradoException;
import exceptions.ResponseError;
import model.Emprestimo;
import service.EmprestimoService;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private static final Logger logger = LoggerFactory.getLogger(EmprestimoController.class);
    

    @Autowired
    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    
    @PostMapping("/realizar")
    public ResponseEntity<Object> realizarEmprestimo(@RequestParam String isbn, @RequestParam String username) {
        try {
            // Log adicional para depuração
            logger.info("Recebendo requisição de empréstimo: ISBN={}, Username={}", isbn, username);

            // Chama o serviço para realizar o empréstimo
            Emprestimo emprestimo = emprestimoService.realizarEmprestimo(isbn, username);

            // Se o empréstimo for criado com sucesso, retorne o objeto emprestado
            if (emprestimo != null) {
                logger.info("Empréstimo realizado com sucesso para o livro: {}", emprestimo.getLivro().getTitulo());
                return ResponseEntity.ok(emprestimo);
            } else {
                // Em caso de falha ao criar o empréstimo, retornar 400
                logger.error("Falha ao realizar o empréstimo.");
                return ResponseEntity.badRequest().body(null); // Retorna um corpo vazio
            }
//        } catch (RecursoNaoEncontradoException e) {
//            logger.error("Erro ao realizar empréstimo: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseError("Recurso não encontrado", e.getMessage()));  // Retorna 404 com mensagem de erro
//        } catch (IllegalArgumentException e) {
//            logger.error("Erro de argumento inválido: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseError("Argumento inválido", e.getMessage()));  // Retorna 400 com mensagem de erro
        } catch (Exception e) {
            logger.error("Erro inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseError("Erro inesperado", e.getMessage()));  // Retorna 500 com mensagem de erro
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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removerEmprestimo(@PathVariable int id) {
        try {
            boolean removido = emprestimoService.removerEmprestimo(id);
            return removido ? ResponseEntity.ok("Emprestimo removido com sucesso.") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Emprestimo não encontrado.");
        }
    }
}
