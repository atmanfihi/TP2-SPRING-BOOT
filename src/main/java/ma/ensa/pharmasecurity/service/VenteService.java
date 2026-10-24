package ma.ensa.pharmasecurity.service;

import ma.ensa.pharmasecurity.model.Medicament;
import ma.ensa.pharmasecurity.model.Vente;
import ma.ensa.pharmasecurity.repository.MedicamentRepository;
import ma.ensa.pharmasecurity.repository.VenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final MedicamentRepository medicamentRepository;

    public VenteService(VenteRepository venteRepository, MedicamentRepository medicamentRepository) {
        this.venteRepository = venteRepository;
        this.medicamentRepository = medicamentRepository;
    }

    @Transactional(readOnly = true)
    public List<Vente> dernieresVentes() {
        return venteRepository.findTop50ByOrderByDateVenteDesc();
    }

    @Transactional(readOnly = true)
    public List<Vente> dernieresVentesDe(String vendeur) {
        return venteRepository.findTop50ByVendeurOrderByDateVenteDesc(vendeur);
    }

    public Vente enregistrer(Long medicamentId, Integer quantite, String vendeur) {
        if (medicamentId == null) {
            throw new OperationRefuseeException("Veuillez choisir un médicament");
        }
        if (quantite == null || quantite <= 0) {
            throw new OperationRefuseeException("La quantité vendue doit être supérieure à 0");
        }
        Medicament medicament = medicamentRepository.findByIdPourMiseAJour(medicamentId)
                .orElseThrow(() -> new OperationRefuseeException("Médicament introuvable"));
        if (medicament.getQuantiteStock() < quantite) {
            throw new OperationRefuseeException("Stock insuffisant pour « " + medicament.getNom()
                    + " » : " + medicament.getQuantiteStock() + " unité(s) disponible(s)");
        }
        medicament.setQuantiteStock(medicament.getQuantiteStock() - quantite);
        return venteRepository.save(new Vente(medicament, quantite, vendeur));
    }
}
