//package model;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//
//@Entity
//@Table(name = "clientes") // Define o nome da tabela como "clientes" no banco de dados
//public class Cliente extends Usuario {
//
//    @Column(nullable = false, length = 255)
//    private String endereco;
//
//    @Column(nullable = false, length = 15)
//    private String telefone;
//
//    @Column(nullable = false, unique = true, length = 100)
//    private String email;
//
//    // Construtor padrão para uso do JPA
// 
//
//    // Construtor principal para inicialização completa
//    public Cliente(int id, String nome, String senha, String cpf, String username, String endereco, String telefone, String email,boolean usuarioAtivo) {
//        super(id, nome, senha, cpf, username, TipoUsuario.CLIENTE, usuarioAtivo); // Certifique-se de que o tipo é CLIENTE
//        this.endereco = endereco;
//        this.telefone = telefone;
//        this.email = email;
//    }
//
//    // Construtor padrão exigido pelo JPA
//    protected Cliente() {} 
//
//    // Construtor sem ID, útil para criação de novos clientes
//    public Cliente(String nome, String senha, String cpf, String username, String endereco, String telefone, String email, boolean usuarioAtivo) {
//        super(nome, senha, cpf, username, TipoUsuario.CLIENTE, usuarioAtivo); // Chama o construtor da classe base Usuario, tipo CLIENTE
//        this.endereco = endereco;
//        this.telefone = telefone;
//        this.email = email;
//    }
//
//    // Getters e Setters
//    public String getEndereco() {
//        return endereco;
//    }
//
//    public void setEndereco(String endereco) {
//        this.endereco = endereco;
//    }
//
//    public String getTelefone() {
//        return telefone;
//    }
//
//    public void setTelefone(String telefone) {
//        this.telefone = telefone;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    @Override
//    public String toString() {
//        return "Cliente{" +
//                "id=" + getId() +
//                ", nome='" + getNome() + '\'' +
//                ", cpf='" + getCpf() + '\'' +
//                ", endereco='" + endereco + '\'' +
//                ", telefone='" + telefone + '\'' +
//                ", email='" + email + '\'' +
//                '}';
//    }
//}
