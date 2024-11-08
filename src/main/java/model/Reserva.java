package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    private boolean livroReservado;

    private LocalDate dataReserva;
    private LocalDate dataExpiracao;


    // Construtor com ID, utilizado para consultas
    public Reserva(int id, Livro livro, Usuario usuario, boolean livroReservado, LocalDate dataReserva, LocalDate dataExpiracao) {
        if (dataExpiracao.isBefore(dataReserva)) {
            throw new IllegalArgumentException("A data de expiração não pode ser anterior à data da reserva.");
        }
        this.id = id;
        this.livro = livro;
        this.usuario = usuario;
        this.livroReservado = livroReservado;
        this.dataReserva = dataReserva;
        this.dataExpiracao = dataExpiracao;

    }

    // Construtor sem ID, para novas reservas
    public Reserva(Livro livro, Usuario usuario, boolean livroReservado, LocalDate dataReserva, LocalDate dataExpiracao) {
        if (dataExpiracao.isBefore(dataReserva)) {
            throw new IllegalArgumentException("A data de expiração não pode ser anterior à data da reserva.");
        }
        this.livro = livro;
        this.usuario = usuario;
        this.livroReservado = livroReservado;
        this.dataReserva = dataReserva;
        this.dataExpiracao = dataExpiracao;
    }

    // Construtor padrão exigido pelo JPA
    protected Reserva() {}

    // Getters e Setters
    public int getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public LocalDate getDataExpiracao() {
        return dataExpiracao;
    }

    public boolean isLivroReservado() { // Getter para livroReservado
        return livroReservado;
    }

    public void setLivroReservado(boolean livroReservado) { // Setter para livroReservado
        this.livroReservado = livroReservado;
    }

    public boolean isReservaAtiva() {
        LocalDate hoje = LocalDate.now();
        return hoje.isBefore(dataExpiracao) || hoje.isEqual(dataExpiracao);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Reserva)) return false;
        Reserva reserva = (Reserva) obj;
        return id == reserva.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", livro=" + livro.getTitulo() +
                ", usuario=" + usuario.getNome() +
                ", dataReserva=" + dataReserva +
                ", dataExpiracao=" + dataExpiracao +
                ", livroReservado=" + livroReservado + // Inclui no toString
                '}';
    }
}
