package backend.api.controller;

import backend.api.model.ContactMessage;
import backend.api.services.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private TelegramService telegramService;

    @PostMapping("send")  
    public String send(@RequestBody ContactMessage contactMessage) {
        telegramService.sendMessage(contactMessage);
        return "Message sent";
    }
}