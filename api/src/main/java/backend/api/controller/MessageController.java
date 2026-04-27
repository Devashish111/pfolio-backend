package backend.api.controller;

import backend.api.model.ContactMessage;
import backend.api.services.TelegramService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private TelegramService telegramService;

    @PostMapping("send")  
    public ResponseEntity<?> send(@Valid @RequestBody ContactMessage contactMessage,
                                   @RequestHeader("X-Message-ID") String messageId) {
        
        // Send message asynchronously and emit SSE updates
        telegramService.sendMessage(contactMessage, messageId);
        
        // Return the message ID to the client
        Map<String, String> response = new HashMap<>();
        response.put("messageId", messageId);
        response.put("message", "Message queued for sending");
        
        return ResponseEntity.accepted().body(response);
    }
}