package ma.emsi.charitywebapp.repositories;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Participation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUtilisateur(Utilisateur utilisateur);
    List<Participation> findByAction(ActionCharite action);
    Optional<Participation> findByUtilisateurAndAction(Utilisateur utilisateur, ActionCharite action);
}
