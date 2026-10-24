package ma.ensa.pharmasecurity.service;

import ma.ensa.pharmasecurity.model.Role;
import ma.ensa.pharmasecurity.model.User;
import ma.ensa.pharmasecurity.repository.RoleRepository;
import ma.ensa.pharmasecurity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private static final int LONGUEUR_MIN_MOT_DE_PASSE = 6;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> lister() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public List<Role> roles() {
        return roleRepository.findAllByOrderByNameAsc();
    }

    public User creer(String username, String motDePasse, String nomRole) {
        String identifiant = username == null ? "" : username.trim();
        if (identifiant.length() < 3) {
            throw new OperationRefuseeException("Le nom d'utilisateur doit contenir au moins 3 caractères");
        }
        if (userRepository.existsByUsername(identifiant)) {
            throw new OperationRefuseeException("Le nom d'utilisateur « " + identifiant + " » existe déjà");
        }
        verifierMotDePasse(motDePasse);
        User user = new User(identifiant, passwordEncoder.encode(motDePasse), role(nomRole));
        return userRepository.save(user);
    }

    public void changerRole(Long id, String nomRole, String utilisateurConnecte) {
        User user = trouver(id);
        if (user.getUsername().equals(utilisateurConnecte) && !Role.ADMIN.equals(nomRole)) {
            throw new OperationRefuseeException("Vous ne pouvez pas retirer votre propre rôle ADMIN");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(role(nomRole));
        user.setRoles(roles);
    }

    public void basculerActivation(Long id, String utilisateurConnecte) {
        User user = trouver(id);
        if (user.getUsername().equals(utilisateurConnecte)) {
            throw new OperationRefuseeException("Vous ne pouvez pas désactiver votre propre compte");
        }
        user.setEnabled(!user.isEnabled());
    }

    public void reinitialiserMotDePasse(Long id, String motDePasse) {
        verifierMotDePasse(motDePasse);
        User user = trouver(id);
        user.setPassword(passwordEncoder.encode(motDePasse));
    }

    public void supprimer(Long id, String utilisateurConnecte) {
        User user = trouver(id);
        if (user.getUsername().equals(utilisateurConnecte)) {
            throw new OperationRefuseeException("Vous ne pouvez pas supprimer votre propre compte");
        }
        userRepository.delete(user);
    }

    private User trouver(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new OperationRefuseeException("Utilisateur introuvable"));
    }

    private Role role(String nomRole) {
        return roleRepository.findByName(nomRole)
                .orElseThrow(() -> new OperationRefuseeException("Rôle inconnu : " + nomRole));
    }

    private void verifierMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN_MOT_DE_PASSE) {
            throw new OperationRefuseeException("Le mot de passe doit contenir au moins " + LONGUEUR_MIN_MOT_DE_PASSE + " caractères");
        }
    }
}
