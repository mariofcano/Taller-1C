package com.biblioteca.digital.config;

import com.biblioteca.digital.model.*;
import com.biblioteca.digital.repository.*;
import com.biblioteca.digital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * CARGADOR DE DATOS INICIALES PARA EL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA CLASE SE ENCARGA DE POBLAR LA BASE DE DATOS CON DATOS DE EJEMPLO
 * CUANDO LA APLICACION INICIA POR PRIMERA VEZ. CREO USUARIOS, LIBROS
 * Y PRESTAMOS DE MUESTRA PARA QUE EL SISTEMA TENGA CONTENIDO VISIBLE
 * Y LAS ESTADISTICAS DE LA PAGINA PRINCIPAL MUESTREN INFORMACION REAL.
 *
 * UTILIZO CommandLineRunner PARA QUE ESTE CODIGO SE EJECUTE AUTOMATICAMENTE
 * DESPUES DE QUE SPRING BOOT TERMINE DE INICIALIZAR TODOS LOS COMPONENTES.
 * ESTO GARANTIZA QUE TODOS LOS SERVICIOS Y REPOSITORIOS ESTEN DISPONIBLES.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 *
 * @see CommandLineRunner
 * @see User
 * @see Book
 * @see Loan
 */
@Component
public class DataLoader implements CommandLineRunner {

    /**
     * REPOSITORIO PARA GESTION DE USUARIOS
     *
     * INYECTO EL REPOSITORIO DE USUARIOS PARA PODER CREAR Y GUARDAR
     * USUARIOS DE EJEMPLO EN LA BASE DE DATOS. NECESITO CREAR DIFERENTES
     * TIPOS DE USUARIOS PARA PROBAR TODOS LOS ROLES DEL SISTEMA.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * REPOSITORIO PARA GESTION DE LIBROS
     *
     * INYECTO EL REPOSITORIO DE LIBROS PARA POBLAR EL CATALOGO
     * CON EJEMPLARES DE DIFERENTES CATEGORIAS Y GENEROS.
     * ESTO PERMITE MOSTRAR UN CATALOGO DIVERSO Y ATRACTIVO.
     */
    @Autowired
    private BookRepository bookRepository;

    /**
     * REPOSITORIO PARA GESTION DE PRESTAMOS
     *
     * INYECTO EL REPOSITORIO DE PRESTAMOS PARA CREAR PRESTAMOS
     * DE EJEMPLO QUE MUESTREN EL FUNCIONAMIENTO DEL SISTEMA
     * Y GENEREN ESTADISTICAS REALISTAS.
     */
    @Autowired
    private LoanRepository loanRepository;

    /**
     * ENCODER PARA CIFRADO DE CONTRASEÑAS
     *
     * INYECTO EL PASSWORD ENCODER PARA CIFRAR LAS CONTRASEÑAS
     * DE LOS USUARIOS DE EJEMPLO. MANTENGO LA CONSISTENCIA
     * CON EL SISTEMA DE SEGURIDAD CONFIGURADO.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * METODO PRINCIPAL QUE EJECUTA LA CARGA DE DATOS
     *
     * ESTE METODO SE EJECUTA AUTOMATICAMENTE CUANDO LA APLICACION INICIA.
     * VERIFICO SI YA EXISTEN DATOS EN LA BASE DE DATOS PARA EVITAR
     * DUPLICADOS EN REINICIOS. SI NO HAY DATOS, PROCEDO A CREAR
     * USUARIOS, LIBROS Y PRESTAMOS DE EJEMPLO.
     *
     * @param args ARGUMENTOS DE LINEA DE COMANDOS (NO UTILIZADOS)
     * @throws Exception SI OCURRE ERROR DURANTE LA CARGA DE DATOS
     */
    @Override
    public void run(String... args) throws Exception {
        // VERIFICO SI YA EXISTEN DATOS PARA EVITAR DUPLICACION
        if (userRepository.count() == 0) {
            System.out.println("CARGANDO DATOS INICIALES DEL SISTEMA...");

            // CARGO DATOS EN ORDEN JERARQUICO: USUARIOS -> LIBROS -> PRESTAMOS
            loadUsers();
            loadBooks();
            loadLoans();

            System.out.println("DATOS INICIALES CARGADOS EXITOSAMENTE");
        } else {
            System.out.println("DATOS YA EXISTENTES - OMITIENDO CARGA INICIAL");
        }
    }

