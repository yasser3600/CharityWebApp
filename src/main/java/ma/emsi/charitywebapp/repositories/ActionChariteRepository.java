package ma.emsi.charitywebapp.repositories;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Categorie;
import ma.emsi.charitywebapp.entities.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionChariteRepository extends JpaRepository<ActionCharite, Long> {
    @Query("SELECT a FROM ActionCharite a WHERE a.organisation.id = :#{#organisation.id}")
    List<ActionCharite> findByOrganisation(Organisation organisation);

    List<ActionCharite> findByCategorie(Categorie categorie);

    List<ActionCharite> findByStatut(String statut);

    List<ActionCharite> findByDateDebutAfter(LocalDateTime date);

    List<ActionCharite> findByTitreContainingIgnoreCase(String keyword);

    List<ActionCharite> findTop10ByOrderByMontantCollecteDesc();

    @Query("SELECT a FROM ActionCharite a WHERE a.statut = 'active'")
    List<ActionCharite> findByStatutActive();

    long countByStatut(String statut);
}