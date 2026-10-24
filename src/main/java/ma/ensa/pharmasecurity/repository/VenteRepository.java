package ma.ensa.pharmasecurity.repository;

import ma.ensa.pharmasecurity.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    List<Vente> findTop50ByOrderByDateVenteDesc();

    List<Vente> findTop50ByVendeurOrderByDateVenteDesc(String vendeur);

    boolean existsByMedicamentId(Long medicamentId);
}
