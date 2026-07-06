package ifba.engsoft.vidaplena.infrastructure.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import ifba.engsoft.vidaplena.domain.service.familia.RegraNegocioException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * Trata exceções de status HTTP explícitas (ResponseStatusException).
	 * 
	 * @param ex exceção de status HTTP	
	 * @param request contexto da requisição
	 * @return ResponseEntity com detalhes do erro e status HTTP apropriado
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		return ResponseEntity.status(status).body(new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				ex.getReason(),
				request.getDescription(false).replace("uri=", ""),
				List.of()));
	}

	/**
	 * Trata exceções de segurança: acesso negado (403) e autenticação ausente/inválida (401).
	 * 
	 * @param ex exceção de segurança
	 * @param request contexto da requisição
	 * @return ResponseEntity com detalhes do erro e status HTTP apropriado
	 */
	@ExceptionHandler({AccessDeniedException.class, AuthenticationCredentialsNotFoundException.class})
	public ResponseEntity<ApiErrorResponse> handleSecurity(RuntimeException ex, WebRequest request) {
		HttpStatus status = ex instanceof AccessDeniedException ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
		return ResponseEntity.status(status).body(new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				ex.getMessage(),
				request.getDescription(false).replace("uri=", ""),
				List.of()));
	}

	/**
	 * Trata exceções de validação de dados (Jakarta Validation / Spring Validator).
	 * Lançada quando um DTO com @Valid falha nas validações (@NotBlank, @NotNull, @Pattern, etc.).
	 * Retorna HTTP 400 Bad Request com detalhes dos campos inválidos.
	 * 
	 * @param ex exceção de validação
	 * @param request contexto da requisição
	 * @return ResponseEntity com detalhes do erro e status HTTP 400
	 */
	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ApiErrorResponse> handleRegraNegocio(RegraNegocioException ex, WebRequest request) {
		List<String> details = List.of(ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(new ApiErrorResponse(
				Instant.now(),
				HttpStatus.UNPROCESSABLE_CONTENT.value(),
				HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(),
				"Regra de negócio violada",
				request.getDescription(false).replace("uri=", ""),
				details));
	}


	/**
	 * Trata exceções de validação de argumentos de método (MethodArgumentNotValidException).
	 * Lançada quando um DTO com @Valid falha nas validações (@NotBlank, @NotNull, @Pattern, etc.).
	 * Retorna HTTP 400 Bad Request com detalhes dos campos inválidos.	
	 * 
	 * @param ex exceção de validação
	 * @param headers cabeçalhos HTTP
	 * @param status status HTTP
	 * @param request contexto da requisição
	 * @return ResponseEntity com detalhes do erro e status HTTP 400
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Dados inválidos",
				request.getDescription(false).replace("uri=", ""),
				details));
	}

	/**
	 * Trata exceções de requisições malformadas (HttpMessageNotReadableException).
	 * Lançada quando o corpo da requisição não pode ser lido ou convertido para o DTO esperado.
	 * Retorna HTTP 400 Bad Request com detalhes do erro.
	 * 
	 * @param ex exceção de requisição malformada
	 * @param headers cabeçalhos HTTP
	 * @param status status HTTP
	 * @param request contexto da requisição
	 * @return ResponseEntity com detalhes do erro e status HTTP 400
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Requisição inválida",
				request.getDescription(false).replace("uri=", ""),
				List.of(ex.getMostSpecificCause().getMessage())));
	}

	/**
	 * Trata exceções inesperadas não capturadas por outros handlers.
	 * 
	 * @param ex exceção inesperada
	 * @param request contexto da requisição
	 * @return ResponseEntity com detalhes do erro e status HTTP 500
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				"Erro interno inesperado",
				request.getDescription(false).replace("uri=", ""),
				List.of(ex.getClass().getSimpleName())));
	}
}