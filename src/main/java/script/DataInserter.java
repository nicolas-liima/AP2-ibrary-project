package script;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import model.Emprestimo;
import model.Livro;
import model.Reserva;
import model.Usuario;
import model.Usuario.TipoUsuario;

@Component
public class DataInserter {

	private final DatabaseManager databaseManager;
	private static final Logger logger = LoggerFactory.getLogger(DataInserter.class);

	@Autowired
	public DataInserter(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	// Método para inserir um único livro
	public boolean inserirLivro(Livro livro) {
		String sql = "INSERT INTO Livro (titulo, autor, categoria, quantidadeEstoque, isbn) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, livro.getTitulo());
			pstmt.setString(2, livro.getAutor());
			pstmt.setString(3, livro.getCategoria());
			pstmt.setInt(4, livro.getQuantidadeEstoque());
			pstmt.setString(5, livro.getIsbn());

			pstmt.executeUpdate();
			logger.info("Livro inserido com sucesso: {}", livro.getTitulo());
			return true;

		} catch (SQLException e) {
			logger.error("Erro ao inserir livro: {}", e.getMessage(), e);
			return false;
		}
	}

	// Método para atualizar um livro existente
	public boolean atualizarLivro(String isbnAntigo, Livro livroAtualizado) {
	    String sql = "UPDATE Livro SET titulo = ?, autor = ?, categoria = ?, quantidadeEstoque = ?, isbn = ? WHERE isbn = ?";

	    try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, livroAtualizado.getTitulo());
	        pstmt.setString(2, livroAtualizado.getAutor());
	        pstmt.setString(3, livroAtualizado.getCategoria());
	        pstmt.setInt(4, livroAtualizado.getQuantidadeEstoque());
	        pstmt.setString(5, livroAtualizado.getIsbn());  // Novo ISBN do corpo
	        pstmt.setString(6, isbnAntigo);                 // ISBN original da URL

