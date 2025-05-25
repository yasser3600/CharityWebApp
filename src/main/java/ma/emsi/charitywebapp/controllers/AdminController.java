package ma.emsi.charitywebapp.controllers;

import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private ActionChariteService actionChariteService;
    @Autowired
    private DonService donService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("totalActions", actionChariteService.countAll());
        model.addAttribute("totalOrganisations", organisationService.countByValidated(true));
        model.addAttribute("totalDons", donService.countAll());
        model.addAttribute("totalMontant", donService.sumMontant());
        model.addAttribute("totalUtilisateurs", utilisateurService.countByRole("UTILISATEUR"));
        model.addAttribute("donsParMois", donService.getDonsParMois());
        model.addAttribute("orgsEnAttente", organisationService.countPending());
        model.addAttribute("actionsActives", actionChariteService.countActive());
        model.addAttribute("montantTotalDons", donService.sumMontant());
        model.addAttribute("actionsParMois", actionChariteService.getActionsParMois());
        long adminCount = utilisateurService.countByRole("ADMIN");
        long orgCount = utilisateurService.countByRole("ORGANISATION");
        long userCount = utilisateurService.countByRole("UTILISATEUR");
        model.addAttribute("rolesLabels", List.of("ADMIN", "ORGANISATION", "UTILISATEUR"));
        model.addAttribute("rolesData", Arrays.asList(adminCount, orgCount, userCount));
        utilisateurService.findAll()
                .forEach(u -> System.out.println("User: " + u.getEmail() + " Roles: " + u.getRoles()));
        return "admin/dashboard";
    }

    // Organisations
    @GetMapping("/organisations")
    public String organisations(@RequestParam(value = "filter", required = false) String filter, Model model) {
        List<Organisation> organisations;
        if ("validées".equals(filter)) {
            organisations = organisationService.findByValidated(true);
        } else if ("nonvalidées".equals(filter)) {
            organisations = organisationService.findByValidated(false);
        } else {
            organisations = organisationService.findAll();
        }
        model.addAttribute("organisations", organisations);
        model.addAttribute("filter", filter);
        return "admin/organisations";
    }

    @PostMapping("/organisations/{id}/valider")
    public String validerOrganisation(@PathVariable Long id) {
        organisationService.valider(id, true);
        return "redirect:/admin/organisations?success";
    }

    @PostMapping("/organisations/{id}/refuser")
    public String refuserOrganisation(@PathVariable Long id) {
        organisationService.valider(id, false);
        return "redirect:/admin/organisations?refused";
    }

    @PostMapping("/organisations/{id}/toggle")
    public String toggleOrganisation(@PathVariable Long id) {
        organisationService.toggleEnabled(id);
        return "redirect:/admin/organisations";
    }

    // Utilisateurs
    @GetMapping("/utilisateurs")
    public String utilisateurs(Model model) {
        List<Utilisateur> utilisateursVisibles = utilisateurService.findAll().stream()
                .filter(u -> u.getRoles() != null
                        && u.getRoles().stream().noneMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ADMIN")))
                .toList();
        model.addAttribute("utilisateurs", utilisateursVisibles);
        return "admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/toggle")
    public String toggleUtilisateur(@PathVariable Long id) {
        utilisateurService.toggleEnabled(id);
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/roles")
    public String updateRoles(@PathVariable Long id, @RequestParam List<String> roles) {
        utilisateurService.updateRoles(id, roles);
        return "redirect:/admin/utilisateurs";
    }

    @GetMapping("/parametres")
    public String parametres(Model model, HttpServletRequest request) {
        // Ajouter les préférences actuelles au modèle
        model.addAttribute("preferences", utilisateurService.getPreferences());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/parametres";
    }

    @PostMapping("/parametres/password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que les mots de passe correspondent
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
                return "redirect:/admin/parametres";
            }

            // Changer le mot de passe
            utilisateurService.changePassword(oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du changement de mot de passe");
        }
        return "redirect:/admin/parametres";
    }

    @PostMapping("/parametres/notifications")
    public String updateNotifications(
            @RequestParam(required = false) boolean emailNotifs,
            @RequestParam(required = false) boolean newOrgNotifs,
            @RequestParam(required = false) boolean newCampaignNotifs,
            RedirectAttributes redirectAttributes) {
        try {
            // Mettre à jour les préférences de notifications
            utilisateurService.updateNotificationPreferences(emailNotifs, newOrgNotifs, newCampaignNotifs);
            redirectAttributes.addFlashAttribute("success", "Préférences de notifications mises à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour des préférences");
        }
        return "redirect:/admin/parametres";
    }

    @PostMapping("/parametres/general")
    public String updateGeneralSettings(
            @RequestParam String defaultLanguage,
            @RequestParam String timezone,
            RedirectAttributes redirectAttributes) {
        try {
            // Mettre à jour les paramètres généraux
            utilisateurService.updateGeneralSettings(defaultLanguage, timezone);
            redirectAttributes.addFlashAttribute("success", "Paramètres généraux mis à jour");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour des paramètres");
        }
        return "redirect:/admin/parametres";
    }
}
