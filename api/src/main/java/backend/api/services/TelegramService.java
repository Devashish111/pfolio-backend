package backend.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import backend.api.model.ContactMessage;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Autowired
    private SseEmitterService sseEmitterService;

    /**
     * Send message asynchronously and emit SSE updates
     */
    @Async
    public void sendMessage(ContactMessage message, String messageId) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> body = new HashMap<>();
            body.put("chat_id", chatId);
            String text = "Name: " + message.getName() + "\nEmail: " + message.getEmail() + "\nMessage: " + message.getMessage();
            body.put("text", text);

            restTemplate.postForObject(url, body, String.class);

            // Emit "sent" status on success
            Map<String, Object> successData = new HashMap<>();
            successData.put("status", "sent");
            sseEmitterService.sendEvent(messageId, "message", successData);

        } catch (Exception e) {
            e.printStackTrace();
            
            // Emit "failed" status on error
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("status", "failed");
            errorData.put("error", e.getMessage());
            sseEmitterService.sendEvent(messageId, "message", errorData);
        }
    }

    /**
     * Overloaded method for backward compatibility
     */
    public void sendMessage(ContactMessage message) {
        sendMessage(message, "default");
    }
}

