package backend.api.controller;

import backend.api.services.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/message")
public class SseController {

    @Autowired
    private SseEmitterService sseEmitterService;

    /**
     * SSE endpoint for listening to message status updates
     * Client connects with: new EventSource(`/message/status/{messageId}`)
     */
    @GetMapping("/status/{messageId}")
    public SseEmitter subscribeToStatus(@PathVariable String messageId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minute timeout
        sseEmitterService.addEmitter(messageId, emitter);
        
        try {
            // Send initial connection event
            emitter.send(SseEmitter.event()
                    .id(messageId)
                    .name("connected")
                    .data("Connected to message status stream")
                    .reconnectTime(1000));
        } catch (Exception e) {
            sseEmitterService.removeEmitter(messageId);
        }
        
        return emitter;
    }
}
