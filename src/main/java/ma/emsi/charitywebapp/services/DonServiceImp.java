package ma.emsi.charitywebapp.services;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.repositories.DonRepository;
import ma.emsi.charitywebapp.services.ActionChariteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DonServiceImp implements DonService {

    private final DonRepository donRepository;
    private final ActionChariteService actionChariteService;

    @Autowired
    public DonServiceImp(DonRepository donRepository, ActionChariteService actionChariteService) {
        this.donRepository = donRepository;
        this.actionChariteService = actionChariteService;
    }

    @Override
    @Transactional
    public Don saveDon(Don don) {
        Don savedDon = donRepository.save(don);
        actionChariteService.updateMontantCollecte(don.getAction().getId());
        return savedDon;
    }

    @Override
    @Transactional
    public Don updateDon(Don don) {
        Don updatedDon = donRepository.save(don);
        actionChariteService.updateMontantCollecte(don.getAction().getId());
        return updatedDon;
    }

    @Override
    public Optional<Don> getDonById(Long id) {
        return donRepository.findById(id);
    }

    @Override
    public List<Don> getDonsByOrganisation(Organisation organisation) {
        return donRepository.findByAction_Organisation(organisation);  // Modifier ici aussi
    }

    @Override
    public List<Don> getAllDons() {
        return donRepository.findAll();
    }

    @Override
    public List<Don> getDonsByDonateur(Utilisateur donateur) {
        return donRepository.findByDonateur(donateur);
    }

    @Override
    public List<Don> getDonsByAction(ActionCharite action) {
        return donRepository.findByAction(action);
    }

    @Override
    public Double getTotalDonsByAction(ActionCharite action) {
        Double total = donRepository.sumMontantByAction(action);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTotalDonsByDonateur(Utilisateur donateur) {
        Double total = donRepository.sumMontantByDonateur(donateur);
        return total != null ? total : 0.0;
    }

    @Override
    public void deleteDon(Long id) {
        Don don = donRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don non trouvé"));
        Long actionId = don.getAction().getId();
        donRepository.deleteById(id);
        actionChariteService.updateMontantCollecte(actionId);
    }

    @Override
    public String genererRecu(Long donId) {
        Don don = donRepository.findById(donId)
                .orElseThrow(() -> new RuntimeException("Don non trouvé"));
        return don.genererRecu();
    }

    @Override
    public Don getDonByStripeSessionId(String sessionId) {
        return donRepository.findByStripeSessionId(sessionId).orElse(null);
    }

    @Override
    public long countAll() {
        return donRepository.count();
    }

    @Override
    public double sumMontant() {
        Double sum = donRepository.sumMontant();
        return sum != null ? sum : 0.0;
    }

    @Override
    public Map<String, Double> getDonsParMois() {
        return donRepository.sumMontantGroupByMonth();
    }
}