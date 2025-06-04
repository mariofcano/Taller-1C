package com.biblioteca.digital.controller;

import com.biblioteca.digital.model.Book;
import com.biblioteca.digital.model.BookCategory;
import com.biblioteca.digital.model.User;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CONTROLADOR PARA LA GESTION COMPLETA DE LIBROS
 *
 * ESTE CONTROLADOR MANEJA TODAS LAS OPERACIONES CRUD DEL CATALOGO DE LIBROS
 * EN EL SISTEMA DE BIBLIOTECA DIGITAL. INCLUYE FUNCIONALIDADES PARA LISTAR,
 * CREAR, EDITAR, VER DETALLES Y GESTIONAR EL INVENTARIO DE LIBROS.
 *
 * IMPLEMENTA CONTROL DE ACCESO PARA GARANTIZAR QUE SOLO BIBLIOTECARIOS
 * Y ADMINISTRADORES PUEDAN MODIFICAR EL CATALOGO.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 */
@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /**
     * LISTA TODOS LOS LIBROS CON PAGINACION Y FILTROS
     *
     * MUESTRA UNA TABLA PAGINADA DE TODOS LOS LIBROS DEL CATALOGO
     * CON OPCIONES DE BUSQUEDA, FILTRADO POR CATEGORIA Y ORDENAMIENTO.
     */
    @GetMapping
    public String listBooks(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "12") int size,
                            @RequestParam(defaultValue = "title") String sortBy,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) BookCategory category,
                            @RequestParam(required = false) Boolean available,
                            @RequestParam(required = false) Boolean featured,
                            Model model) {
        try {
            // CONFIGURACION DE PAGINACION Y ORDENAMIENTO
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // BUSQUEDA Y FILTRADO
            List<Book> allBooks;
            if (search != null && !search.trim().isEmpty()) {
                allBooks = bookService.searchBooks(search);
            } else {
                allBooks = bookService.findAvailableBooks();
            }

            // APLICAR FILTROS
            if (category != null) {
                allBooks = allBooks.stream()
                        .filter(book -> book.getCategory().equals(category))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (available != null) {
                allBooks = allBooks.stream()
                        .filter(book -> available ? book.isAvailableForLoan() : !book.isAvailableForLoan())
                        .collect(java.util.stream.Collectors.toList());
            }

            if (featured != null) {
                allBooks = allBooks.stream()
                        .filter(book -> book.getFeatured().equals(featured))
                        .collect(java.util.stream.Collectors.toList());
            }

            // CONVERTIR A PAGE
            Page<Book> booksPage = convertListToPage(allBooks, pageable);

            // ESTADISTICAS PARA EL DASHBOARD
            long totalBooks = bookService.countActiveBooks();
            long availableBooks = bookService.countAvailableBooks();
            long outOfStockBooks = bookService.findOutOfStockBooks().size();
            long featuredBooks = bookService.findFeaturedBooks().size();

            // DATOS PARA LA VISTA
            model.addAttribute("booksPage", booksPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", booksPage.getTotalPages());
            model.addAttribute("totalElements", booksPage.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            model.addAttribute("search", search);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedAvailable", available);
            model.addAttribute("selectedFeatured", featured);

            // ESTADISTICAS
            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("availableBooks", availableBooks);
            model.addAttribute("outOfStockBooks", outOfStockBooks);
            model.addAttribute("featuredBooksCount", featuredBooks);

            // DATOS PARA FILTROS
            model.addAttribute("bookCategories", BookCategory.values());

            return "books/list";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de libros: " + e.getMessage());
            return "books/list";
        }
    }

    /**
     * MUESTRA EL FORMULARIO PARA CREAR UN NUEVO LIBRO
     */
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("bookCategories", BookCategory.values());
        model.addAttribute("pageTitle", "Agregar Nuevo Libro");
        return "books/create";
    }

    /**
     * PROCESA LA CREACION DE UN NUEVO LIBRO
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String createBook(@Valid @ModelAttribute Book book,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Principal principal) {
        try {
            // VALIDACIONES
            if (result.hasErrors()) {
                model.addAttribute("bookCategories", BookCategory.values());
                model.addAttribute("pageTitle", "Agregar Nuevo Libro");
                return "books/create";
            }

            // VERIFICAR QUE EL ISBN NO ESTE EN USO
            if (!bookService.isIsbnAvailable(book.getIsbn())) {
                model.addAttribute("error", "Ya existe un libro con el ISBN: " + book.getIsbn());
                model.addAttribute("bookCategories", BookCategory.values());
                model.addAttribute("pageTitle", "Agregar Nuevo Libro");
                return "books/create";
            }

            // OBTENER USUARIO ACTUAL PARA VALIDACION
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser == null) {
                model.addAttribute("error", "Error de autenticación");
                return "books/create";
            }

            // CREAR LIBRO
            Book createdBook = bookService.registerBook(
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getDescription(),
                    book.getCategory(),
                    book.getPublisher(),
                    book.getPublicationDate(),
                    book.getPages(),
                    book.getLanguage(),
                    book.getPrice(),
                    book.getTotalCopies(),
                    currentUser.getId()
            );

            redirectAttributes.addFlashAttribute("success",
                    "Libro '" + createdBook.getTitle() + "' agregado exitosamente al catálogo");
            return "redirect:/books";

        } catch (Exception e) {
            model.addAttribute("error", "Error al crear libro: " + e.getMessage());
            model.addAttribute("bookCategories", BookCategory.values());
            model.addAttribute("pageTitle", "Agregar Nuevo Libro");
            return "books/create";
        }
    }

    /**
     * MUESTRA LOS DETALLES DE UN LIBRO ESPECIFICO
     */
    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        try {
            Optional<Book> bookOptional = bookService.findBookById(id);
            if (!bookOptional.isPresent()) {
                model.addAttribute("error", "Libro no encontrado");
                return "redirect:/books";
            }

            Book book = bookOptional.get();

            // CALCULAR ESTADISTICAS DEL LIBRO
            int loanedCopies = book.getLoanedCopies();
            double availabilityPercentage = book.getAvailabilityPercentage();
            boolean isPopular = book.isPopular();

            model.addAttribute("book", book);
            model.addAttribute("loanedCopies", loanedCopies);
            model.addAttribute("availabilityPercentage", availabilityPercentage);
            model.addAttribute("isPopular", isPopular);
            model.addAttribute("pageTitle", book.getTitle());

            return "books/view";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar libro: " + e.getMessage());
            return "redirect:/books";
        }
    }

    /**
     * MUESTRA EL FORMULARIO PARA EDITAR UN LIBRO
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Optional<Book> bookOptional = bookService.findBookById(id);
            if (!bookOptional.isPresent()) {
                model.addAttribute("error", "Libro no encontrado");
                return "redirect:/books";
            }

            Book book = bookOptional.get();
            model.addAttribute("book", book);
            model.addAttribute("bookCategories", BookCategory.values());
            model.addAttribute("pageTitle", "Editar " + book.getTitle());

            return "books/edit";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar libro: " + e.getMessage());
            return "redirect:/books";
        }
    }

    /**
     * PROCESA LA ACTUALIZACION DE UN LIBRO
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute Book bookForm,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Principal principal) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("bookCategories", BookCategory.values());
                model.addAttribute("pageTitle", "Editar Libro");
                return "books/edit";
            }

            // OBTENER USUARIO ACTUAL
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser == null) {
                model.addAttribute("error", "Error de autenticación");
                return "books/edit";
            }

            // ACTUALIZAR LIBRO
            Book updatedBook = bookService.updateBook(
                    id,
                    bookForm.getTitle(),
                    bookForm.getAuthor(),
                    bookForm.getDescription(),
                    bookForm.getCategory(),
                    bookForm.getPublisher(),
                    bookForm.getPublicationDate(),
                    bookForm.getPages(),
                    bookForm.getLanguage(),
                    bookForm.getPrice(),
                    currentUser.getId()
            );

            redirectAttributes.addFlashAttribute("success", "Libro actualizado exitosamente");
            return "redirect:/books/" + id;

        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar libro: " + e.getMessage());
            model.addAttribute("bookCategories", BookCategory.values());
            model.addAttribute("pageTitle", "Editar Libro");
            return "books/edit";
        }
    }

    /**
     * ACTUALIZA EL INVENTARIO DE UN LIBRO
     */
    @PostMapping("/{id}/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String updateInventory(@PathVariable Long id,
                                  @RequestParam Integer totalCopies,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {
        try {
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Error de autenticación");
                return "redirect:/books/" + id;
            }

            bookService.updateBookInventory(id, totalCopies, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Inventario actualizado exitosamente");
            return "redirect:/books/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar inventario: " + e.getMessage());
            return "redirect:/books/" + id;
        }
    }

    /**
     * MARCA UN LIBRO COMO DESTACADO O NO DESTACADO
     */
    @PostMapping("/{id}/toggle-featured")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String toggleFeaturedStatus(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes,
                                       Principal principal) {
        try {
            Optional<Book> bookOptional = bookService.findBookById(id);
            if (!bookOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Libro no encontrado");
                return "redirect:/books";
            }

            Book book = bookOptional.get();
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);

            if (currentUser != null) {
                Boolean newFeaturedStatus = !book.getFeatured();
                bookService.toggleBookFeaturedStatus(id, newFeaturedStatus, currentUser.getId());

                String statusText = newFeaturedStatus ? "destacado" : "no destacado";
                redirectAttributes.addFlashAttribute("success",
                        "Libro marcado como " + statusText + " exitosamente");
            }

            return "redirect:/books/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al cambiar estado destacado: " + e.getMessage());
            return "redirect:/books/" + id;
        }
    }

    /**
     * ACTIVA O DESACTIVA UN LIBRO
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String toggleBookStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        try {
            Optional<Book> bookOptional = bookService.findBookById(id);
            if (!bookOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Libro no encontrado");
                return "redirect:/books";
            }

            Book book = bookOptional.get();
            User currentUser = userService.findUserByUsername(principal.getName()).orElse(null);

            if (currentUser != null) {
                Boolean newStatus = !book.getActive();
                bookService.toggleBookActiveStatus(id, newStatus, currentUser.getId());

                String statusText = newStatus ? "activado" : "desactivado";
                redirectAttributes.addFlashAttribute("success",
                        "Libro " + statusText + " exitosamente");
            }

            return "redirect:/books";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al cambiar estado del libro: " + e.getMessage());
            return "redirect:/books";
        }
    }

    /**
     * BUSQUEDA AVANZADA DE LIBROS
     */
    @GetMapping("/advanced-search")
    public String showAdvancedSearch(Model model) {
        model.addAttribute("bookCategories", BookCategory.values());
        model.addAttribute("pageTitle", "Búsqueda Avanzada");
        return "books/advanced-search";
    }

    /**
     * PROCESA LA BUSQUEDA AVANZADA
     */
    @PostMapping("/advanced-search")
    public String processAdvancedSearch(@RequestParam(required = false) String title,
                                        @RequestParam(required = false) String author,
                                        @RequestParam(required = false) BookCategory category,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice,
                                        @RequestParam(required = false) String language,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "12") int size,
                                        Model model) {
        try {
            List<Book> searchResults = bookService.findBooksByAdvancedSearch(
                    title, author, category, minPrice, maxPrice, language);

            Pageable pageable = PageRequest.of(page, size);
            Page<Book> booksPage = convertListToPage(searchResults, pageable);

            model.addAttribute("booksPage", booksPage);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("resultCount", searchResults.size());
            model.addAttribute("bookCategories", BookCategory.values());

            // MANTENER PARAMETROS DE BUSQUEDA
            model.addAttribute("searchTitle", title);
            model.addAttribute("searchAuthor", author);
            model.addAttribute("searchCategory", category);
            model.addAttribute("searchMinPrice", minPrice);
            model.addAttribute("searchMaxPrice", maxPrice);
            model.addAttribute("searchLanguage", language);

            return "books/advanced-search";

        } catch (Exception e) {
            model.addAttribute("error", "Error en la búsqueda: " + e.getMessage());
            return "books/advanced-search";
        }
    }

    /**
     * MUESTRA LIBROS CON BAJA DISPONIBILIDAD
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showLowStockBooks(@RequestParam(defaultValue = "2") int threshold,
                                    Model model) {
        try {
            List<Book> lowStockBooks = bookService.findBooksWithLowStock(threshold);

            model.addAttribute("lowStockBooks", lowStockBooks);
            model.addAttribute("threshold", threshold);
            model.addAttribute("pageTitle", "Libros con Baja Disponibilidad");

            return "books/low-stock";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar libros: " + e.getMessage());
            return "books/low-stock";
        }
    }

    /**
     * MUESTRA ESTADISTICAS DE LIBROS
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String showStatistics(Model model) {
        try {
            // ESTADISTICAS GENERALES
            long totalBooks = bookService.countActiveBooks();
            long availableBooks = bookService.countAvailableBooks();
            List<Book> popularBooks = bookService.findPopularBooks(PageRequest.of(0, 10)).getContent();
            List<Book> neverBorrowedBooks = bookService.findNeverBorrowedBooks();

            // ESTADISTICAS POR CATEGORIA
            List<Object[]> categoryStats = bookService.getBookStatisticsByCategory();

            // VALOR TOTAL DEL CATALOGO
            BigDecimal totalValue = bookService.calculateTotalCatalogValue();

            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("availableBooks", availableBooks);
            model.addAttribute("popularBooks", popularBooks);
            model.addAttribute("neverBorrowedBooks", neverBorrowedBooks);
            model.addAttribute("categoryStats", categoryStats);
            model.addAttribute("totalValue", totalValue);
            model.addAttribute("pageTitle", "Estadísticas del Catálogo");

            return "books/statistics";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
            return "books/statistics";
        }
    }

    /**
     * EXPORTA EL CATALOGO EN DIFERENTES FORMATOS
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public String exportCatalog(@RequestParam(defaultValue = "excel") String format,
                                RedirectAttributes redirectAttributes) {
        try {
            // TODO: IMPLEMENTAR EXPORTACION REAL
            redirectAttributes.addFlashAttribute("info",
                    "Funcionalidad de exportación en desarrollo");
            return "redirect:/books";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al exportar catálogo: " + e.getMessage());
            return "redirect:/books";
        }
    }

    /**
     * CONVIERTE UNA LISTA A PAGE PARA PAGINACION
     */
    private Page<Book> convertListToPage(List<Book> books, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());

        List<Book> pageContent = start < books.size() ? books.subList(start, end) :
                new java.util.ArrayList<>();
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, books.size());
    }
}