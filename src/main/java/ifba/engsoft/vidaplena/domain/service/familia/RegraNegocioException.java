package ifba.engsoft.vidaplena.domain.service.familia;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 * Deve ser tratada pelo GlobalExceptionHandler retornando HTTP 422 Unprocessable Entity.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String message) {
        super(message);
    }

    public RegraNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}