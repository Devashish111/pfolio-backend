package backend.api.services;

import org.springframework.beans.factory.annotation.Value;
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

    public void sendMessage(ContactMessage message) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> body = new HashMap<>();
            body.put("chat_id", chatId);
            String text = "Name: " + message.getName() + "\nEmail: " + message.getEmail() + "\nMessage: " + message.getMessage();
            body.put("text", text);

            restTemplate.postForObject(url, body, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}