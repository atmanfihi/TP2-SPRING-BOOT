package ma.ensa.pharmasecurity.repository;

import jakarta.persistence.LockModeType;
import ma.ensa.pharmasecurity.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    List<Medicament> findAllByOrderByNomAsc();

    List<Medicament> findByQuantiteStockGreaterThanOrderByNomAsc(Integer quantite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicament m where m.id = :id")
    Optional<Medicament> findByIdPourMiseAJour(@Param("id") Long id);
}
