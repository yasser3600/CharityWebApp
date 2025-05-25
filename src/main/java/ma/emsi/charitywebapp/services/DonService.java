package ma.emsi.charitywebapp.services;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Utilisateur;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DonService {
    Don saveDon(Don don);

    Don updateDon(Don don);

    Optional<Don> getDonById(Long id);

    List<Don> getAllDons();

    List<Don> getDonsByDonateur(Utilisateur donateur);

    List<Don> getDonsByAction(ActionCharite action);

    Double getTotalDonsByAction(ActionCharite action);

    Double getTotalDonsByDonateur(Utilisateur donateur);

    List<Don> getDonsByOrganisation(Organisation organisation);

    void deleteDon(Long id);

    String genererRecu(Long donId);

    Don getDonByStripeSessionId(String sessionId);

    long countAll();

    double sumMontant();

    Map<String, Double> getDonsParMois();
}