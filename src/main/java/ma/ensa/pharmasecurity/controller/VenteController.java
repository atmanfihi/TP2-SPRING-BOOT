package ma.ensa.pharmasecurity.controller;

import ma.ensa.pharmasecurity.model.Vente;
import ma.ensa.pharmasecurity.service.MedicamentService;
import ma.ensa.pharmasecurity.service.OperationRefuseeException;
import ma.ensa.pharmasecurity.service.VenteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/ventes")
@PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN','CAISSIER')")
public class VenteController {

    private final VenteService venteService;
    private final MedicamentService medicamentService;

    public VenteController(VenteService venteService, MedicamentService medicamentService) {
        this.venteService = venteService;
        this.medicamentService = medicamentService;
    }

    @GetMapping
    public String ventes(Authentication authentication, Model model) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Vente> ventes = admin
                ? venteService.dernieresVentes()
                : venteService.dernieresVentesDe(authentication.getName());
        BigDecimal total = ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("medicaments", medicamentService.disponibles());
        model.addAttribute("ventes", ventes);
        model.addAttribute("total", total);
        model.addAttribute("toutesLesVentes", admin);
        return "ventes";
    }

    @PostMapping
    public String enregistrer(@RequestParam(required = false) Long medicamentId,
                              @RequestParam(required = false) Integer quantite,
                              Authentication authentication,
                              RedirectAttributes redirect) {
        try {
            Vente vente = venteService.enregistrer(medicamentId, quantite, authentication.getName());
            redirect.addFlashAttribute("succes", "Vente enregistrée : " + vente.getQuantite() + " × "
                    + vente.getMedicament().getNom() + " = " + vente.getMontantTotal() + " DH");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/ventes";
    }
}
