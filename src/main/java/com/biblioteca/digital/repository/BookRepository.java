package com.biblioteca.digital.repository;

import com.biblioteca.digital.model.Book;
import com.biblioteca.digital.model.BookCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO JPA PARA LA GESTION DE ENTIDADES BOOK
 *
 * ESTA INTERFAZ PROPORCIONA OPERACIONES CRUD COMPLETAS Y CONSULTAS ESPECIALIZADAS
 * PARA LA GESTION DEL CATALOGO DE LIBROS EN EL SISTEMA DE BIBLIOTECA DIGITAL.
 * INCLUYE METODOS OPTIMIZADOS PARA BUSQUEDAS POR DIFERENTES CRITERIOS,
 * GESTION DE DISPONIBILIDAD Y ESTADISTICAS DEL CATALOGO.
 *
 * SPRING DATA JPA GENERA AUTOMATICAMENTE LA IMPLEMENTACION DE ESTOS METODOS
 * BASANDOSE EN LAS CONVENCIONES DE NOMENCLATURA Y ANOTACIONES PERSONALIZADAS.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Book
 * @see BookCategory
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * BUSCA UN LIBRO POR SU CODIGO ISBN
     *
     * LOCALIZO UN LIBRO ESPECIFICO MEDIANTE SU CODIGO ISBN UNICO.
     * ESTE METODO ES FUNDAMENTAL PARA EVITAR DUPLICADOS EN EL CATALOGO
     * Y PARA BUSQUEDAS PRECISAS DE OBRAS ESPECIFICAS.
     *
     * @param isbn CODIGO ISBN DEL LIBRO A BUSCAR
     * @return OPTIONAL CON EL LIBRO ENCONTRADO O VACIO SI NO EXISTE
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * VERIFICA SI EXISTE UN LIBRO CON EL ISBN ESPECIFICADO
     *
     * COMPRUEBO LA EXISTENCIA DE UN LIBRO ANTES DE PERMITIR SU REGISTRO.
     * EVITA CONFLICTOS CON LA RESTRICCION DE UNICIDAD DEL ISBN
     * EN LA BASE DE DATOS.
     *
     * @param isbn CODIGO ISBN A VERIFICAR
     * @return TRUE SI EXISTE, FALSE EN CASO CONTRARIO
     */
    boolean existsByIsbn(String isbn);

    /**
     * BUSCA LIBROS POR TITULO CON COINCIDENCIA PARCIAL
     *
     * IMPLEMENTO BUSQUEDA FLEXIBLE QUE PERMITE ENCONTRAR LIBROS
     * MEDIANTE COINCIDENCIAS PARCIALES EN EL TITULO. LA BUSQUEDA
     * IGNORA MAYUSCULAS Y MINUSCULAS PARA MAYOR USABILIDAD.
     *
     * @param title TEXTO A BUSCAR EN EL TITULO
     * @return LISTA DE LIBROS CON COINCIDENCIAS EN EL TITULO
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * BUSCA LIBROS POR AUTOR CON COINCIDENCIA PARCIAL
     *
     * LOCALIZO LIBROS DE UN AUTOR ESPECIFICO PERMITIENDO COINCIDENCIAS
     * PARCIALES EN EL NOMBRE. FACILITA LA BUSQUEDA CUANDO LOS USUARIOS
     * NO RECUERDAN EL NOMBRE COMPLETO DEL AUTOR.
     *
     * @param author TEXTO A BUSCAR EN EL NOMBRE DEL AUTOR
     * @return LISTA DE LIBROS DEL AUTOR ESPECIFICADO
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * BUSCA LIBROS POR CATEGORIA ESPECIFICA
     *
     * OBTENGO TODOS LOS LIBROS QUE PERTENECEN A UNA CATEGORIA DETERMINADA.
     * FUNDAMENTAL PARA LA NAVEGACION POR GENEROS Y LA ORGANIZACION
     * TEMATICA DEL CATALOGO.
     *
     * @param category CATEGORIA DE LIBROS A FILTRAR
     * @return LISTA DE LIBROS DE LA CATEGORIA ESPECIFICADA
     */
    List<Book> findByCategory(BookCategory category);

    /**
     * BUSCA LIBROS ACTIVOS EN EL CATALOGO
     *
     * FILTRO LIBROS SEGUN SU ESTADO DE ACTIVACION. SOLO INCLUYO
     * LIBROS ACTIVOS QUE ESTAN DISPONIBLES PARA PRESTAMO Y
     * VISIBLES EN EL CATALOGO PUBLICO.
     *
     * @param active ESTADO DE ACTIVACION A FILTRAR
     * @return LISTA DE LIBROS CON EL ESTADO ESPECIFICADO
     */
    List<Book> findByActive(Boolean active);

    /**
     * BUSCA LIBROS DESTACADOS DEL CATALOGO
     *
     * OBTENGO LIBROS MARCADOS COMO DESTACADOS PARA MOSTRAR
     * EN SECCIONES ESPECIALES, RECOMENDACIONES O PROMOCIONES
     * EN LA INTERFAZ PRINCIPAL DEL SISTEMA.
     *
     * @param featured ESTADO DESTACADO A FILTRAR
     * @return LISTA DE LIBROS DESTACADOS
     */
    List<Book> findByFeatured(Boolean featured);

    /**
     * BUSCA LIBROS CON COPIAS DISPONIBLES PARA PRESTAMO
     *
     * LOCALIZO LIBROS QUE TIENEN AL MENOS UNA COPIA DISPONIBLE
     * PARA PRESTAMO. ESENCIAL PARA MOSTRAR SOLO LIBROS QUE
     * EFECTIVAMENTE PUEDEN SER SOLICITADOS POR LOS USUARIOS.
     *
     * @return LISTA DE LIBROS CON DISPONIBILIDAD
     */
    @Query("SELECT b FROM Book b WHERE b.active = true AND b.availableCopies > 0")
    List<Book> findAvailableBooks();

    /**
     * BUSCA LIBROS SIN DISPONIBILIDAD (AGOTADOS)
     *
     * IDENTIFICO LIBROS QUE NO TIENEN COPIAS DISPONIBLES PARA PRESTAMO.
     * UTIL PARA GESTION DE INVENTARIO Y PLANIFICACION DE ADQUISICIONES
     * DE NUEVOS EJEMPLARES.
     *
     * @return LISTA DE LIBROS AGOTADOS
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0")
    List<Book> findOutOfStockBooks();

    /**
     * BUSCA LIBROS POPULARES ORDENADOS POR NUMERO DE PRESTAMOS
     *
     * OBTENGO LOS LIBROS MAS SOLICITADOS DEL CATALOGO BASANDOME
     * EN EL CONTADOR DE PRESTAMOS. PERMITE IDENTIFICAR TENDENCIAS
     * DE LECTURA Y LIBROS DE MAYOR DEMANDA.
     *
     * @param pageable CONFIGURACION DE PAGINACION Y ORDENAMIENTO
     * @return PAGINA CON LOS LIBROS MAS POPULARES
     */
    Page<Book> findByActiveTrueOrderByLoanCountDesc(Pageable pageable);

    /**
     * BUSCA LIBROS RECIEN AGREGADOS AL CATALOGO
     *
     * LOCALIZO LIBROS AÑADIDOS RECIENTEMENTE AL SISTEMA ORDENADOS
     * POR FECHA DE CREACION. UTIL PARA MOSTRAR NOVEDADES Y
     * ACTUALIZACIONES DEL CATALOGO.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON LOS LIBROS MAS RECIENTES
     */
    Page<Book> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * BUSCA LIBROS POR EDITORIAL ESPECIFICA
     *
     * FILTRO LIBROS SEGUN LA EDITORIAL QUE LOS PUBLICO.
     * PERMITE BUSQUEDAS ESPECIALIZADAS Y ANALISIS DE CATALOGO
     * POR CASA EDITORIAL.
     *
     * @param publisher NOMBRE DE LA EDITORIAL A BUSCAR
     * @return LISTA DE LIBROS DE LA EDITORIAL ESPECIFICADA
     */
    List<Book> findByPublisherContainingIgnoreCase(String publisher);

    /**
     * BUSCA LIBROS PUBLICADOS EN UN AÑO ESPECIFICO
     *
     * LOCALIZO LIBROS CUYA FECHA DE PUBLICACION CORRESPONDE
     * A UN AÑO DETERMINADO. UTIL PARA BUSQUEDAS HISTORICAS
     * Y FILTROS TEMPORALES DEL CATALOGO.
     *
     * @param year AÑO DE PUBLICACION A FILTRAR
     * @return LISTA DE LIBROS PUBLICADOS EN EL AÑO ESPECIFICADO
     */
    @Query("SELECT b FROM Book b WHERE YEAR(b.publicationDate) = :year")
    List<Book> findByPublicationYear(@Param("year") int year);

    /**
     * BUSCA LIBROS EN UN RANGO DE PRECIOS
     *
     * FILTRO LIBROS CUYO PRECIO DE REFERENCIA SE ENCUENTRA
     * ENTRE DOS VALORES ESPECIFICADOS. UTIL PARA BUSQUEDAS
     * POR CRITERIOS ECONOMICOS Y GESTION DE PRESUPUESTOS.
     *
     * @param minPrice PRECIO MINIMO DEL RANGO
     * @param maxPrice PRECIO MAXIMO DEL RANGO
     * @return LISTA DE LIBROS EN EL RANGO DE PRECIOS
     */
    List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * BUSCA LIBROS POR IDIOMA ESPECIFICO
     *
     * LOCALIZO LIBROS ESCRITOS EN UN IDIOMA DETERMINADO.
     * PERMITE FILTRAR EL CATALOGO SEGUN PREFERENCIAS
     * LINGUISTICAS DE LOS USUARIOS.
     *
     * @param language CODIGO DE IDIOMA A FILTRAR
     * @return LISTA DE LIBROS EN EL IDIOMA ESPECIFICADO
     */
    List<Book> findByLanguage(String language);

    /**
     * BUSCA LIBROS CON NUMERO DE PAGINAS EN UN RANGO
     *
     * FILTRO LIBROS SEGUN SU EXTENSION (NUMERO DE PAGINAS).
     * UTIL PARA USUARIOS QUE BUSCAN LECTURAS DE DURACION
     * ESPECIFICA O PARA CLASIFICACIONES PEDAGOGICAS.
     *
     * @param minPages NUMERO MINIMO DE PAGINAS
     * @param maxPages NUMERO MAXIMO DE PAGINAS
     * @return LISTA DE LIBROS EN EL RANGO DE PAGINAS
     */
    List<Book> findByPagesBetween(Integer minPages, Integer maxPages);

    /**
     * BUSQUEDA AVANZADA CON MULTIPLES CRITERIOS
     *
     * CONSULTA FLEXIBLE QUE PERMITE COMBINAR DIFERENTES FILTROS
     * DE BUSQUEDA. ACEPTA VALORES NULOS PARA IGNORAR CRITERIOS
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
    @Query("SELECT b FROM Book b WHERE b.active = true AND " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:category IS NULL OR b.category = :category) AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "(:language IS NULL OR b.language = :language)")
    List<Book> findByAdvancedSearch(@Param("title") String title,
                                    @Param("author") String author,
                                    @Param("category") BookCategory category,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    @Param("language") String language);

    /**
     * BUSQUEDA GLOBAL EN MULTIPLES CAMPOS
     *
     * BUSQUEDA INTEGRAL QUE EXAMINA COINCIDENCIAS EN TITULO,
     * AUTOR, DESCRIPCION Y EDITORIAL SIMULTANEAMENTE.
     * PROPORCIONA FUNCIONALIDAD DE BUSQUEDA GENERAL EN EL CATALOGO.
     *
     * @param searchTerm TERMINO DE BUSQUEDA A APLICAR
     * @return LISTA DE LIBROS CON COINCIDENCIAS
     */
    @Query("SELECT b FROM Book b WHERE b.active = true AND (" +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Book> findByGlobalSearch(@Param("searchTerm") String searchTerm);

    /**
     * CUENTA LIBROS POR CATEGORIA ESPECIFICA
     *
     * OBTENGO LA CANTIDAD TOTAL DE LIBROS EN UNA CATEGORIA DETERMINADA.
     * ESTADISTICA IMPORTANTE PARA ANALISIS DE DISTRIBUCION
     * DEL CATALOGO POR GENEROS.
     *
     * @param category CATEGORIA A CONTAR
     * @return NUMERO TOTAL DE LIBROS EN LA CATEGORIA
     */
    long countByCategory(BookCategory category);

    /**
     * CUENTA LIBROS ACTIVOS EN EL CATALOGO
     *
     * CALCULO EL NUMERO TOTAL DE LIBROS HABILITADOS Y VISIBLES.
     * METRICA FUNDAMENTAL PARA REPORTES DE INVENTARIO
     * Y ESTADISTICAS DEL CATALOGO.
     *
     * @return NUMERO TOTAL DE LIBROS ACTIVOS
     */
    long countByActiveTrue();

    /**
     * CUENTA LIBROS DISPONIBLES PARA PRESTAMO
     *
     * CALCULO LA CANTIDAD DE LIBROS QUE TIENEN AL MENOS UNA COPIA
     * DISPONIBLE PARA PRESTAMO. INDICADOR CLAVE DE DISPONIBILIDAD
     * DEL CATALOGO PARA LOS USUARIOS.
     *
     * @return NUMERO DE LIBROS CON DISPONIBILIDAD
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.active = true AND b.availableCopies > 0")
    long countAvailableBooks();

    /**
     * OBTIENE ESTADISTICAS DE LIBROS POR CATEGORIA
     *
     * CONSULTA QUE GENERA UN RESUMEN ESTADISTICO AGRUPANDO
     * LIBROS POR CATEGORIA Y CONTANDO LA CANTIDAD EN CADA UNA.
     * RESULTA EN DATOS ESTRUCTURADOS PARA DASHBOARDS Y REPORTES.
     *
     * @return LISTA DE ARRAYS CON [CATEGORIA, CANTIDAD]
     */
    @Query("SELECT b.category, COUNT(b) FROM Book b WHERE b.active = true GROUP BY b.category")
    List<Object[]> getBookStatsByCategory();

    /**
     * BUSCA LIBROS CON BAJA DISPONIBILIDAD
     *
     * IDENTIFICO LIBROS QUE TIENEN POCAS COPIAS DISPONIBLES
     * (MENOS DE UN UMBRAL ESPECIFICADO). UTIL PARA ALERTAS
     * DE INVENTARIO Y PLANIFICACION DE ADQUISICIONES.
     *
     * @param threshold UMBRAL MINIMO DE COPIAS DISPONIBLES
     * @return LISTA DE LIBROS CON BAJA DISPONIBILIDAD
     */
    @Query("SELECT b FROM Book b WHERE b.active = true AND b.availableCopies <= :threshold AND b.availableCopies > 0")
    List<Book> findBooksWithLowStock(@Param("threshold") int threshold);

    /**
     * BUSCA LIBROS NUNCA PRESTADOS
     *
     * LOCALIZO LIBROS QUE NUNCA HAN SIDO SOLICITADOS EN PRESTAMO.
     * IMPORTANTE PARA ANALISIS DE POPULARIDAD Y REVISION
     * DE LA RELEVANCIA DEL CATALOGO.
     *
     * @return LISTA DE LIBROS SIN HISTORIAL DE PRESTAMOS
     */
    @Query("SELECT b FROM Book b WHERE b.loanCount = 0")
    List<Book> findNeverBorrowedBooks();

    /**
     * BUSCA LIBROS MAS PRESTADOS EN UN PERIODO
     *
     * IDENTIFICO LOS LIBROS MAS POPULARES BASANDOME EN EL NUMERO
     * DE PRESTAMOS REALIZADOS EN UN RANGO DE FECHAS ESPECIFICO.
     * UTIL PARA ANALISIS DE TENDENCIAS PERIODICAS.
     *
     * @param startDate FECHA INICIAL DEL PERIODO
     * @param endDate FECHA FINAL DEL PERIODO
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON LOS LIBROS MAS PRESTADOS EN EL PERIODO
     */
    @Query("SELECT b FROM Book b JOIN b.loans l WHERE l.loanDate BETWEEN :startDate AND :endDate " +
            "GROUP BY b ORDER BY COUNT(l) DESC")
    Page<Book> findMostBorrowedInPeriod(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        Pageable pageable);

    /**
     * BUSCA LIBROS POR CATEGORIA ACTIVOS Y DISPONIBLES
     *
     * FILTRO LIBROS DE UNA CATEGORIA ESPECIFICA QUE ESTAN ACTIVOS
     * Y TIENEN COPIAS DISPONIBLES PARA PRESTAMO. OPTIMIZA LA CONSULTA
     * PARA MOSTRAR SOLO OPCIONES VIABLES A LOS USUARIOS.
     *
     * @param category CATEGORIA DE LIBROS A FILTRAR
     * @return LISTA DE LIBROS DISPONIBLES DE LA CATEGORIA
     */
    @Query("SELECT b FROM Book b WHERE b.category = :category AND b.active = true AND b.availableCopies > 0")
    List<Book> findAvailableBooksByCategory(@Param("category") BookCategory category);

    /**
     * ACTUALIZA LA DISPONIBILIDAD DE UN LIBRO
     *
     * OPERACION DE ACTUALIZACION QUE MODIFICA EL NUMERO DE COPIAS
     * DISPONIBLES DE UN LIBRO ESPECIFICO. OPTIMIZA LA OPERACION
     * AL EVITAR CARGAR LA ENTIDAD COMPLETA.
     *
     * @param bookId ID DEL LIBRO A ACTUALIZAR
     * @param availableCopies NUEVO NUMERO DE COPIAS DISPONIBLES
     */
    @Modifying
    @Query("UPDATE Book b SET b.availableCopies = :availableCopies WHERE b.id = :bookId")
    void updateAvailableCopies(@Param("bookId") Long bookId, @Param("availableCopies") Integer availableCopies);

    /**
     * INCREMENTA EL CONTADOR DE PRESTAMOS DE UN LIBRO
     *
     * OPERACION ATOMICA QUE INCREMENTA EN UNO EL CONTADOR DE PRESTAMOS
     * DE UN LIBRO ESPECIFICO. SE EJECUTA CADA VEZ QUE SE REALIZA
     * UN NUEVO PRESTAMO DEL LIBRO.
     *
     * @param bookId ID DEL LIBRO A ACTUALIZAR
     */
    @Modifying
    @Query("UPDATE Book b SET b.loanCount = b.loanCount + 1 WHERE b.id = :bookId")
    void incrementLoanCount(@Param("bookId") Long bookId);

    /**
     * ACTUALIZA EL ESTADO DESTACADO DE UN LIBRO
     *
     * OPERACION QUE MODIFICA LA MARCA DE DESTACADO DE UN LIBRO.
     * PERMITE GESTIONAR PROMOCIONES Y RECOMENDACIONES
     * DE FORMA EFICIENTE.
     *
     * @param bookId ID DEL LIBRO A ACTUALIZAR
     * @param featured NUEVO ESTADO DESTACADO
     */
    @Modifying
    @Query("UPDATE Book b SET b.featured = :featured WHERE b.id = :bookId")
    void updateFeaturedStatus(@Param("bookId") Long bookId, @Param("featured") Boolean featured);

    /**
     * BUSCA LIBROS AGREGADOS RECIENTEMENTE
     *
     * LOCALIZO LIBROS AÑADIDOS AL CATALOGO DESPUES DE UNA FECHA
     * ESPECIFICA. UTIL PARA MOSTRAR NOVEDADES Y ACTUALIZACIONES
     * RECIENTES DEL INVENTARIO.
     *
     * @param date FECHA DE CORTE PARA LA BUSQUEDA
     * @return LISTA DE LIBROS AGREGADOS DESPUES DE LA FECHA
     */
    List<Book> findByCreatedAtAfter(LocalDateTime date);

    /**
     * BUSCA LIBROS CON PRESTAMOS ACTIVOS
     *
     * IDENTIFICO LIBROS QUE ACTUALMENTE TIENEN COPIAS EN PRESTAMO
     * (PRESTAMOS NO DEVUELTOS). IMPORTANTE PARA GESTION DE INVENTARIO
     * Y SEGUIMIENTO DE LIBROS EN CIRCULACION.
     *
     * @return LISTA DE LIBROS CON PRESTAMOS ACTIVOS
     */
    @Query("SELECT DISTINCT b FROM Book b JOIN b.loans l WHERE l.returnedAt IS NULL")
    List<Book> findBooksWithActiveLoans();

    /**
     * CALCULA EL VALOR TOTAL DEL CATALOGO
     *
     * SUMO EL VALOR TOTAL DE TODOS LOS LIBROS ACTIVOS EN EL CATALOGO
     * MULTIPLICANDO PRECIO POR NUMERO TOTAL DE COPIAS.
     * METRICA IMPORTANTE PARA INVENTARIOS Y SEGUROS.
     *
     * @return VALOR TOTAL DEL CATALOGO
     */
    @Query("SELECT SUM(b.price * b.totalCopies) FROM Book b WHERE b.active = true AND b.price IS NOT NULL")
    BigDecimal calculateTotalCatalogValue();

    /**
     * BUSCA LIBROS CON DESCRIPCION DISPONIBLE
     *
     * LOCALIZO LIBROS QUE TIENEN DESCRIPCION O SINOPSIS CONFIGURADA.
     * UTIL PARA MOSTRAR LIBROS CON INFORMACION COMPLETA
     * EN INTERFACES DETALLADAS.
     *
     * @return LISTA DE LIBROS CON DESCRIPCION
     */
    @Query("SELECT b FROM Book b WHERE b.description IS NOT NULL AND b.description != ''")
    List<Book> findBooksWithDescription();

    /**
     * OBTIENE AUTORES MAS POPULARES DEL CATALOGO
     *
     * CONSULTA QUE AGRUPA LIBROS POR AUTOR Y CUENTA LA CANTIDAD
     * DE OBRAS DE CADA UNO. ORDENA POR NUMERO DE LIBROS
     * PARA IDENTIFICAR AUTORES MAS REPRESENTADOS.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return LISTA DE ARRAYS CON [AUTOR, CANTIDAD_LIBROS]
     */
    @Query("SELECT b.author, COUNT(b) as bookCount FROM Book b WHERE b.active = true " +
            "GROUP BY b.author ORDER BY bookCount DESC")
    Page<Object[]> findMostPopularAuthors(Pageable pageable);
}