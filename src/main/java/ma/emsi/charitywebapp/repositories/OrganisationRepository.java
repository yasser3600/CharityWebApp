package ma.emsi.charitywebapp.repositories;

import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    List<Organisation> findByValidated(boolean validated);

    Optional<Organisation> findByNom(String nom);

    List<Organisation> findByAdmin(Utilisateur admin);

    long countByValidated(boolean validated);
}