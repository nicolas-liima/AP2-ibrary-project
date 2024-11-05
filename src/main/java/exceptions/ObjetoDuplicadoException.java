package exceptions;

public class ObjetoDuplicadoException extends RuntimeException {
    public ObjetoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}