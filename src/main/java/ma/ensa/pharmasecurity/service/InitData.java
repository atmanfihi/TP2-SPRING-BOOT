package ma.ensa.pharmasecurity.service;

import ma.ensa.pharmasecurity.model.Medicament;
import ma.ensa.pharmasecurity.model.Role;
import ma.ensa.pharmasecurity.model.User;
import ma.ensa.pharmasecurity.repository.MedicamentRepository;
import ma.ensa.pharmasecurity.repository.RoleRepository;
import ma.ensa.pharmasecurity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class InitData implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitData.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MedicamentRepository medicamentRepository;
    private final PasswordEncoder passwordEncoder;

    public InitData(RoleRepository roleRepository,
                    UserRepository userRepository,
                    MedicamentRepository medicamentRepository,
                    PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.medicamentRepository = medicamentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = role(Role.ADMIN);
        Role pharmacien = role(Role.PHARMACIEN);
        Role caissier = role(Role.CAISSIER);
        Role client = role(Role.CLIENT);

        utilisateur("admin", "admin123", admin);
        utilisateur("pharma", "pharma123", pharmacien);
        utilisateur("caisse", "caisse123", caissier);
        utilisateur("client", "client123", client);

        if (medicamentRepository.count() == 0) {
            medicamentRepository.saveAll(List.of(
                    new Medicament("Amoxicilline 500mg", "Antibiotique à large spectre.", new BigDecimal("45.00"), 120),
                    new Medicament("Doliprane 500mg", "Antalgique et antipyrétique.", new BigDecimal("25.50"), 300),
                    new Medicament("Vitamine C 1000mg", "Complément pour renforcer les défenses.", new BigDecimal("30.00"), 80),
                    new Medicament("Bétadine Solution 10%", "Désinfectant pour usage externe.", new BigDecimal("18.00"), 8),
                    new Medicament("Nurofen 400mg", "Soulage la douleur et la fièvre.", new BigDecimal("42.00"), 60),
                    new Medicament("Voltaren 50mg", "Anti-inflammatoire pour douleurs musculaires.", new BigDecimal("58.90"), 0)
            ));
            log.info("Médicaments de démonstration créés");
        }
    }

    private Role role(String nom) {
        return roleRepository.findByName(nom).orElseGet(() -> roleRepository.save(new Role(nom)));
    }

    private void utilisateur(String username, String motDePasse, Role role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(new User(username, passwordEncoder.encode(motDePasse), role));
            log.info("Utilisateur créé : {} ({})", username, role.getName());
        }
    }
}
