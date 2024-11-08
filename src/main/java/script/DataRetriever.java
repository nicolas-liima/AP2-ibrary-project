package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import model.Emprestimo;
import model.Livro;
import model.Usuario;

@Component
public class DataRetriever {

	private static final String SELECT_LIVRO = "SELECT * FROM Livro";
	private static final String SELECT_USUARIO = "SELECT * FROM Usuario";
//	private static final String SELECT_RESERVA = "SELECT r.id AS reserva_id, r.livro_id, r.cliente_id, r.dataReserva, r.dataExpiracao, l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email FROM Reserva r JOIN Livro l ON r.livro_id = l.id JOIN Usuario u ON r.cliente_id = u.id";
	private static final String SELECT_EMPRESTIMO = "SELECT * FROM Emprestimo";
//	private static final String SELECT_EMPRESTIMO = "SELECT e.id AS emprestimo_id, e.livro_id, e.usuario_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email FROM Emprestimo e JOIN Livro l ON e.livro_id = l.id JOIN Usuario u ON e.usuario_id = u.id";
    private static final String SELECT_EMPRESTIMO_ABERTO = "SELECT * FROM Emprestimo WHERE usuario_id = ? AND livro_id = ? AND dataDevolucaoEfetiva IS NULL";

	private static final Logger logger = LoggerFactory.getLogger(DataRetriever.class);
	private DatabaseManager databaseManager;

    @Autowired
    public DataRetriever(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

	private <T> List<T> buscarPorFiltro(Class<T> tipo, String sql, Object parametro) {
		List<T> resultados = new ArrayList<>();
		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			if (parametro != null) {
				if (parametro instanceof String) {
					pstmt.setString(1, (String) parametro);
				} else if (parametro instanceof Integer) {
					pstmt.setInt(1, (Integer) parametro);
				}
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (tipo == Livro.class) {
					resultados.add(tipo.cast(criarLivro(rs)));
				} else if (tipo == Usuario.class) {
					resultados.add(tipo.cast(criarUsuario(rs)));
//				} else if (tipo == Reserva.class) {
//					resultados.add(tipo.cast(criarReserva(rs)));
				} else if (tipo == Emprestimo.class) {
					resultados.add(tipo.cast(criarEmprestimo(rs)));
				}
			}
		} catch (SQLException e) {
			logger.error("Erro ao realizar busca: {}", e.getMessage(), e); // Tratar erro de forma apropriada
		}
		return resultados; // Retorna a lista de resultados
	}

