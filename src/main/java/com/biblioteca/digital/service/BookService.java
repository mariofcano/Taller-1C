package com.biblioteca.digital.service;

import com.biblioteca.digital.model.Book;
import com.biblioteca.digital.model.BookCategory;
import com.biblioteca.digital.model.User;
import com.biblioteca.digital.repository.BookRepository;
import com.biblioteca.digital.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO DE NEGOCIO PARA LA GESTION COMPLETA DEL CATALOGO DE LIBROS
 *
 * ESTA CLASE IMPLEMENTA TODA LA LOGICA DE NEGOCIO RELACIONADA CON LA GESTION
 * DEL CATALOGO DE LIBROS EN EL SISTEMA DE BIBLIOTECA DIGITAL. PROPORCIONA
 * OPERACIONES SEGURAS Y VALIDADAS PARA EL REGISTRO, ACTUALIZACION, BUSQUEDA
 * Y ADMINISTRACION DEL INVENTARIO DE LIBROS.
 *
 * EL SERVICIO MANEJA TODAS LAS REGLAS DE NEGOCIO PARA CONTROL DE DISPONIBILIDAD,
 * VALIDACIONES DE DATOS BIBLIOGRAFICOS, GESTION DE COPIAS Y ESTADISTICAS
 * DEL CATALOGO. IMPLEMENTA TRANSACCIONES PARA GARANTIZAR LA CONSISTENCIA.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Book
 * @see BookCategory
 * @see BookRepository
 * @see org.springframework.stereotype.Service
 */
@Service
@Transactional
public class BookService {

    /**
     * REPOSITORIO PARA ACCESO A DATOS DE LIBROS
     *
     * INYECTO EL REPOSITORIO QUE PROPORCIONA TODAS LAS OPERACIONES
     * DE ACCESO A DATOS PARA LA ENTIDAD BOOK. UTILIZO SPRING DEPENDENCY
     * INJECTION PARA GESTIONAR LA DEPENDENCIA AUTOMATICAMENTE.
     */
    @Autowired
    private BookRepository bookRepository;

    /**
     * REPOSITORIO PARA VALIDACIONES DE USUARIOS
     *
     * INYECTO EL REPOSITORIO DE USUARIOS PARA VALIDAR PERMISOS EN OPERACIONES
     * QUE REQUIEREN ROLES ADMINISTRATIVOS COMO CREAR O MODIFICAR LIBROS.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * REGISTRA UN NUEVO LIBRO EN EL CATALOGO
     *
     * PROCESO COMPLETO DE REGISTRO QUE INCLUYE VALIDACION DE DATOS BIBLIOGRAFICOS,
     * VERIFICACION DE ISBN UNICO, CONFIGURACION DE DISPONIBILIDAD INICIAL
     * Y APLICACION DE TODAS LAS REGLAS DE NEGOCIO DEL CATALOGO.
     *
     * @param isbn CODIGO ISBN UNICO DEL LIBRO
     * @param title TITULO DE LA OBRA
     * @param author AUTOR PRINCIPAL
     * @param description DESCRIPCION O SINOPSIS
     * @param category CATEGORIA DEL LIBRO
     * @param publisher EDITORIAL
     * @param publicationDate FECHA DE PUBLICACION
     * @param pages NUMERO DE PAGINAS
     * @param language IDIOMA DEL LIBRO
     * @param price PRECIO DE REFERENCIA
     * @param totalCopies NUMERO TOTAL DE COPIAS DISPONIBLES
     * @param adminUserId ID DEL ADMINISTRADOR QUE REGISTRA EL LIBRO
     * @return LIBRO REGISTRADO CON TODOS LOS CAMPOS CONFIGURADOS
     * @throws IllegalArgumentException SI LOS DATOS NO SON VALIDOS
     * @throws RuntimeException SI EL ISBN YA EXISTE O NO TIENE PERMISOS
     */
    public Book registerBook(String isbn, String title, String author, String description,
                             BookCategory category, String publisher, LocalDate publicationDate,
                             Integer pages, String language, BigDecimal price, Integer totalCopies,
                             Long adminUserId) {

        // VERIFICO PERMISOS DEL USUARIO ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // VALIDO TODOS LOS CAMPOS REQUERIDOS
        validateBookFields(isbn, title, author, category, totalCopies);

        // VERIFICO QUE EL ISBN NO ESTE EN USO
        if (bookRepository.existsByIsbn(isbn)) {
            throw new RuntimeException("YA EXISTE UN LIBRO CON EL ISBN: " + isbn);
        }

        // CREO EL NUEVO LIBRO CON CONFIGURACION INICIAL
        Book newBook = new Book();
        newBook.setIsbn(isbn);
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setDescription(description);
        newBook.setCategory(category);
        newBook.setPublisher(publisher);
        newBook.setPublicationDate(publicationDate);
        newBook.setPages(pages);
        newBook.setLanguage(language != null ? language : "ES");
        newBook.setPrice(price);
        newBook.setTotalCopies(totalCopies);
        newBook.setAvailableCopies(totalCopies); // TODAS LAS COPIAS DISPONIBLES INICIALMENTE
        newBook.setActive(true); // LIBRO ACTIVO DESDE EL REGISTRO
        newBook.setFeatured(false); // NO DESTACADO POR DEFECTO
        newBook.setLoanCount(0); // SIN PRESTAMOS INICIALMENTE

        // GUARDO EL LIBRO EN LA BASE DE DATOS
        Book savedBook = bookRepository.save(newBook);

        return savedBook;
    }

