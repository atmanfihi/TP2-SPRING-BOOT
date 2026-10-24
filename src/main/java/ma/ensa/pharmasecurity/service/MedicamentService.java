package ma.ensa.pharmasecurity.service;

import ma.ensa.pharmasecurity.model.Medicament;
import ma.ensa.pharmasecurity.repository.MedicamentRepository;
import ma.ensa.pharmasecurity.repository.VenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;
    private final VenteRepository venteRepository;

    public MedicamentService(MedicamentRepository medicamentRepository, VenteRepository venteRepository) {
        this.medicamentRepository = medicamentRepository;
        this.venteRepository = venteRepository;
    }

    @Transactional(readOnly = true)
    public List<Medicament> lister() {
        return medicamentRepository.findAllByOrderByNomAsc();
    }

    @Transactional(readOnly = true)
    public List<Medicament> disponibles() {
        return medicamentRepository.findByQuantiteStockGreaterThanOrderByNomAsc(0);
    }

    @Transactional(readOnly = true)
    public Medicament trouver(Long id) {
        return medicamentRepository.findById(id)
                .orElseThrow(() -> new OperationRefuseeException("Médicament introuvable"));
    }

    public Medicament enregistrer(Medicament medicament) {
        if (medicament.getId() == null) {
            return medicamentRepository.save(medicament);
        }
        Medicament existant = trouver(medicament.getId());
        existant.setNom(medicament.getNom());
        existant.setDescription(medicament.getDescription());
        existant.setPrix(medicament.getPrix());
        existant.setQuantiteStock(medicament.getQuantiteStock());
        return existant;
    }

    public Medicament modifierStock(Long id, Integer quantite) {
        if (quantite == null || quantite < 0) {
            throw new OperationRefuseeException("La quantité en stock doit être un entier positif ou nul");
        }
        Medicament medicament = medicamentRepository.findByIdPourMiseAJour(id)
                .orElseThrow(() -> new OperationRefuseeException("Médicament introuvable"));
        medicament.setQuantiteStock(quantite);
        return medicament;
    }

    public void supprimer(Long id) {
        Medicament medicament = trouver(id);
        if (venteRepository.existsByMedicamentId(id)) {
            throw new OperationRefuseeException("Suppression impossible : des ventes sont associées à « " + medicament.getNom() + " »");
        }
        medicamentRepository.delete(medicament);
    }
}
