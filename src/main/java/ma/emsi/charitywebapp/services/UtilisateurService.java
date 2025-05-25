package ma.emsi.charitywebapp.services;

import ma.emsi.charitywebapp.entities.Utilisateur;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface UtilisateurService extends UserDetailsService {
    Utilisateur saveUser(Utilisateur user);

    Utilisateur updateUser(Utilisateur user);

    Optional<Utilisateur> getUserById(Long id);

    Optional<Utilisateur> getUserByEmail(String email);

    List<Utilisateur> getAllUsers();

    void deleteUser(Long id);

    boolean checkEmailExists(String email);

    List<Utilisateur> findAll();

    void toggleEnabled(Long id);

    void updateRoles(Long id, List<String> roles);

    long countAll();

    void changePassword(String oldPassword, String newPassword);

    void updateNotificationPreferences(boolean emailNotifs, boolean newOrgNotifs, boolean newCampaignNotifs);

    Map<String, Object> getPreferences();

    public void updateGeneralSettings(String defaultLanguage, String timezone);

    List<Integer> getUsersCountByRole();

    public void processOAuthPostLogin(String email, String name);

    long countByRole(String role);
}