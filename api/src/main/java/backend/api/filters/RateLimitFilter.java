package backend.api.filters;

import backend.api.common.IpUtil;
import backend.api.services.SseEmitterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${rate.limit.requests.per.day:1}")
    private int requestsPerDay;

    @Autowired
    private SseEmitterService sseEmitterService;

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(requestsPerDay, Duration.ofDays(1)))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Skip rate limiting for SSE endpoints
        String requestPath = req.getRequestURI();
        if (requestPath.contains("/message/status/") || requestPath.contains("admin") || requestPath.contains("/resume.pdf")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = IpUtil.getClientIp(req);

        Bucket bucket = cache.computeIfAbsent(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            // Get messageId from header if present
            String messageId = req.getHeader("X-Message-ID");
            
            // Send rate limit error via SSE if messageId exists
            if (messageId != null && !messageId.isEmpty()) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("status", "failed");
                errorData.put("error", "Too many requests - try again after sometime");
                sseEmitterService.sendEvent(messageId, "message", errorData);
            }
            
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Too many requests - try again after sometime.\"}");
        }
    }
}