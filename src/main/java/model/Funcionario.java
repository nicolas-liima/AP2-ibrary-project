//	package model;
//	
//	import jakarta.persistence.Entity;
//	
//	@Entity
//	public class Funcionario extends Usuario {
//	
//	    // Construtor para criação de um novo Funcionario
//	    public Funcionario(int id, String nome, String senha, String cpf, String username, boolean usuarioAtivo) {
//	        super(id, nome, senha, cpf, username, TipoUsuario.FUNCIONARIO, usuarioAtivo); // Chama o construtor da classe base Usuario, tipo FUNCIONARIO
//	    }
//	
//	    // Construtor sem argumentos exigido pelo JPA
//	    protected Funcionario() {
//	        super.setTipo(TipoUsuario.FUNCIONARIO); // Define o tipo automaticamente como FUNCIONARIO
//	    }
//	}
