package model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(unique = true, nullable = false)
    private String username;


    @Column(nullable = false) // Adicionando restrições para a senha
    private String senha;
    
    @Enumerated(EnumType.STRING)  // Use STRING para garantir que o valor seja convertido corretamente para o nome da enum
    @Column(name = "tipoUsuario") 
    private TipoUsuario tipoUsuario;
    
    @Column
    protected boolean usuarioAtivo;
    
    @Column(nullable = false, length = 100) // Adicionando restrições para o nome
    private String nome;

    @Column(nullable = false)
    private String cpf;

    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = true, length = 255)
    private String endereco;

    @Column(nullable = true, length = 15)
    private String telefone;
    
    


    public enum TipoUsuario {
        CLIENTE,
        FUNCIONARIO;

		}
    

    // Construtor completo com ID
    public Usuario(int id, String username, String senha,TipoUsuario tipoUsuario, boolean usuarioAtivo, String nome, String cpf, String email, String endereco, String telefone) {
        this.id = id;
        this.username = username;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.usuarioAtivo = usuarioAtivo;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.endereco = endereco;
        this.telefone = telefone;
    }
    
    // Construtor sem ID
    public Usuario(String username, String senha,TipoUsuario tipoUsuario, boolean usuarioAtivo, String nome, String cpf, String email, String endereco, String telefone) {
        this.username = username;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.usuarioAtivo = usuarioAtivo;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.endereco = endereco;
        this.telefone = telefone;
    }
    public Usuario(String username,TipoUsuario tipoUsuario, boolean usuarioAtivo, String nome, String cpf, String email, String endereco, String telefone) {
        this.username = username;
        this.tipoUsuario = tipoUsuario;
        this.usuarioAtivo = usuarioAtivo;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.endereco = endereco;
        this.telefone = telefone;
    }
    
    
    public Usuario(String username,String senha, boolean usuarioAtivo) {
        this.username = username;
        this.senha = senha;
        this.usuarioAtivo = usuarioAtivo;
    }
    public Usuario(String username) {
        this.username = username;

    }
    
    public Usuario(int id,String username, String nome) {
    	
    	this.id = id;
    	this.username = username;
        this.nome = nome;
    }
    
    public Usuario(int id, String username, boolean usuarioAtivo, String nome, String email, String endereco, String telefone) {
        this.id = id;
        this.username = username;
        this.usuarioAtivo = usuarioAtivo;
        this.nome = nome;
        this.email = email;
        this.endereco = endereco;
        this.telefone = telefone;
    }

    // Construtor padrão exigido pelo JPA
    protected Usuario() {}

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public boolean isUsuarioAtivo() {
        return usuarioAtivo;
    }

    public void setUsuarioAtivo(boolean usuarioAtivo) {
        this.usuarioAtivo = usuarioAtivo;
    }

    public boolean validarSenha(String senha) {
        return this.senha.equals(senha);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", endereco='" + endereco + '\'' +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                ", tipo=" + tipoUsuario +
                ", usuarioAtivo=" + usuarioAtivo +
                '}';
    }
}
