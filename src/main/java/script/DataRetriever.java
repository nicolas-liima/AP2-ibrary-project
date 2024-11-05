package script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
	private static final String SELECT_RESERVA = "SELECT r.id AS reserva_id, r.livro_id, r.cliente_id, r.dataReserva, r.dataExpiracao, l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email FROM Reserva r JOIN Livro l ON r.livro_id = l.id JOIN Usuario u ON r.cliente_id = u.id";
	private static final String SELECT_EMPRESTIMO = "SELECT e.id AS emprestimo_id, e.livro_id, e.cliente_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email FROM Emprestimo e JOIN Livro l ON e.livro_id = l.id JOIN Usuario u ON e.cliente_id = u.id";
	private DatabaseManager dbManager;

    @Autowired
    public DataRetriever(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

	private <T> List<T> buscarPorFiltro(Class<T> tipo, String sql, Object parametro) {
		List<T> resultados = new ArrayList<>();
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
				} else if (tipo == Reserva.class) {
					resultados.add(tipo.cast(criarReserva(rs)));
				} else if (tipo == Emprestimo.class) {
					resultados.add(tipo.cast(criarEmprestimo(rs)));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); // Tratar erro de forma apropriada
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
		} else if (tipo == Reserva.class) {
			sql = SELECT_RESERVA;
		} else if (tipo == Emprestimo.class) {
			sql = SELECT_EMPRESTIMO;
		}

		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (tipo == Livro.class) {
					resultados.add(tipo.cast(criarLivro(rs)));
				} else if (tipo == Usuario.class) {
					resultados.add(tipo.cast(criarUsuario(rs)));
				} else if (tipo == Reserva.class) {
					resultados.add(tipo.cast(criarReserva(rs)));
				} else if (tipo == Emprestimo.class) {
					resultados.add(tipo.cast(criarEmprestimo(rs)));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); // Adicione um tratamento de erro apropriado
		}
		return resultados; // Retorne a lista de resultados
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
	    String nome = rs.getString("nome");
	    String senha = rs.getString("senha");
	    String cpf = rs.getString("cpf");
	    String username = rs.getString("username");
	    String endereco = rs.getString("endereco"); // Agora essas informações serão parte do Usuario
	    String telefone = rs.getString("telefone");
	    String email = rs.getString("email");
	    int tipoUsuario = rs.getInt("tipoUsuario");
	    boolean usuarioAtivo = rs.getBoolean("usuarioAtivo");

	    // Verifica se o tipo de usuário está dentro do intervalo do enum
	    if (tipoUsuario < 1 || tipoUsuario > Usuario.TipoUsuario.values().length) {
	        throw new SQLException("Tipo de usuário inválido: " + tipoUsuario);
	    }

	    Usuario.TipoUsuario tipoUsuarioEnum = Usuario.TipoUsuario.values()[tipoUsuario - 1]; // Supondo que o enum comece em 1

	    // Criação do Usuario com base nas informações obtidas do ResultSet
	    return new Usuario(id, username, senha, tipoUsuarioEnum, usuarioAtivo, nome, cpf, email, endereco, telefone);
	}


	private Reserva criarReserva(ResultSet rs) throws SQLException {
	    int id = rs.getInt("id");

	    // Montar objeto Livro utilizando o método criarLivro
	    Livro livro = criarLivro(rs); // Chamando o método criarLivro para montar o objeto Livro

	    // Montar objeto Usuario (agora representando Cliente)
	    Usuario usuario = criarUsuario(rs); // Chamando o método criarUsuario para montar o objeto Usuario
	    boolean livroReservado = rs.getBoolean("livro_reservado");
	    LocalDate dataReserva = rs.getDate("data_reserva").toLocalDate();
	    LocalDate dataExpiracao = rs.getDate("data_expiracao").toLocalDate();
	    // Criar a Reserva, incluindo o novo parâmetro
	    return new Reserva(id, livro, usuario, livroReservado, dataReserva, dataExpiracao);
	}



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

	    try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, senha);
	        ResultSet rs = pstmt.executeQuery();

	        // Se houver resultados, cria o objeto Usuario
	        if (rs.next()) {
	            usuario = criarUsuario(rs); // Presumindo que criarUsuario trata da criação do objeto
	        }
	    } catch (SQLException e) {
	        e.printStackTrace(); // Tratar erro de forma apropriada
	    }

	    return usuario; // Retorna o usuário encontrado ou nulo se não encontrado
	}
	
