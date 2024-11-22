package controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exceptions.ObjetoDuplicadoException;
import exceptions.RecursoNaoEncontradoException;
import model.Reserva;
import service.ReservaService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    @Autowired
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Realizar uma reserva
    @PostMapping("/realizar")
    public ResponseEntity<?> realizarReserva(@RequestParam String isbn, @RequestParam String username) {
        try {
            logger.info("Recebendo requisição para realizar reserva: ISBN={}, Username={}", isbn, username);
            int novaReservaID = reservaService.realizarReserva(isbn, username);
            logger.info("Reserva realizada com sucesso. ID da Reserva: {}", novaReservaID);
            return ResponseEntity.ok("Reserva realizada com o Id: " + novaReservaID);
        } catch (ObjetoDuplicadoException e) {
            logger.warn("Falha ao realizar reserva: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RecursoNaoEncontradoException e) {
            logger.warn("Recurso não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao realizar reserva: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }

    // Listar todas as reservas
    @GetMapping
    public ResponseEntity<?> listarTodasReservas() {
        try {
            logger.info("Recebendo requisição para listar todas as reservas.");
            List<Reserva> reservas = reservaService.listarTodasReservas();
            logger.info("Listagem de reservas realizada com sucesso.");
            return ResponseEntity.ok(reservas);
        }catch (RecursoNaoEncontradoException e) {
            logger.warn("Recurso não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar reservas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }

    // Listar reservas por nome de usuário
    @GetMapping("/username/{username}")
    public ResponseEntity<List<Reserva>> listarReservasPorUsername(@PathVariable String username) {
        try {
            List<Reserva> reservas = reservaService.listarReservasPorUsername(username);
            if (reservas.isEmpty()) {
                throw new RecursoNaoEncontradoException("Nenhuma reserva encontrada para o usuário: " + username);
            }
            return ResponseEntity.ok(reservas);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Erro ao listar reservas para o usuário {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Buscar reserva por ID
    @GetMapping("/id/{id}")
    public ResponseEntity<Reserva> buscarReservaPorId(@PathVariable int id) {
        try {
            Reserva reserva = reservaService.buscarReservaPorId(id);
            if (reserva == null) {
                throw new RecursoNaoEncontradoException("Nenhuma reserva encontrada com o ID: " + id);
            }
            return ResponseEntity.ok(reserva);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Erro ao listar reserva para o ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Listar reservas atrasadas
    @GetMapping("/atrasadas")
    public ResponseEntity<?> listarReservasAtrasadas() {
        try {
            logger.info("Recebendo requisição para listar todas as reservas atrasadas.");
            List<Reserva> reservas = reservaService.listarReservasAtrasadas();
            logger.info("Listagem de reservas atrasadas realizada com sucesso.");
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            logger.error("Erro inesperado ao listar reservas atrasadas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }

    // Deletar uma reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarReserva(@PathVariable int id) {
        try {
            reservaService.deletarReserva(id);
            return ResponseEntity.ok("Reserva removida com sucesso.");
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva não encontrada.");
        } catch (Exception e) {
            logger.error("Erro ao remover reserva: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }


    // Endpoint para transformar uma reserva em empréstimo
    @PostMapping("/transformar/{reservaId}")
    public ResponseEntity<?> transformarReservaEmEmprestimo(
            @PathVariable int reservaId) {
        try {
            logger.info("Recebendo requisição para transformar a reserva em empréstimo: ReservaId={}", reservaId);
            
            // Chama o serviço para transformar a reserva em empréstimo
            int emprestimoId = reservaService.transformarReservaEmEmprestimo(reservaId);
            logger.info("Reserva transformada em empréstimo com sucesso. ID do Empréstimo: {}", emprestimoId);
            
            return ResponseEntity.ok("Reserva transformada em empréstimo com o Id: " + emprestimoId);
        } catch (RecursoNaoEncontradoException e) {
            logger.warn("Recurso não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ObjetoDuplicadoException e) {
            logger.warn("Falha ao transformar a reserva em empréstimo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao transformar a reserva em empréstimo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a solicitação.");
        }
    }
    
}