	        int rowsUpdated = pstmt.executeUpdate();
	        if (rowsUpdated > 0) {
	            logger.info("Livro atualizado com sucesso: {}", livroAtualizado.getTitulo());
	            return true;
	        } else {
	            logger.warn("Erro: Livro com ISBN {} não encontrado.", isbnAntigo);
	            return false;
	        }

	    } catch (SQLException e) {
	        logger.error("Erro ao atualizar livro: {}", e.getMessage(), e);
	        return false;
	    }
	}


	// Método para remover um livro pelo ISBN
	public boolean removerLivroPorIsbn(String isbn) {
		String sql = "DELETE FROM Livro WHERE isbn = ?";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, isbn);
			int rowsDeleted = pstmt.executeUpdate();

			if (rowsDeleted > 0) {
				logger.info("Livro com ISBN {} removido com sucesso.", isbn);
				return true;
			} else {
				logger.warn("Erro: Livro com ISBN {} não encontrado.", isbn);
				return false;
			}

		} catch (SQLException e) {
			logger.error("Erro ao remover livro com ISBN {}: {}", isbn, e.getMessage(), e);
			return false;
		}
	}

	// Método para inserir um único usuário
	public boolean inserirUsuario(Usuario usuario) {
		String sql = "INSERT INTO Usuario (nome, senha, cpf, username, tipoUsuario, usuarioAtivo, email, endereco, telefone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, usuario.getNome());
			pstmt.setString(2, usuario.getSenha());
			pstmt.setString(3, usuario.getCpf());
			pstmt.setString(4, usuario.getUsername());
			int tipoUsuario = usuario.getTipoUsuario() == Usuario.TipoUsuario.CLIENTE ? 1 : 2;
			pstmt.setInt(5, tipoUsuario);
			pstmt.setBoolean(6, usuario.isUsuarioAtivo());
			pstmt.setString(7, usuario.getEmail());
			pstmt.setString(8, usuario.getEndereco());
			pstmt.setString(9, usuario.getTelefone());

			pstmt.executeUpdate();
			logger.info("Usuário inserido com sucesso: {}", usuario.getNome());
			return true;

		} catch (SQLException e) {
			logger.error("Erro ao inserir usuário:", e);
			return false;
		}
	}

	@Transactional
	public boolean atualizarUsuario(String username, Usuario usuario) {
	    String sql = "UPDATE Usuario SET nome = ?, senha = ?, endereco = ?, telefone = ?, email = ?, tipoUsuario = ?, usuarioAtivo = ?, username = ? WHERE username = ?";

	    try (Connection conn = databaseManager.getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setString(1, usuario.getNome());
	        pstmt.setString(2, usuario.getSenha());
	        pstmt.setString(3, usuario.getEndereco());
	        pstmt.setString(4, usuario.getTelefone());
	        pstmt.setString(5, usuario.getEmail());

	        // Lógica para garantir que o valor de tipoUsuario seja o esperado pelo banco
	        if (usuario.getTipoUsuario() == TipoUsuario.CLIENTE) {
	            pstmt.setInt(6, 1); // CLIENTE = 1
	        } else if (usuario.getTipoUsuario() == TipoUsuario.FUNCIONARIO) {
	            pstmt.setInt(6, 2); // FUNCIONARIO = 2
	        }

	        pstmt.setBoolean(7, usuario.isUsuarioAtivo());
	        pstmt.setString(8, usuario.getUsername());
	        pstmt.setString(9, username);

	        int rowsUpdated = pstmt.executeUpdate();

	        if (rowsUpdated > 0) {
	            logger.info("Usuário com Username {} atualizado com sucesso.", usuario.getUsername());
	            return true;
	        } else {
	            logger.warn("Nenhuma linha atualizada para o usuário com Username {}.", usuario.getUsername());
	            return false;
	        }
	    } catch (SQLException e) {
	        logger.error("Erro ao atualizar usuário com Username {}: {}", usuario.getUsername(), e.getMessage(), e);
	        return false;
	    }
	}






	public boolean removerUsuario(Usuario usuario) {
		String sql = "DELETE FROM Usuario WHERE username = ?";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, usuario.getUsername());
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Usuário com username '{}' removido com sucesso.", usuario.getUsername());
				return true;
			} else {
				logger.warn("Nenhum usuário encontrado com username '{}'.", usuario.getUsername());
				return false;
			}

		} catch (SQLException e) {
			logger.error("Erro ao remover usuário com username '{}': {}", usuario.getUsername(), e.getMessage(), e);
			return false;
		}
	}

	// Método para inserir uma nova reserva no banco de dados
	public boolean inserirReserva(Reserva reserva) {
		String sql = "INSERT INTO Reserva (livro_id, cliente_id, livroReservado, dataReserva, dataExpiracao) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, reserva.getLivro().getId());
			pstmt.setInt(2, reserva.getUsuario().getId());
			pstmt.setBoolean(3, reserva.isLivroReservado());
			pstmt.setDate(4, Date.valueOf(reserva.getDataReserva()));
			pstmt.setDate(5, Date.valueOf(reserva.getDataExpiracao()));

			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Reserva inserida com sucesso para o cliente '{}' no livro '{}'.",
						reserva.getUsuario().getId(), reserva.getLivro().getId());
				return true;
			} else {
				logger.warn("A inserção da reserva não afetou nenhuma linha.");
				return false;
			}
		} catch (SQLException e) {
			logger.error("Erro ao inserir reserva para o cliente '{}' no livro '{}': {}", reserva.getUsuario().getId(),
					reserva.getLivro().getId(), e.getMessage(), e);
			return false;
		}
	}

	public boolean removerReserva(int reservaId) {
		String sql = "DELETE FROM Reserva WHERE id = ?";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, reservaId);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Reserva com ID '{}' removida com sucesso.", reservaId);
				return true;
			} else {
				logger.warn("Nenhuma reserva encontrada com o ID '{}'.", reservaId);
				return false;
			}
		} catch (SQLException e) {
			logger.error("Erro ao remover reserva com ID '{}': {}", reservaId, e.getMessage(), e);
			return false;
		}
	}

	// Método para inserir um empréstimo
	public boolean inserirEmprestimo(Emprestimo emprestimo) {
		String sql = "INSERT INTO Emprestimo (livro_id, usuario_id, dataEmprestimo, dataDevolucaoPrevista) VALUES (?, ?, ?, ?)";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			logger.info("Tentando inserir empréstimo no banco de dados: {}", emprestimo);


			pstmt.setInt(1, emprestimo.getLivro().getId());
			pstmt.setInt(2, emprestimo.getUsuario().getId());
			pstmt.setDate(3, Date.valueOf(emprestimo.getDataEmprestimo()));
			pstmt.setDate(4, Date.valueOf(emprestimo.getDataDevolucaoPrevista()));

			int rowsAffected = pstmt.executeUpdate();
			logger.info("Executado: {}", emprestimo);

			if (rowsAffected > 0) {
				logger.info("Empréstimo inserido com sucesso para o livro '{}' pelo cliente '{}'.",
						emprestimo.getLivro().getTitulo(), emprestimo.getUsuario().getNome());
				return true;
			} else {
				logger.info("Falha ao inserir o empréstimo: nenhuma linha foi afetada.");
				return false;
			}

		} catch (SQLException e) {
			logger.info("Erro ao inserir empréstimo para o livro '{}': {}", emprestimo.getLivro().getTitulo(), e.getMessage(), e);

			return false;
		}
	}

	public boolean devolverEmprestimo(int emprestimoId) {
		String sql = "UPDATE Emprestimo SET dataDevolucaoEfetiva = ? WHERE id = ?";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setDate(1, Date.valueOf(LocalDate.now()));
			pstmt.setInt(2, emprestimoId);

			int rowsUpdated = pstmt.executeUpdate();
			if (rowsUpdated > 0) {
				logger.info("Empréstimo com ID {} devolvido com sucesso na data {}.", emprestimoId, LocalDate.now());
				return true;
			} else {
				logger.warn("Falha na devolução: Empréstimo com ID {} não encontrado.", emprestimoId);
				return false;
			}

		} catch (SQLException e) {
			logger.error("Erro ao devolver empréstimo com ID {}: {}", emprestimoId, e.getMessage(), e);
			return false;
		}
	}

	public boolean removerEmprestimo(int emprestimoId) {
		String sql = "DELETE FROM Emprestimo WHERE id = ?";

		try (Connection conn = databaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, emprestimoId);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				logger.info("Empréstimo com ID {} removido com sucesso.", emprestimoId);
				return true;
			} else {
				logger.warn("Falha na remoção: Empréstimo com ID {} não encontrado.", emprestimoId);
				return false;
			}
		} catch (SQLException e) {
			logger.error("Erro ao remover empréstimo com ID {}: {}", emprestimoId, e.getMessage(), e);
			return false;
		}
	}
}
