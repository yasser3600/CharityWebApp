package ma.emsi.charitywebapp.repositories;

import ma.emsi.charitywebapp.entities.ActionCharite;
import ma.emsi.charitywebapp.entities.Don;
import ma.emsi.charitywebapp.entities.Organisation;
import ma.emsi.charitywebapp.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;

@Repository
public interface DonRepository extends JpaRepository<Don, Long> {
    List<Don> findByDonateur(Utilisateur donateur);

    List<Don> findByAction(ActionCharite action);

    List<Don> findByAction_Organisation(Organisation organisation);  // Au lieu de findByActionCharite_Organisation

    @Query("SELECT SUM(d.montant) FROM Don d WHERE d.action = ?1")
    Double sumMontantByAction(ActionCharite action);

    @Query("SELECT SUM(d.montant) FROM Don d WHERE d.donateur = ?1")
    Double sumMontantByDonateur(Utilisateur donateur);

    Optional<Don> findByStripeSessionId(String stripeSessionId);

    @Query("SELECT SUM(d.montant) FROM Don d")
    Double sumMontant();

    @Query("SELECT FUNCTION('DATE_FORMAT', d.dateDon, '%Y-%m') as mois, SUM(d.montant) as total " +
            "FROM Don d GROUP BY mois ORDER BY mois")
    List<Object[]> sumMontantGroupByMonthRaw();

    default Map<String, Double> sumMontantGroupByMonth() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Object[] row : sumMontantGroupByMonthRaw()) {
            result.put((String) row[0], ((Number) row[1]).doubleValue());
        }
        return result;
    }
}