	public <T> List<T> listarTodos(Class<T> tipo) {
		List<T> resultados = new ArrayList<>();
		String sql = "";

		if (tipo == Livro.class) {
			sql = SELECT_LIVRO;
		} else if (tipo == Usuario.class) {
			sql = SELECT_USUARIO;
//		} else if (tipo == Reserva.class) {
//			sql = SELECT_RESERVA;
		} else if (tipo == Emprestimo.class) {
			sql = SELECT_EMPRESTIMO;
		}

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (tipo == Livro.class) {
					resultados.add(tipo.cast(criarLivro(rs)));
				} else if (tipo == Usuario.class) {
					resultados.add(tipo.cast(criarUsuario(rs)));
//				} else if (tipo == Reserva.class) {
//					resultados.add(tipo.cast(criarReserva(rs)));
				} else if (tipo == Emprestimo.class) {
					resultados.add(tipo.cast(criarEmprestimo(rs)));
				}
			}
		} catch (SQLException e) {
			logger.error("Erro ao realizar busca: {}", e.getMessage(), e); // Adicione um tratamento de erro apropriado
		}
		return resultados; // Retorne a lista de resultados
	}
	
	public List<Emprestimo> listarEmprestimos() {
	    List<Emprestimo> emprestimos = new ArrayList<>();
	    
	    // Consulta com JOINs para buscar todas as informações em uma única query
	    String sql = "SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva,  " +
	             "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome,  " +
	             "l.id AS livroId, l.titulo AS livroTitulo  " +
	             "FROM Emprestimo e " +
	             "JOIN Usuario u ON e.usuario_id = u.id  " +
	             "JOIN Livro l ON e.livro_id = l.id";
	    
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet rs = pstmt.executeQuery();
	        
	        while (rs.next()) {
	            // Cria os objetos Usuario e Livro com as informações necessárias
	            Usuario usuario = new Usuario(
	                rs.getInt("usuarioId"),
	                rs.getString("usuarioUsername"),
	                rs.getString("usuarioNome")
	                
	            );
	            
	            Livro livro = new Livro(
	                rs.getInt("livroId"),
	                rs.getString("livroTitulo")
	                
	            );
	            LocalDate dataEmprestimo = rs.getDate("dataEmprestimo").toLocalDate();
	            LocalDate dataDevolucaoPrevista = rs.getDate("dataDevolucaoPrevista").toLocalDate();
	            LocalDate dataDevolucaoEfetiva = rs.getDate("dataDevolucaoEfetiva") != null ? 
	                                             rs.getDate("dataDevolucaoEfetiva").toLocalDate() : null;
	      
	            
	            // Cria o objeto Emprestimo com os objetos completos de Usuario e Livro
	            Emprestimo emprestimo = new Emprestimo(
	                    rs.getInt("emprestimoId"),
	                    livro,
	                    usuario,
	                    dataEmprestimo,
	                    dataDevolucaoPrevista,
	                    dataDevolucaoEfetiva
	                );
	            
	            emprestimos.add(emprestimo);
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar empréstimos: {}", e.getMessage(), e);
	    }
	    
	    return emprestimos;
	}
	public List<Emprestimo> listarEmprestimosPorUsername(String username) {
	    List<Emprestimo> emprestimos = new ArrayList<>();
	    String sql = "SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, " +
	                 "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, " +
	                 "l.id AS livroId, l.titulo AS livroTitulo " +
	                 "FROM Emprestimo e " +
	                 "JOIN Usuario u ON e.usuario_id = u.id " +
	                 "JOIN Livro l ON e.livro_id = l.id " +
	                 "WHERE u.username = ?"; // Filtrando pelo username

	    try (Connection conn = databaseManager.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username); // Definindo o parâmetro username
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            Livro livro = new Livro(rs.getInt("livroId"), rs.getString("livroTitulo"));
	            Usuario usuario = new Usuario(rs.getInt("usuarioId"), rs.getString("usuarioUsername"), rs.getString("usuarioNome"));
	            Emprestimo emprestimo = new Emprestimo(
	                rs.getInt("emprestimoId"),
	                livro,
	                usuario,
	                rs.getDate("dataEmprestimo").toLocalDate(),
	                rs.getDate("dataDevolucaoPrevista").toLocalDate(),
	                rs.getDate("dataDevolucaoEfetiva") != null ? rs.getDate("dataDevolucaoEfetiva").toLocalDate() : null
	            );
	            emprestimos.add(emprestimo);
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar empréstimos para o usuário {}: {}", username, e.getMessage(), e);
	    }

	    return emprestimos;
	}
	
	public Emprestimo buscarEmprestimoPorId(int id) {
	    String sql = "SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, " +
	                 "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, u.email AS usuarioEmail, " +
	                 "u.endereco AS usuarioEndereco, u.telefone AS usuarioTelefone, u.usuarioAtivo as userAtivo, " +
	                 "l.id AS livroId, l.titulo AS livroTitulo, l.isbn AS livroIsbn,l.quantidadeEstoque AS livroQuantidade , " +
	                 "l.categoria AS livroCategoria, l.autor AS livroAutor " +
	                 "FROM Emprestimo e " +
	                 "JOIN Usuario u ON e.usuario_id = u.id " +
	                 "JOIN Livro l ON e.livro_id = l.id " +
	                 "WHERE e.id = ?";

	    try (Connection conn = databaseManager.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, id);
	        
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            Usuario usuario = new Usuario(
	                rs.getInt("usuarioId"),
	                rs.getString("usuarioUsername"),
	                rs.getBoolean("userAtivo"),
	                rs.getString("usuarioNome"),
	                rs.getString("usuarioEmail"),
	                rs.getString("usuarioEndereco"),
	                rs.getString("usuarioTelefone")
	            );

	            Livro livro = new Livro(
	                    rs.getInt("livroId"),
	                    rs.getString("livroTitulo"),
	                    rs.getString("livroAutor"),
	                    rs.getString("livroCategoria"),
	                    rs.getInt("livroQuantidade"),
	                    rs.getString("livroIsbn")
	                );

	            return new Emprestimo(
	                rs.getInt("emprestimoId"),
	                livro,
	                usuario,
	                rs.getDate("dataEmprestimo").toLocalDate(),
	                rs.getDate("dataDevolucaoPrevista").toLocalDate(),
	                rs.getDate("dataDevolucaoEfetiva") != null ? rs.getDate("dataDevolucaoEfetiva").toLocalDate() : null
	            );
	            
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar empréstimo com ID {}: {}", id, e.getMessage(), e);
	    }
	    
	    return null; // Retorna null se o empréstimo não for encontrado
	}
	
	public List<Emprestimo> listarEmprestimosAtrasados() throws SQLException {
		List<Emprestimo> emprestimos = new ArrayList<>();
	    String sql = "SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, " +
	                 "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, " +
	                 "l.id AS livroId, l.titulo AS livroTitulo " +
	                 "FROM Emprestimo e " +
	                 "JOIN Usuario u ON e.usuario_id = u.id " +
	                 "JOIN Livro l ON e.livro_id = l.id " +
	                 "WHERE e.dataDevolucaoPrevista < CAST(GETDATE() AS DATE) AND e.dataDevolucaoEfetiva IS NULL";
	    
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet rs = pstmt.executeQuery();
	        
	        while (rs.next()) {
	            Livro livro = new Livro(rs.getInt("livroId"), rs.getString("livroTitulo"));
	            Usuario usuario = new Usuario(rs.getInt("usuarioId"), rs.getString("usuarioUsername"), rs.getString("usuarioNome"));
	            Emprestimo emprestimo = new Emprestimo(
	                rs.getInt("emprestimoId"),
	                livro,
	                usuario,
	                rs.getDate("dataEmprestimo").toLocalDate(),
	                rs.getDate("dataDevolucaoPrevista").toLocalDate(),
	                rs.getDate("dataDevolucaoEfetiva") != null ? rs.getDate("dataDevolucaoEfetiva").toLocalDate() : null
	            );
	            emprestimos.add(emprestimo);
	    }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar empréstimos atrasados: {}", e.getMessage(), e);
	    }

	    return emprestimos;
	}
	public List<Emprestimo> listarEmprestimosAtivos() throws SQLException {
	    List<Emprestimo> emprestimos = new ArrayList<>();
	    String sql = "SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, " +
	                 "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, " +
	                 "l.id AS livroId, l.titulo AS livroTitulo " +
	                 "FROM Emprestimo e " +
	                 "JOIN Usuario u ON e.usuario_id = u.id " +
	                 "JOIN Livro l ON e.livro_id = l.id " +
	                 "WHERE e.dataDevolucaoEfetiva IS NULL";
	    
	    
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet rs = pstmt.executeQuery();
	        
	        while (rs.next()) {
	            Livro livro = new Livro(rs.getInt("livroId"), rs.getString("livroTitulo"));
	            Usuario usuario = new Usuario(rs.getInt("usuarioId"), rs.getString("usuarioUsername"), rs.getString("usuarioNome"));
	            Emprestimo emprestimo = new Emprestimo(
	                rs.getInt("emprestimoId"),
	                livro,
	                usuario,
	                rs.getDate("dataEmprestimo").toLocalDate(),
	                rs.getDate("dataDevolucaoPrevista").toLocalDate(),
	                rs.getDate("dataDevolucaoEfetiva") != null ? rs.getDate("dataDevolucaoEfetiva").toLocalDate() : null
	            );
	            emprestimos.add(emprestimo);
	    }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar empréstimos atrasados: {}", e.getMessage(), e);
	    }

	    return emprestimos;
	}
	public List<Emprestimo> listarEmprestimosFinalizados() throws SQLException {
	    List<Emprestimo> emprestimos = new ArrayList<>();
	    String sql = "SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, " +
	                 "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, " +
	                 "l.id AS livroId, l.titulo AS livroTitulo " +
	                 "FROM Emprestimo e " +
	                 "JOIN Usuario u ON e.usuario_id = u.id " +
	                 "JOIN Livro l ON e.livro_id = l.id " +
	                 "WHERE e.dataDevolucaoEfetiva IS NOT NULL";
	    
	    
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet rs = pstmt.executeQuery();
	        
	        while (rs.next()) {
	            Livro livro = new Livro(rs.getInt("livroId"), rs.getString("livroTitulo"));
	            Usuario usuario = new Usuario(rs.getInt("usuarioId"), rs.getString("usuarioUsername"), rs.getString("usuarioNome"));
	            Emprestimo emprestimo = new Emprestimo(
	                rs.getInt("emprestimoId"),
	                livro,
	                usuario,
	                rs.getDate("dataEmprestimo").toLocalDate(),
	                rs.getDate("dataDevolucaoPrevista").toLocalDate(),
	                rs.getDate("dataDevolucaoEfetiva").toLocalDate()
	            );
	            emprestimos.add(emprestimo);
	    }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar empréstimos atrasados: {}", e.getMessage(), e);
	    }

	    return emprestimos;
	}

	



	private Livro criarLivro(ResultSet rs) throws SQLException {
		int id = rs.getInt("id");
		String titulo = rs.getString("titulo");
		String autor = rs.getString("autor");
		String categoria = rs.getString("categoria");
		int quantidadeEstoque = rs.getInt("quantidadeEstoque");
		String isbn = rs.getString("isbn");
		return new Livro(id, titulo, autor, categoria, quantidadeEstoque, isbn);
	}

	private Usuario criarUsuario(ResultSet rs) throws SQLException {
	    int id = rs.getInt("id");
	    String username = rs.getString("username");
	    String senha = rs.getString("senha");
	    int tipoUsuario = rs.getInt("tipoUsuario");
	    boolean usuarioAtivo = rs.getBoolean("usuarioAtivo");
	    String nome = rs.getString("nome");	    
	    String cpf = rs.getString("cpf");
	    String email = rs.getString("email");    
	    String endereco = rs.getString("endereco"); // Agora essas informações serão parte do Usuario
	    String telefone = rs.getString("telefone");
	    
	    // Verifica se o tipo de usuário está dentro do intervalo do enum
	    if (tipoUsuario < 1 || tipoUsuario > Usuario.TipoUsuario.values().length) {
	        throw new SQLException("Tipo de usuário inválido: " + tipoUsuario);
	    }

	    Usuario.TipoUsuario tipoUsuarioEnum = Usuario.TipoUsuario.values()[tipoUsuario - 1]; // Supondo que o enum comece em 1

	    // Criação do Usuario com base nas informações obtidas do ResultSet
	    return new Usuario(id, username, senha, tipoUsuarioEnum, usuarioAtivo, nome, cpf, email, endereco, telefone);
	}


