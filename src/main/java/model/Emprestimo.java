package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Emprestimo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucaoEfetiva;

    // Construtor padrão exigido pelo JPA
    public Emprestimo() {
    }

    // Construtor para novo empréstimo (sem data de devolução efetiva)
    public Emprestimo(Livro livro, Usuario usuario, LocalDate dataEmprestimo, LocalDate dataDevolucaoPrevista) {
        validarDatas(dataEmprestimo, dataDevolucaoPrevista);
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucaoEfetiva = null; // Não devolvido inicialmente
    }

    // Construtor completo para consulta de empréstimos
    public Emprestimo(int id, Livro livro, Usuario usuario, LocalDate dataEmprestimo, LocalDate dataDevolucaoPrevista, LocalDate dataDevolucaoEfetiva) {
        validarDatas(dataEmprestimo, dataDevolucaoPrevista);
        this.id = id;
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
    }
    

    private void validarDatas(LocalDate dataEmprestimo, LocalDate dataDevolucaoPrevista) {
        if (dataDevolucaoPrevista.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A data de devolução prevista não pode ser anterior à data do empréstimo.");
        }
    }

    public int getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public LocalDate getDataDevolucaoEfetiva() {
        return dataDevolucaoEfetiva;
    }

    public void registrarDevolucao(LocalDate dataDevolucaoEfetiva) {
        if (dataDevolucaoEfetiva.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A data de devolução não pode ser anterior à data do empréstimo.");
        }
        this.dataDevolucaoEfetiva = dataDevolucaoEfetiva;
    }

    public boolean isEmprestimoAtivo() {
        return dataDevolucaoEfetiva == null;
    }

    public boolean isAtrasado() {
        if (dataDevolucaoEfetiva != null) {
            return dataDevolucaoEfetiva.isAfter(dataDevolucaoPrevista);
        }
        return LocalDate.now().isAfter(dataDevolucaoPrevista);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Emprestimo)) return false;
        Emprestimo emprestimo = (Emprestimo) obj;
        return id != null && id.equals(emprestimo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Emprestimo{" +
                "id=" + id +
                ", livro=" + (livro != null ? livro.getTitulo() : "N/A") +
                ", usuario=" + (usuario != null ? usuario.getNome() : "N/A") +
                ", dataEmprestimo=" + dataEmprestimo +
                ", dataDevolucaoPrevista=" + dataDevolucaoPrevista +
                ", dataDevolucaoEfetiva=" + dataDevolucaoEfetiva +
                '}';
    }
}