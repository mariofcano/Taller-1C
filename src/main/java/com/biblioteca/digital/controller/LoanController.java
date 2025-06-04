package com.biblioteca.digital.controller;

import com.biblioteca.digital.model.Loan;
import com.biblioteca.digital.model.LoanStatus;
import com.biblioteca.digital.model.Book;
import com.biblioteca.digital.model.User;
import com.biblioteca.digital.service.LoanService;
import com.biblioteca.digital.service.BookService;
import com.biblioteca.digital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CONTROLADOR PARA LA GESTION COMPLETA DE PRESTAMOS
 *
 * ESTE CONTROLADOR MANEJA TODAS LAS OPERACIONES CRUD DEL SISTEMA DE PRESTAMOS
 * EN LA BIBLIOTECA DIGITAL. INCLUYE FUNCIONALIDADES PARA CREAR PRESTAMOS,
 * PROCESAR DEVOLUCIONES, RENOVAR PRESTAMOS Y GESTIONAR MULTAS.
 *
 * IMPLEMENTA CONTROL DE ACCESO SEGUN EL ROL DEL USUARIO Y EL PROPIETARIO
 * DE LOS PRESTAMOS PARA GARANTIZAR LA SEGURIDAD DEL SISTEMA.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 */
@Controller
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /**
     * LISTA TODOS LOS PRESTAMOS CON FILTROS Y PAGINACION
     *
     * MUESTRA UNA TABLA PAGINADA DE PRESTAMOS CON OPCIONES DE FILTRADO
     * POR ESTADO, USUARIO Y FECHAS. ACCESO DIFERENCIADO SEGUN ROL.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String listLoans(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "15") int size,
                            @RequestParam(defaultValue = "loanDate") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir,
                            @RequestParam(required = false) LoanStatus status,
                            @RequestParam(required = false) Long userId,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            Model model) {
        try {
            // CONFIGURACION DE PAGINACION Y ORDENAMIENTO
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // OBTENER PRESTAMOS SEGUN FILTROS
            List<Loan> allLoans = getAllLoansWithFilters(status, userId, startDate, endDate);
            Page<Loan> loansPage = convertListToPage(allLoans, pageable);

            // ESTADISTICAS PARA EL DASHBOARD
            long totalLoans = loanService.countTotalLoans();
            long activeLoans = loanService.countActiveLoans();
            List<Loan> overdueLoans = loanService.findOverdueLoans();
            List<Loan> dueToday = loanService.findLoansDueToday();
            List<Loan> dueSoon = loanService.findLoansDueSoon(3);
            BigDecimal unpaidFines = loanService.calculateTotalUnpaidFines();

            // DATOS PARA LA VISTA
            model.addAttribute("loansPage", loansPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", loansPage.getTotalPages());
            model.addAttribute("totalElements", loansPage.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

            // FILTROS
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedUserId", userId);
            model.addAttribute("selectedStartDate", startDate);
            model.addAttribute("selectedEndDate", endDate);

            // ESTADISTICAS
            model.addAttribute("totalLoans", totalLoans);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("overdueLoansCount", overdueLoans.size());
            model.addAttribute("dueTodayCount", dueToday.size());
            model.addAttribute("dueSoonCount", dueSoon.size());
            model.addAttribute("unpaidFines", unpaidFines);

            // DATOS PARA FILTROS
            model.addAttribute("loanStatuses", LoanStatus.values());
            model.addAttribute("activeUsers", userService.findAllActiveUsers());

            return "loans/list";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar préstamos: " + e.getMessage());
            return "loans/list";
        }
    }

    /**
     * MUESTRA EL FORMULARIO PARA CREAR UN NUEVO PRESTAMO
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showCreateForm(@RequestParam(required = false) Long bookId,
                                 @RequestParam(required = false) Long userId,
                                 Model model) {
        try {
            // DATOS PARA EL FORMULARIO
            List<Book> availableBooks = bookService.findAvailableBooks();
            List<User> activeUsers = userService.findAllActiveUsers();

            model.addAttribute("availableBooks", availableBooks);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("preselectedBookId", bookId);
            model.addAttribute("preselectedUserId", userId);
            model.addAttribute("pageTitle", "Crear Nuevo Préstamo");

            return "loans/create";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "redirect:/loans";
        }
    }

    /**
     * PROCESA LA CREACION DE UN NUEVO PRESTAMO
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String createLoan(@RequestParam Long userId,
                             @RequestParam Long bookId,
                             @RequestParam(required = false) String notes,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            // VALIDAR QUE EL USUARIO PUEDE REALIZAR PRESTAMOS
            if (!loanService.canUserBorrow(userId)) {
                redirectAttributes.addFlashAttribute("error",
                        "El usuario no puede realizar más préstamos en este moment o");
                return "redirect:/loans/create?userId=" + userId + "&bookId=" + bookId;
            }

            // CREAR EL PRESTAMO
            Loan newLoan = loanService.createLoan(userId, bookId);

            // AGREGAR NOTAS SI SE PROPORCIONAN
            if (notes != null && !notes.trim().isEmpty()) {
                newLoan.setNotes(notes);
            }

            // OBTENER INFORMACION PARA EL MENSAJE
            Optional<User> userOptional = userService.findUserById(userId);
            Optional<Book> bookOptional = bookService.findBookById(bookId);

            String userName = userOptional.map(User::getFullName).orElse("Usuario");
            String bookTitle = bookOptional.map(Book::getTitle).orElse("Libro");

            redirectAttributes.addFlashAttribute("success",
                    "Préstamo creado exitosamente: '" + bookTitle + "' para " + userName);
            return "redirect:/loans/" + newLoan.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al crear préstamo: " + e.getMessage());
            return "redirect:/loans/create";
        }
    }

    /**
     * MUESTRA LOS DETALLES DE UN PRESTAMO ESPECIFICO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or (hasRole('USER') and @loanService.findLoanById(#id).orElse(new com.biblioteca.digital.model.Loan()).user.username == authentication.name)")
    public String viewLoan(@PathVariable Long id, Model model, Principal principal) {
        try {
            Optional<Loan> loanOptional = loanService.findLoanById(id);
            if (!loanOptional.isPresent()) {
                model.addAttribute("error", "Préstamo no encontrado");
                return "redirect:/loans";
            }

            Loan loan = loanOptional.get();

            // VERIFICAR PERMISOS PARA USUARIOS REGULARES
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser != null &&
                    currentUser.getRole().name().equals("USER") &&
                    !loan.getUser().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "No tiene permisos para ver este préstamo");
                return "redirect:/";
            }

            // CALCULAR INFORMACION ADICIONAL
            boolean isOverdue = loan.isOverdue();
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), loan.getDueDate());
            boolean canRenew = loan.canBeRenewed();

            model.addAttribute("loan", loan);
            model.addAttribute("isOverdue", isOverdue);
            model.addAttribute("daysUntilDue", daysUntilDue);
            model.addAttribute("canRenew", canRenew);
            model.addAttribute("pageTitle", "Préstamo #" + loan.getId());

            return "loans/view";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar préstamo: " + e.getMessage());
            return "redirect:/loans";
        }
    }

    /**
     * PROCESA LA DEVOLUCION DE UN LIBRO
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String returnBook(@PathVariable Long id,
                             @RequestParam(required = false) String returnNotes,
                             @RequestParam(required = false, defaultValue = "false") boolean damaged,
                             @RequestParam(required = false) String damageDescription,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {
        try {
            Optional<Loan> loanOptional = loanService.findLoanById(id);
            if (!loanOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Préstamo no encontrado");
                return "redirect:/loans";
            }

            Loan loan = loanOptional.get();
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);

            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Error de autenticación");
                return "redirect:/loans/" + id;
            }

            // PROCESAR DEVOLUCION SEGUN EL ESTADO
            if (damaged && damageDescription != null && !damageDescription.trim().isEmpty()) {
                // LIBRO DEVUELTO CON DAÑOS
                loanService.markLoanAsDamaged(id, damageDescription, currentUser.getId());
                redirectAttributes.addFlashAttribute("warning",
                        "Libro devuelto con daños registrados. Se aplicará multa correspondiente.");
            } else {
                // DEVOLUCION NORMAL
                Loan returnedLoan = loanService.processReturn(id);

                if (returnedLoan.getStatus() == LoanStatus.RETURNED_LATE) {
                    redirectAttributes.addFlashAttribute("warning",
                            "Libro devuelto con retraso. Multa aplicada: €" + returnedLoan.getFineAmount());
                } else {
                    redirectAttributes.addFlashAttribute("success", "Libro devuelto exitosamente");
                }
            }

            // AGREGAR NOTAS DE DEVOLUCION
            if (returnNotes != null && !returnNotes.trim().isEmpty()) {
                loan.setNotes((loan.getNotes() != null ? loan.getNotes() + " | " : "") +
                        "DEVOLUCIÓN: " + returnNotes);
            }

            return "redirect:/loans/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar devolución: " + e.getMessage());
            return "redirect:/loans/" + id;
        }
    }

    /**
     * RENUEVA UN PRESTAMO
     */
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or (hasRole('USER') and @loanService.findLoanById(#id).orElse(new com.biblioteca.digital.model.Loan()).user.username == authentication.name)")
    public String renewLoan(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            Loan renewedLoan = loanService.renewLoan(id);
            redirectAttributes.addFlashAttribute("success",
                    "Préstamo renovado exitosamente. Nueva fecha límite: " + renewedLoan.getDueDate());
            return "redirect:/loans/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al renovar préstamo: " + e.getMessage());
            return "redirect:/loans/" + id;
        }
    }

    /**
     * PROCESA EL PAGO DE UNA MULTA
     */
    @PostMapping("/{id}/pay-fine")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String payFine(@PathVariable Long id,
                          @RequestParam BigDecimal paidAmount,
                          @RequestParam(required = false) String paymentMethod,
                          RedirectAttributes redirectAttributes) {
        try {
            Optional<Loan> loanOptional = loanService.findLoanById(id);
            if (!loanOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Préstamo no encontrado");
                return "redirect:/loans";
            }

            Loan loan = loanOptional.get();

            // VALIDAR MONTO
            if (paidAmount.compareTo(loan.getFineAmount()) < 0) {
                redirectAttributes.addFlashAttribute("error",
                        "El monto pagado es menor a la multa total: €" + loan.getFineAmount());
                return "redirect:/loans/" + id;
            }

            // PROCESAR PAGO
            loanService.payFine(id);

            // REGISTRAR INFORMACION DEL PAGO
            String paymentInfo = "PAGO MULTA: €" + paidAmount;
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                paymentInfo += " (" + paymentMethod + ")";
            }

            loan.setNotes((loan.getNotes() != null ? loan.getNotes() + " | " : "") + paymentInfo);

            redirectAttributes.addFlashAttribute("success",
                    "Multa pagada exitosamente. Monto: €" + paidAmount);
            return "redirect:/loans/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar pago: " + e.getMessage());
            return "redirect:/loans/" + id;
        }
    }

    /**
     * MUESTRA PRESTAMOS VENCIDOS
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showOverdueLoans(Model model) {
        try {
            List<Loan> overdueLoans = loanService.findOverdueLoans();
            BigDecimal totalFines = loanService.calculateTotalUnpaidFines();

            model.addAttribute("overdueLoans", overdueLoans);
            model.addAttribute("totalFines", totalFines);
            model.addAttribute("pageTitle", "Préstamos Vencidos");

            return "loans/overdue";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar préstamos vencidos: " + e.getMessage());
            return "loans/overdue";
        }
    }

    /**
     * MUESTRA PRESTAMOS QUE VENCEN PRONTO
     */
    @GetMapping("/due-soon")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showDueSoonLoans(@RequestParam(defaultValue = "3") int days,
                                   Model model) {
        try {
            List<Loan> dueSoonLoans = loanService.findLoansDueSoon(days);
            List<Loan> dueToday = loanService.findLoansDueToday();

            model.addAttribute("dueSoonLoans", dueSoonLoans);
            model.addAttribute("dueToday", dueToday);
            model.addAttribute("daysAhead", days);
            model.addAttribute("pageTitle", "Próximos Vencimientos");

            return "loans/due-soon";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar próximos vencimientos: " + e.getMessage());
            return "loans/due-soon";
        }
    }

    /**
     * MUESTRA EL HISTORIAL DE PRESTAMOS DE UN USUARIO
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or (hasRole('USER') and #userId == authentication.principal.id)")
    public String showUserLoans(@PathVariable Long userId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        try {
            Optional<User> userOptional = userService.findUserById(userId);
            if (!userOptional.isPresent()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/loans";
            }

            User user = userOptional.get();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate"));
            Page<Loan> userLoansPage = loanService.findLoanHistoryForUser(userId, pageable);

            // ESTADISTICAS DEL USUARIO
            long activeLoans = loanService.findActiveLoansForUser(userId).size();
            long overdueLoans = loanService.findOverdueLoansForUser(userId).size();

            model.addAttribute("user", user);
            model.addAttribute("userLoansPage", userLoansPage);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("overdueLoans", overdueLoans);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userLoansPage.getTotalPages());
            model.addAttribute("pageTitle", "Préstamos de " + user.getFullName());

            return "loans/user-history";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar historial: " + e.getMessage());
            return "redirect:/loans";
        }
    }

    /**
     * MUESTRA ESTADISTICAS DE PRESTAMOS
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showStatistics(Model model) {
        try {
            // ESTADISTICAS GENERALES
            long totalLoans = loanService.countTotalLoans();
            long activeLoans = loanService.countActiveLoans();
            List<Loan> overdueLoans = loanService.findOverdueLoans();
            BigDecimal totalFines = loanService.calculateTotalUnpaidFines();

            // ESTADISTICAS POR ESTADO
            List<Object[]> statusStats = loanService.getLoanStatisticsByStatus();

            // ESTADISTICAS MENSUALES
            List<Object[]> monthlyStats = loanService.getLoanStatisticsByMonth();

            // USUARIOS MAS ACTIVOS
            Page<Object[]> activeUsers = loanService.findMostActiveUsers(PageRequest.of(0, 10));

            // LIBROS MAS PRESTADOS
            Page<Object[]> popularBooks = loanService.findMostBorrowedBooks(PageRequest.of(0, 10));

            model.addAttribute("totalLoans", totalLoans);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("overdueLoansCount", overdueLoans.size());
            model.addAttribute("totalFines", totalFines);
            model.addAttribute("statusStats", statusStats);
            model.addAttribute("monthlyStats", monthlyStats);
            model.addAttribute("activeUsers", activeUsers.getContent());
            model.addAttribute("popularBooks", popularBooks.getContent());
            model.addAttribute("pageTitle", "Estadísticas de Préstamos");

            return "loans/statistics";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
            return "loans/statistics";
        }
    }

    /**
     * MARCA UN PRESTAMO COMO PERDIDO
     */
    @PostMapping("/{id}/mark-lost")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String markAsLost(@PathVariable Long id,
                             @RequestParam String reason,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {
        try {
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Error de autenticación");
                return "redirect:/loans/" + id;
            }

            loanService.markLoanAsLost(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("warning",
                    "Préstamo marcado como perdido. Se aplicará multa por reposición.");
            return "redirect:/loans/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al marcar como perdido: " + e.getMessage());
            return "redirect:/loans/" + id;
        }
    }

    /**
     * CANCELA UN PRESTAMO
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public String cancelLoan(@PathVariable Long id,
                             @RequestParam String reason,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {
        try {
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Error de autenticación");
                return "redirect:/loans/" + id;
            }

            loanService.cancelLoan(id, reason, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Préstamo cancelado exitosamente");
            return "redirect:/loans/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al cancelar préstamo: " + e.getMessage());
            return "redirect:/loans/" + id;
        }
    }

    /**
     * GENERA REPORTE DE PRESTAMOS MOROSOS
     */
    @GetMapping("/overdue-report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String generateOverdueReport(Model model) {
        try {
            List<Loan> overdueReport = loanService.generateOverdueReport();
            BigDecimal totalFines = loanService.calculateTotalUnpaidFines();

            model.addAttribute("overdueReport", overdueReport);
            model.addAttribute("totalFines", totalFines);
            model.addAttribute("reportDate", LocalDate.now());
            model.addAttribute("pageTitle", "Reporte de Morosos");

            return "loans/overdue-report";

        } catch (Exception e) {
            model.addAttribute("error", "Error al generar reporte: " + e.getMessage());
            return "loans/overdue-report";
        }
    }

    /**
     * MUESTRA MIS PRESTAMOS (PARA USUARIOS REGULARES)
     */
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('USER')")
    public String showMyLoans(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model,
                              Principal principal) {
        try {
            Optional<User> userOptional = userService.findUserByUsername(principal.getName());
            if (!userOptional.isPresent()) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/";
            }

            User currentUser = userOptional.get();

            // PRESTAMOS ACTIVOS
            List<Loan> activeLoans = loanService.findActiveLoansForUser(currentUser.getId());

            // HISTORIAL CON PAGINACION
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate"));
            Page<Loan> loanHistory = loanService.findLoanHistoryForUser(currentUser.getId(), pageable);

            // PRESTAMOS VENCIDOS
            List<Loan> overdueLoans = loanService.findOverdueLoansForUser(currentUser.getId());

            model.addAttribute("user", currentUser);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("loanHistory", loanHistory);
            model.addAttribute("overdueLoans", overdueLoans);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", loanHistory.getTotalPages());
            model.addAttribute("pageTitle", "Mis Préstamos");

            return "loans/my-loans";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar préstamos: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * PROCESA ACTUALIZACION MASIVA DE PRESTAMOS VENCIDOS
     */
    @PostMapping("/update-overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String updateOverdueLoans(RedirectAttributes redirectAttributes) {
        try {
            int updatedCount = loanService.updateOverdueLoans();
            redirectAttributes.addFlashAttribute("success",
                    "Se actualizaron " + updatedCount + " préstamos vencidos");
            return "redirect:/loans/overdue";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar préstamos: " + e.getMessage());
            return "redirect:/loans/overdue";
        }
    }

    /**
     * EXPORTA REPORTES DE PRESTAMOS
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String exportLoans(@RequestParam(defaultValue = "excel") String format,
                              @RequestParam(required = false) LoanStatus status,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              RedirectAttributes redirectAttributes) {
        try {
            // TODO: IMPLEMENTAR EXPORTACION REAL
            redirectAttributes.addFlashAttribute("info",
                    "Funcionalidad de exportación en desarrollo");
            return "redirect:/loans";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al exportar préstamos: " + e.getMessage());
            return "redirect:/loans";
        }
    }

    /**
     * METODOS AUXILIARES
     */

    /**
     * OBTIENE PRESTAMOS CON FILTROS APLICADOS
     */
    private List<Loan> getAllLoansWithFilters(LoanStatus status, Long userId,
                                              String startDate, String endDate) {
        // IMPLEMENTACION BASICA - EN PRODUCCION USARIA SPECIFICATIONS
        List<Loan> allLoans = new java.util.ArrayList<>();

        if (userId != null) {
            // FILTRAR POR USUARIO
            allLoans.addAll(loanService.findActiveLoansForUser(userId));
        } else if (status != null) {
            // FILTRAR POR ESTADO
            switch (status) {
                case OVERDUE:
                    allLoans.addAll(loanService.findOverdueLoans());
                    break;
                case ACTIVE:
                    // Obtener todos los préstamos activos del sistema
                    allLoans.addAll(userService.findAllActiveUsers().stream()
                            .flatMap(user -> loanService.findActiveLoansForUser(user.getId()).stream())
                            .collect(java.util.stream.Collectors.toList()));
                    break;
                default:
                    // TODO: Implementar otros filtros de estado
                    break;
            }
        } else {
            // SIN FILTROS - OBTENER TODOS (LIMITADO PARA PERFORMANCE)
            allLoans.addAll(userService.findAllActiveUsers().stream()
                    .flatMap(user -> loanService.findActiveLoansForUser(user.getId()).stream())
                    .collect(java.util.stream.Collectors.toList()));
        }

        // FILTRAR POR FECHAS SI SE PROPORCIONAN
        if (startDate != null && endDate != null) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                allLoans = allLoans.stream()
                        .filter(loan -> !loan.getLoanDate().isBefore(start) &&
                                !loan.getLoanDate().isAfter(end))
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                // IGNORAR FILTRO DE FECHA SI HAY ERROR DE PARSING
            }
        }

        return allLoans;
    }

    /**
     * CONVIERTE UNA LISTA A PAGE PARA PAGINACION
     */
    private Page<Loan> convertListToPage(List<Loan> loans, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), loans.size());

        List<Loan> pageContent = start < loans.size() ? loans.subList(start, end) :
                new java.util.ArrayList<>();
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, loans.size());
    }
}