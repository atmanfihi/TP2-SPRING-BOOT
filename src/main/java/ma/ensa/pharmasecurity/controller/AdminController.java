package ma.ensa.pharmasecurity.controller;

import ma.ensa.pharmasecurity.service.OperationRefuseeException;
import ma.ensa.pharmasecurity.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String admin(Model model) {
        model.addAttribute("users", userService.lister());
        model.addAttribute("roles", userService.roles());
        return "admin";
    }

    @PostMapping("/users")
    public String creer(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        RedirectAttributes redirect) {
        try {
            userService.creer(username, password, role);
            redirect.addFlashAttribute("succes", "Utilisateur « " + username.trim() + " » créé avec le rôle " + role);
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/role")
    public String changerRole(@PathVariable Long id,
                              @RequestParam String role,
                              Authentication authentication,
                              RedirectAttributes redirect) {
        try {
            userService.changerRole(id, role, authentication.getName());
            redirect.addFlashAttribute("succes", "Rôle mis à jour");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/toggle")
    public String basculer(@PathVariable Long id,
                           Authentication authentication,
                           RedirectAttributes redirect) {
        try {
            userService.basculerActivation(id, authentication.getName());
            redirect.addFlashAttribute("succes", "Statut du compte mis à jour");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/password")
    public String reinitialiser(@PathVariable Long id,
                                @RequestParam String password,
                                RedirectAttributes redirect) {
        try {
            userService.reinitialiserMotDePasse(id, password);
            redirect.addFlashAttribute("succes", "Mot de passe réinitialisé");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String supprimer(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirect) {
        try {
            userService.supprimer(id, authentication.getName());
            redirect.addFlashAttribute("succes", "Utilisateur supprimé");
        } catch (OperationRefuseeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }
}
