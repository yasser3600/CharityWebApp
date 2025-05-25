package ma.emsi.charitywebapp.controllers;

import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Role;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.services.OrganisationService;
import ma.emsi.charitywebapp.services.RoleService;
import ma.emsi.charitywebapp.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;

@Controller
public class AuthController {

    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private RoleService roleService;

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(
            @RequestParam String userType,
            @RequestParam String email,
            @RequestParam String motDePasse,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String orgNom,
            @RequestParam(required = false) String orgDescription,
            @RequestParam(required = false) String orgAdresseLegale,
            @RequestParam(required = false) String orgNif,
            @RequestParam(required = false) String orgContactPrincipal,
            Model model) {
        System.out.println(">>> signupSubmit called");

        // Email format
        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            model.addAttribute("error", "Email invalide.");
            return "signup";
        }
        // Email unique
        if (utilisateurService.checkEmailExists(email)) {
            System.out.println(">>> Email existe déjà");
            model.addAttribute("error", "Cet email existe déjà.");
            return "signup";
        }
        // Mot de passe fort
        if (motDePasse.length() < 8 ||
                !motDePasse.matches(".*[A-Z].*") ||
                !motDePasse.matches(".*[a-z].*") ||
                !motDePasse.matches(".*[0-9].*") ||
                !motDePasse.matches(".*[!@#$%^&*()].*")) {
            model.addAttribute("error",
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.");
            return "signup";
        }
        // Téléphone (optionnel, mais si présent doit être numérique)
        if (telephone != null && !telephone.isBlank() && !telephone.matches("\\d{8,15}")) {
            model.addAttribute("error", "Le téléphone doit être numérique (8 à 15 chiffres).");
            return "signup";
        }

        if ("USER".equals(userType)) {
            Role userRole = roleService.getRoleByNom("UTILISATEUR").orElseThrow();
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse(motDePasse);
            user.setTelephone(telephone);
            user.setAdresse(adresse);
            user.setRole(userRole);
            user.setRoles(Arrays.asList("ROLE_UTILISATEUR"));
            utilisateurService.saveUser(user);
            return "redirect:/user/profile"; // Redirect to user profile

        } else if ("ORGANISATION".equals(userType)) {
            Role orgRole = roleService.getRoleByNom("ORGANISATION").orElseThrow();
            Utilisateur orgAdmin = new Utilisateur();
            orgAdmin.setNom(nom);
            orgAdmin.setPrenom(prenom);
            orgAdmin.setEmail(email);
            orgAdmin.setMotDePasse(motDePasse);
            orgAdmin.setTelephone(telephone);
            orgAdmin.setAdresse(adresse);
            orgAdmin.setRole(orgRole);
            utilisateurService.saveUser(orgAdmin);

            Organisation org = new Organisation();
            org.setNom(orgNom);
            org.setDescription(orgDescription);
            org.setAdresseLegale(orgAdresseLegale);
            org.setNumeroIdentificationFiscale(orgNif);
            org.setContactPrincipal(orgContactPrincipal);
            org.setAdmin(orgAdmin);
            org.setValidated(false);
            organisationService.saveOrganisation(org);
            return "redirect:/organisation/profil"; // Redirect to organization profile
        }

        return "redirect:/login?success";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
}