//	private Reserva criarReserva(ResultSet rs) throws SQLException {
//	    int id = rs.getInt("id");
//
//	    // Montar objeto Livro utilizando o método criarLivro
//	    Livro livro = criarLivro(rs); // Chamando o método criarLivro para montar o objeto Livro
//
//	    // Montar objeto Usuario (agora representando Cliente)
//	    Usuario usuario = criarUsuario(rs); // Chamando o método criarUsuario para montar o objeto Usuario
//	    boolean livroReservado = rs.getBoolean("livro_reservado");
//	    LocalDate dataReserva = rs.getDate("data_reserva").toLocalDate();
//	    LocalDate dataExpiracao = rs.getDate("data_expiracao").toLocalDate();
//	    // Criar a Reserva, incluindo o novo parâmetro
//	    return new Reserva(id, livro, usuario, livroReservado, dataReserva, dataExpiracao);
//	}



	private Emprestimo criarEmprestimo(ResultSet rs) throws SQLException {
	    int id = rs.getInt("id");

	    // Montar objeto Livro utilizando o método criarLivro
	    Livro livro = criarLivro(rs); // Chamando o método criarLivro para montar o objeto Livro

	    // Montar objeto Usuario utilizando o método criarUsuario
	    Usuario usuario = criarUsuario(rs); // Chamando o método criarUsuario para montar o objeto Usuario

	    LocalDate dataEmprestimo = rs.getDate("data_emprestimo").toLocalDate();
	    LocalDate dataDevolucaoPrevista = rs.getDate("data_devolucao_prevista").toLocalDate();

	    LocalDate dataDevolucaoEfetiva = rs.getDate("data_devolucao_efetiva") != null
	            ? rs.getDate("data_devolucao_efetiva").toLocalDate()
	            : null;

	    // Criar o Empréstimo
	    return new Emprestimo(id, livro, usuario, dataEmprestimo, dataDevolucaoPrevista, dataDevolucaoEfetiva);
	}
	
