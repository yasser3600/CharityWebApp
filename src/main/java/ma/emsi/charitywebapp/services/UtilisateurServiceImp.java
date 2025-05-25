package ma.emsi.charitywebapp.services;

import ma.emsi.charitywebapp.entities.Utilisateur;
import ma.emsi.charitywebapp.repositories.UtilisateurRepository;
import ma.emsi.charitywebapp.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImp implements UtilisateurService {
    private final UtilisateurRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public UtilisateurServiceImp(UtilisateurRepository userRepository, @Lazy PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }


    @Override
    public Utilisateur saveUser(Utilisateur user) {
        // Encode le mot de passe
        user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
        
        // Ajoute le rôle par défaut si nécessaire
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Arrays.asList("ROLE_" + user.getRole().getNom()));
        }
        
        // Sauvegarde l'utilisateur
        return userRepository.save(user);
    }

    @Override
    public Utilisateur updateUser(Utilisateur user) {
        if (user.getMotDePasse() != null && !user.getMotDePasse().isEmpty()) {
            user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
        } else {
            Utilisateur existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null) {
                user.setMotDePasse(existingUser.getMotDePasse());
            }
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<Utilisateur> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<Utilisateur> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Map<String, Object> getPreferences() {
        // Return default or dummy preferences for now
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("emailNotifs", true);
        prefs.put("newOrgNotifs", false);
        prefs.put("newCampaignNotifs", true);
        prefs.put("defaultLanguage", "fr");
        prefs.put("timezone", "Europe/Paris");
        return prefs;
    }

    @Override
    public List<Utilisateur> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

        // Add this method to fix the compile error
    public void updateGeneralSettings(String defaultLanguage, String timezone) {
        // TODO: Implement logic to update general settings (e.g., save to database or preferences)
        System.out.println("Updating general settings: Language=" + defaultLanguage + ", Timezone=" + timezone);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getMotDePasse(),
                user.isEnabled(),
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<Utilisateur> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void toggleEnabled(Long id) {
        Utilisateur user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    public void updateRoles(Long id, List<String> roles) {
        Utilisateur user = userRepository.findById(id).orElseThrow();
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    public long countAll() {
        return userRepository.count();
    }

    @Override
    public List<Integer> getUsersCountByRole() {
        int admin = 0, org = 0, user = 0;
        for (Utilisateur u : userRepository.findAll()) {
            System.out.println("User: " + u.getEmail() + " Roles: " + u.getRoles());
            if (u.getRoles() != null) {
                if (u.getRoles().contains("ROLE_ADMIN"))
                    admin++;
                if (u.getRoles().contains("ROLE_ORGANISATION"))
                    org++;
                if (u.getRoles().contains("ROLE_UTILISATEUR"))
                    user++;
            }
        }
        return List.of(admin, org, user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // TODO: Implement password change logic (e.g., get current user, verify old password, update to new password)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateNotificationPreferences(boolean emailNotifs, boolean newOrgNotifs, boolean newCampaignNotifs) {
        // TODO: Implement logic to update notification preferences for the current user or admin
        // This is a placeholder implementation
        System.out.println("Updating notification preferences: email=" + emailNotifs + ", newOrg=" + newOrgNotifs + ", newCampaign=" + newCampaignNotifs);
    }

    @Override
    public long countByRole(String role) {
        return userRepository.findAll().stream()
                .filter(u -> u.isEnabled())
                .filter(u -> u.getRoles() != null && u.getRoles().size() == 1 &&
                        (u.getRoles().get(0).equalsIgnoreCase(role) || u.getRoles().get(0).equalsIgnoreCase("ROLE_" + role)))
                .count();
    }

    public void processOAuthPostLogin(String email, String name) {
        Optional<Utilisateur> existingUser = getUserByEmail(email);
        
        if (existingUser.isEmpty()) {
            Utilisateur newUser = new Utilisateur();
            newUser.setEmail(email);
            String[] nameParts = name.split(" ");
            newUser.setNom(nameParts[nameParts.length - 1]);
            newUser.setPrenom(String.join(" ", Arrays.copyOfRange(nameParts, 0, nameParts.length - 1)));
            newUser.setRole(roleService.getRoleByNom("UTILISATEUR").orElseThrow());
            newUser.setRole(roleService.getRoleByNom("UTILISATEUR").orElseThrow());
            // newUser.setAuthProvider(AuthProvider.GOOGLE); // Removed due to missing AuthProvider
            saveUser(newUser);
        }
    }
}