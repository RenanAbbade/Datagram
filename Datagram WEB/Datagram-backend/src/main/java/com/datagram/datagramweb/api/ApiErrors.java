package com.datagram.datagramweb.api;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("message", e.getReason() == null ? "Operação inválida" : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
    var error = e.getBindingResult().getFieldErrors().get(0);
    return ResponseEntity.badRequest()
        .body(Map.of("message", error.getField() + ": " + error.getDefaultMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Map<String, String>> conflict() {
    return ResponseEntity.status(409)
        .body(Map.of("message", "Dados em conflito ou recurso ainda utilizado."));
  }

  @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
  ResponseEntity<Map<String, String>> concurrent() {
    return ResponseEntity.status(409)
        .body(Map.of("message", "O recurso foi alterado. Tente novamente."));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<Map<String, String>> size() {
    return ResponseEntity.status(413)
        .body(Map.of("message", "Arquivo maior que o limite de 5 MB."));
  }
}
