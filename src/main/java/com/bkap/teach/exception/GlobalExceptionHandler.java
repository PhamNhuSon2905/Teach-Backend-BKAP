//package com.bkap.teach.exception;
//
//
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    // ========== 404 ==========
//    @ExceptionHandler(NotFoundException.class)
//    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
//        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
//    }
//
//    // ========== 400 ==========
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<?> handleBusiness(BusinessException ex) {
//        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
//    }
//
//    // ========== 401 ==========
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
//        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
//    }
//
//    // ========== VALIDATION ==========
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
//        String message = ex.getBindingResult()
//                .getFieldError()
//                .getDefaultMessage();
//        return buildError(HttpStatus.BAD_REQUEST, message);
//    }
//
//    // ========== 500 ==========
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleOther(Exception ex) {
//        return buildError(
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                "Internal server error"
//        );
//    }
//
//    // ========== COMMON ==========
//    private ResponseEntity<Map<String, Object>> buildError(
//            HttpStatus status,
//            String message
//    ) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", message);
//        body.put("status", status.value());
//        return new ResponseEntity<>(body, status);
//    }
//}