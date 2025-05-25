package ma.emsi.charitywebapp.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.services.DonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

// Ajoutez l'import pour Utilisateur
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.services.UtilisateurService;
import ma.emsi.charitywebapp.services.StripeService;
// Ajoutez l'import pour ActionCharite
import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.services.ActionChariteService;

@Controller
@RequestMapping("/api/paiement")
public class PaiementController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final DonService donService;
    private final UtilisateurService utilisateurService;
    private final ActionChariteService actionChariteService;
    private final StripeService stripeService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    public PaiementController(DonService donService, UtilisateurService utilisateurService, ActionChariteService actionChariteService, StripeService stripeService) {
        this.donService = donService;
        this.utilisateurService = utilisateurService;
        this.actionChariteService = actionChariteService;
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    public String createCheckoutSession(
            @RequestParam Double montant,
            @RequestParam Long actionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'utilisateur connecté
            Utilisateur user = utilisateurService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Récupérer l'action
            ActionCharite action = actionChariteService.getActionById(actionId)
                    .orElseThrow(() -> new RuntimeException("Action non trouvée"));

            // Créer le don
            Don don = new Don();
            don.setMontant(montant);
            don.setDonateur(user);
            don.setAction(action);
            don.setDateDon(LocalDateTime.now());
            don.setStatutPaiement("EN_ATTENTE");
            donService.saveDon(don);

            // Créer la session Stripe
            Session session = stripeService.createPaymentSession(montant, user.getEmail(), don.getId());
            
            return "redirect:" + session.getUrl();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du paiement");
            return "redirect:/campaigns";
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        String payload = "";
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            payload = sb.toString();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("");
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                Don don = donService.getDonByStripeSessionId(session.getId());
                if (don != null) {
                    don.setStatutPaiement("PAYE");
                    donService.saveDon(don);
                }
            }
        }
        return ResponseEntity.ok("");
    }

    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "payment-success";
    }
}