    /**
     * CARGA USUARIOS DE EJEMPLO EN EL SISTEMA
     *
     * CREO USUARIOS CON DIFERENTES ROLES PARA DEMOSTRAR
     * LAS CAPACIDADES DEL SISTEMA. INCLUYO UN ADMINISTRADOR,
     * UN BIBLIOTECARIO Y VARIOS USUARIOS REGULARES.
     */
    private void loadUsers() {
        // CREO USUARIO ADMINISTRADOR PRINCIPAL
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@biblioteca.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Mario Flores - Administrador");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setPhone("+34 666 123 456");
        admin.setAddress("Calle Principal, 123 - Madrid");
        userRepository.save(admin);

        // CREO BIBLIOTECARIO PARA GESTION DIARIA
        User librarian = new User();
        librarian.setUsername("bibliotecario");
        librarian.setEmail("bibliotecario@biblioteca.com");
        librarian.setPassword(passwordEncoder.encode("biblio123"));
        librarian.setFullName("Ana Garcia - Bibliotecaria");
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setActive(true);
        librarian.setPhone("+34 666 789 012");
        librarian.setAddress("Avenida del Libro, 456 - Madrid");
        userRepository.save(librarian);

        // CREO USUARIOS REGULARES PARA PRESTAMOS
        List<String[]> usuariosData = Arrays.asList(
                new String[]{"juan_perez", "juan_perez@email.com", "Juan Perez Lopez", "+34 666 111 222"},
                new String[]{"maria_gonzalez", "maria_gonzalez@email.com", "Maria Gonzalez Ruiz", "+34 666 333 444"},
                new String[]{"carlos_rodriguez", "carlos_rodriguez@email.com", "Carlos Rodriguez Martin", "+34 666 555 666"},
                new String[]{"laura_martin", "laura_martin@email.com", "Laura Martin Fernandez", "+34 666 777 888"},
                new String[]{"david_sanchez", "david_sanchez@email.com", "David Sanchez Lopez", "+34 666 999 000"}
        );

        for (String[] userData : usuariosData) {
            User user = new User();
            user.setUsername(userData[0]);
            user.setEmail(userData[1]);
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFullName(userData[2]);
            user.setRole(UserRole.USER);
            user.setActive(true);
            user.setPhone(userData[3]);
            user.setAddress("Direccion de " + userData[2]);
            userRepository.save(user);
        }

        System.out.println("USUARIOS CARGADOS: " + userRepository.count() + " registros");
    }

