package ma.emsi.charitywebapp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Récupère les rôles de l'utilisateur
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Redirige en fonction du rôle
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/dashboard");
        }
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ORGANISATION"))) {
            response.sendRedirect("/organisation/profil");
        }
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_UTILISATEUR"))) {
            response.sendRedirect("/user/profile");
        }
        else {
            response.sendRedirect("/"); // Redirection par défaut
        }
    }
}
