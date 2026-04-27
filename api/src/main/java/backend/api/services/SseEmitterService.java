package backend.api.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Add an emitter for a specific message ID
     */
    public void addEmitter(String messageId, SseEmitter emitter) {
        emitters.put(messageId, emitter);
        
        // Set up timeout and callbacks
        emitter.onTimeout(() -> {
            emitters.remove(messageId);
        });
        
        emitter.onCompletion(() -> {
            emitters.remove(messageId);
        });
    }

    /**
     * Send a status update for a specific message ID
     */
    public void sendStatusUpdate(String messageId, String status) {
        SseEmitter emitter = emitters.get(messageId);
        if (emitter != null) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(messageId)
                        .name("status")
                        .data(status)
                        .reconnectTime(1000);
                
                emitter.send(event);
            } catch (IOException e) {
                emitters.remove(messageId);
            }
        }
    }

    /**
     * Send a complete message with custom fields
     */
    public void sendEvent(String messageId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(messageId);
        if (emitter != null) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(messageId)
                        .name(eventName)
                        .data(data)
                        .reconnectTime(1000);
                
                emitter.send(event);
            } catch (IOException e) {
                emitters.remove(messageId);
            }
        }
        else{
            System.out.println("No emitter found for messageId: " + messageId + ", event: " + eventName);
        }
    }

    /**
     * Remove an emitter
     */
    public void removeEmitter(String messageId) {
        emitters.remove(messageId);
    }
}
