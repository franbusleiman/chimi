package pet.liro.chimi.api.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pet.liro.chimi.config.ChimiProperties;

import java.io.IOException;

@Component
@Order(1)
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final ChimiProperties props;

    public InternalApiKeyFilter(ChimiProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("X-Internal-Api-Key");
        if (header == null || !header.equals(props.internal().apiKey())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"forbidden\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