//	private Emprestimo criarEmprestimo(ResultSet rs) throws SQLException {
//	    int id = rs.getInt("id");
//
//	    int livroId = rs.getInt("livro_id");
//	    int usuarioId = rs.getInt("usuario_id");
//
//	    // Buscar o Livro e o Usuário completos pelos seus IDs
//	    Livro livro = buscarLivroPorId(livroId);  // Buscar Livro pelo ID
//	    Usuario usuario = buscarUsuarioPorId(usuarioId);  // Buscar Usuário pelo ID
//
//	    if (livro == null || usuario == null) {
//	        throw new SQLException("Livro ou Usuário não encontrado para o empréstimo.");
//	    }
//
//	    LocalDate dataEmprestimo = rs.getDate("data_emprestimo").toLocalDate();
//	    LocalDate dataDevolucaoPrevista = rs.getDate("data_devolucao_prevista").toLocalDate();
//
//	    LocalDate dataDevolucaoEfetiva = rs.getDate("data_devolucao_efetiva") != null
//	            ? rs.getDate("data_devolucao_efetiva").toLocalDate()
//	            : null;
//
//	    // Criar e retornar o objeto Emprestimo com os objetos completos de Livro e Usuario
//	    return new Emprestimo(id, livro, usuario, dataEmprestimo, dataDevolucaoPrevista, dataDevolucaoEfetiva);
//	}




	// Exemplo de como chamar o método genérico
	public List<Livro> buscarLivrosPorTitulo(String titulo) {
		String sql = "SELECT * FROM Livro WHERE titulo LIKE ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, "%" + titulo + "%");
		return livro; // Retorna a lista de reservas (pode ser vazia)
	}

	public List<Livro> buscarLivrosPorAutor(String autor) {
		String sql = "SELECT * FROM Livro WHERE autor LIKE ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, "%" + autor + "%");
		return livro; // Retorna a lista de reservas (pode ser vazia)
	}

	public Livro buscarLivroPorISBN(String isbn) {
		String sql = "SELECT * FROM Livro WHERE isbn = ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, isbn);
		return livro.isEmpty() ? null : livro.get(0); // Retorna o primeiro usuário ou null se não houver
	}
	
	public Livro buscarLivroPorId(int id) {
		String sql = "SELECT * FROM Livro WHERE id = ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, id);
		return livro.isEmpty() ? null : livro.get(0); // Retorna o primeiro usuário ou null se não houver
	}


	public List<Livro> buscarLivrosPorCategoria(String categoria) {
		String sql = "SELECT * FROM Livro WHERE categoria = ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, categoria);
		return livro; // Retorna a lista de reservas (pode ser vazia)
	}
	

	public List<Usuario> buscarUsuariosPorNome(String nome) {
		String sql = "SELECT * FROM Usuario WHERE nome LIKE ?";
		List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, "%" + nome + "%");
		return usuario; // Retorna a lista de reservas (pode ser vazia)
	}

	public Usuario buscarUsuarioPorUsername(String username) {
	    String sql = "SELECT * FROM Usuario WHERE username = ?";
	    List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, username); // Passa o username sem aspas
	    return usuario.isEmpty() ? null : usuario.get(0); // Retorna o primeiro usuário ou null se não houver
	}
	
	public Usuario buscarUsuarioAtivoPorUsernameESenha(String username, String senha) {
	    String sql = "SELECT * FROM Usuarios WHERE username = ? AND senha = ? AND ativo = 1"; // Ajuste conforme sua tabela
	    Usuario usuario = null; // Inicializa o usuário como nulo

	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, senha);
	        ResultSet rs = pstmt.executeQuery();

	        // Se houver resultados, cria o objeto Usuario
	        if (rs.next()) {
	            usuario = criarUsuario(rs); // Presumindo que criarUsuario trata da criação do objeto
	        }
	    } catch (SQLException e) {
	    	logger.error("Erro ao realizar busca: {}", e.getMessage(), e); // Tratar erro de forma apropriada
	    }

	    return usuario; // Retorna o usuário encontrado ou nulo se não encontrado
	}
	
