//package controller;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import exceptions.RecursoNaoEncontradoException;
//import model.Livro;
//import model.Reserva;
//import model.Usuario;
//import service.LivroService;
//import service.ReservaService;
//
//@RestController
//@RequestMapping("/reservas")
//public class ReservaController {
//
//    private final ReservaService reservaService;
//    private final LivroService livroService;
//
//    @Autowired
//    public ReservaController(ReservaService reservaService, LivroService livroService) {
//        this.reservaService = reservaService;
//        this.livroService = livroService;
//    }
//
//    @PostMapping
//    public ResponseEntity<String> cadastrarReserva(@RequestBody Reserva reserva) {
//        try {
//            reservaService.cadastrarReserva(reserva);
//            return ResponseEntity.ok("Reserva cadastrada com sucesso.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Erro ao cadastrar reserva: " + e.getMessage());
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Reserva>> listarReservas() {
//        try {
//            List<Reserva> reservas = reservaService.listarReservas();
//            return ResponseEntity.ok(reservas);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//
//    @GetMapping("/id/{id}")
//    public ResponseEntity<Reserva> buscarReservaPorId(@PathVariable int id) {
//        try {
//            Reserva reserva = reservaService.buscarReservaPorId(id);
//            return ResponseEntity.ok(reserva);
//        } catch (RecursoNaoEncontradoException e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//
//    @GetMapping("/username/{username}")
//    public ResponseEntity<List<Reserva>> buscarReservasPorUsername(@PathVariable String username) {
//        try {
//            List<Reserva> reservas = reservaService.buscarReservasPorUsername(username);
//            return ResponseEntity.ok(reservas);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//
//    @PostMapping("/reservar")
//    public ResponseEntity<String> reservarLivro(@RequestParam String username, @RequestParam String isbn, @RequestParam LocalDate dataReserva) {
//        try {
//            Usuario usuario = new Usuario(username);
//            Livro livro = livroService.buscarLivroPorISBN(isbn);
//            boolean sucesso = reservaService.reservarLivro(usuario, livro, dataReserva);
//            return sucesso ? ResponseEntity.ok("Reserva realizada com sucesso.") : ResponseEntity.badRequest().body("Erro ao reservar livro.");
//        } catch (RecursoNaoEncontradoException e) {
//            return ResponseEntity.badRequest().body("Erro ao reservar livro: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Erro desconhecido: " + e.getMessage());
//        }
//    }
//}
