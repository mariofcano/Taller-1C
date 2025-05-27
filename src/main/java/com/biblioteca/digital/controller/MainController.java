package com.biblioteca.digital.controller;

import com.biblioteca.digital.model.Book;
import com.biblioteca.digital.service.BookService;
import com.biblioteca.digital.service.UserService;
import com.biblioteca.digital.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * CONTROLADOR PRINCIPAL PARA LA GESTION DE RUTAS PUBLICAS DEL SISTEMA DE BIBLIOTECA DIGITAL
 *
 * ESTA CLASE IMPLEMENTA TODOS LOS ENDPOINTS PRINCIPALES PARA LAS PAGINAS PUBLICAS
 * DEL SISTEMA DE BIBLIOTECA DIGITAL. MANEJO LAS RUTAS DE ACCESO GENERAL QUE NO REQUIEREN
 * AUTENTICACION COMO LA PAGINA PRINCIPAL, CATALOGO PUBLICO, LOGIN Y REGISTRO.
 *
 * EL CONTROLADOR COORDINA LAS OPERACIONES ENTRE LAS CAPAS DE SERVICIO Y LAS VISTAS,
 * PREPARANDO TODOS LOS DATOS NECESARIOS PARA QUE LAS PLANTILLAS THYMELEAF PUEDAN
 * RENDERIZAR CORRECTAMENTE LA INFORMACION DEL SISTEMA CON DATOS REALES Y ACTUALIZADOS.
 *
 * IMPLEMENTO UN DISEÑO CENTRADO EN LA EXPERIENCIA DE USUARIO, PROPORCIONANDO
 * INTERFACES INTUITIVAS Y ESTADISTICAS RELEVANTES EN TIEMPO REAL PARA MOSTRAR
 * EL ESTADO ACTUAL DE LA BIBLIOTECA DIGITAL.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 *
 * @see BookService
 * @see UserService
 * @see LoanService
 * @see org.springframework.stereotype.Controller
 */
@Controller
public class MainController {

    /**
     * SERVICIO DE GESTION DE LIBROS DEL SISTEMA
     *
     * INYECTO EL SERVICIO DE LIBROS QUE ME PROPORCIONA TODAS LAS OPERACIONES
     * NECESARIAS PARA CONSULTAR EL CATALOGO, OBTENER ESTADISTICAS DE INVENTARIO
     * Y GESTIONAR LA INFORMACION BIBLIOGRAFICA QUE MUESTRO EN LAS INTERFACES.
     */
    @Autowired
    private BookService bookService;

    /**
     * SERVICIO DE GESTION DE USUARIOS DEL SISTEMA
     *
     * INYECTO EL SERVICIO DE USUARIOS PARA ACCEDER A FUNCIONALIDADES DE REGISTRO,
     * AUTENTICACION Y ESTADISTICAS DE USUARIOS ACTIVOS QUE NECESITO MOSTRAR
     * EN LOS DASHBOARDS Y PAGINAS DE INFORMACION GENERAL.
     */
    @Autowired
    private UserService userService;

    /**
     * SERVICIO DE GESTION DE PRESTAMOS DEL SISTEMA
     *
     * INYECTO EL SERVICIO DE PRESTAMOS PARA OBTENER ESTADISTICAS DE ACTIVIDAD,
     * PRESTAMOS ACTIVOS Y METRICAS OPERACIONALES QUE ENRIQUECEN LA EXPERIENCIA
     * DEL USUARIO AL MOSTRAR EL NIVEL DE ACTIVIDAD DE LA BIBLIOTECA.
     */
    @Autowired
    private LoanService loanService;

