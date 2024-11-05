	package exceptions; // ou onde você achar mais apropriado

	public class RecursoNaoEncontradoException extends RuntimeException {
	    public RecursoNaoEncontradoException(String mensagem) {
	        super(mensagem);
	    }
	}

