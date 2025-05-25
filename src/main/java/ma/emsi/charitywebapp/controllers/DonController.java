package ma.emsi.charitywebapp.controllers;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import ma.emsi.charitywebapp.DTOs.DonDTO;
import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.services.ActionChariteService;
import ma.emsi.charitywebapp.services.DonService;
import ma.emsi.charitywebapp.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dons")
public class DonController {

    private final DonService donService;
    private final UtilisateurService utilisateurService;
    private final ActionChariteService actionChariteService;

    @Autowired
    public DonController(DonService donService, UtilisateurService userService,
            ActionChariteService actionChariteService) {
        this.donService = donService;
        this.utilisateurService = userService;
        this.actionChariteService = actionChariteService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createDon(@RequestBody DonDTO donDTO, Authentication authentication) {
        String email = authentication.getName();
        Optional<Utilisateur> donateur = utilisateurService.getUserByEmail(email);
        if (donateur.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        Optional<ActionCharite> action = actionChariteService.getActionChariteById(donDTO.getActionId());
        if (action.isEmpty()) {
            return ResponseEntity.badRequest().body("Action de charité non trouvée");
        }

        Don don = new Don();
        don.setMontant(donDTO.getMontant());
        don.setMethodePaiement(donDTO.getMethodePaiement());
        don.setStatutPaiement(donDTO.getStatutPaiement());
        don.setReferencePaiement(donDTO.getReferencePaiement());
        don.setDonateur(donateur.get());
        don.setAction(action.get());
        don.setDateDon(LocalDateTime.now());

        Don savedDon = donService.saveDon(don);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedDon));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @donService.getDonById(#id).get().getDonateur().getId() == authentication.principal.id")
    public ResponseEntity<?> getDonById(@PathVariable Long id) {
        Optional<Don> don = donService.getDonById(id);
        return don.map(value -> ResponseEntity.ok(convertToDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DonDTO>> getAllDons() {
        List<Don> dons = donService.getAllDons();
        List<DonDTO> donDTOs = dons.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donDTOs);
    }

    @GetMapping("/donateur/{donateurId}")
    @PreAuthorize("hasRole('ADMIN') or #donateurId == authentication.principal.id")
    public ResponseEntity<List<DonDTO>> getDonsByDonateur(@PathVariable Long donateurId) {
        Optional<Utilisateur> donateur = utilisateurService.getUserById(donateurId);
        if (donateur.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Don> dons = donService.getDonsByDonateur(donateur.get());
        List<DonDTO> donDTOs = dons.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donDTOs);
    }

    @GetMapping("/action/{actionId}")
    public ResponseEntity<List<DonDTO>> getDonsByAction(@PathVariable Long actionId) {
        Optional<ActionCharite> action = actionChariteService.getActionChariteById(actionId);
        if (action.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Don> dons = donService.getDonsByAction(action.get());
        List<DonDTO> donDTOs = dons.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donDTOs);
    }

    @GetMapping("/recu/{id}")
    @PreAuthorize("hasRole('ADMIN') or @donService.getDonById(#id).get().getDonateur().getId() == authentication.principal.id")
    public ResponseEntity<String> genererRecu(@PathVariable Long id) {
        Optional<Don> don = donService.getDonById(id);
        if (don.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String recu = donService.genererRecu(id);
        return ResponseEntity.ok(recu);
    }

    @GetMapping("/recu-pdf/{id}")
    @PreAuthorize("hasRole('ADMIN') or @donService.getDonById(#id).get().getDonateur().getId() == authentication.principal.id")
    public ResponseEntity<byte[]> genererRecuPdf(@PathVariable Long id) {
        Optional<Don> donOpt = donService.getDonById(id);
        if (donOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Don don = donOpt.get();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Reçu de don"));
            document.add(new Paragraph("Numéro du don : " + don.getId()));
            document.add(new Paragraph("Montant : " + don.getMontant() + " €"));
            document.add(new Paragraph("Date : " + don.getDateDon()));
            document.add(
                    new Paragraph("Donateur : " + don.getDonateur().getNom() + " " + don.getDonateur().getPrenom()));
            document.add(new Paragraph("Action : " + (don.getAction() != null ? don.getAction().getTitre() : "")));
            document.add(new Paragraph("Merci pour votre générosité !"));

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu-don-" + don.getId() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDon(@PathVariable Long id, @RequestBody DonDTO donDTO) {
        Optional<Don> optionalDon = donService.getDonById(id);
        if (optionalDon.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Don don = optionalDon.get();
        don.setMontant(donDTO.getMontant());
        don.setMethodePaiement(donDTO.getMethodePaiement());
        don.setStatutPaiement(donDTO.getStatutPaiement());
        don.setReferencePaiement(donDTO.getReferencePaiement());

        Don updatedDon = donService.updateDon(don);
        return ResponseEntity.ok(convertToDTO(updatedDon));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDon(@PathVariable Long id) {
        Optional<Don> don = donService.getDonById(id);
        if (don.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        donService.deleteDon(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/dons")
    @PreAuthorize("hasAnyRole('UTILISATEUR','ORGANISATION')")
    public String userDons(Model model, org.springframework.security.core.Authentication authentication) {
        Utilisateur user = utilisateurService.getUserByEmail(authentication.getName()).orElseThrow();
        List<Don> dons = donService.getDonsByDonateur(user);
        model.addAttribute("dons", dons);
        return "user/dons";
    }

    private DonDTO convertToDTO(Don don) {
        DonDTO dto = new DonDTO();
        dto.setId(don.getId());
        dto.setMontant(don.getMontant());
        dto.setMethodePaiement(don.getMethodePaiement());
        dto.setStatutPaiement(don.getStatutPaiement());
        dto.setReferencePaiement(don.getReferencePaiement());
        dto.setDateDon(don.getDateDon());

        if (don.getDonateur() != null) {
            dto.setDonateurId(don.getDonateur().getId());
            dto.setDonateurNom(don.getDonateur().getNom() + " " + don.getDonateur().getPrenom());
        }

        if (don.getAction() != null) {
            dto.setActionId(don.getAction().getId());
            dto.setActionTitre(don.getAction().getTitre());
        }

        return dto;
    }
}