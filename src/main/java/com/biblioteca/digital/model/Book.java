package com.biblioteca.digital.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD JPA QUE REPRESENTA UN LIBRO EN EL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA CLASE MAPEA LA TABLA 'books' EN LA BASE DE DATOS Y CONTIENE TODA LA INFORMACION
 * NECESARIA PARA GESTIONAR EL CATALOGO DE LIBROS DISPONIBLES EN LA BIBLIOTECA.
 * CADA LIBRO PUEDE TENER MULTIPLES COPIAS Y ESTAR ASOCIADO A VARIOS PRESTAMOS
 * A LO LARGO DEL TIEMPO.
 *
 * LA ENTIDAD INCLUYE INFORMACION BIBLIOGRAFICA COMPLETA, CONTROL DE INVENTARIO,
 * GESTION DE DISPONIBILIDAD Y RELACIONES CON EL SISTEMA DE PRESTAMOS.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see User
 * @see Loan
 * @see jakarta.persistence.Entity
 * @see jakarta.validation.constraints
 */
@Entity
@Table(name = "books", indexes = {
        @Index(name = "IDX_BOOK_ISBN", columnList = "isbn"),
        @Index(name = "IDX_BOOK_TITLE", columnList = "title"),
        @Index(name = "IDX_BOOK_AUTHOR", columnList = "author"),
        @Index(name = "IDX_BOOK_CATEGORY", columnList = "category")
})
public class Book {