//	private List<Usuario> buscarPorFiltroUsuarioAtivo(String username, String senha) {
//	    String sql = "SELECT * FROM Usuarios WHERE username = ? AND senha = ? AND ativo = 1"; // Ajuste conforme sua tabela
//	    return buscarPorFiltro(Usuario.class, sql, new Object[]{username, senha});
//	}


	public List<Usuario> buscarUsuarioPorCpf(String cpf) {
		String sql = "SELECT * FROM Usuario WHERE cpf LIKE ?";
		List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, cpf + "%");
		return usuario; // Retorna a lista de reservas (pode ser vazia) 
	}

	public Usuario buscarUsuarioPorId(int id) {
	    String sql = "SELECT * FROM Usuario WHERE id = ?";
	    List<Usuario> livros = buscarPorFiltro(Usuario.class, sql, id);
	    return livros.isEmpty() ? null : livros.get(0); // Retorna o primeiro usuario ou null se não houver
	}

//	public List<Reserva> buscarReservasPorUsername(String username) {
//		String sql = "SELECT r.id AS reserva_id, r.livro_id, r.usuario_id, r.dataReserva, r.dataExpiracao, "
//				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
//				+ "u.nome AS usuario_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
//				+ "FROM Reserva r " + "JOIN Livro l ON r.livro_id = l.id " + "JOIN Usuario u ON r.usuario_id = u.id "
//				+ "WHERE u.username = ?";
//
//		List<Reserva> reservas = buscarPorFiltro(Reserva.class, sql, username); // Passa o CPF diretamente
//		return reservas; // Retorna a lista de reservas (pode ser vazia)
//	}


