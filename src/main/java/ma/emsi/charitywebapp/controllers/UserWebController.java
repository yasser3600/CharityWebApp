package ma.emsi.charitywebapp.controllers;

import jakarta.servlet.http.HttpServletRequest; // Changer l'import
import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.services.ActionChariteService;
import ma.emsi.charitywebapp.services.DonService;
import ma.emsi.charitywebapp.services.UtilisateurService;
import ma.emsi.charitywebapp.services.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserWebController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private DonService donService;

    @Autowired
    private ActionChariteService actionChariteService;

    @Autowired
    private CategorieService categorieService;

    @GetMapping("/user/profile")
    public String userProfile(Model model, Authentication authentication, HttpServletRequest request) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("currentPage", "/user/profile"); // Ajoutez cette ligne
        return "user/profile";
    }

    @GetMapping("/user/dons")
    public String userDons(Model model, Authentication authentication) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        List<Don> dons = donService.getDonsByDonateur(user);
        model.addAttribute("dons", dons);
        return "user/dons";
    }

    @GetMapping("/user/actions")
    public String userActions(Model model, Authentication authentication) {
        // Récupérer les campagnes actives
        List<ActionCharite> actions = actionChariteService.getAllActiveActions();
        model.addAttribute("actions", actions);
        model.addAttribute("currentPage", "/user/actions");
        return "user/actions";
    }
}