    /**
     * CLAVE PRIMARIA DE LA ENTIDAD BOOK
     *
     * UTILIZO ESTRATEGIA DE GENERACION IDENTITY PARA QUE LA BASE DE DATOS
     * ASIGNE AUTOMATICAMENTE LOS VALORES INCREMENTALES. ESTE IDENTIFICADOR
     * UNICO PERMITE REFERENCIAR CADA LIBRO DE FORMA INEQUIVOCA EN TODO EL SISTEMA.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    /**
     * CODIGO ISBN DEL LIBRO
     *
     * ALMACENO EL INTERNATIONAL STANDARD BOOK NUMBER QUE IDENTIFICA
     * UNICAMENTE CADA PUBLICACION A NIVEL MUNDIAL. ESTE CODIGO ES FUNDAMENTAL
     * PARA LA GESTION BIBLIOGRAFICA Y LA INTEGRACION CON OTROS SISTEMAS.
     */
    @NotBlank(message = "EL ISBN ES OBLIGATORIO")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "EL ISBN DEBE TENER UN FORMATO VALIDO")
    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    /**
     * TITULO DEL LIBRO
     *
     * CAMPO PRINCIPAL QUE CONTIENE EL TITULO COMPLETO DE LA OBRA.
     * ES EL ELEMENTO MAS VISIBLE EN LAS BUSQUEDAS Y LISTADOS DEL CATALOGO.
     */
    @NotBlank(message = "EL TITULO ES OBLIGATORIO")
    @Size(min = 1, max = 200, message = "EL TITULO DEBE TENER ENTRE 1 Y 200 CARACTERES")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * AUTOR PRINCIPAL DEL LIBRO
     *
     * ALMACENO EL NOMBRE DEL AUTOR O AUTORES PRINCIPALES DE LA OBRA.
     * ESTE CAMPO ES CLAVE PARA LAS BUSQUEDAS POR AUTOR Y LA ORGANIZACION
     * DEL CATALOGO BIBLIOGRAFICO.
     */
    @NotBlank(message = "EL AUTOR ES OBLIGATORIO")
    @Size(min = 2, max = 100, message = "EL AUTOR DEBE TENER ENTRE 2 Y 100 CARACTERES")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    /**
     * DESCRIPCION O SINOPSIS DEL LIBRO
     *
     * TEXTO DESCRIPTIVO QUE PROPORCIONA INFORMACION SOBRE EL CONTENIDO,
     * ARGUMENTO O TEMATICA DEL LIBRO. AYUDA A LOS USUARIOS A CONOCER
     * LA OBRA ANTES DE SOLICITARLA EN PRESTAMO.
     */
    @Size(max = 1000, message = "LA DESCRIPCION NO PUEDE EXCEDER LOS 1000 CARACTERES")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * CATEGORIA O GENERO DEL LIBRO
     *
     * CLASIFICACION TEMATICA QUE PERMITE ORGANIZAR EL CATALOGO POR GENEROS
     * LITERARIOS, AREAS DE CONOCIMIENTO O CATEGORIAS ESPECIFICAS.
     * FACILITA LA NAVEGACION Y BUSQUEDA SEGMENTADA.
     */
    @NotNull(message = "LA CATEGORIA ES OBLIGATORIA")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private BookCategory category;

    /**
     * EDITORIAL QUE PUBLICO EL LIBRO
     *
     * NOMBRE DE LA CASA EDITORIAL RESPONSABLE DE LA PUBLICACION.
     * INFORMACION IMPORTANTE PARA LA CATALOGACION BIBLIOGRAFICA COMPLETA.
     */
    @Size(max = 100, message = "LA EDITORIAL NO PUEDE EXCEDER LOS 100 CARACTERES")
    @Column(name = "publisher", length = 100)
    private String publisher;

    /**
     * FECHA DE PUBLICACION DEL LIBRO
     *
     * ALMACENO LA FECHA EN QUE SE PUBLICO LA OBRA. DATO RELEVANTE PARA
     * LA CATALOGACION Y PARA FILTROS DE BUSQUEDA POR EPOCA O PERIODO.
     */
    @Column(name = "publication_date")
    private LocalDate publicationDate;

    /**
     * NUMERO DE PAGINAS DEL LIBRO
     *
     * CANTIDAD TOTAL DE PAGINAS QUE CONTIENE LA OBRA. INFORMACION UTIL
     * PARA LOS USUARIOS AL SELECCIONAR LECTURAS Y PARA ESTADISTICAS.
     */
    @Min(value = 1, message = "EL NUMERO DE PAGINAS DEBE SER MAYOR A 0")
    @Max(value = 10000, message = "EL NUMERO DE PAGINAS NO PUEDE EXCEDER 10000")
    @Column(name = "pages")
    private Integer pages;

    /**
     * IDIOMA EN QUE ESTA ESCRITO EL LIBRO
     *
     * CODIGO DE IDIOMA QUE PERMITE FILTRAR EL CATALOGO POR LENGUA
     * Y FACILITA LA BUSQUEDA SEGUN LAS PREFERENCIAS LINGUISTICAS.
     */
    @Size(min = 2, max = 10, message = "EL IDIOMA DEBE TENER ENTRE 2 Y 10 CARACTERES")
    @Column(name = "language", length = 10)
    private String language = "ES";

    /**
     * PRECIO DE REFERENCIA DEL LIBRO
     *
     * VALOR MONETARIO DE LA OBRA PARA CONTROL DE INVENTARIO, SEGUROS
     * Y CALCULO DE PENALIZACIONES POR PERDIDA O DAÑO.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "EL PRECIO DEBE SER MAYOR A 0")
    @DecimalMax(value = "9999.99", message = "EL PRECIO NO PUEDE EXCEDER 9999.99")
    @Column(name = "price", precision = 6, scale = 2)
    private BigDecimal price;

    /**
     * NUMERO TOTAL DE COPIAS DISPONIBLES
     *
     * CANTIDAD TOTAL DE EJEMPLARES DE ESTE LIBRO QUE POSEE LA BIBLIOTECA.
     * CONTROLO EL INVENTARIO TOTAL INDEPENDIENTEMENTE DEL ESTADO DE PRESTAMO.
     */
    @NotNull(message = "EL NUMERO DE COPIAS ES OBLIGATORIO")
    @Min(value = 0, message = "EL NUMERO DE COPIAS NO PUEDE SER NEGATIVO")
    @Max(value = 1000, message = "EL NUMERO DE COPIAS NO PUEDE EXCEDER 1000")
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies = 1;

    /**
     * NUMERO DE COPIAS DISPONIBLES PARA PRESTAMO
     *
     * CANTIDAD DE EJEMPLARES QUE ACTUALMENTE ESTAN DISPONIBLES PARA SER
     * PRESTADOS. ESTE NUMERO SE ACTUALIZA AUTOMATICAMENTE CON CADA PRESTAMO
     * Y DEVOLUCION REALIZADA EN EL SISTEMA.
     */
    @NotNull(message = "EL NUMERO DE COPIAS DISPONIBLES ES OBLIGATORIO")
    @Min(value = 0, message = "LAS COPIAS DISPONIBLES NO PUEDEN SER NEGATIVAS")
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies = 1;

    /**
     * URL DE LA IMAGEN DE PORTADA DEL LIBRO
     *
     * ENLACE A LA IMAGEN QUE REPRESENTA LA PORTADA DEL LIBRO PARA
     * MOSTRAR EN LA INTERFAZ DEL USUARIO Y MEJORAR LA EXPERIENCIA VISUAL.
     */
    @Size(max = 500, message = "LA URL DE LA IMAGEN NO PUEDE EXCEDER 500 CARACTERES")
    @Pattern(regexp = "^(https?://.+\\.(jpg|jpeg|png|gif|webp))|$",
            message = "LA URL DE LA IMAGEN DEBE SER VALIDA Y TERMINAR EN JPG, PNG, GIF O WEBP")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * ESTADO ACTIVO DEL LIBRO EN EL CATALOGO
     *
     * CONTROLO SI EL LIBRO APARECE EN LAS BUSQUEDAS Y ESTA DISPONIBLE
     * PARA PRESTAMO. LOS LIBROS INACTIVOS SE MANTIENEN EN LA BASE DE DATOS
     * PERO NO SON VISIBLES PARA LOS USUARIOS.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * INDICA SI EL LIBRO ESTA DESTACADO EN EL CATALOGO
     *
     * MARCADOR QUE PERMITE RESALTAR CIERTOS LIBROS EN LA PAGINA PRINCIPAL
     * O EN SECCIONES ESPECIALES COMO "RECOMENDADOS" O "NOVEDADES".
     */
    @Column(name = "featured", nullable = false)
    private Boolean featured = false;

    /**
     * CONTADOR DE VECES QUE EL LIBRO HA SIDO PRESTADO
     *
     * ESTADISTICA QUE REGISTRA LA POPULARIDAD DEL LIBRO BASADA EN
     * EL NUMERO TOTAL DE PRESTAMOS REALIZADOS. UTIL PARA REPORTES
     * Y ANALISIS DE TENDENCIAS DE LECTURA.
     */
    @Min(value = 0, message = "EL CONTADOR DE PRESTAMOS NO PUEDE SER NEGATIVO")
    @Column(name = "loan_count", nullable = false)
    private Integer loanCount = 0;

    /**
     * FECHA Y HORA DE CREACION DEL REGISTRO
     *
     * TIMESTAMP AUTOMATICO QUE REGISTRA CUANDO SE AGREGO EL LIBRO AL CATALOGO.
     * UTILIZO @CreationTimestamp PARA QUE HIBERNATE GESTIONE AUTOMATICAMENTE
     * ESTE CAMPO SIN INTERVENCION MANUAL.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * FECHA Y HORA DE ULTIMA ACTUALIZACION
     *
     * TIMESTAMP QUE SE ACTUALIZA AUTOMATICAMENTE CADA VEZ QUE SE MODIFICA
     * CUALQUIER CAMPO DE LA ENTIDAD. PERMITE AUDITORIA Y CONTROL DE CAMBIOS
     * EN LA INFORMACION DEL LIBRO.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * LISTA DE PRESTAMOS REALIZADOS DE ESTE LIBRO
     *
     * RELACION ONE-TO-MANY CON LA ENTIDAD LOAN QUE PERMITE ACCEDER AL HISTORIAL
     * COMPLETO DE PRESTAMOS DE ESTE LIBRO. UTILIZO FETCH LAZY PARA OPTIMIZAR
     * LAS CONSULTAS Y CARGAR LOS PRESTAMOS SOLO CUANDO SEA NECESARIO.
     */
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Loan> loans = new ArrayList<>();

    /**
     * CONSTRUCTOR VACIO REQUERIDO POR JPA
     *
     * JPA NECESITA UN CONSTRUCTOR SIN PARAMETROS PARA PODER INSTANCIAR
     * LA ENTIDAD MEDIANTE REFLECTION. AUNQUE NO LO USO DIRECTAMENTE,
     * ES OBLIGATORIO PARA EL CORRECTO FUNCIONAMIENTO DEL ORM.
     */
    public Book() {
        // CONSTRUCTOR VACIO REQUERIDO POR JPA
    }

    /**
     * CONSTRUCTOR PARA CREAR UN LIBRO CON DATOS BASICOS
     *
     * UTILIZO ESTE CONSTRUCTOR PARA CREAR LIBROS CON LA INFORMACION
     * MINIMA REQUERIDA. LOS CAMPOS OPCIONALES SE PUEDEN ESTABLECER
     * POSTERIORMENTE MEDIANTE LOS SETTERS CORRESPONDIENTES.
     *
     * @param isbn CODIGO ISBN UNICO DEL LIBRO
     * @param title TITULO DE LA OBRA
     * @param author AUTOR PRINCIPAL
     * @param category CATEGORIA O GENERO DEL LIBRO
     */
    public Book(String isbn, String title, String author, BookCategory category) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = 1;
        this.availableCopies = 1;
        this.active = true;
        this.featured = false;
        this.loanCount = 0;
    }

    /**
     * CONSTRUCTOR COMPLETO PARA CREAR LIBRO CON TODOS LOS DATOS
     *
     * PERMITE CREAR UN LIBRO ESTABLECIENDO TODOS LOS CAMPOS PRINCIPALES
     * EN UNA SOLA OPERACION. UTIL PARA IMPORTACION DE DATOS O CARGA
     * MASIVA DEL CATALOGO.
     *
     * @param isbn CODIGO ISBN
     * @param title TITULO DEL LIBRO
     * @param author AUTOR PRINCIPAL
     * @param description DESCRIPCION O SINOPSIS
     * @param category CATEGORIA DEL LIBRO
     * @param publisher EDITORIAL
     * @param publicationDate FECHA DE PUBLICACION
     * @param pages NUMERO DE PAGINAS
     * @param price PRECIO DE REFERENCIA
     * @param totalCopies NUMERO TOTAL DE COPIAS
     */
    public Book(String isbn, String title, String author, String description,
                BookCategory category, String publisher, LocalDate publicationDate,
                Integer pages, BigDecimal price, Integer totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.pages = pages;
        this.price = price;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.active = true;
        this.featured = false;
        this.loanCount = 0;
    }

    // GETTERS Y SETTERS CON DOCUMENTACION JAVADOC

    /**
     * OBTIENE EL IDENTIFICADOR UNICO DEL LIBRO
     *
     * @return ID NUMERICO DEL LIBRO
     */
    public Long getId() {
        return id;
    }

    /**
     * ESTABLECE EL IDENTIFICADOR UNICO DEL LIBRO
     *
     * @param id IDENTIFICADOR NUMERICO A ASIGNAR
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * OBTIENE EL CODIGO ISBN DEL LIBRO
     *
     * @return CODIGO ISBN
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * ESTABLECE EL CODIGO ISBN DEL LIBRO
     *
     * @param isbn CODIGO ISBN A ASIGNAR
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * OBTIENE EL TITULO DEL LIBRO
     *
     * @return TITULO COMPLETO DE LA OBRA
     */
    public String getTitle() {
        return title;
    }

    /**
     * ESTABLECE EL TITULO DEL LIBRO
     *
     * @param title TITULO A ASIGNAR
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * OBTIENE EL AUTOR DEL LIBRO
     *
     * @return NOMBRE DEL AUTOR PRINCIPAL
     */
    public String getAuthor() {
        return author;
    }

    /**
     * ESTABLECE EL AUTOR DEL LIBRO
     *
     * @param author AUTOR A ASIGNAR
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * OBTIENE LA DESCRIPCION DEL LIBRO
     *
     * @return DESCRIPCION O SINOPSIS
     */
    public String getDescription() {
        return description;
    }

    /**
     * ESTABLECE LA DESCRIPCION DEL LIBRO
     *
     * @param description DESCRIPCION A ASIGNAR
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * OBTIENE LA CATEGORIA DEL LIBRO
     *
     * @return CATEGORIA O GENERO
     */
    public BookCategory getCategory() {
        return category;
    }

    /**
     * ESTABLECE LA CATEGORIA DEL LIBRO
     *
     * @param category CATEGORIA A ASIGNAR
     */
    public void setCategory(BookCategory category) {
        this.category = category;
    }

    /**
     * OBTIENE LA EDITORIAL DEL LIBRO
     *
     * @return NOMBRE DE LA EDITORIAL
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * ESTABLECE LA EDITORIAL DEL LIBRO
     *
     * @param publisher EDITORIAL A ASIGNAR
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * OBTIENE LA FECHA DE PUBLICACION DEL LIBRO
     *
     * @return FECHA DE PUBLICACION
     */
    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    /**
     * ESTABLECE LA FECHA DE PUBLICACION DEL LIBRO
     *
     * @param publicationDate FECHA A ASIGNAR
     */
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    /**
     * OBTIENE EL NUMERO DE PAGINAS DEL LIBRO
     *
     * @return CANTIDAD DE PAGINAS
     */
    public Integer getPages() {
        return pages;
    }

    /**
     * ESTABLECE EL NUMERO DE PAGINAS DEL LIBRO
     *
     * @param pages NUMERO DE PAGINAS A ASIGNAR
     */
    public void setPages(Integer pages) {
        this.pages = pages;
    }

    /**
     * OBTIENE EL IDIOMA DEL LIBRO
     *
     * @return CODIGO DE IDIOMA
     */
    public String getLanguage() {
        return language;
    }

    /**
     * ESTABLECE EL IDIOMA DEL LIBRO
     *
     * @param language CODIGO DE IDIOMA A ASIGNAR
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * OBTIENE EL PRECIO DEL LIBRO
     *
     * @return PRECIO DE REFERENCIA
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * ESTABLECE EL PRECIO DEL LIBRO
     *
     * @param price PRECIO A ASIGNAR
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * OBTIENE EL NUMERO TOTAL DE COPIAS
     *
     * @return CANTIDAD TOTAL DE COPIAS
     */
    public Integer getTotalCopies() {
        return totalCopies;
    }

    /**
     * ESTABLECE EL NUMERO TOTAL DE COPIAS
     *
     * @param totalCopies CANTIDAD TOTAL A ASIGNAR
     */
    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    /**
     * OBTIENE EL NUMERO DE COPIAS DISPONIBLES
     *
     * @return CANTIDAD DE COPIAS DISPONIBLES
     */
    public Integer getAvailableCopies() {
        return availableCopies;
    }

    /**
     * ESTABLECE EL NUMERO DE COPIAS DISPONIBLES
     *
     * @param availableCopies CANTIDAD DISPONIBLE A ASIGNAR
     */
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    /**
     * OBTIENE LA URL DE LA IMAGEN DE PORTADA
     *
     * @return URL DE LA IMAGEN
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * ESTABLECE LA URL DE LA IMAGEN DE PORTADA
     *
     * @param imageUrl URL DE LA IMAGEN A ASIGNAR
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * VERIFICA SI EL LIBRO ESTA ACTIVO
     *
     * @return TRUE SI ESTA ACTIVO, FALSE EN CASO CONTRARIO
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * ESTABLECE EL ESTADO ACTIVO DEL LIBRO
     *
     * @param active ESTADO A ASIGNAR
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * VERIFICA SI EL LIBRO ESTA DESTACADO
     *
     * @return TRUE SI ESTA DESTACADO, FALSE EN CASO CONTRARIO
     */
    public Boolean getFeatured() {
        return featured;
    }

    /**
     * ESTABLECE SI EL LIBRO ESTA DESTACADO
     *
     * @param featured ESTADO DESTACADO A ASIGNAR
     */
    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    /**
     * OBTIENE EL CONTADOR DE PRESTAMOS
     *
     * @return NUMERO TOTAL DE PRESTAMOS REALIZADOS
     */
    public Integer getLoanCount() {
        return loanCount;
    }

    /**
     * ESTABLECE EL CONTADOR DE PRESTAMOS
     *
     * @param loanCount CONTADOR A ASIGNAR
     */
    public void setLoanCount(Integer loanCount) {
        this.loanCount = loanCount;
    }

    /**
     * OBTIENE LA FECHA DE CREACION DEL LIBRO
     *
     * @return TIMESTAMP DE CREACION
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * OBTIENE LA FECHA DE ULTIMA ACTUALIZACION
     *
     * @return TIMESTAMP DE ULTIMA MODIFICACION
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * OBTIENE LA LISTA DE PRESTAMOS DEL LIBRO
     *
     * @return LISTA DE PRESTAMOS REALIZADOS
     */
    public List<Loan> getLoans() {
        return loans;
    }

    /**
     * ESTABLECE LA LISTA DE PRESTAMOS DEL LIBRO
     *
     * @param loans LISTA DE PRESTAMOS A ASIGNAR
     */
    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    /**
     * AGREGA UN PRESTAMO A LA LISTA DEL LIBRO
     *
     * METODO DE CONVENIENCIA PARA AÑADIR UN PRESTAMO Y MANTENER
     * LA CONSISTENCIA BIDIRECCIONAL DE LA RELACION.
     *
     * @param loan PRESTAMO A AGREGAR
     */
    public void addLoan(Loan loan) {
        loans.add(loan);
        loan.setBook(this);
        // ACTUALIZO EL CONTADOR DE PRESTAMOS
        this.loanCount++;
    }

    /**
     * ELIMINA UN PRESTAMO DE LA LISTA DEL LIBRO
     *
     * METODO DE CONVENIENCIA PARA REMOVER UN PRESTAMO Y MANTENER
     * LA CONSISTENCIA BIDIRECCIONAL DE LA RELACION.
     *
     * @param loan PRESTAMO A ELIMINAR
     */
    public void removeLoan(Loan loan) {
        loans.remove(loan);
        loan.setBook(null);
    }

    /**
     * VERIFICA SI EL LIBRO TIENE COPIAS DISPONIBLES PARA PRESTAMO
     *
     * @return TRUE SI HAY COPIAS DISPONIBLES, FALSE EN CASO CONTRARIO
     */
    public boolean isAvailableForLoan() {
        return active && availableCopies != null && availableCopies > 0;
    }

    /**
     * VERIFICA SI EL LIBRO ESTA AGOTADO (SIN COPIAS DISPONIBLES)
     *
     * @return TRUE SI NO HAY COPIAS DISPONIBLES, FALSE EN CASO CONTRARIO
     */
    public boolean isOutOfStock() {
        return availableCopies == null || availableCopies <= 0;
    }

    /**
     * CALCULA EL PORCENTAJE DE DISPONIBILIDAD DEL LIBRO
     *
     * @return PORCENTAJE DE COPIAS DISPONIBLES RESPECTO AL TOTAL
     */
    public double getAvailabilityPercentage() {
        if (totalCopies == null || totalCopies == 0) {
            return 0.0;
        }
        return (availableCopies != null ? availableCopies : 0) * 100.0 / totalCopies;
    }

    /**
     * RESERVA UNA COPIA DEL LIBRO PARA PRESTAMO
     *
     * DECREMENTA EL NUMERO DE COPIAS DISPONIBLES CUANDO SE REALIZA UN PRESTAMO.
     * CONTROLO QUE HAYA COPIAS DISPONIBLES ANTES DE REALIZAR LA OPERACION.
     *
     * @return TRUE SI SE PUDO RESERVAR UNA COPIA, FALSE EN CASO CONTRARIO
     */
    public boolean reserveCopy() {
        if (isAvailableForLoan()) {
            availableCopies--;
            return true;
        }
        return false;
    }

    /**
     * LIBERA UNA COPIA DEL LIBRO AL DEVOLVER EL PRESTAMO
     *
     * INCREMENTA EL NUMERO DE COPIAS DISPONIBLES CUANDO SE DEVUELVE UN LIBRO.
     * CONTROLO QUE NO SE EXCEDA EL NUMERO TOTAL DE COPIAS.
     *
     * @return TRUE SI SE PUDO LIBERAR UNA COPIA, FALSE EN CASO CONTRARIO
     */
    public boolean releaseCopy() {
        if (availableCopies != null && totalCopies != null && availableCopies < totalCopies) {
            availableCopies++;
            return true;
        }
        return false;
    }

    /**
     * OBTIENE EL NUMERO DE COPIAS ACTUALMENTE PRESTADAS
     *
     * @return CANTIDAD DE COPIAS EN PRESTAMO
     */
    public int getLoanedCopies() {
        if (totalCopies == null || availableCopies == null) {
            return 0;
        }
        return totalCopies - availableCopies;
    }

    /**
     * VERIFICA SI EL LIBRO ES POPULAR BASADO EN EL NUMERO DE PRESTAMOS
     *
     * CONSIDERO UN LIBRO POPULAR SI HA SIDO PRESTADO MAS DE 10 VECES.
     *
     * @return TRUE SI ES POPULAR, FALSE EN CASO CONTRARIO
     */
    public boolean isPopular() {
        return loanCount != null && loanCount > 10;
    }

    /**
     * REPRESENTACION EN CADENA DE LA ENTIDAD BOOK
     *
     * GENERO UNA REPRESENTACION LEGIBLE QUE INCLUYE LOS CAMPOS MAS
     * IMPORTANTES PARA DEBUGGING Y LOGGING.
     *
     * @return CADENA REPRESENTATIVA DEL LIBRO
     */
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category=" + category +
                ", totalCopies=" + totalCopies +
                ", availableCopies=" + availableCopies +
                ", active=" + active +
                ", loanCount=" + loanCount +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * VERIFICA LA IGUALDAD ENTRE DOS OBJETOS BOOK
     *
     * DOS LIBROS SON IGUALES SI TIENEN EL MISMO ID O EL MISMO ISBN
     * (DADO QUE ISBN ES UNICO EN EL SISTEMA).
     *
     * @param obj OBJETO A COMPARAR
     * @return TRUE SI SON IGUALES, FALSE EN CASO CONTRARIO
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Book book = (Book) obj;

        if (id != null && book.id != null) {
            return id.equals(book.id);
        }

        return isbn != null && isbn.equals(book.isbn);
    }

    /**
     * GENERA EL CODIGO HASH DEL OBJETO BOOK
     *
     * UTILIZO EL ISBN PARA GENERAR EL HASH YA QUE ES UNICO
     * Y INMUTABLE UNA VEZ ESTABLECIDO.
     *
     * @return CODIGO HASH DEL LIBRO
     */
    @Override
    public int hashCode() {
        return isbn != null ? isbn.hashCode() : 0;
    }
}