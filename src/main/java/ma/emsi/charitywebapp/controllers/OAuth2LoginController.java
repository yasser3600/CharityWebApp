package ma.emsi.charitywebapp.controllers;

import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.services.RoleService;
import ma.emsi.charitywebapp.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class OAuth2LoginController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/oauth2/callback/google")
    public String handleGoogleCallback(OAuth2AuthenticationToken authentication) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Vérifier si l'utilisateur existe déjà
        if (!utilisateurService.checkEmailExists(email)) {
            // Créer un nouvel utilisateur
            Utilisateur user = new Utilisateur();
            user.setEmail(email);
            String[] nameParts = name.split(" ");
            user.setNom(nameParts[nameParts.length - 1]);
            user.setPrenom(String.join(" ", Arrays.copyOfRange(nameParts, 0, nameParts.length - 1)));
            user.setRole(roleService.getRoleByNom("UTILISATEUR").orElseThrow());
            utilisateurService.saveUser(user);
        }

        return "redirect:/user/profile";
    }
}