//	public Reserva buscarReservaPorId(int id) {
//		String sql = "SELECT r.id AS reserva_id, r.livro_id, r.usuario_id, r.dataReserva, r.dataExpiracao, "
//				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
//				+ "u.nome AS usuario_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
//				+ "FROM Reserva r " + "JOIN Livro l ON r.livro_id = l.id " + "JOIN Usuario u ON r.usuario_id = u.id "
//				+ "WHERE r.id = ?";
//
//		List<Reserva> reserva = buscarPorFiltro(Reserva.class, sql, id);
//		return reserva.isEmpty() ? null : reserva.get(0); // Retorna o primeiro resultado ou null se não houver
//	}

	public List<Emprestimo> buscarEmprestimosPorUsername(String username) {
		String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.usuario_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
				+ "u.nome AS usuario_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
				+ "FROM Emprestimo e " + "JOIN Livro l ON e.livro_id = l.id " + "JOIN Usuario u ON e.usuario_id = u.id "
				+ "WHERE u.username = ?";

		List<Emprestimo> emprestimos = buscarPorFiltro(Emprestimo.class, sql, username);

		// Verifica se a lista está vazia e imprime uma mensagem
		if (emprestimos.isEmpty()) {
			System.out.println("Nenhum empréstimo encontrado para o username: " + username);
		}

		return emprestimos; // Retorna a lista de empréstimos (pode ser vazia)
	}

