package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate dataReserva;

    @Column(nullable = false)
    private LocalDate dataExpiracao;


    // Construtor padrão exigido pelo JPA
    protected Reserva() {}

    // Construtor para criar uma nova reserva
    public Reserva(Livro livro, Usuario usuario, LocalDate dataReserva, LocalDate dataExpiracao) {
        validarDatas(dataReserva, dataExpiracao);
        this.livro = livro;
        this.usuario = usuario;
        this.dataReserva = dataReserva;
        this.dataExpiracao = dataExpiracao;
    }
    // Construtor para criar uma nova reserva
    public Reserva(int id, Livro livro, Usuario usuario, LocalDate dataReserva, LocalDate dataExpiracao) {
        validarDatas(dataReserva, dataExpiracao);
        this.id = id;
        this.livro = livro;
        this.usuario = usuario;
        this.dataReserva = dataReserva;
        this.dataExpiracao = dataExpiracao;

    }

    private void validarDatas(LocalDate dataReserva, LocalDate dataExpiracao) {
        if (dataExpiracao.isBefore(dataReserva)) {
            throw new IllegalArgumentException("A data de expiração não pode ser anterior à data de reserva.");
        }
    }

    public Integer getId() {
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


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Reserva)) return false;
        Reserva reserva = (Reserva) obj;
        return id != null && id.equals(reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", livro=" + (livro != null ? livro.getTitulo() : "N/A") +
                ", usuario=" + (usuario != null ? usuario.getNome() : "N/A") +
                ", dataReserva=" + dataReserva +
                ", dataExpiracao=" + dataExpiracao +
                '}';
    }
}
