package ma.emsi.charitywebapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.services.OrganisationService;
import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Categorie;
import ma.emsi.charitywebapp.services.ActionChariteService;
import ma.emsi.charitywebapp.services.CategorieService;

@Controller
public class HomeController {
    @Autowired
    private OrganisationService organisationService;
    
    @Autowired
    private ActionChariteService actionService;
    
    @Autowired
    private CategorieService categorieService;

    @GetMapping("/campaigns")
    public String showCampaigns(Model model) {
        List<ActionCharite> actions = actionService.getAllActiveActions();
        model.addAttribute("actions", actions);
        return "campaigns";
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        // Récupérer les 3 campagnes les plus récentes
        List<ActionCharite> actions = actionService.getAllActiveActions();
        model.addAttribute("actions", actions);
        
        model.addAttribute("currentPath", request.getRequestURI());
        return "index";
    }

    @GetMapping("/about")
    public String showAbout(Model model) {
        return "about";
    }

    @GetMapping("/volunteer")
    public String showVolunteerSignup() {
        return "redirect:/signup?type=USER";
    }
}