    /**
     * RENDERIZA LA PAGINA PRINCIPAL DEL SISTEMA DE BIBLIOTECA DIGITAL
     *
     * ESTE METODO MANEJA LA RUTA RAIZ DEL SISTEMA Y SE ENCARGA DE PREPARAR
     * TODOS LOS DATOS ESTADISTICOS Y DE CONTENIDO DESTACADO QUE NECESITO
     * MOSTRAR EN LA PAGINA DE INICIO. RECOPILO INFORMACION EN TIEMPO REAL
     * SOBRE EL ESTADO DEL CATALOGO, USUARIOS ACTIVOS Y PRESTAMOS VIGENTES.
     *
     * LA PAGINA PRINCIPAL FUNCIONA COMO DASHBOARD PUBLICO QUE PERMITE A LOS
     * VISITANTES CONOCER EL VOLUMEN Y ACTIVIDAD DE LA BIBLIOTECA SIN NECESIDAD
     * DE AUTENTICACION. TAMBIEN MUESTRO LOS LIBROS DESTACADOS PARA PROMOVER
     * EL INTERES EN EL CATALOGO DISPONIBLE.
     *
     * IMPLEMENTO MANEJO ROBUSTO DE ERRORES PARA GARANTIZAR QUE LA PAGINA
     * SE RENDERICE CORRECTAMENTE INCLUSO SI ALGUNOS SERVICIOS NO ESTAN
     * DISPONIBLES TEMPORALMENTE. PROPORCIONO VALORES POR DEFECTO
     * PARA MANTENER LA FUNCIONALIDAD BASICA.
     *
     * @param model OBJETO MODEL DE SPRING MVC QUE USO PARA PASAR DATOS A LA VISTA
     * @return NOMBRE DE LA PLANTILLA THYMELEAF "index" PARA RENDERIZAR LA PAGINA PRINCIPAL
     */
    @GetMapping("/")
    public String home(Model model) {
        try {
            // OBTENGO ESTADISTICAS GENERALES DEL SISTEMA PARA EL DASHBOARD
            // ESTAS METRICAS PROPORCIONAN UNA VISION GENERAL DEL ESTADO DE LA BIBLIOTECA

            // CUENTO EL TOTAL DE LIBROS ACTIVOS EN EL CATALOGO
            long totalBooks = bookService.countActiveBooks();
            model.addAttribute("totalBooks", totalBooks);

            // CALCULO CUANTOS LIBROS TIENEN COPIAS DISPONIBLES PARA PRESTAMO
            long availableBooks = bookService.countAvailableBooks();
            model.addAttribute("availableBooks", availableBooks);

            // OBTENGO EL NUMERO DE PRESTAMOS ACTUALMENTE VIGENTES
            long activeLoans = loanService.countActiveLoans();
            model.addAttribute("activeLoans", activeLoans);

            // CUENTO LOS USUARIOS REGISTRADOS Y ACTIVOS EN EL SISTEMA
            long totalUsers = userService.findAllActiveUsers().size();
            model.addAttribute("totalUsers", totalUsers);

            // RECUPERO LOS LIBROS MARCADOS COMO DESTACADOS PARA PROMOCIONAR
            // EN LA SECCION PRINCIPAL DE LA PAGINA DE INICIO
            List<Book> featuredBooks = bookService.findFeaturedBooks();
            model.addAttribute("featuredBooks", featuredBooks);

            // AÑADO INFORMACION ADICIONAL PARA MEJORAR LA EXPERIENCIA
            model.addAttribute("welcomeMessage", "¡Bienvenido a nuestra Biblioteca Digital!");
            model.addAttribute("systemStatus", "Sistema operativo y actualizado");

            System.out.println("ESTADISTICAS CARGADAS - Libros: " + totalBooks +
                    ", Disponibles: " + availableBooks +
                    ", Prestamos activos: " + activeLoans +
                    ", Usuarios: " + totalUsers);

        } catch (Exception e) {
            // SI OCURRE CUALQUIER ERROR AL CARGAR LOS DATOS, REGISTRO EL PROBLEMA
            // PERO PERMITO QUE LA PAGINA SE MUESTRE CON UN MENSAJE DE ERROR
            // ESTO GARANTIZA QUE EL SISTEMA SIGA SIENDO ACCESIBLE INCLUSO
            // CUANDO HAY PROBLEMAS TEMPORALES CON LA BASE DE DATOS
            System.err.println("ERROR AL CARGAR ESTADISTICAS: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "ERROR AL CARGAR DATOS DEL SISTEMA. ALGUNOS VALORES PUEDEN NO ESTAR ACTUALIZADOS.");

            // PROPORCIONO VALORES POR DEFECTO PARA MANTENER LA FUNCIONALIDAD
            model.addAttribute("totalBooks", 0L);
            model.addAttribute("availableBooks", 0L);
            model.addAttribute("activeLoans", 0L);
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("featuredBooks", List.of());
        }

        // RETORNO EL NOMBRE DE LA PLANTILLA QUE SPRING MVC DEBE RENDERIZAR
        return "index";
    }

    /**
     * PROCESA BUSQUEDAS REALIZADAS DESDE LA PAGINA PRINCIPAL
     *
     * ESTE METODO MANEJA LAS SOLICITUDES POST DEL FORMULARIO DE BUSQUEDA
     * DE LA PAGINA PRINCIPAL. PERMITE A LOS USUARIOS BUSCAR LIBROS
     * SIN NECESIDAD DE AUTENTICACION, PROPORCIONANDO UNA FUNCIONALIDAD
     * BASICA DE EXPLORACION DEL CATALOGO.
     *
     * IMPLEMENTO BUSQUEDA GLOBAL QUE EXAMINA TITULOS, AUTORES Y DESCRIPCIONES
     * PARA PROPORCIONAR RESULTADOS RELEVANTES. MANEJO CASOS ESPECIALES
     * COMO BUSQUEDAS VACIAS O TERMINOS DEMASIADO CORTOS.
     *
     * @param searchTerm TERMINO DE BUSQUEDA INTRODUCIDO POR EL USUARIO
     * @param model OBJETO MODEL PARA PASAR RESULTADOS A LA VISTA
     * @return NOMBRE DE LA PLANTILLA CON LOS RESULTADOS DE BUSQUEDA
     */
    @PostMapping("/search")
    public String searchBooks(@RequestParam("searchTerm") String searchTerm, Model model) {
        try {
            // VALIDO EL TERMINO DE BUSQUEDA
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                model.addAttribute("searchError", "Por favor, introduce un término de búsqueda");
                return "redirect:/";
            }

            // LIMPIO EL TERMINO DE BUSQUEDA
            String cleanSearchTerm = searchTerm.trim();

            if (cleanSearchTerm.length() < 2) {
                model.addAttribute("searchError", "El término de búsqueda debe tener al menos 2 caracteres");
                return "redirect:/";
            }

            // REALIZO LA BUSQUEDA UTILIZANDO EL SERVICIO DE LIBROS
            List<Book> searchResults = bookService.searchBooks(cleanSearchTerm);

            // PREPARO LOS DATOS PARA LA VISTA DE RESULTADOS
            model.addAttribute("searchTerm", cleanSearchTerm);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("resultCount", searchResults.size());

            // AÑADO MENSAJE DESCRIPTIVO SEGUN LOS RESULTADOS
            if (searchResults.isEmpty()) {
                model.addAttribute("searchMessage",
                        "No se encontraron libros que coincidan con tu búsqueda. Intenta con otros términos.");
            } else {
                model.addAttribute("searchMessage",
                        "Se encontraron " + searchResults.size() + " libros relacionados con tu búsqueda.");
            }

            System.out.println("BUSQUEDA REALIZADA: '" + cleanSearchTerm + "' - Resultados: " + searchResults.size());

        } catch (Exception e) {
            System.err.println("ERROR EN BUSQUEDA: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("searchError", "Error al realizar la búsqueda. Por favor, intenta nuevamente.");
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("searchResults", List.of());
            model.addAttribute("resultCount", 0);
        }

        // RETORNO LA VISTA DE RESULTADOS DE BUSQUEDA
        return "search-results";
    }

    /**
     * RENDERIZA LA PAGINA DE INICIO DE SESION DEL SISTEMA
     *
     * ESTE ENDPOINT MANEJA LAS SOLICITUDES GET PARA MOSTRAR EL FORMULARIO
     * DE LOGIN A LOS USUARIOS QUE DESEAN AUTENTICARSE EN EL SISTEMA.
     * LA PAGINA DE LOGIN ES EL PUNTO DE ENTRADA PARA USUARIOS REGISTRADOS
     * QUE QUIEREN ACCEDER A FUNCIONALIDADES AVANZADAS COMO REALIZAR PRESTAMOS.
     *
     * IMPLEMENTO MANEJO DE PARAMETROS PARA MOSTRAR MENSAJES INFORMATIVOS
     * COMO CONFIRMACIONES DE REGISTRO O ERRORES DE AUTENTICACION.
     * ESTO MEJORA LA EXPERIENCIA DE USUARIO AL PROPORCIONAR FEEDBACK CLARO.
     *
     * @param error PARAMETRO QUE INDICA SI HUBO ERROR EN EL LOGIN
     * @param logout PARAMETRO QUE INDICA SI EL USUARIO ACABA DE HACER LOGOUT
     * @param registered PARAMETRO QUE INDICA SI EL USUARIO SE ACABA DE REGISTRAR
     * @param model OBJETO MODEL PARA PASAR MENSAJES A LA VISTA
     * @return NOMBRE DE LA PLANTILLA THYMELEAF "login" PARA EL FORMULARIO DE AUTENTICACION
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {

        // MANEJO DIFERENTES MENSAJES SEGUN LOS PARAMETROS RECIBIDOS
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos. Por favor, verifica tus credenciales.");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión correctamente. ¡Hasta pronto!");
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Registro completado exitosamente. Ya puedes iniciar sesión.");
        }

        // AÑADO INFORMACION ADICIONAL PARA LA VISTA
        model.addAttribute("pageTitle", "Iniciar Sesión");
        model.addAttribute("loginInfo", "Accede a tu cuenta para disfrutar de todos los servicios de la biblioteca");

        System.out.println("ACCESO A PAGINA DE LOGIN - Error: " + error + ", Logout: " + logout + ", Registered: " + registered);

        // RETORNO LA VISTA DE LOGIN SIN LOGICA ADICIONAL
        // SPRING SECURITY MANEJA LA AUTENTICACION AUTOMATICAMENTE
        return "login";
    }

    /**
     * RENDERIZA LA PAGINA DE REGISTRO DE NUEVOS USUARIOS
     *
     * ESTE METODO PROPORCIONA EL FORMULARIO DE REGISTRO DONDE LOS NUEVOS
     * VISITANTES PUEDEN CREAR UNA CUENTA EN EL SISTEMA DE BIBLIOTECA DIGITAL.
     * LA PAGINA DE REGISTRO ES FUNDAMENTAL PARA EL CRECIMIENTO DE LA BASE
     * DE USUARIOS Y LA ADOPCION DEL SISTEMA.
     *
     * MUESTRO UN FORMULARIO SIMPLE CON LOS CAMPOS NECESARIOS PARA CREAR
     * UNA CUENTA BASICA: USERNAME, EMAIL, PASSWORD Y NOMBRE COMPLETO.
     * EL PROCESAMIENTO DEL REGISTRO SE MANEJA EN UN ENDPOINT SEPARADO.
     *
     * @param model OBJETO MODEL PARA PASAR DATOS A LA VISTA
     * @return NOMBRE DE LA PLANTILLA THYMELEAF "register" PARA EL FORMULARIO DE REGISTRO
     */
    @GetMapping("/register")
    public String register(Model model) {
        // PREPARO INFORMACION PARA LA VISTA DE REGISTRO
        model.addAttribute("pageTitle", "Crear Cuenta Nueva");
        model.addAttribute("registerInfo", "Únete a nuestra comunidad y accede a miles de libros digitales");

        // AÑADO INSTRUCCIONES PARA EL USUARIO
        model.addAttribute("passwordRequirements",
                "La contraseña debe tener al menos 8 caracteres, incluir letras y números");

        System.out.println("ACCESO A PAGINA DE REGISTRO");

        // RETORNO LA VISTA DE REGISTRO PARA NUEVOS USUARIOS
        // EL PROCESAMIENTO DEL FORMULARIO SE MANEJA EN OTRO ENDPOINT
        return "register";
    }

    /**
     * PROCESA EL REGISTRO DE NUEVOS USUARIOS
     *
     * ESTE METODO MANEJA EL FORMULARIO POST DE REGISTRO, VALIDANDO
     * LOS DATOS INTRODUCIDOS Y CREANDO UNA NUEVA CUENTA DE USUARIO.
     * IMPLEMENTO VALIDACIONES ROBUSTAS Y MANEJO DE ERRORES PARA
     * GARANTIZAR LA INTEGRIDAD DE LOS DATOS.
     *
     * @param username NOMBRE DE USUARIO ELEGIDO
     * @param email DIRECCION DE EMAIL DEL USUARIO
     * @param password CONTRASEÑA ELEGIDA
     * @param fullName NOMBRE COMPLETO DEL USUARIO
     * @param phone TELEFONO (OPCIONAL)
     * @param model OBJETO MODEL PARA MENSAJES DE ERROR O EXITO
     * @return REDIRECCION A LOGIN CON MENSAJE DE EXITO O VUELTA A REGISTRO CON ERRORES
     */
    @PostMapping("/register")
    public String processRegistration(@RequestParam("username") String username,
                                      @RequestParam("email") String email,
                                      @RequestParam("password") String password,
                                      @RequestParam("fullName") String fullName,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      Model model) {
        try {
            // VALIDO LOS DATOS DE ENTRADA
            if (username == null || username.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    fullName == null || fullName.trim().isEmpty()) {

                model.addAttribute("errorMessage", "Todos los campos obligatorios deben estar completados");
                return "register";
            }

            // VERIFICO DISPONIBILIDAD DEL USERNAME Y EMAIL
            if (!userService.isUsernameAvailable(username.trim())) {
                model.addAttribute("errorMessage", "El nombre de usuario ya está en uso. Por favor, elige otro.");
                return "register";
            }

            if (!userService.isEmailAvailable(email.trim())) {
                model.addAttribute("errorMessage", "El email ya está registrado. ¿Ya tienes una cuenta?");
                return "register";
            }

            // REGISTRO EL NUEVO USUARIO
            userService.registerUser(username.trim(), email.trim(), password, fullName.trim());

            System.out.println("NUEVO USUARIO REGISTRADO: " + username);

            // REDIRECCION CON MENSAJE DE EXITO
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            System.err.println("ERROR EN REGISTRO: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("errorMessage", "Error al crear la cuenta: " + e.getMessage());

            // MANTENGO LOS DATOS INTRODUCIDOS PARA QUE EL USUARIO NO LOS PIERDA
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("fullName", fullName);
            model.addAttribute("phone", phone);

            return "register";
        }
    }

    /**
     * RENDERIZA EL CATALOGO PUBLICO DE LIBROS DISPONIBLES
     *
     * ESTE ENDPOINT MUESTRA UNA VISTA PUBLICA DEL CATALOGO DE LIBROS
     * QUE PERMITE A LOS VISITANTES EXPLORAR LA COLECCION DISPONIBLE
     * SIN NECESIDAD DE AUTENTICACION. ES UNA HERRAMIENTA DE MARKETING
     * QUE INCENTIVA EL REGISTRO Y USO DEL SISTEMA.
     *
     * CARGO TODOS LOS LIBROS QUE TIENEN COPIAS DISPONIBLES PARA PRESTAMO
     * Y LOS ORGANIZO EN UNA VISTA ATRACTIVA CON INFORMACION BIBLIOGRAFICA
     * BASICA COMO TITULO, AUTOR, CATEGORIA Y DESCRIPCION.
     *
     * IMPLEMENTO MANEJO DE ERRORES PARA GARANTIZAR QUE LA PAGINA
     * SEA ACCESIBLE INCLUSO SI HAY PROBLEMAS TEMPORALES CON LA BASE DE DATOS.
     *
     * @param model OBJETO MODEL PARA PASAR DATOS DE LIBROS A LA VISTA
     * @return NOMBRE DE LA PLANTILLA THYMELEAF "catalog" PARA MOSTRAR EL CATALOGO
     */
    @GetMapping("/catalog")
    public String catalog(Model model) {
        try {
            // CARGO TODOS LOS LIBROS DISPONIBLES PARA MOSTRAR EN EL CATALOGO
            // SOLO INCLUYO LIBROS ACTIVOS CON COPIAS DISPONIBLES PARA PRESTAMO
            List<Book> availableBooks = bookService.findAvailableBooks();
            model.addAttribute("books", availableBooks);

            // OBTENGO ESTADISTICAS DEL CATALOGO PARA INFORMACION ADICIONAL
            long totalBooks = bookService.countActiveBooks();
            model.addAttribute("totalBooksInCatalog", totalBooks);
            model.addAttribute("availableBooksCount", availableBooks.size());

            // AÑADO INFORMACION PARA LA VISTA
            model.addAttribute("pageTitle", "Catálogo de Libros");
            model.addAttribute("catalogDescription",
                    "Explora nuestra colección de " + totalBooks + " libros disponibles");

            System.out.println("CATALOGO CARGADO - Total libros: " + totalBooks + ", Disponibles: " + availableBooks.size());

        } catch (Exception e) {
            // SI HAY ERROR AL CARGAR EL CATALOGO, MUESTRO UN MENSAJE DE ERROR
            // PERO PERMITO QUE LA PAGINA SE RENDERICE PARA MANTENER LA EXPERIENCIA
            System.err.println("ERROR AL CARGAR CATALOGO: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "ERROR AL CARGAR EL CATÁLOGO: " + e.getMessage());
            model.addAttribute("books", List.of());
            model.addAttribute("totalBooksInCatalog", 0L);
            model.addAttribute("availableBooksCount", 0);
        }

        // RETORNO LA VISTA DEL CATALOGO CON LOS LIBROS CARGADOS
        return "catalog";
    }

    /**
     * RENDERIZA LA PAGINA DE INFORMACION DE CONTACTO
     *
     * ESTE ENDPOINT PROPORCIONA UNA PAGINA ESTATICA CON INFORMACION
     * DE CONTACTO DE LA BIBLIOTECA, HORARIOS DE ATENCION, DIRECCION
     * Y OTROS DATOS RELEVANTES PARA LOS USUARIOS QUE NECESITEN
     * COMUNICARSE CON EL PERSONAL DE LA BIBLIOTECA.
     *
     * ES UNA PAGINA INFORMATIVA SIMPLE QUE NO REQUIERE DATOS DINAMICOS
     * DEL SISTEMA, SOLO CONTENIDO ESTATICO DEFINIDO EN LA PLANTILLA.
     *
     * @param model OBJETO MODEL PARA PASAR INFORMACION A LA VISTA
     * @return NOMBRE DE LA PLANTILLA THYMELEAF "contact" CON INFORMACION DE CONTACTO
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        // PREPARO INFORMACION DE CONTACTO PARA LA VISTA
        model.addAttribute("pageTitle", "Información de Contacto");
        model.addAttribute("libraryName", "Biblioteca Digital Mario Flores");
        model.addAttribute("libraryAddress", "Calle del Conocimiento, 123 - 28001 Madrid");
        model.addAttribute("libraryPhone", "+34 91 123 45 67");
        model.addAttribute("libraryEmail", "info@biblioteca-digital.com");

        // HORARIOS DE ATENCION
        model.addAttribute("schedule", "Lunes a Viernes: 9:00 - 20:00, Sábados: 10:00 - 14:00");

        // INFORMACION ADICIONAL
        model.addAttribute("contactDescription",
                "Estamos aquí para ayudarte. Contáctanos para cualquier consulta sobre nuestros servicios.");

        System.out.println("ACCESO A PAGINA DE CONTACTO");

        // RETORNO LA PAGINA ESTATICA DE CONTACTO
        // NO REQUIERE LOGICA ADICIONAL, SOLO CONTENIDO INFORMATIVO
        return "contact";
    }
}