public List<Emprestimo> listarEmprestimosAtivosPorLivro(int livroId) {
    String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.usuario_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
            + "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
            + "u.nome AS usuario_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
            + "FROM Emprestimo e "
            + "JOIN Livro l ON e.livro_id = l.id "
            + "JOIN Usuario u ON e.usuario_id = u.id "
            + "WHERE e.dataDevolucaoEfetiva IS NULL AND e.livro_id = ?"; // Filtra empréstimos ativos pelo ID do livro

    // Chama o método genérico que busca por filtros, passando o ID do livro como parâmetro
    return buscarPorFiltro(Emprestimo.class, sql, new Object[]{livroId});
}


public List<LocalDate> verificarDisponibilidade(int livroId) {
    Livro livro = buscarLivroPorId(livroId); // Método que busca o livro pelo ID
    List<LocalDate> datasDisponiveis = new ArrayList<>();

    if (livro.getQuantidadeEstoque() > 0) {
        // Se o livro está em estoque, retorna as próximas 5 datas a partir de hoje
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            datasDisponiveis.add(hoje.plusDays(i)); // Adiciona as próximas 5 datas
        }
        return datasDisponiveis;
    } else {
        // Se não há estoque, verifica a data de devolução mais próxima dos empréstimos
        List<Emprestimo> emprestimosAtivos = listarEmprestimosAtivosPorLivro(livroId); // Método que busca empréstimos ativos para o livro
        if (!emprestimosAtivos.isEmpty()) {
            for (Emprestimo emprestimo : emprestimosAtivos) {
                LocalDate dataDevolucao = emprestimo.getDataDevolucaoPrevista();
                if (!datasDisponiveis.contains(dataDevolucao)) {
                    datasDisponiveis.add(dataDevolucao);
                }
            }
            // Ordena as datas de devolução e retorna as 5 mais próximas
            datasDisponiveis.sort(LocalDate::compareTo);
            return datasDisponiveis.size() > 5 ? datasDisponiveis.subList(0, 5) : datasDisponiveis;
        }
    }
    return datasDisponiveis; // Retorna a lista vazia se não houver datas
}


public Integer verificarEmprestimoAberto(int usuarioId, int livroId) {
    try (Connection conn = databaseManager.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(SELECT_EMPRESTIMO_ABERTO)) {
        
        pstmt.setInt(1, usuarioId);  // Passa o ID do cliente
        pstmt.setInt(2, livroId);    // Passa o ID do livro
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            // Se a consulta retornar algum resultado, significa que o empréstimo está em aberto
            logger.info("Empréstimo em aberto encontrado: Cliente ID = {}, Livro ID = {}", usuarioId, livroId);
            return rs.getInt("id"); // Emprestimo em aberto encontrado
        }
    } catch (SQLException e) {
        logger.error("Erro ao verificar empréstimo em aberto: {}", e.getMessage(), e);
    }
    return null; // Nenhum empréstimo em aberto encontrado
}

}