    /**
     * CARGA LIBROS DE EJEMPLO EN EL CATALOGO
     *
     * CREO UNA COLECCION DIVERSA DE LIBROS DE DIFERENTES CATEGORIAS
     * PARA MOSTRAR LA VARIEDAD DEL CATALOGO. INCLUYO ALGUNOS LIBROS
     * DESTACADOS Y OTROS REGULARES PARA PROBAR LAS FUNCIONALIDADES.
     */
    private void loadBooks() {
        // LISTA DE LIBROS DE EJEMPLO CON INFORMACION COMPLETA
        Object[][] librosData = {
                // FICCION
                {"978-84-376-0494-7", "Cien años de soledad", "Gabriel García Márquez",
                        "Una obra maestra del realismo mágico que narra la historia de la familia Buendía.",
                        BookCategory.FICTION, "Editorial Sudamericana", LocalDate.of(1967, 6, 5), 432, "ES",
                        new BigDecimal("18.50"), 3, true},

                {"978-84-204-8216-5", "1984", "George Orwell",
                        "Una distopía que presenta un futuro totalitario donde el Gran Hermano todo lo ve.",
                        BookCategory.FICTION, "Debolsillo", LocalDate.of(1949, 6, 8), 328, "ES",
                        new BigDecimal("12.95"), 5, true},

                // CIENCIA FICCION
                {"978-84-450-7567-4", "Dune", "Frank Herbert",
                        "Épica historia de política, religión y supervivencia en el planeta desértico Arrakis.",
                        BookCategory.SCIENCE_FICTION, "Plaza & Janés", LocalDate.of(1965, 4, 1), 688, "ES",
                        new BigDecimal("24.90"), 2, true},

                // MISTERIO
                {"978-84-206-7318-4", "El nombre de la rosa", "Umberto Eco",
                        "Misterio medieval ambientado en un monasterio benedictino del siglo XIV.",
                        BookCategory.MYSTERY, "Lumen", LocalDate.of(1980, 10, 15), 632, "ES",
                        new BigDecimal("22.00"), 4, false},

                // FANTASIA
                {"978-84-450-7761-6", "El Señor de los Anillos", "J.R.R. Tolkien",
                        "La épica aventura de Frodo y la Comunidad del Anillo para destruir el Anillo Único.",
                        BookCategory.FANTASY, "Minotauro", LocalDate.of(1954, 7, 29), 1178, "ES",
                        new BigDecimal("35.95"), 3, true},

                // NO FICCION
                {"978-84-233-5094-2", "Sapiens", "Yuval Noah Harari",
                        "De animales a dioses: Una breve historia de la humanidad.",
                        BookCategory.NON_FICTION, "Debate", LocalDate.of(2011, 1, 1), 496, "ES",
                        new BigDecimal("19.90"), 6, true},

                // HISTORIA
                {"978-84-376-2186-9", "Una historia de España", "Arturo Pérez-Reverte",
                        "Un recorrido personal y apasionado por la historia de España.",
                        BookCategory.HISTORY, "Alfaguara", LocalDate.of(2019, 10, 17), 256, "ES",
                        new BigDecimal("17.90"), 4, false},

                // TECNOLOGIA
                {"978-84-415-3984-5", "El mundo digital", "Nicholas Negroponte",
                        "Reflexiones sobre el impacto de la tecnología digital en nuestras vidas.",
                        BookCategory.TECHNOLOGY, "Ediciones B", LocalDate.of(1995, 1, 1), 288, "ES",
                        new BigDecimal("16.50"), 2, false},

                // AUTOAYUDA
                {"978-84-666-5847-2", "El poder del ahora", "Eckhart Tolle",
                        "Una guía para la iluminación espiritual y la vida en el presente.",
                        BookCategory.SELF_HELP, "Gaia Ediciones", LocalDate.of(1997, 1, 1), 264, "ES",
                        new BigDecimal("14.95"), 5, false},

                // INFANTIL
                {"978-84-261-3442-1", "El Principito", "Antoine de Saint-Exupéry",
                        "Un piloto perdido en el desierto conoce a un pequeño príncipe de otro planeta.",
                        BookCategory.CHILDREN, "Salamandra", LocalDate.of(1943, 4, 6), 128, "ES",
                        new BigDecimal("12.00"), 8, true}
        };

        // CREO Y GUARDO CADA LIBRO EN LA BASE DE DATOS
        for (Object[] bookData : librosData) {
            Book book = new Book();
            book.setIsbn((String) bookData[0]);
            book.setTitle((String) bookData[1]);
            book.setAuthor((String) bookData[2]);
            book.setDescription((String) bookData[3]);
            book.setCategory((BookCategory) bookData[4]);
            book.setPublisher((String) bookData[5]);
            book.setPublicationDate((LocalDate) bookData[6]);
            book.setPages((Integer) bookData[7]);
            book.setLanguage((String) bookData[8]);
            book.setPrice((BigDecimal) bookData[9]);
            book.setTotalCopies((Integer) bookData[10]);
            book.setAvailableCopies((Integer) bookData[10]);
            book.setFeatured((Boolean) bookData[11]);
            book.setActive(true);
            book.setLoanCount(0);

            bookRepository.save(book);
        }

        System.out.println("LIBROS CARGADOS: " + bookRepository.count() + " registros");
    }

