package ma.emsi.charitywebapp.services;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Participation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.repositories.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipationServiceImp implements ParticipationService {

    private final ParticipationRepository participationRepository;

    @Autowired
    public ParticipationServiceImp(ParticipationRepository participationRepository) {
        this.participationRepository = participationRepository;
    }

    @Override
    public Participation saveParticipation(Participation participation) {
        return participationRepository.save(participation);
    }

    @Override
    public Participation updateParticipation(Participation participation) {
        return participationRepository.save(participation);
    }

    @Override
    public Optional<Participation> getParticipationById(Long id) {
        return participationRepository.findById(id);
    }

    @Override
    public List<Participation> getAllParticipations() {
        return participationRepository.findAll();
    }

    @Override
    public List<Participation> getParticipationsByUtilisateur(Utilisateur utilisateur) {
        return participationRepository.findByUtilisateur(utilisateur);
    }

    @Override
    public List<Participation> getParticipationsByAction(ActionCharite action) {
        return participationRepository.findByAction(action);
    }

    @Override
    public Optional<Participation> getParticipationByUtilisateurAndAction(Utilisateur utilisateur, ActionCharite action) {
        return participationRepository.findByUtilisateurAndAction(utilisateur, action);
    }

    @Override
    public void deleteParticipation(Long id) {
        participationRepository.deleteById(id);
    }
}