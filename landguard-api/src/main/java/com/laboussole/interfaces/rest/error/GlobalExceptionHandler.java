package com.laboussole.interfaces.rest.error;

import com.laboussole.domain.exception.CadastralReferenceTakenException;
import com.laboussole.domain.exception.DomainException;
import com.laboussole.domain.exception.EmailAlreadyRegisteredException;
import com.laboussole.domain.exception.InvalidCredentialsException;
import com.laboussole.domain.exception.InvalidRefreshTokenException;
import com.laboussole.domain.exception.ParcelNotFoundException;
import com.laboussole.domain.exception.UserDisabledException;
import com.laboussole.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> emailTaken(EmailAlreadyRegisteredException ex, HttpServletRequest req) {
        return body(HttpStatus.CONFLICT, ex.code(), "Cet e-mail est déjà associé à un compte.", req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> invalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return body(HttpStatus.UNAUTHORIZED, ex.code(), "E-mail ou mot de passe invalide.", req);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<ApiError> invalidRefresh(InvalidRefreshTokenException ex, HttpServletRequest req) {
        return body(HttpStatus.UNAUTHORIZED, ex.code(), "Session expirée, veuillez vous reconnecter.", req);
    }

    @ExceptionHandler(UserDisabledException.class)
    ResponseEntity<ApiError> disabled(UserDisabledException ex, HttpServletRequest req) {
        return body(HttpStatus.FORBIDDEN, ex.code(), "Ce compte est désactivé.", req);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ApiError> userNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, ex.code(), "Utilisateur introuvable.", req);
    }

    @ExceptionHandler(ParcelNotFoundException.class)
    ResponseEntity<ApiError> parcelNotFound(ParcelNotFoundException ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, ex.code(), "Parcelle introuvable.", req);
    }

    @ExceptionHandler(com.laboussole.domain.exception.OcrExtractionNotFoundException.class)
    ResponseEntity<ApiError> ocrExtractionNotFound(
            com.laboussole.domain.exception.OcrExtractionNotFoundException ex, HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, ex.code(), "Analyse OCR introuvable pour ce document.", req);
    }

    @ExceptionHandler(CadastralReferenceTakenException.class)
    ResponseEntity<ApiError> cadastralTaken(CadastralReferenceTakenException ex, HttpServletRequest req) {
        return body(HttpStatus.CONFLICT, ex.code(), "Cette référence cadastrale est déjà enregistrée.", req);
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> generic(DomainException ex, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, ex.code(), ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.of(400, "VALIDATION_FAILED", "Données invalides.", req.getRequestURI(), violations));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> illegal(IllegalArgumentException ex, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", safeMessage(ex), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "MALFORMED_BODY", "Requête JSON invalide.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> unauthenticated(AuthenticationException ex, HttpServletRequest req) {
        if (isEventStream(req)) {
            return null;
        }
        return body(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentification requise.", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest req) {
        // An SSE reconnect with an expired token lands here: no JSON body can
        // be written into text/event-stream — the client refreshes and retries.
        if (isEventStream(req)) {
            return null;
        }
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", "Accès refusé.", req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> uploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD_TOO_LARGE",
                "Le fichier dépasse la taille maximale autorisée (15 Mo).", req);
    }

    /** The client disconnected mid-response (page reload, closed tab) — nothing to write. */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    ResponseEntity<ApiError> clientDisconnected(AsyncRequestNotUsableException ex, HttpServletRequest req) {
        log.debug("Client disconnected during async response on {} {}", req.getMethod(), req.getRequestURI());
        return null;
    }

    /**
     * SSE connections (e.g. {@code /monitoring/stream}) time out as part of
     * their normal lifecycle — the browser's EventSource reconnects on its
     * own, and no JSON body can be written into a {@code text/event-stream}
     * response. Returning {@code null} marks the exception handled without
     * touching the response.
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    ResponseEntity<ApiError> asyncTimeout(HttpServletRequest req) {
        if (isEventStream(req)) {
            return null;
        }
        return body(HttpStatus.SERVICE_UNAVAILABLE, "REQUEST_TIMEOUT", "La requête a expiré.", req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        if (isEventStream(req)) {
            return null;
        }
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erreur interne.", req);
    }

    private static boolean isEventStream(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    private static ResponseEntity<ApiError> body(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, message, req.getRequestURI(), List.of()));
    }

    private static String safeMessage(Exception ex) {
        return ex.getMessage() == null ? "Argument invalide." : ex.getMessage();
    }
}