    /**
     * CARGA PRESTAMOS DE EJEMPLO EN EL SISTEMA
     *
     * CREO PRESTAMOS CON DIFERENTES ESTADOS PARA MOSTRAR
     * EL FUNCIONAMIENTO COMPLETO DEL SISTEMA DE PRESTAMOS.
     * INCLUYO PRESTAMOS ACTIVOS, VENCIDOS Y DEVUELTOS.
     */
    private void loadLoans() {
        // OBTENGO USUARIOS Y LIBROS PARA CREAR PRESTAMOS
        List<User> users = userRepository.findByRole(UserRole.USER);
        List<Book> books = bookRepository.findAll();

        if (users.isEmpty() || books.isEmpty()) {
            System.out.println("NO HAY USUARIOS O LIBROS PARA CREAR PRESTAMOS");
            return;
        }

        // CREO PRESTAMOS ACTIVOS
        createLoan(users.get(0), books.get(0), LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, LoanStatus.ACTIVE);
        createLoan(users.get(1), books.get(1), LocalDate.now().minusDays(3), LocalDate.now().plusDays(11), null, LoanStatus.ACTIVE);
        createLoan(users.get(2), books.get(2), LocalDate.now().minusDays(7), LocalDate.now().plusDays(7), null, LoanStatus.ACTIVE);

        // CREO PRESTAMOS VENCIDOS
        createLoan(users.get(0), books.get(3), LocalDate.now().minusDays(20), LocalDate.now().minusDays(6), null, LoanStatus.OVERDUE);
        createLoan(users.get(3), books.get(4), LocalDate.now().minusDays(25), LocalDate.now().minusDays(11), null, LoanStatus.OVERDUE);

        // CREO PRESTAMOS DEVUELTOS
        createLoan(users.get(1), books.get(5), LocalDate.now().minusDays(30), LocalDate.now().minusDays(16), LocalDateTime.now().minusDays(18), LoanStatus.RETURNED);
        createLoan(users.get(2), books.get(6), LocalDate.now().minusDays(45), LocalDate.now().minusDays(31), LocalDateTime.now().minusDays(29), LoanStatus.RETURNED);
        createLoan(users.get(4), books.get(7), LocalDate.now().minusDays(60), LocalDate.now().minusDays(46), LocalDateTime.now().minusDays(35), LoanStatus.RETURNED_LATE);

        // ACTUALIZO CONTADORES DE PRESTAMOS EN LOS LIBROS
        updateBookLoanCounters();

        System.out.println("PRESTAMOS CARGADOS: " + loanRepository.count() + " registros");
    }

    /**
     * CREA UN PRESTAMO INDIVIDUAL CON LOS DATOS ESPECIFICADOS
     *
     * METODO AUXILIAR PARA CREAR PRESTAMOS DE FORMA CONSISTENTE.
     * CONFIGURA TODOS LOS CAMPOS NECESARIOS Y AJUSTA LA DISPONIBILIDAD
     * DE LOS LIBROS SEGUN EL ESTADO DEL PRESTAMO.
     *
     * @param user USUARIO QUE REALIZA EL PRESTAMO
     * @param book LIBRO QUE SE PRESTA
     * @param loanDate FECHA DE INICIO DEL PRESTAMO
     * @param dueDate FECHA LIMITE DE DEVOLUCION
     * @param returnDate FECHA REAL DE DEVOLUCION (NULL SI ESTA ACTIVO)
     * @param status ESTADO DEL PRESTAMO
     */
    private void createLoan(User user, Book book, LocalDate loanDate, LocalDate dueDate,
                            LocalDateTime returnDate, LoanStatus status) {
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(loanDate);
        loan.setDueDate(dueDate);
        loan.setReturnedAt(returnDate);
        loan.setStatus(status);
        loan.setRenewals(0);
        loan.setFineAmount(BigDecimal.ZERO);
        loan.setFinePaid(true);
        loan.setMaxRenewals(3);
        loan.setFinePerDay(new BigDecimal("0.50"));

        // CALCULO MULTA SI EL PRESTAMO ESTA VENCIDO O SE DEVOLVIO TARDE
        if (status == LoanStatus.OVERDUE || status == LoanStatus.RETURNED_LATE) {
            loan.calculateFine();
            loan.setFinePaid(false);
        }

        loanRepository.save(loan);

        // AJUSTO LA DISPONIBILIDAD DEL LIBRO
        if (returnDate == null) {
            // PRESTAMO ACTIVO - REDUZCO COPIAS DISPONIBLES
            book.setAvailableCopies(book.getAvailableCopies() - 1);
        }
        // INCREMENTO CONTADOR DE PRESTAMOS
        book.setLoanCount(book.getLoanCount() + 1);
        bookRepository.save(book);
    }

    /**
     * ACTUALIZA LOS CONTADORES DE PRESTAMOS EN LOS LIBROS
     *
     * METODO QUE SINCRONIZA EL CAMPO loanCount DE CADA LIBRO
     * CON EL NUMERO REAL DE PRESTAMOS REGISTRADOS EN LA BASE DE DATOS.
     * GARANTIZA CONSISTENCIA EN LAS ESTADISTICAS.
     */
    private void updateBookLoanCounters() {
        List<Book> books = bookRepository.findAll();
        for (Book book : books) {
            long loanCount = loanRepository.countActiveByBook(book);
            book.setLoanCount((int) loanCount);
            bookRepository.save(book);
        }
    }
}