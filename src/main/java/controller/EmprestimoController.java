package controller;

import java.sql.SQLException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
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
    public ResponseEntity<?> realizarEmprestimo(@RequestParam String isbn, @RequestParam String username) {
        try {
            logger.info("Recebendo requisição para realizar empréstimo: ISBN={}, Username={}", isbn, username);
            int novoEmprestimoID = emprestimoService.realizarEmprestimo(isbn, username);
            logger.info("Empréstimo realizado com sucesso. ID do Empréstimo: {}", novoEmprestimoID);
            return ResponseEntity.ok(novoEmprestimoID);
        } catch (ObjetoDuplicadoException e) {
            logger.warn("Falha ao realizar empréstimo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RecursoNaoEncontradoException e) {
            logger.warn("Recurso não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao realizar empréstimo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }

    
    @GetMapping
    public ResponseEntity<?> listarTodosEmprestimos() {
        try {
            logger.info("Recebendo requisição para listar todos os empréstimos.");
            List<Emprestimo> emprestimos = emprestimoService.listarTodosEmprestimos();
            logger.info("Listagem de empréstimos realizada com sucesso.");
            return ResponseEntity.ok(emprestimos);
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar empréstimos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<List<Emprestimo>> listarEmprestimosPorUsername(@PathVariable String username) {
        try {
            List<Emprestimo> emprestimos = emprestimoService.listarEmprestimosPorUsername(username);
            
            if (emprestimos.isEmpty()) {
                throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado para o usuário: " + username);
            }
            
            return ResponseEntity.ok(emprestimos); // Retorna 200 com a lista de empréstimos
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retorna 404 se o recurso não for encontrado
        } catch (Exception e) {
            logger.error("Erro ao listar empréstimos para o usuário {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna 500 para erros inesperados
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Emprestimo> buscarEmprestimoPorId(@PathVariable int id) {
        try {
            Emprestimo emprestimo = emprestimoService.buscarEmprestimoPorId(id);
            if (emprestimo == null) {
                throw new RecursoNaoEncontradoException("Nenhum empréstimo encontrado para o usuário: " + id);
            }
            return ResponseEntity.ok(emprestimo);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retorna 404 se o recurso não for encontrado
        } catch (Exception e) {
            logger.error("Erro ao listar empréstimos para o id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna 500 para erros inesperados
        }
    }
    
    @GetMapping("/atrasados")
    public ResponseEntity<?> listarEmprestimosAtrasados() {
        try {
            logger.info("Recebendo requisição para listar todos os empréstimos atrasados.");
            List<Emprestimo> emprestimos = emprestimoService.listarEmprestimosAtrasados();
            logger.info("Listagem de empréstimos atrasados realizada com sucesso.");
            return ResponseEntity.ok(emprestimos);
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar empréstimos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }
    
    @GetMapping("/ativos")
    public ResponseEntity<?> listarEmprestimosAtivos() {
        try {
            logger.info("Recebendo requisição para listar todos os empréstimos atrasados.");
            List<Emprestimo> emprestimos = emprestimoService.listarEmprestimosAtivos();
            logger.info("Listagem de empréstimos atrasados realizada com sucesso.");
            return ResponseEntity.ok(emprestimos);
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar empréstimos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }
    
    @GetMapping("/finalizados")
    public ResponseEntity<?> listarEmprestimosFinalizados() {
        try {
            logger.info("Recebendo requisição para listar todos os empréstimos atrasados.");
            List<Emprestimo> emprestimos = emprestimoService.listarEmprestimosFinalizados();
            logger.info("Listagem de empréstimos atrasados realizada com sucesso.");
            return ResponseEntity.ok(emprestimos);
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar empréstimos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }
 
    @DeleteMapping("/{id}") //NÂO USAR AINDA
    public ResponseEntity<String> removerEmprestimo(@PathVariable int id) {
        try {
            boolean removido = emprestimoService.removerEmprestimo(id);
            return removido ? ResponseEntity.ok("Emprestimo removido com sucesso.") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Emprestimo não encontrado.");
        }
    }
    
    @PutMapping("/devolver/{id}")
    public ResponseEntity<String> devolverEmprestimo(@PathVariable int id) {
        try {
            emprestimoService.devolverEmprestimo(id);
            return ResponseEntity.ok("Empréstimo devolvido com sucesso.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar livro.");
        }
    }

}
