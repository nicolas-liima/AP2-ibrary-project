//package service;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import exceptions.RecursoNaoEncontradoException;
//import model.Emprestimo;
//import model.Livro;
//import model.Reserva;
//import model.Usuario;
//import script.DataInserter;
//import script.DataRetriever;
//
//@Service
//public class ReservaService {
//
//    private final DataInserter dataInserter;
//    private final DataRetriever dataRetriever;
//
//    @Autowired
//    public ReservaService(DataInserter dataInserter, DataRetriever dataRetriever) {
//        this.dataInserter = dataInserter;
//        this.dataRetriever = dataRetriever;
//    }
//
//    public List<LocalDate> verificarDataDisponivel(int livroId) {
//        List<LocalDate> datasDisponiveis = dataRetriever.verificarDisponibilidade(livroId);
//        return datasDisponiveis;
//    }
//
//    public void cadastrarReserva(Reserva reserva) {
//        dataInserter.inserirReserva(reserva);
//    }
//
//    public List<Reserva> listarReservas() {
//        return dataRetriever.listarTodos(Reserva.class);
//    }
//
//    public Reserva buscarReservaPorId(int reservaId) {
//        Reserva reserva = dataRetriever.buscarReservaPorId(reservaId);
//        if (reserva == null) {
//            throw new RecursoNaoEncontradoException("Reserva não encontrada com ID: " + reservaId);
//        }
//        return reserva;
//    }
//
//    public List<Reserva> buscarReservasPorUsername(String username) {
//        return dataRetriever.buscarReservasPorUsername(username);
//    }
//
//    public boolean reservarLivro(Usuario usuario, Livro livro, LocalDate dataReserva) {
//        if (livro.getQuantidadeEstoque() <= 0) {
//            List<LocalDate> datasDevolucao = dataRetriever.verificarDisponibilidade(livro.getId());
//            if (datasDevolucao.isEmpty()) {
//                throw new RecursoNaoEncontradoException("Livro não disponível para reserva.");
//            } else {
//                throw new RecursoNaoEncontradoException("Livro não disponível. Próxima devolução será em: " + datasDevolucao.get(0));
//            }
//        }
//
//        livro.setQuantidadeEstoque(livro.getQuantidadeEstoque() - 1);
//        Reserva reserva = new Reserva(0, livro, usuario, true, dataReserva, dataReserva.plusDays(7));
//        cadastrarReserva(reserva);
//        return true;
//    }
//
//    public boolean removerReserva(int reservaId, int usuarioId, int tipoUsuario) {
//        if (tipoUsuario == 1) {
//            Reserva reserva = buscarReservaPorId(reservaId);
//            if (reserva.getUsuario().getId() == usuarioId) {
//                return dataInserter.removerReserva(reservaId);
//            } else {
//                throw new IllegalArgumentException("Erro: Você não pode excluir esta reserva.");
//            }
//        } else if (tipoUsuario == 2) {
//            return dataInserter.removerReserva(reservaId);
//        }
//        return false;
//    }
//
//    public boolean finalizarReserva(int reservaId, int usuarioId, int tipoUsuario) {
//        Reserva reserva = buscarReservaPorId(reservaId);
//
//        if (tipoUsuario == 1 && reserva.getUsuario().getId() != usuarioId) {
//            throw new IllegalArgumentException("Erro: Você não pode finalizar esta reserva.");
//        }
//
//        Emprestimo emprestimo = new Emprestimo(reserva.getLivro(), reserva.getUsuario(), reserva.getDataReserva(),
//                reserva.getDataExpiracao());
//
//        if (dataInserter.inserirEmprestimo(emprestimo)) {
//            dataInserter.removerReserva(reservaId);
//            return true;
//        } else {
//            throw new RuntimeException("Erro ao transformar a reserva em empréstimo.");
//        }
//    }
//}
