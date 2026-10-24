package ma.ensa.pharmasecurity.controller;

import jakarta.validation.Valid;
import ma.ensa.pharmasecurity.model.Medicament;
import ma.ensa.pharmasecurity.service.MedicamentService;
import ma.ensa.pharmasecurity.service.OperationRefuseeException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medicaments")
public class MedicamentController {

    private final MedicamentService medicamentService;

    public MedicamentController(MedicamentService medicamentService) {
        this.medicamentService = medicamentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN','CLIENT')")
    public String lister(Model model) {
        model.addAttribute("medicaments", medicamentService.lister());
        return "medicaments";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String nouveau(Model model) {
        model.addAttribute("medicament", new Medicament());
        return "medicament-form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String modifier(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("medicament", medicamentService.trouver(id));
            return "medicament-form";
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/medicaments";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String enregistrer(@Valid @ModelAttribute("medicament") Medicament medicament,
                              BindingResult resultat,
                              RedirectAttributes redirect) {
        if (resultat.hasErrors()) {
            return "medicament-form";
        }
        boolean creation = medicament.getId() == null;
        try {
            Medicament enregistre = medicamentService.enregistrer(medicament);
            redirect.addFlashAttribute("succes", "« " + enregistre.getNom() + " » "
                    + (creation ? "ajouté" : "modifié") + " avec succès");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/medicaments";
    }

    @PostMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIEN')")
    public String modifierStock(@PathVariable Long id,
                                @RequestParam(required = false) Integer quantite,
                                RedirectAttributes redirect) {
        try {
            Medicament medicament = medicamentService.modifierStock(id, quantite);
            redirect.addFlashAttribute("succes", "Stock de « " + medicament.getNom() + " » mis à jour : "
                    + medicament.getQuantiteStock() + " unité(s)");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/medicaments";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            medicamentService.supprimer(id);
            redirect.addFlashAttribute("succes", "Médicament supprimé");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/medicaments";
    }
}
