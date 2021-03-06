package account.config;

import account.security.SecurityEvents;
import account.security.SecurityLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private SecurityLogService securityLogService;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());

        securityLogService.saveEvent(
                SecurityEvents.ACCESS_DENIED,
                securityLogService.getUserName(),
                request.getRequestURI(),
                request.getRequestURI());

        Map<String, Object> data = new HashMap<>();
        data.put(
                "timestamp",
                LocalDateTime.now().toString());
        data.put(
                "status",
                response.getStatus());
        data.put(
                "error",
                "Forbidden");
        data.put(
                "message",
                "Access Denied!");
        data.put(
                "path",
                request.getRequestURI());

        response.getOutputStream()
                .println(objectMapper.writeValueAsString(data));
    }
}