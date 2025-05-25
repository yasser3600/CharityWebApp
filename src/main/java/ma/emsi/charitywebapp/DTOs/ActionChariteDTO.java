package ma.emsi.charitywebapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionChariteDTO {
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private double objectifCollecte;
    private double montantCollecte;
    private String statut;
    private Long categorieId;
    private String categorieNom;
    private Long organisationId;
    private String organisationNom;
}