package model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String titulo;

    private String autor;

    private String categoria;

    private int quantidadeEstoque;

    @Column(unique = true, nullable = false)
    private String isbn;
    
    private String capa;

    // Construtor completo com ID
    public Livro(int id, String titulo, String autor, String categoria, int quantidadeEstoque, String isbn, String capa) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.quantidadeEstoque = quantidadeEstoque;
        this.isbn = isbn;
        this.capa = capa;
    }
    public Livro(String titulo,String isbn) {
        this.titulo = titulo;
        this.isbn = isbn;
    }
    public Livro(String titulo,String isbn, int quantidadeEstoque) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.quantidadeEstoque = quantidadeEstoque;
    }
    

    // Construtor sem ID
    public Livro(String titulo, String autor, String categoria, int quantidadeEstoque, String isbn, String capa) {
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.quantidadeEstoque = quantidadeEstoque;
        this.isbn = isbn;
        this.capa = capa;
    }
    
    public Livro(int id, String titulo, String autor, String categoria, String isbn, String capa) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.isbn = isbn;
        this.capa = capa;
    }
    
    
    public Livro(int id, String titulo) {
        this.id = id;
        this.titulo = titulo;

    }

    // Construtor padrão exigido pelo JPA
    protected Livro() {}

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    // Verifica se o livro está disponível para empréstimo
    public boolean isDisponivelParaEmprestimo() {
        return quantidadeEstoque > 0;
    }

    // Reduz o estoque ao registrar um empréstimo
    public void reduzirEstoque() {
        if (quantidadeEstoque > 0) {
            quantidadeEstoque--;
        }
    }

    // Aumenta o estoque ao devolver um livro
    public void aumentarEstoque() {
        quantidadeEstoque++;
    }

    // Atualiza o estoque com uma quantidade específica
    public void atualizarEstoque(int quantidade) {
        this.quantidadeEstoque += quantidade;
        if (this.quantidadeEstoque < 0) {
            this.quantidadeEstoque = 0;
        }
    }

    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", categoria='" + categoria + '\'' +
                ", quantidadeEstoque=" + quantidadeEstoque +
                ", isbn='" + isbn + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Livro)) return false;
        Livro livro = (Livro) obj;
        return id == livro.id || Objects.equals(isbn, livro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
}