    /**
     * BUSCA UN LIBRO POR SU ID
     *
     * LOCALIZO UN LIBRO ESPECIFICO MEDIANTE SU IDENTIFICADOR UNICO.
     * METODO FUNDAMENTAL PARA OPERACIONES QUE REQUIEREN ACCESO
     * A DATOS COMPLETOS DE UN LIBRO.
     *
     * @param id IDENTIFICADOR UNICO DEL LIBRO
     * @return OPTIONAL CON EL LIBRO ENCONTRADO O VACIO SI NO EXISTE
     */
    @Transactional(readOnly = true)
    public Optional<Book> findBookById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("EL ID DEL LIBRO NO PUEDE SER NULO");
        }

        return bookRepository.findById(id);
    }

    /**
     * BUSCA UN LIBRO POR SU ISBN
     *
     * LOCALIZO UN LIBRO MEDIANTE SU CODIGO ISBN UNICO.
     * FUNDAMENTAL PARA VERIFICACIONES DE DUPLICADOS Y
     * BUSQUEDAS PRECISAS DE OBRAS ESPECIFICAS.
     *
     * @param isbn CODIGO ISBN DEL LIBRO
     * @return OPTIONAL CON EL LIBRO ENCONTRADO O VACIO SI NO EXISTE
     */
    @Transactional(readOnly = true)
    public Optional<Book> findBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("EL ISBN NO PUEDE SER NULO O VACIO");
        }

        return bookRepository.findByIsbn(isbn);
    }

    /**
     * BUSCA LIBROS POR TITULO CON COINCIDENCIA PARCIAL
     *
     * IMPLEMENTO BUSQUEDA FLEXIBLE QUE PERMITE ENCONTRAR LIBROS
     * MEDIANTE COINCIDENCIAS PARCIALES EN EL TITULO. IGNORA
     * MAYUSCULAS Y MINUSCULAS PARA MAYOR USABILIDAD.
     *
     * @param title TEXTO A BUSCAR EN EL TITULO
     * @return LISTA DE LIBROS CON COINCIDENCIAS EN EL TITULO
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("EL TITULO DE BUSQUEDA ES REQUERIDO");
        }

        return bookRepository.findByTitleContainingIgnoreCase(title.trim());
    }

    /**
     * BUSCA LIBROS POR AUTOR CON COINCIDENCIA PARCIAL
     *
     * LOCALIZO LIBROS DE UN AUTOR ESPECIFICO PERMITIENDO COINCIDENCIAS
     * PARCIALES EN EL NOMBRE. FACILITA LA BUSQUEDA CUANDO NO SE
     * CONOCE EL NOMBRE COMPLETO DEL AUTOR.
     *
     * @param author TEXTO A BUSCAR EN EL NOMBRE DEL AUTOR
     * @return LISTA DE LIBROS DEL AUTOR ESPECIFICADO
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("EL AUTOR DE BUSQUEDA ES REQUERIDO");
        }

        return bookRepository.findByAuthorContainingIgnoreCase(author.trim());
    }

    /**
     * BUSCA LIBROS POR CATEGORIA ESPECIFICA
     *
     * OBTENGO TODOS LOS LIBROS ACTIVOS QUE PERTENECEN A UNA CATEGORIA
     * DETERMINADA. FUNDAMENTAL PARA LA NAVEGACION POR GENEROS Y
     * LA ORGANIZACION TEMATICA DEL CATALOGO.
     *
     * @param category CATEGORIA DE LIBROS A FILTRAR
     * @return LISTA DE LIBROS DE LA CATEGORIA ESPECIFICADA
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByCategory(BookCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("LA CATEGORIA NO PUEDE SER NULA");
        }

        return bookRepository.findAvailableBooksByCategory(category);
    }

    /**
     * OBTIENE TODOS LOS LIBROS DISPONIBLES PARA PRESTAMO
     *
     * RECUPERO LA LISTA DE LIBROS QUE ESTAN ACTIVOS Y TIENEN
     * AL MENOS UNA COPIA DISPONIBLE. ESENCIAL PARA MOSTRAR
     * SOLO OPCIONES VIABLES A LOS USUARIOS.
     *
     * @return LISTA DE LIBROS DISPONIBLES PARA PRESTAMO
     */
    @Transactional(readOnly = true)
    public List<Book> findAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    /**
     * OBTIENE LIBROS POPULARES CON PAGINACION
     *
     * RECUPERO LOS LIBROS MAS SOLICITADOS DEL CATALOGO BASANDOME
     * EN EL NUMERO DE PRESTAMOS. PERMITE IDENTIFICAR TENDENCIAS
     * DE LECTURA CON SOPORTE PARA PAGINACION.
     *
     * @param pageable CONFIGURACION DE PAGINACION Y ORDENAMIENTO
     * @return PAGINA CON LOS LIBROS MAS POPULARES
     */
    @Transactional(readOnly = true)
    public Page<Book> findPopularBooks(Pageable pageable) {
        return bookRepository.findByActiveTrueOrderByLoanCountDesc(pageable);
    }

    /**
     * OBTIENE LIBROS RECIENTES CON PAGINACION
     *
     * RECUPERO LOS LIBROS AÑADIDOS RECIENTEMENTE AL CATALOGO
     * ORDENADOS POR FECHA DE CREACION. UTIL PARA MOSTRAR
     * NOVEDADES DEL SISTEMA.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON LOS LIBROS MAS RECIENTES
     */
    @Transactional(readOnly = true)
    public Page<Book> findRecentBooks(Pageable pageable) {
        return bookRepository.findByActiveTrueOrderByCreatedAtDesc(pageable);
    }

    /**
     * OBTIENE LIBROS DESTACADOS DEL CATALOGO
     *
     * RECUPERO LIBROS MARCADOS COMO DESTACADOS PARA MOSTRAR
     * EN SECCIONES ESPECIALES, RECOMENDACIONES O PROMOCIONES
     * EN LA INTERFAZ PRINCIPAL.
     *
     * @return LISTA DE LIBROS DESTACADOS
     */
    @Transactional(readOnly = true)
    public List<Book> findFeaturedBooks() {
        return bookRepository.findByFeatured(true);
    }

    /**
     * REALIZA BUSQUEDA AVANZADA CON MULTIPLES CRITERIOS
     *
     * IMPLEMENTO BUSQUEDA FLEXIBLE QUE PERMITE COMBINAR DIFERENTES
     * FILTROS. ACEPTA VALORES NULOS PARA IGNORAR CRITERIOS
     * NO APLICABLES EN LA CONSULTA.
     *
     * @param title FILTRO POR TITULO (OPCIONAL)
     * @param author FILTRO POR AUTOR (OPCIONAL)
     * @param category FILTRO POR CATEGORIA (OPCIONAL)
     * @param minPrice PRECIO MINIMO (OPCIONAL)
     * @param maxPrice PRECIO MAXIMO (OPCIONAL)
     * @param language FILTRO POR IDIOMA (OPCIONAL)
     * @return LISTA DE LIBROS QUE CUMPLEN LOS CRITERIOS
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByAdvancedSearch(String title, String author, BookCategory category,
                                                BigDecimal minPrice, BigDecimal maxPrice, String language) {

        return bookRepository.findByAdvancedSearch(title, author, category, minPrice, maxPrice, language);
    }

    /**
     * REALIZA BUSQUEDA GLOBAL EN EL CATALOGO
     *
     * BUSQUEDA INTEGRAL QUE EXAMINA COINCIDENCIAS EN TITULO, AUTOR,
     * DESCRIPCION Y EDITORIAL SIMULTANEAMENTE. PROPORCIONA
     * FUNCIONALIDAD DE BUSQUEDA GENERAL EN EL CATALOGO.
     *
     * @param searchTerm TERMINO DE BUSQUEDA A APLICAR
     * @return LISTA DE LIBROS CON COINCIDENCIAS
     */
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("EL TERMINO DE BUSQUEDA ES REQUERIDO");
        }

        return bookRepository.findByGlobalSearch(searchTerm.trim());
    }

    /**
     * ACTUALIZA LA INFORMACION DE UN LIBRO
     *
     * PROCESO DE ACTUALIZACION QUE PERMITE MODIFICAR DATOS DE UN LIBRO
     * MANTENIENDO LA INTEGRIDAD DE LAS VALIDACIONES. SOLO USUARIOS
     * CON PERMISOS ADMINISTRATIVOS PUEDEN REALIZAR MODIFICACIONES.
     *
     * @param bookId ID DEL LIBRO A ACTUALIZAR
     * @param title NUEVO TITULO
     * @param author NUEVO AUTOR
     * @param description NUEVA DESCRIPCION
     * @param category NUEVA CATEGORIA
     * @param publisher NUEVA EDITORIAL
     * @param publicationDate NUEVA FECHA DE PUBLICACION
     * @param pages NUEVO NUMERO DE PAGINAS
     * @param language NUEVO IDIOMA
     * @param price NUEVO PRECIO
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA ACTUALIZACION
     * @return LIBRO ACTUALIZADO
     * @throws RuntimeException SI EL LIBRO NO EXISTE O NO TIENE PERMISOS
     */
    public Book updateBook(Long bookId, String title, String author, String description,
                           BookCategory category, String publisher, LocalDate publicationDate,
                           Integer pages, String language, BigDecimal price, Long adminUserId) {

        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // BUSCO EL LIBRO EXISTENTE
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // ACTUALIZO LOS CAMPOS PROPORCIONADOS
        if (title != null && !title.trim().isEmpty()) {
            validateTitle(title);
            book.setTitle(title);
        }

        if (author != null && !author.trim().isEmpty()) {
            validateAuthor(author);
            book.setAuthor(author);
        }

        if (description != null) {
            book.setDescription(description.trim().isEmpty() ? null : description);
        }

        if (category != null) {
            book.setCategory(category);
        }

        if (publisher != null) {
            book.setPublisher(publisher.trim().isEmpty() ? null : publisher);
        }

        if (publicationDate != null) {
            book.setPublicationDate(publicationDate);
        }

        if (pages != null) {
            validatePages(pages);
            book.setPages(pages);
        }

        if (language != null && !language.trim().isEmpty()) {
            book.setLanguage(language);
        }

        if (price != null) {
            validatePrice(price);
            book.setPrice(price);
        }

        // GUARDO LOS CAMBIOS
        return bookRepository.save(book);
    }

    /**
     * ACTUALIZA EL INVENTARIO DE COPIAS DE UN LIBRO
     *
     * OPERACION QUE PERMITE MODIFICAR EL NUMERO TOTAL DE COPIAS
     * Y AJUSTAR AUTOMATICAMENTE LAS COPIAS DISPONIBLES. MANTIENE
     * LA CONSISTENCIA DEL INVENTARIO.
     *
     * @param bookId ID DEL LIBRO A ACTUALIZAR
     * @param newTotalCopies NUEVO NUMERO TOTAL DE COPIAS
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return LIBRO CON EL INVENTARIO ACTUALIZADO
     */
    public Book updateBookInventory(Long bookId, Integer newTotalCopies, Long adminUserId) {
        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // VALIDO EL NUMERO DE COPIAS
        if (newTotalCopies == null || newTotalCopies < 0) {
            throw new IllegalArgumentException("EL NUMERO DE COPIAS DEBE SER MAYOR O IGUAL A 0");
        }

        // BUSCO EL LIBRO
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // CALCULO LAS COPIAS ACTUALMENTE PRESTADAS
        int currentlyLoaned = book.getTotalCopies() - book.getAvailableCopies();

        // VERIFICO QUE EL NUEVO TOTAL SEA SUFICIENTE
        if (newTotalCopies < currentlyLoaned) {
            throw new RuntimeException("NO SE PUEDE REDUCIR A " + newTotalCopies +
                    " COPIAS. HAY " + currentlyLoaned + " COPIAS PRESTADAS");
        }

        // ACTUALIZO EL INVENTARIO
        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(newTotalCopies - currentlyLoaned);

        return bookRepository.save(book);
    }

    /**
     * MARCA UN LIBRO COMO DESTACADO O NO DESTACADO
     *
     * OPERACION ADMINISTRATIVA QUE PERMITE GESTIONAR LA VISIBILIDAD
     * ESPECIAL DE LIBROS EN SECCIONES PROMOCIONALES DEL SISTEMA.
     *
     * @param bookId ID DEL LIBRO A MODIFICAR
     * @param featured NUEVO ESTADO DESTACADO
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return LIBRO CON EL ESTADO ACTUALIZADO
     */
    public Book toggleBookFeaturedStatus(Long bookId, Boolean featured, Long adminUserId) {
        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // VALIDO PARAMETROS
        if (bookId == null || featured == null) {
            throw new IllegalArgumentException("LOS PARAMETROS SON REQUERIDOS");
        }

        // BUSCO EL LIBRO
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // ACTUALIZO EL ESTADO DESTACADO
        book.setFeatured(featured);

        return bookRepository.save(book);
    }

    /**
     * ACTIVA O DESACTIVA UN LIBRO DEL CATALOGO
     *
     * OPERACION ADMINISTRATIVA QUE PERMITE HABILITAR O DESHABILITAR
     * UN LIBRO EN EL CATALOGO. LIBROS DESACTIVADOS NO APARECEN
     * EN BUSQUEDAS NI PUEDEN SER PRESTADOS.
     *
     * @param bookId ID DEL LIBRO A MODIFICAR
     * @param active NUEVO ESTADO DE ACTIVACION
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return LIBRO CON EL ESTADO ACTUALIZADO
     */
    public Book toggleBookActiveStatus(Long bookId, Boolean active, Long adminUserId) {
        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // VALIDO PARAMETROS
        if (bookId == null || active == null) {
            throw new IllegalArgumentException("LOS PARAMETROS SON REQUERIDOS");
        }

        // BUSCO EL LIBRO
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // VERIFICO SI HAY PRESTAMOS ACTIVOS ANTES DE DESACTIVAR
        if (!active && book.getLoanedCopies() > 0) {
            throw new RuntimeException("NO SE PUEDE DESACTIVAR EL LIBRO. HAY " +
                    book.getLoanedCopies() + " COPIAS PRESTADAS");
        }

        // ACTUALIZO EL ESTADO
        book.setActive(active);

        return bookRepository.save(book);
    }

    /**
     * RESERVA UNA COPIA DE UN LIBRO PARA PRESTAMO
     *
     * OPERACION ATOMICA QUE REDUCE EL NUMERO DE COPIAS DISPONIBLES
     * CUANDO SE REALIZA UN PRESTAMO. VERIFICA LA DISPONIBILIDAD
     * ANTES DE REALIZAR LA RESERVA.
     *
     * @param bookId ID DEL LIBRO A RESERVAR
     * @return TRUE SI SE PUDO RESERVAR, FALSE EN CASO CONTRARIO
     */
    public boolean reserveBookCopy(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("EL ID DEL LIBRO ES REQUERIDO");
        }

        // BUSCO EL LIBRO
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // INTENTO RESERVAR UNA COPIA
        if (book.reserveCopy()) {
            bookRepository.save(book);
            return true;
        }

        return false;
    }

    /**
     * LIBERA UNA COPIA DE UN LIBRO AL DEVOLVER EL PRESTAMO
     *
     * OPERACION ATOMICA QUE INCREMENTA EL NUMERO DE COPIAS DISPONIBLES
     * CUANDO SE DEVUELVE UN LIBRO. ACTUALIZA TAMBIEN EL CONTADOR
     * DE PRESTAMOS REALIZADOS.
     *
     * @param bookId ID DEL LIBRO A LIBERAR
     * @return TRUE SI SE PUDO LIBERAR, FALSE EN CASO CONTRARIO
     */
    public boolean releaseBookCopy(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("EL ID DEL LIBRO ES REQUERIDO");
        }

        // BUSCO EL LIBRO
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // INTENTO LIBERAR UNA COPIA
        if (book.releaseCopy()) {
            bookRepository.save(book);
            return true;
        }

        return false;
    }

    /**
     * OBTIENE LIBROS CON BAJA DISPONIBILIDAD
     *
     * IDENTIFICO LIBROS QUE TIENEN POCAS COPIAS DISPONIBLES
     * PARA ALERTAS DE INVENTARIO Y PLANIFICACION DE ADQUISICIONES.
     *
     * @param threshold UMBRAL MINIMO DE COPIAS DISPONIBLES
     * @return LISTA DE LIBROS CON BAJA DISPONIBILIDAD
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksWithLowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("EL UMBRAL DEBE SER MAYOR O IGUAL A 0");
        }

        return bookRepository.findBooksWithLowStock(threshold);
    }

    /**
     * OBTIENE LIBROS AGOTADOS (SIN DISPONIBILIDAD)
     *
     * IDENTIFICO LIBROS QUE NO TIENEN COPIAS DISPONIBLES PARA PRESTAMO.
     * UTIL PARA GESTION DE INVENTARIO Y PLANIFICACION DE REPOSICION.
     *
     * @return LISTA DE LIBROS AGOTADOS
     */
    @Transactional(readOnly = true)
    public List<Book> findOutOfStockBooks() {
        return bookRepository.findOutOfStockBooks();
    }

    /**
     * OBTIENE ESTADISTICAS DE LIBROS POR CATEGORIA
     *
     * GENERO ESTADISTICAS AGRUPADAS POR CATEGORIA PARA REPORTES
     * Y ANALISIS DE DISTRIBUCION DEL CATALOGO.
     *
     * @return LISTA CON ESTADISTICAS [CATEGORIA, CANTIDAD]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBookStatisticsByCategory() {
        return bookRepository.getBookStatsByCategory();
    }

    /**
     * CALCULA EL VALOR TOTAL DEL CATALOGO
     *
     * SUMO EL VALOR MONETARIO DE TODOS LOS LIBROS ACTIVOS
     * MULTIPLICANDO PRECIO POR NUMERO DE COPIAS. METRICA
     * IMPORTANTE PARA INVENTARIOS Y SEGUROS.
     *
     * @return VALOR TOTAL DEL CATALOGO
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCatalogValue() {
        BigDecimal totalValue = bookRepository.calculateTotalCatalogValue();
        return totalValue != null ? totalValue : BigDecimal.ZERO;
    }

    /**
     * CUENTA LIBROS ACTIVOS EN EL CATALOGO
     *
     * CALCULO EL NUMERO TOTAL DE LIBROS HABILITADOS Y VISIBLES.
     * METRICA FUNDAMENTAL PARA REPORTES DE INVENTARIO.
     *
     * @return NUMERO TOTAL DE LIBROS ACTIVOS
     */
    @Transactional(readOnly = true)
    public long countActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    /**
     * CUENTA LIBROS DISPONIBLES PARA PRESTAMO
     *
     * CALCULO LA CANTIDAD DE LIBROS QUE TIENEN AL MENOS UNA COPIA
     * DISPONIBLE. INDICADOR CLAVE DE DISPONIBILIDAD DEL CATALOGO.
     *
     * @return NUMERO DE LIBROS CON DISPONIBILIDAD
     */
    @Transactional(readOnly = true)
    public long countAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }

    /**
     * VERIFICA SI UN LIBRO EXISTE POR SU ID
     *
     * COMPRUEBO LA EXISTENCIA DE UN LIBRO SIN CARGAR LA ENTIDAD COMPLETA.
     * OPTIMIZA VALIDACIONES RAPIDAS EN OPERACIONES DE PRESTAMO.
     *
     * @param bookId ID DEL LIBRO A VERIFICAR
     * @return TRUE SI EXISTE, FALSE EN CASO CONTRARIO
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long bookId) {
        if (bookId == null) {
            return false;
        }

        return bookRepository.existsById(bookId);
    }

    /**
     * VERIFICA DISPONIBILIDAD DE UN ISBN
     *
     * COMPRUEBO SI UN CODIGO ISBN ESTA DISPONIBLE PARA REGISTRO.
     * UTIL PARA VALIDACIONES EN TIEMPO REAL AL AGREGAR LIBROS.
     *
     * @param isbn CODIGO ISBN A VERIFICAR
     * @return TRUE SI ESTA DISPONIBLE, FALSE SI YA EXISTE
     */
    @Transactional(readOnly = true)
    public boolean isIsbnAvailable(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        return !bookRepository.existsByIsbn(isbn);
    }

    /**
     * VALIDA PERMISOS ADMINISTRATIVOS DEL USUARIO
     *
     * VERIFICO QUE EL USUARIO TENGA PERMISOS PARA GESTIONAR EL CATALOGO
     * DE LIBROS. SOLO ADMINISTRADORES Y BIBLIOTECARIOS PUEDEN
     * REALIZAR OPERACIONES DE MODIFICACION DEL CATALOGO.
     *
     * @param adminUserId ID DEL USUARIO A VALIDAR
     * @throws RuntimeException SI NO TIENE PERMISOS O NO EXISTE
     */
    private void validateAdminPermissions(Long adminUserId) {
        if (adminUserId == null) {
            throw new IllegalArgumentException("EL ID DEL ADMINISTRADOR ES REQUERIDO");
        }

        Optional<User> adminOptional = userRepository.findById(adminUserId);
        if (!adminOptional.isPresent()) {
            throw new RuntimeException("ADMINISTRADOR NO ENCONTRADO");
        }

        User admin = adminOptional.get();
        if (!admin.getRole().canManageBooks()) {
            throw new RuntimeException("NO TIENE PERMISOS PARA GESTIONAR EL CATALOGO");
        }

        if (!admin.getActive()) {
            throw new RuntimeException("LA CUENTA DEL ADMINISTRADOR ESTA DESACTIVADA");
        }
    }

    /**
     * VALIDA LOS CAMPOS REQUERIDOS PARA REGISTRO DE LIBRO
     *
     * VERIFICO QUE TODOS LOS CAMPOS OBLIGATORIOS ESTEN PRESENTES
     * Y TENGAN VALORES VALIDOS ANTES DE PROCESAR EL REGISTRO.
     *
     * @param isbn CODIGO ISBN DEL LIBRO
     * @param title TITULO DEL LIBRO
     * @param author AUTOR DEL LIBRO
     * @param category CATEGORIA DEL LIBRO
     * @param totalCopies NUMERO TOTAL DE COPIAS
     * @throws IllegalArgumentException SI ALGUN CAMPO NO ES VALIDO
     */
    private void validateBookFields(String isbn, String title, String author,
                                    BookCategory category, Integer totalCopies) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("EL ISBN ES REQUERIDO");
        }

        validateTitle(title);
        validateAuthor(author);

        if (category == null) {
            throw new IllegalArgumentException("LA CATEGORIA ES REQUERIDA");
        }

        if (totalCopies == null || totalCopies < 1) {
            throw new IllegalArgumentException("EL NUMERO DE COPIAS DEBE SER MAYOR A 0");
        }

        if (totalCopies > 1000) {
            throw new IllegalArgumentException("EL NUMERO DE COPIAS NO PUEDE EXCEDER 1000");
        }
    }

    /**
     * VALIDA EL TITULO DE UN LIBRO
     *
     * VERIFICO QUE EL TITULO CUMPLA CON LOS REQUISITOS
     * DE LONGITUD Y CONTENIDO ESTABLECIDOS.
     *
     * @param title TITULO A VALIDAR
     * @throws IllegalArgumentException SI EL TITULO NO ES VALIDO
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("EL TITULO ES REQUERIDO");
        }

        if (title.length() < 1 || title.length() > 200) {
            throw new IllegalArgumentException("EL TITULO DEBE TENER ENTRE 1 Y 200 CARACTERES");
        }
    }

    /**
     * VALIDA EL AUTOR DE UN LIBRO
     *
     * VERIFICO QUE EL NOMBRE DEL AUTOR CUMPLA CON LOS REQUISITOS
     * DE LONGITUD Y FORMATO ESTABLECIDOS.
     *
     * @param author AUTOR A VALIDAR
     * @throws IllegalArgumentException SI EL AUTOR NO ES VALIDO
     */
    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("EL AUTOR ES REQUERIDO");
        }

        if (author.length() < 2 || author.length() > 100) {
            throw new IllegalArgumentException("EL AUTOR DEBE TENER ENTRE 2 Y 100 CARACTERES");
        }
    }

    /**
     * VALIDA EL NUMERO DE PAGINAS DE UN LIBRO
     *
     * VERIFICO QUE EL NUMERO DE PAGINAS SEA UN VALOR RAZONABLE
     * DENTRO DE LOS RANGOS ESTABLECIDOS.
     *
     * @param pages NUMERO DE PAGINAS A VALIDAR
     * @throws IllegalArgumentException SI EL NUMERO NO ES VALIDO
     */
    private void validatePages(Integer pages) {
        if (pages != null) {
            if (pages < 1) {
                throw new IllegalArgumentException("EL NUMERO DE PAGINAS DEBE SER MAYOR A 0");
            }

            if (pages > 10000) {
                throw new IllegalArgumentException("EL NUMERO DE PAGINAS NO PUEDE EXCEDER 10000");
            }
        }
    }

    /**
     * VALIDA EL PRECIO DE UN LIBRO
     *
     * VERIFICO QUE EL PRECIO SEA UN VALOR POSITIVO DENTRO
     * DE LOS RANGOS ESTABLECIDOS PARA EL SISTEMA.
     *
     * @param price PRECIO A VALIDAR
     * @throws IllegalArgumentException SI EL PRECIO NO ES VALIDO
     */
    private void validatePrice(BigDecimal price) {
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("EL PRECIO DEBE SER MAYOR A 0");
            }

            if (price.compareTo(new BigDecimal("9999.99")) > 0) {
                throw new IllegalArgumentException("EL PRECIO NO PUEDE EXCEDER 9999.99");
            }
        }
    }

    /**
     * OBTIENE AUTORES MAS POPULARES DEL CATALOGO
     *
     * CONSULTA QUE IDENTIFICA LOS AUTORES CON MAS LIBROS
     * EN EL CATALOGO, ORDENADOS POR CANTIDAD DE OBRAS.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON AUTORES MAS REPRESENTADOS
     */
    @Transactional(readOnly = true)
    public Page<Object[]> findMostPopularAuthors(Pageable pageable) {
        return bookRepository.findMostPopularAuthors(pageable);
    }

    /**
     * BUSCA LIBROS NUNCA PRESTADOS
     *
     * IDENTIFICO LIBROS QUE NUNCA HAN SIDO SOLICITADOS EN PRESTAMO.
     * IMPORTANTE PARA ANALISIS DE POPULARIDAD Y REVISION
     * DE LA RELEVANCIA DEL CATALOGO.
     *
     * @return LISTA DE LIBROS SIN HISTORIAL DE PRESTAMOS
     */
    @Transactional(readOnly = true)
    public List<Book> findNeverBorrowedBooks() {
        return bookRepository.findNeverBorrowedBooks();
    }

    /**
     * BUSCA LIBROS CON PRESTAMOS ACTIVOS
     *
     * IDENTIFICO LIBROS QUE ACTUALMENTE TIENEN COPIAS EN PRESTAMO.
     * IMPORTANTE PARA GESTION DE INVENTARIO Y SEGUIMIENTO
     * DE LIBROS EN CIRCULACION.
     *
     * @return LISTA DE LIBROS CON PRESTAMOS ACTIVOS
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksWithActiveLoans() {
        return bookRepository.findBooksWithActiveLoans();
    }

    /**
     * BUSCA LIBROS AGREGADOS RECIENTEMENTE
     *
     * LOCALIZO LIBROS AÑADIDOS AL CATALOGO DESPUES DE UNA FECHA
     * ESPECIFICA. UTIL PARA MOSTRAR NOVEDADES DEL INVENTARIO.
     *
     * @param date FECHA DE CORTE PARA LA BUSQUEDA
     * @return LISTA DE LIBROS AGREGADOS DESPUES DE LA FECHA
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksAddedAfter(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("LA FECHA ES REQUERIDA");
        }

        return bookRepository.findByCreatedAtAfter(date);
    }

    /**
     * BUSCA LIBROS POR RANGO DE PRECIOS
     *
     * FILTRO LIBROS CUYO PRECIO DE REFERENCIA SE ENCUENTRA
     * ENTRE DOS VALORES ESPECIFICADOS. UTIL PARA BUSQUEDAS
     * POR CRITERIOS ECONOMICOS.
     *
     * @param minPrice PRECIO MINIMO DEL RANGO
     * @param maxPrice PRECIO MAXIMO DEL RANGO
     * @return LISTA DE LIBROS EN EL RANGO DE PRECIOS
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            throw new IllegalArgumentException("AL MENOS UN LIMITE DE PRECIO ES REQUERIDO");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("EL PRECIO MINIMO NO PUEDE SER MAYOR AL MAXIMO");
        }

        return bookRepository.findByPriceBetween(
                minPrice != null ? minPrice : BigDecimal.ZERO,
                maxPrice != null ? maxPrice : new BigDecimal("9999.99")
        );
    }

    /**
     * BUSCA LIBROS POR IDIOMA ESPECIFICO
     *
     * LOCALIZO LIBROS ESCRITOS EN UN IDIOMA DETERMINADO.
     * PERMITE FILTRAR EL CATALOGO SEGUN PREFERENCIAS LINGUISTICAS.
     *
     * @param language CODIGO DE IDIOMA A FILTRAR
     * @return LISTA DE LIBROS EN EL IDIOMA ESPECIFICADO
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("EL IDIOMA ES REQUERIDO");
        }

        return bookRepository.findByLanguage(language.trim());
    }

    /**
     * BUSCA LIBROS CON DESCRIPCION DISPONIBLE
     *
     * LOCALIZO LIBROS QUE TIENEN DESCRIPCION O SINOPSIS CONFIGURADA.
     * UTIL PARA MOSTRAR LIBROS CON INFORMACION COMPLETA.
     *
     * @return LISTA DE LIBROS CON DESCRIPCION
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksWithDescription() {
        return bookRepository.findBooksWithDescription();
    }

    /**
     * INCREMENTA EL CONTADOR DE PRESTAMOS DE UN LIBRO
     *
     * OPERACION ATOMICA QUE SE EJECUTA CADA VEZ QUE SE REALIZA
     * UN NUEVO PRESTAMO. MANTIENE ESTADISTICAS DE POPULARIDAD.
     *
     * @param bookId ID DEL LIBRO A ACTUALIZAR
     */
    public void incrementLoanCount(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("EL ID DEL LIBRO ES REQUERIDO");
        }

        bookRepository.incrementLoanCount(bookId);
    }
}