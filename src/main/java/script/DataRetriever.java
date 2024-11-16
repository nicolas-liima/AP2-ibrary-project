package script;

import java.sql.Connection;
import java.sql.Date;
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
import model.Reserva;
import model.Usuario;

@Component
public class DataRetriever {

	private static final String SELECT_LIVRO = "SELECT * FROM Livro";
	private static final String SELECT_USUARIO = "SELECT * FROM Usuario";
    private static final String SELECT_RESERVA = "SELECT r.id AS reservaId, r.dataReserva, r.dataExpiracao, " +
    		"u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, u.email AS usuarioEmail, " +
		    "u.endereco AS usuarioEndereco, u.telefone AS usuarioTelefone, u.usuarioAtivo as userAtivo, " +
		    "l.id AS livroId, l.titulo AS livroTitulo, l.isbn AS livroIsbn,l.quantidadeEstoque AS livroQuantidade , " +
		    "l.categoria AS livroCategoria, l.autor AS livroAutor, l.capa as livroCapa " +
    		"FROM Reserva r " +
		    "JOIN Livro l ON r.livro_id = l.id " +
    		"JOIN Usuario u ON r.usuario_id = u.id ";
	private static final String SELECT_EMPRESTIMO ="SELECT e.id AS emprestimoId, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, " +
		    "u.id AS usuarioId, u.username AS usuarioUsername, u.nome AS usuarioNome, u.email AS usuarioEmail, " +
		    "u.endereco AS usuarioEndereco, u.telefone AS usuarioTelefone, u.usuarioAtivo as userAtivo, " +
		    "l.id AS livroId, l.titulo AS livroTitulo, l.isbn AS livroIsbn,l.quantidadeEstoque AS livroQuantidade , " +
		    "l.categoria AS livroCategoria, l.autor AS livroAutor, l.capa AS livroCapa " +
		    "FROM Emprestimo e " +
		    "JOIN Usuario u ON e.usuario_id = u.id " +
		    "JOIN Livro l ON e.livro_id = l.id ";
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
		}

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (tipo == Livro.class) {
					resultados.add(tipo.cast(criarLivro(rs)));
				} else if (tipo == Usuario.class) {
					resultados.add(tipo.cast(criarUsuario(rs)));
				}
			}
		} catch (SQLException e) {
			logger.error("Erro ao realizar busca: {}", e.getMessage(), e); // Adicione um tratamento de erro apropriado
		}
		return resultados; // Retorne a lista de resultados
	}
	
	public List<Emprestimo> listarEmprestimos() {
	    List<Emprestimo> emprestimos = new ArrayList<>();
	    
	    
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_EMPRESTIMO)) {
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
	    String sql = SELECT_EMPRESTIMO + "WHERE u.username = ?"; // Filtrando pelo username

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
	    String sql = SELECT_EMPRESTIMO + "WHERE e.id = ?";

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
	                    rs.getString("livroIsbn"),
	                    rs.getString("LivroCapa")
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

	
    // Método para listar todas as reservas
	public List<Reserva> listarReservas() {
	    List<Reserva> reservas = new ArrayList<>();
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_RESERVA)) {
	        ResultSet rs = pstmt.executeQuery();
	        
	        while (rs.next()) {
	            // Criação do objeto Usuario
	            Usuario usuario = new Usuario(
		                rs.getInt("usuarioId"),
		                rs.getString("usuarioUsername"),
		                rs.getString("usuarioNome")
		                
		            );
		            
		            Livro livro = new Livro(
		                rs.getInt("livroId"),
		                rs.getString("livroTitulo")
		                
		            );
	            // Conversão das datas e outros campos
	            LocalDate dataSolicitacao = rs.getDate("dataReserva").toLocalDate();
	            LocalDate dataRetirada = rs.getDate("dataExpiracao") != null ? rs.getDate("dataExpiracao").toLocalDate() : null;
	            
	            // Criação do objeto Reserva com os dados completos
	            Reserva reserva = new Reserva(
	                rs.getInt("reservaId"), // ID da reserva
	                livro, // Objeto Livro
	                usuario, // Objeto Usuario
	                dataSolicitacao, // Data da solicitação
	                dataRetirada // Data de retirada (pode ser null)
	            );
	            
	            reservas.add(reserva);
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar reservas: {}", e.getMessage(), e);
	    }
	    return reservas;
	}

	public Reserva buscarReservaPorId(int id) {
	    String sql = SELECT_RESERVA + "WHERE r.id = ?";

	    try (Connection conn = databaseManager.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, id);
	        
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	        	
	            Livro livro = new Livro(
	                    rs.getInt("livroId"),
	                    rs.getString("livroTitulo"),
	                    rs.getString("livroAutor"),
	                    rs.getString("livroCategoria"),
	                    rs.getInt("livroQuantidade"),
	                    rs.getString("livroIsbn"),
	                    rs.getString("capa")
	                );
	            
	            Usuario usuario = new Usuario(
	                rs.getInt("usuarioId"),
	                rs.getString("usuarioUsername"),
	                rs.getBoolean("userAtivo"),
	                rs.getString("usuarioNome"),
	                rs.getString("usuarioEmail"),
	                rs.getString("usuarioEndereco"),
	                rs.getString("usuarioTelefone")
	            );

	            return new Reserva(
	                rs.getInt("reservaId"),
	                livro,
	                usuario,
	                rs.getDate("dataReserva").toLocalDate(),
	                rs.getDate("dataExpiracao").toLocalDate()
	            );
	            
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar reserva com ID {}: {}", id, e.getMessage(), e);
	    }
	    
	    return null; // Retorna null se o empréstimo não for encontrado
	}
	
	public List<Reserva> listarReservasAtrasadas() {
	    List<Reserva> reservas = new ArrayList<>();
	    String sql = SELECT_RESERVA + " WHERE r.dataExpiracao < CAST(GETDATE() AS DATE)"; // Filtrando pela reserva com atraso na retirada.

	    try (Connection conn = databaseManager.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet rs = pstmt.executeQuery();	

	        while (rs.next()) {
	            Livro livro = new Livro(rs.getInt("livroId"), rs.getString("livroTitulo"));
	            Usuario usuario = new Usuario(rs.getInt("usuarioId"), rs.getString("usuarioUsername"), rs.getString("usuarioNome"));
	            Reserva reserva = new Reserva(
		                rs.getInt("reservaId"),
		                livro,
		                usuario,
		                rs.getDate("dataReserva").toLocalDate(),
		                rs.getDate("dataExpiracao").toLocalDate()
		            );
	            reservas.add(reserva);
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar reservas atrasadas: {}", e.getMessage(), e);
	    }

	    return reservas;
	}
	
	public List<Reserva> listarReservasPorUsername(String username) {
	    List<Reserva> reservas = new ArrayList<>();
	    String sql = SELECT_RESERVA + "WHERE u.username = ?"; // Filtrando pelo username

	    try (Connection conn = databaseManager.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username); // Definindo o parâmetro username
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            Livro livro = new Livro(rs.getInt("livroId"), rs.getString("livroTitulo"));
	            Usuario usuario = new Usuario(rs.getInt("usuarioId"), rs.getString("usuarioUsername"), rs.getString("usuarioNome"));
	            Reserva reserva = new Reserva(
		                rs.getInt("reservaId"),
		                livro,
		                usuario,
		                rs.getDate("dataReserva").toLocalDate(),
		                rs.getDate("dataExpiracao").toLocalDate()
		            );
	            reservas.add(reserva);
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar reservas para o usuário {}: {}", username, e.getMessage(), e);
	    }

	    return reservas;
	}
	
	private Livro criarLivro(ResultSet rs) throws SQLException {
		int id = rs.getInt("id");
		String titulo = rs.getString("titulo");
		String autor = rs.getString("autor");
		String categoria = rs.getString("categoria");
		int quantidadeEstoque = rs.getInt("quantidadeEstoque");
		String isbn = rs.getString("isbn");
		String capa = rs.getString("capa");
		return new Livro(id, titulo, autor, categoria, quantidadeEstoque, isbn, capa);
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
	    String endereco = rs.getString("endereco");
	    String telefone = rs.getString("telefone");
	    
	    // Verifica se o tipo de usuário está dentro do intervalo do enum
	    if (tipoUsuario < 1 || tipoUsuario > Usuario.TipoUsuario.values().length) {
	        throw new SQLException("Tipo de usuário inválido: " + tipoUsuario);
	    }

	    Usuario.TipoUsuario tipoUsuarioEnum = Usuario.TipoUsuario.values()[tipoUsuario - 1];

	    // Criação do Usuario com base nas informações obtidas do ResultSet
	    return new Usuario(id, username, senha, tipoUsuarioEnum, usuarioAtivo, nome, cpf, email, endereco, telefone);
	}

	// Exemplo de como chamar o método genérico
	public List<Livro> buscarLivrosPorTitulo(String titulo) {
		String sql = "SELECT * FROM Livro WHERE titulo LIKE ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, "%" + titulo + "%");
		return livro;
	}

	public List<Livro> buscarLivrosPorAutor(String autor) {
		String sql = "SELECT * FROM Livro WHERE autor LIKE ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, "%" + autor + "%");
		return livro; 
	}

	public Livro buscarLivroPorISBN(String isbn) {
		String sql = "SELECT * FROM Livro WHERE isbn = ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, isbn);
		return livro.isEmpty() ? null : livro.get(0); 
	}
	
	public Livro buscarLivroPorId(int id) {
		String sql = "SELECT * FROM Livro WHERE id = ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, id);
		return livro.isEmpty() ? null : livro.get(0);
	}


	public List<Livro> buscarLivrosPorCategoria(String categoria) {
		String sql = "SELECT * FROM Livro WHERE categoria = ?";
		List<Livro> livro = buscarPorFiltro(Livro.class, sql, categoria);
		return livro;
	}
	

	public List<Usuario> buscarUsuariosPorNome(String nome) {
		String sql = "SELECT * FROM Usuario WHERE nome LIKE ?";
		List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, "%" + nome + "%");
		return usuario; 
	}

	public Usuario buscarUsuarioPorUsername(String username) {
	    String sql = "SELECT * FROM Usuario WHERE username = ?";
	    List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, username);
	    return usuario.isEmpty() ? null : usuario.get(0); // Retorna o primeiro usuário ou null se não houver
	}
	
	public Usuario buscarUsuarioAtivoPorUsernameESenha(String username, String senha) {
	    String sql = "SELECT * FROM Usuarios WHERE username = ? AND senha = ? AND ativo = 1";
	    Usuario usuario = null; // Inicializa o usuário como nulo

	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, senha);
	        ResultSet rs = pstmt.executeQuery();

	        // Se houver resultados, cria o objeto Usuario
	        if (rs.next()) {
	            usuario = criarUsuario(rs); 
	        }
	    } catch (SQLException e) {
	    	logger.error("Erro ao realizar busca: {}", e.getMessage(), e); // Tratar erro de forma apropriada
	    }

	    return usuario; // Retorna o usuário encontrado ou nulo se não encontrado
	}
	

	public List<Usuario> buscarUsuarioPorCpf(String cpf) {
		String sql = "SELECT * FROM Usuario WHERE cpf LIKE ?";
		List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, cpf + "%");
		return usuario; // Retorna a lista de usuarios 
	}

	public Usuario buscarUsuarioPorId(int id) {
	    String sql = "SELECT * FROM Usuario WHERE id = ?";
	    List<Usuario> livros = buscarPorFiltro(Usuario.class, sql, id);
	    return livros.isEmpty() ? null : livros.get(0); // Retorna o primeiro usuario ou null se não houver
	}

	public List<Emprestimo> listarEmprestimosAtivosPorLivro(int livroId) {
    String sql = SELECT_EMPRESTIMO + "WHERE e.dataDevolucaoEfetiva IS NULL AND e.livro_id = ?"; // Filtra empréstimos ativos pelo ID do livro

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

	public Integer verificarReservaAberta(int usuarioId, int livroId) throws SQLException {
	    String sql = "SELECT id FROM reserva WHERE usuario_id = ? AND livro_id = ?";
	    try (Connection conn = databaseManager.getConnection(); 
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, usuarioId);
	        stmt.setInt(2, livroId);

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("id"); // Retorna o ID da reserva
	            }
	        }
	    } catch (SQLException e) {
	        throw new SQLException("Erro ao verificar reserva aberta", e);
	    }

	    return null; // Retorna null se não encontrar reserva aberta
	}
	
	public List<Reserva> buscarReservasPendentes(int livroId) throws SQLException {
	    String sql = "SELECT * FROM Reserva WHERE livro_id = ? AND dataExpiracao >= ? ORDER BY dataExpiracao ASC";

	    List<Reserva> reservasPendentes = new ArrayList<>();
	    
	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, livroId);  // ID do livro
	        pstmt.setDate(2, Date.valueOf(LocalDate.now()));  // Data atual

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                // Construa a reserva a partir do resultado
	                int reservaId = rs.getInt("id");
	                int usuarioId = rs.getInt("usuario_id");
	                LocalDate dataReserva = rs.getDate("dataReserva").toLocalDate();
	                LocalDate dataExpiracao = rs.getDate("dataExpiracao").toLocalDate();

	                // Recupera o livro e o usuário para a reserva
	                Livro livro = buscarLivroPorId(livroId);
	                Usuario usuario = buscarUsuarioPorId(usuarioId);

	                Reserva reserva = new Reserva(reservaId, livro, usuario, dataReserva, dataExpiracao);
	                reservasPendentes.add(reserva);
	            }
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao buscar reservas para o livro ID {}: {}", livroId, e.getMessage(), e);
	        throw new SQLException("Erro ao buscar reservas.", e);
	    }

	    return reservasPendentes;  // Retorna todas as reservas pendentes
	}


}
