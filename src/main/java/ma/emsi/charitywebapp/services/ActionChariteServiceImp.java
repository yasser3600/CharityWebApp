package ma.emsi.charitywebapp.services;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Categorie;
import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.repositories.ActionChariteRepository;
import ma.emsi.charitywebapp.repositories.DonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.nio.file.StandardCopyOption;

@Service
public class ActionChariteServiceImp implements ActionChariteService {
    
    @Autowired
    private ActionChariteRepository actionChariteRepository;

    @Autowired
    private DonRepository donRepository;
    
    @Override
    @Transactional
    public ActionCharite saveActionCharite(ActionCharite action) {
        System.out.println("Sauvegarde de l'action: " + action.getTitre()); // Log de debug
        if (action.getDateCreation() == null) {
            action.setDateCreation(LocalDateTime.now());
        }
        if (action.getStatut() == null || action.getStatut().isEmpty()) {
            action.setStatut("active");
        }
        if (action.getMontantCollecte() == null) {
            action.setMontantCollecte(0.0);
        }
        if (action.getOrganisation() == null) {
            throw new IllegalArgumentException("L'action doit être associée à une organisation");
        }
        return actionChariteRepository.save(action);
    }

    @Override
    public ActionCharite updateActionCharite(ActionCharite actionCharite) {
        return actionChariteRepository.save(actionCharite);
    }

    @Override
    public Optional<ActionCharite> getActionChariteById(Long id) {
        return actionChariteRepository.findById(id);
    }

    @Override
    public List<ActionCharite> getAllActionsCharite() {
        return actionChariteRepository.findAll();
    }

    @Override
    @Transactional
    public List<ActionCharite> getActionsByOrganisation(Organisation organisation) {
        System.out.println("Recherche des actions pour l'organisation: " + organisation.getId()); // Debug log
        return actionChariteRepository.findByOrganisation(organisation);
    }

    @Override
    public List<ActionCharite> getActionsByCategorie(Categorie categorie) {
        return actionChariteRepository.findByCategorie(categorie);
    }

    @Override
    public List<ActionCharite> getActionsByStatut(String statut) {
        return actionChariteRepository.findByStatut(statut);
    }

    @Override
    public List<ActionCharite> searchActionsByTitle(String keyword) {
        return actionChariteRepository.findByTitreContainingIgnoreCase(keyword);
    }

    @Override
    public List<ActionCharite> getTopActions() {
        return actionChariteRepository.findTop10ByOrderByMontantCollecteDesc();
    }

    @Override
    public Optional<ActionCharite> getActionById(Long id) {
        return actionChariteRepository.findById(id);
    }

    @Override
    public void deleteActionCharite(Long id) {
        actionChariteRepository.deleteById(id);
    }

    @Override
    public void updateMontantCollecte(Long actionId) {
        ActionCharite action = actionChariteRepository.findById(actionId)
                .orElseThrow(() -> new RuntimeException("Action de charité non trouvée"));

        Double totalDons = donRepository.sumMontantByAction(action);
        if (totalDons != null) {
            action.setMontantCollecte(totalDons);
            actionChariteRepository.save(action);
        }
    }

    @Override
    public List<ActionCharite> getAllActiveActions() {
        return actionChariteRepository.findByStatutActive();
    }

    @Override
    public String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty())
            return null;
        try {
            String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path resourcesPath = Paths.get("src", "main", "resources", "static", "uploads");
            
            if (!Files.exists(resourcesPath)) {
                Files.createDirectories(resourcesPath);
            }
            
            Path destinationPath = resourcesPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            
            return "/uploads/" + filename;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void archiverAction(Long id) {
        ActionCharite action = actionChariteRepository.findById(id).orElseThrow();
        action.setStatut("archive");
        actionChariteRepository.save(action);
    }

    @Override
    public long countAll() {
        return actionChariteRepository.count();
    }

    @Override
    public long countActive() {
        return actionChariteRepository.countByStatut("ACTIVE");
    }

    @Override
    public Map<String, Integer> getActionsParMois() {
        List<ActionCharite> actions = actionChariteRepository.findAll();
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ActionCharite action : actions) {
            if (action.getDateCreation() != null) {
                String mois = action.getDateCreation().getYear() + "-"
                        + String.format("%02d", action.getDateCreation().getMonthValue());
                result.put(mois, result.getOrDefault(mois, 0) + 1);
            }
        }
        return result;
    }
}
