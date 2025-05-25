package ma.emsi.charitywebapp.controllers;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.services.ActionChariteService;
import ma.emsi.charitywebapp.services.OrganisationService;
import ma.emsi.charitywebapp.services.ParticipationService;
import ma.emsi.charitywebapp.services.UtilisateurService;
import ma.emsi.charitywebapp.services.ImageStorageService;
import ma.emsi.charitywebapp.entities.Don;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.security.Principal;
import java.time.LocalDateTime;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/organisation")
public class OrganisationWebController {

    @Autowired
    private ActionChariteService actionService;
    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private ImageStorageService imageStorageService;
    @Autowired
    private ma.emsi.charitywebapp.services.DonService donService;

    @GetMapping("/actions/create")
    public String showCreateForm(Model model, Authentication authentication) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        model.addAttribute("organisation", org);
        model.addAttribute("action", new ActionCharite());
        return "organisation/action-form";
    }

    @PostMapping("/actions/create")
@Transactional
public String createAction(@Valid @ModelAttribute("action") ActionCharite action,
                     BindingResult result,
                     @RequestParam("imageFile") MultipartFile imageFile,
                     Authentication authentication,
                     Model model) {
    try {
        System.out.println("Début création action"); // Log de debug
        
        // Récupérer l'utilisateur et l'organisation
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        model.addAttribute("organisation", org); // Ajouter l'organisation au modèle en cas d'erreur
        
        if (result.hasErrors()) {
            return "organisation/action-form";
        }

        // Configurer l'action
        action.setOrganisation(org);
        action.setStatut("active");
        action.setDateCreation(LocalDateTime.now());
        action.setMontantCollecte(0.0);

        // Vérifier si la collection d'actions est initialisée
        if (org.getActions() == null) {
            org.setActions(new HashSet<>());
        }
        
        // Ajouter à la collection de l'organisation AVANT de sauvegarder
        org.getActions().add(action);

        // Gérer l'image
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = actionService.saveImage(imageFile);
            if (imageUrl != null) {
                action.setImageUrl(imageUrl);
            }
        }

        // Sauvegarder l'action
        ActionCharite savedAction = actionService.saveActionCharite(action);
        System.out.println("Action sauvegardée avec ID: " + savedAction.getId()); // Log de debug
        System.out.println("Organisation de l'action: " + (savedAction.getOrganisation() != null ? savedAction.getOrganisation().getId() : "null"));
        
        // Mettre à jour l'organisation
        organisationService.updateOrganisation(org);
        
        // Vérifier si l'action est bien dans la base de données
        ActionCharite verifiedAction = actionService.getActionChariteById(savedAction.getId()).orElse(null);
        if (verifiedAction != null) {
            System.out.println("Action vérifiée dans la BD: " + verifiedAction.getId() + ", Organisation: " + 
                              (verifiedAction.getOrganisation() != null ? verifiedAction.getOrganisation().getId() : "null"));
        } else {
            System.out.println("ERREUR: Action non trouvée dans la BD après sauvegarde!");
        }

        return "redirect:/organisation/actions";
        
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Une erreur est survenue: " + e.getMessage());
        return "organisation/action-form";
    }
}

    @GetMapping("/actions/edit/{id}")
    public String editAction(@PathVariable Long id, Model model, Authentication authentication) {
        ActionCharite action = actionService.getActionChariteById(id).orElseThrow();
        model.addAttribute("action", action);
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        model.addAttribute("organisation", org);
        return "organisation/action-form";
    }

    @PostMapping("/actions/edit/{id}")
    public String updateAction(@PathVariable Long id, @Valid @ModelAttribute("action") ActionCharite action,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication) {
        if (result.hasErrors())
            return "organisation/action-form";
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        action.setOrganisation(org);
        action.setId(id);
        String imageUrl = actionService.saveImage(imageFile);
        if (imageUrl != null)
            action.setImageUrl(imageUrl);
        actionService.updateActionCharite(action);
        return "redirect:/organisation/actions";
    }

    @GetMapping("/actions")
public String listActions(Model model, Authentication authentication) {
    try {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        List<ActionCharite> actions = actionService.getActionsByOrganisation(org);
        
        model.addAttribute("actions", actions);
        return "organisation/actions";
    } catch (Exception e) {
        model.addAttribute("error", "Erreur lors du chargement des actions");
        return "organisation/actions";
    }
}

    @PostMapping("/actions/archive/{id}")
    public String archiveAction(@PathVariable Long id) {
        actionService.archiverAction(id);
        return "redirect:/organisation/actions";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        
        // Récupérer les actions
        List<ActionCharite> actions = actionService.getActionsByOrganisation(org);
        
        model.addAttribute("organisation", org);
        model.addAttribute("totalActions", actions.size());
        return "organisation/dashboard";
    }

    @PostMapping("/actions/objectif/{id}")
    public String setObjectifCollecte(@PathVariable Long id, @RequestParam double valeur) {
        ActionCharite action = actionService.getActionChariteById(id).orElseThrow();
        action.setObjectifCollecte(valeur);
        double objectif = action.getObjectifCollecte();
        actionService.updateActionCharite(action);
        return "redirect:/organisation/actions";
    }

    @GetMapping("/profil")
    public String showProfile(Model model, Authentication authentication) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
        model.addAttribute("organisation", org);
        model.addAttribute("admin", user);
        return "organisation/profil";
    }

    @PostMapping("/profil")
    public String updateProfil(
            @Valid @ModelAttribute("organisation") Organisation organisation,
            BindingResult result,
            @RequestParam("logoFile") MultipartFile logoFile,
            Authentication authentication,
            Model model) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);

        if (result.hasErrors()) {
            model.addAttribute("admin", org.getAdmin());
            return "organisation/profil";
        }

        if (!logoFile.isEmpty()) {
            String contentType = logoFile.getContentType();
            if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                throw new IllegalArgumentException("Format d'image non supporté");
            }
            String logoUrl = imageStorageService.saveLogo(logoFile);
            organisation.setLogo(logoUrl);
        } else {
            organisation.setLogo(org.getLogo());
        }
        organisation.setId(org.getId());
        organisation.setAdmin(org.getAdmin());
        organisationService.updateOrganisation(organisation);

        return "redirect:/organisation/profil?success";
    }

    @GetMapping("/participants")
public String showParticipants(Model model, Authentication authentication) {
    Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
    Organisation org = organisationService.getOrganisationsByAdmin(user).get(0);
    
    // Récupérer tous les dons pour les actions de cette organisation
    List<Don> allDons = donService.getDonsByOrganisation(org);
    
    // Statistiques
    double totalDons = allDons.stream().mapToDouble(Don::getMontant).sum();
    long uniqueDonateurs = allDons.stream().map(don -> don.getDonateur().getId()).distinct().count();
    
    model.addAttribute("dons", allDons);
    model.addAttribute("totalDons", totalDons);
    model.addAttribute("uniqueDonateurs", uniqueDonateurs);
    model.addAttribute("currentUri", "/organisation/participants");
    
    return "organisation/participants";
}
}
