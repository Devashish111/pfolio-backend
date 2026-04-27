package backend.api.config;

import backend.api.services.SseEmitterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private SseEmitterService sseEmitterService;

    /**
     * Handle all unhandled exceptions (except for SSE endpoints)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, HttpServletRequest request) {
        
        // Skip if this is an SSE endpoint - exceptions will be logged separately
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/message/status/")) {
            // Log but don't return response - SSE client will handle timeout
            return null;
        }
        
        // Try to get messageId from request header
        String messageId = request.getHeader("X-Message-ID");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        response.put("timestamp", System.currentTimeMillis());
        
        // Send SSE notification if messageId exists
        if (messageId != null && !messageId.isEmpty()) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("status", "failed");
            errorData.put("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
            sseEmitterService.sendEvent(messageId, "message", errorData);
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