private List<Usuario> buscarPorFiltroUsuarioAtivo(String username, String senha) {
	    String sql = "SELECT * FROM Usuarios WHERE username = ? AND senha = ? AND ativo = 1"; // Ajuste conforme sua tabela
	    return buscarPorFiltro(Usuario.class, sql, new Object[]{username, senha});
	}


	public Usuario buscarUsuarioPorCpf(String cpf) {
		String sql = "SELECT * FROM Usuario WHERE cpf = ?";
		List<Usuario> usuario = buscarPorFiltro(Usuario.class, sql, cpf);
		return usuario.isEmpty() ? null : usuario.get(0); // Retorna o primeiro usuário ou null se não houver
	}

	public List<Reserva> buscarReservasPorUsername(String username) {
		String sql = "SELECT r.id AS reserva_id, r.livro_id, r.cliente_id, r.dataReserva, r.dataExpiracao, "
				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
				+ "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
				+ "FROM Reserva r " + "JOIN Livro l ON r.livro_id = l.id " + "JOIN Usuario u ON r.cliente_id = u.id "
				+ "WHERE u.username = ?";

		List<Reserva> reservas = buscarPorFiltro(Reserva.class, sql, username); // Passa o CPF diretamente
		return reservas; // Retorna a lista de reservas (pode ser vazia)
	}


	public Reserva buscarReservaPorId(int id) {
		String sql = "SELECT r.id AS reserva_id, r.livro_id, r.cliente_id, r.dataReserva, r.dataExpiracao, "
				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
				+ "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
				+ "FROM Reserva r " + "JOIN Livro l ON r.livro_id = l.id " + "JOIN Usuario u ON r.cliente_id = u.id "
				+ "WHERE r.id = ?";

		List<Reserva> reserva = buscarPorFiltro(Reserva.class, sql, id);
		return reserva.isEmpty() ? null : reserva.get(0); // Retorna o primeiro resultado ou null se não houver
	}

	public List<Emprestimo> buscarEmprestimosPorUsername(String username) {
		String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.cliente_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
				+ "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
				+ "FROM Emprestimo e " + "JOIN Livro l ON e.livro_id = l.id " + "JOIN Usuario u ON e.cliente_id = u.id "
				+ "WHERE u.username = ?";

		List<Emprestimo> emprestimos = buscarPorFiltro(Emprestimo.class, sql, username);

		// Verifica se a lista está vazia e imprime uma mensagem
		if (emprestimos.isEmpty()) {
			System.out.println("Nenhum empréstimo encontrado para o username: " + username);
		}

		return emprestimos; // Retorna a lista de empréstimos (pode ser vazia)
	}

	public Emprestimo buscarEmprestimoPorId(int id) {
		String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.cliente_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
				+ "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
				+ "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
				+ "FROM Emprestimo e " + "JOIN Livro l ON e.livro_id = l.id " + "JOIN Usuario u ON e.cliente_id = u.id "
				+ "WHERE e.id = ?";

		List<Emprestimo> emprestimos = buscarPorFiltro(Emprestimo.class, sql, id);
		return emprestimos.isEmpty() ? null : emprestimos.get(0); // Retorna o primeiro resultado ou null se não houver
	}

public List<Emprestimo> listarEmprestimosAtivos() {
    String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.cliente_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
            + "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
            + "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
            + "FROM Emprestimo e "
            + "JOIN Livro l ON e.livro_id = l.id "
            + "JOIN Usuario u ON e.cliente_id = u.id "
            + "WHERE e.dataDevolucaoEfetiva IS NULL"; // Filtra empréstimos ativos

    return buscarPorFiltro(Emprestimo.class, sql, null); // Chama o método genérico
}

public List<Emprestimo> listarEmprestimosAtivosPorLivro(int livroId) {
    String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.cliente_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
            + "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
            + "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
            + "FROM Emprestimo e "
            + "JOIN Livro l ON e.livro_id = l.id "
            + "JOIN Usuario u ON e.cliente_id = u.id "
            + "WHERE e.dataDevolucaoEfetiva IS NULL AND e.livro_id = ?"; // Filtra empréstimos ativos pelo ID do livro

    // Chama o método genérico que busca por filtros, passando o ID do livro como parâmetro
    return buscarPorFiltro(Emprestimo.class, sql, new Object[]{livroId});
}

public List<Emprestimo> listarEmprestimosFinalizados() {
    String sql = "SELECT e.id AS emprestimo_id, e.livro_id, e.cliente_id, e.dataEmprestimo, e.dataDevolucaoPrevista, e.dataDevolucaoEfetiva, "
            + "l.titulo, l.autor, l.categoria, l.quantidadeEstoque, l.isbn, "
            + "u.nome AS cliente_nome, u.senha, u.cpf, u.username, u.endereco, u.telefone, u.email "
            + "FROM Emprestimo e "
            + "JOIN Livro l ON e.livro_id = l.id "
            + "JOIN Usuario u ON e.cliente_id = u.id "
            + "WHERE e.dataDevolucaoEfetiva IS NOT NULL"; // Filtra empréstimos finalizados

    return buscarPorFiltro(Emprestimo.class, sql, null); // Chama o método genérico
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

}
