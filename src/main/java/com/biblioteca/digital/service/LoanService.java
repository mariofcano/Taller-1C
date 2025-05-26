package com.biblioteca.digital.service;

import com.biblioteca.digital.model.Loan;
import com.biblioteca.digital.model.LoanStatus;
import com.biblioteca.digital.model.User;
import com.biblioteca.digital.model.Book;
import com.biblioteca.digital.repository.LoanRepository;
import com.biblioteca.digital.repository.UserRepository;
import com.biblioteca.digital.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO DE NEGOCIO PARA LA GESTION COMPLETA DEL SISTEMA DE PRESTAMOS
 *
 * ESTA CLASE IMPLEMENTA TODA LA LOGICA DE NEGOCIO RELACIONADA CON LA GESTION
 * DE PRESTAMOS EN EL SISTEMA DE BIBLIOTECA DIGITAL. PROPORCIONA OPERACIONES
 * SEGURAS Y VALIDADAS PARA EL PROCESAMIENTO DE PRESTAMOS, DEVOLUCIONES,
 * RENOVACIONES, CALCULO DE MULTAS Y CONTROL DE VENCIMIENTOS.
 *
 * EL SERVICIO MANEJA TODAS LAS REGLAS DE NEGOCIO CRITICAS DEL SISTEMA:
 * LIMITES DE PRESTAMOS, VALIDACIONES DE DISPONIBILIDAD, CALCULO AUTOMATICO
 * DE FECHAS DE VENCIMIENTO, PROCESAMIENTO DE MULTAS Y ESTADOS DE PRESTAMOS.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Loan
 * @see LoanStatus
 * @see User
 * @see Book
 * @see LoanRepository
 * @see org.springframework.stereotype.Service
 */
@Service
@Transactional
public class LoanService {

    /**
     * REPOSITORIO PARA ACCESO A DATOS DE PRESTAMOS
     *
     * INYECTO EL REPOSITORIO QUE PROPORCIONA TODAS LAS OPERACIONES
     * DE ACCESO A DATOS PARA LA ENTIDAD LOAN. UTILIZO SPRING DEPENDENCY
     * INJECTION PARA GESTIONAR LA DEPENDENCIA AUTOMATICAMENTE.
     */
    @Autowired
    private LoanRepository loanRepository;

    /**
     * REPOSITORIO PARA VALIDACIONES DE USUARIOS
     *
     * INYECTO EL REPOSITORIO DE USUARIOS PARA VALIDAR EXISTENCIA,
     * PERMISOS Y RESTRICCIONES DE USUARIOS EN OPERACIONES DE PRESTAMO.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * REPOSITORIO PARA VALIDACIONES DE LIBROS
     *
     * INYECTO EL REPOSITORIO DE LIBROS PARA VALIDAR DISPONIBILIDAD,
     * RESERVAR Y LIBERAR COPIAS EN OPERACIONES DE PRESTAMO.
     */
    @Autowired
    private BookRepository bookRepository;

    /**
     * SERVICIO PARA OPERACIONES DE LIBROS
     *
     * INYECTO EL SERVICIO DE LIBROS PARA UTILIZAR SUS OPERACIONES
     * DE RESERVA Y LIBERACION DE COPIAS DE FORMA COORDINADA.
     */
    @Autowired
    private BookService bookService;

    /**
     * LIMITE MAXIMO DE PRESTAMOS SIMULTANEOS POR USUARIO
     *
     * DEFINO EL NUMERO MAXIMO DE LIBROS QUE UN USUARIO PUEDE TENER
     * EN PRESTAMO AL MISMO TIEMPO. ESTE LIMITE PREVIENE ACAPARAMIENTO
     * Y GARANTIZA DISPONIBILIDAD PARA OTROS USUARIOS.
     */
    private static final int MAX_LOANS_PER_USER = 5;

    /**
     * DURACION ESTANDAR DE UN PRESTAMO EN DIAS
     *
     * ESTABLEZCO EL PERIODO PREDETERMINADO QUE UN USUARIO PUEDE
     * MANTENER UN LIBRO EN PRESTAMO ANTES DE DEVOLVERLO.
     */
    private static final int STANDARD_LOAN_DURATION_DAYS = 14;

    /**
     * LIMITE MAXIMO DE RENOVACIONES POR PRESTAMO
     *
     * DEFINO EL NUMERO MAXIMO DE VECES QUE UN PRESTAMO PUEDE SER
     * RENOVADO PARA EVITAR ACAPARAMIENTO INDEFINIDO DE LIBROS.
     */
    private static final int MAX_RENEWALS_PER_LOAN = 3;

    /**
     * TARIFA DIARIA POR RETRASO EN EUROS
     *
     * ESTABLEZCO EL MONTO QUE SE COBRA POR CADA DIA DE RETRASO
     * EN LA DEVOLUCION DE UN LIBRO DESPUES DE LA FECHA LIMITE.
     */
    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("0.50");

    /**
     * PROCESA UN NUEVO PRESTAMO DE LIBRO
     *
     * OPERACION COMPLETA QUE INCLUYE VALIDACION DE USUARIO, VERIFICACION
     * DE DISPONIBILIDAD DEL LIBRO, APLICACION DE LIMITES DE PRESTAMO,
     * RESERVA DE COPIA Y CREACION DEL REGISTRO DE PRESTAMO CON FECHAS
     * AUTOMATICAMENTE CALCULADAS.
     *
     * @param userId ID DEL USUARIO QUE SOLICITA EL PRESTAMO
     * @param bookId ID DEL LIBRO A PRESTAR
     * @return PRESTAMO CREADO CON TODOS LOS CAMPOS CONFIGURADOS
     * @throws RuntimeException SI NO SE PUEDE REALIZAR EL PRESTAMO
     */
    public Loan createLoan(Long userId, Long bookId) {
        // VALIDO PARAMETROS DE ENTRADA
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }
        if (bookId == null) {
            throw new IllegalArgumentException("EL ID DEL LIBRO ES REQUERIDO");
        }

        // BUSCO Y VALIDO EL USUARIO
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        User user = userOptional.get();

        // VERIFICO QUE EL USUARIO ESTE ACTIVO
        if (!user.getActive()) {
            throw new RuntimeException("EL USUARIO ESTA DESACTIVADO Y NO PUEDE REALIZAR PRESTAMOS");
        }

        // BUSCO Y VALIDO EL LIBRO
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        Book book = bookOptional.get();

        // VERIFICO QUE EL LIBRO ESTE DISPONIBLE PARA PRESTAMO
        if (!book.isAvailableForLoan()) {
            throw new RuntimeException("EL LIBRO NO ESTA DISPONIBLE PARA PRESTAMO");
        }

        // VERIFICO QUE EL USUARIO NO EXCEDA EL LIMITE DE PRESTAMOS
        long currentLoans = loanRepository.countActiveByUser(user);
        if (currentLoans >= MAX_LOANS_PER_USER) {
            throw new RuntimeException("EL USUARIO HA ALCANZADO EL LIMITE MAXIMO DE " +
                    MAX_LOANS_PER_USER + " PRESTAMOS SIMULTANEOS");
        }

        // VERIFICO QUE EL USUARIO NO TENGA MULTAS PENDIENTES
        List<Loan> unpaidFines = loanRepository.findLoansWithUnpaidFines();
        boolean hasUnpaidFines = unpaidFines.stream()
                .anyMatch(loan -> loan.getUser().getId().equals(userId));

        if (hasUnpaidFines) {
            throw new RuntimeException("EL USUARIO TIENE MULTAS PENDIENTES DE PAGO");
        }

        // VERIFICO QUE EL USUARIO NO TENGA YA ESTE LIBRO EN PRESTAMO
        List<Loan> userActiveLoans = loanRepository.findActiveByUser(user);
        boolean alreadyHasBook = userActiveLoans.stream()
                .anyMatch(loan -> loan.getBook().getId().equals(bookId));

        if (alreadyHasBook) {
            throw new RuntimeException("EL USUARIO YA TIENE ESTE LIBRO EN PRESTAMO");
        }

        // RESERVO UNA COPIA DEL LIBRO
        if (!bookService.reserveBookCopy(bookId)) {
            throw new RuntimeException("NO SE PUDO RESERVAR UNA COPIA DEL LIBRO");
        }

        try {
            // CREO EL PRESTAMO CON FECHAS CALCULADAS
            LocalDate loanDate = LocalDate.now();
            LocalDate dueDate = loanDate.plusDays(STANDARD_LOAN_DURATION_DAYS);

            Loan newLoan = new Loan(user, book, loanDate, dueDate, null);
            newLoan.setStatus(LoanStatus.ACTIVE);
            newLoan.setRenewals(0);
            newLoan.setFineAmount(BigDecimal.ZERO);
            newLoan.setFinePaid(true);

            // GUARDO EL PRESTAMO
            Loan savedLoan = loanRepository.save(newLoan);

            // INCREMENTO EL CONTADOR DE PRESTAMOS DEL LIBRO
            bookService.incrementLoanCount(bookId);

            return savedLoan;

        } catch (Exception e) {
            // SI FALLA LA CREACION, LIBERO LA COPIA RESERVADA
            bookService.releaseBookCopy(bookId);
            throw new RuntimeException("ERROR AL CREAR EL PRESTAMO: " + e.getMessage());
        }
    }

    /**
     * PROCESA LA DEVOLUCION DE UN LIBRO
     *
     * OPERACION COMPLETA QUE INCLUYE VALIDACION DEL PRESTAMO,
     * CALCULO DE MULTAS POR RETRASO, ACTUALIZACION DE ESTADO,
     * LIBERACION DE COPIA Y REGISTRO DE FECHA DE DEVOLUCION.
     *
     * @param loanId ID DEL PRESTAMO A DEVOLVER
     * @return PRESTAMO ACTUALIZADO CON INFORMACION DE DEVOLUCION
     * @throws RuntimeException SI NO SE PUEDE PROCESAR LA DEVOLUCION
     */
    public Loan processReturn(Long loanId) {
        // VALIDO PARAMETRO DE ENTRADA
        if (loanId == null) {
            throw new IllegalArgumentException("EL ID DEL PRESTAMO ES REQUERIDO");
        }

        // BUSCO EL PRESTAMO
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new RuntimeException("PRESTAMO NO ENCONTRADO CON ID: " + loanId);
        }

        Loan loan = loanOptional.get();

        // VERIFICO QUE EL PRESTAMO ESTE ACTIVO
        if (loan.getReturnedAt() != null) {
            throw new RuntimeException("EL PRESTAMO YA HA SIDO DEVUELTO");
        }

        // PROCESO LA DEVOLUCION CON FECHA ACTUAL
        LocalDateTime returnDate = LocalDateTime.now();
        loan.processReturn(returnDate);

        // LIBERO LA COPIA DEL LIBRO
        if (!bookService.releaseBookCopy(loan.getBook().getId())) {
            throw new RuntimeException("ERROR AL LIBERAR LA COPIA DEL LIBRO");
        }

        // GUARDO LOS CAMBIOS
        Loan updatedLoan = loanRepository.save(loan);

        return updatedLoan;
    }

    /**
     * RENUEVA UN PRESTAMO EXTENDIENDO SU FECHA DE VENCIMIENTO
     *
     * OPERACION QUE PERMITE EXTENDER EL PERIODO DE PRESTAMO CUANDO
     * EL USUARIO LO SOLICITA ANTES DEL VENCIMIENTO. APLICA TODAS
     * LAS VALIDACIONES Y LIMITES ESTABLECIDOS PARA RENOVACIONES.
     *
     * @param loanId ID DEL PRESTAMO A RENOVAR
     * @return PRESTAMO RENOVADO CON NUEVA FECHA DE VENCIMIENTO
     * @throws RuntimeException SI NO SE PUEDE RENOVAR EL PRESTAMO
     */
    public Loan renewLoan(Long loanId) {
        // VALIDO PARAMETRO DE ENTRADA
        if (loanId == null) {
            throw new IllegalArgumentException("EL ID DEL PRESTAMO ES REQUERIDO");
        }

        // BUSCO EL PRESTAMO
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new RuntimeException("PRESTAMO NO ENCONTRADO CON ID: " + loanId);
        }

        Loan loan = loanOptional.get();

        // VERIFICO QUE EL PRESTAMO PUEDA SER RENOVADO
        if (!loan.canBeRenewed()) {
            throw new RuntimeException("EL PRESTAMO NO PUEDE SER RENOVADO");
        }

        // VERIFICO EL LIMITE DE RENOVACIONES
        if (loan.getRenewals() >= MAX_RENEWALS_PER_LOAN) {
            throw new RuntimeException("SE HA ALCANZADO EL LIMITE MAXIMO DE " +
                    MAX_RENEWALS_PER_LOAN + " RENOVACIONES");
        }

        // VERIFICO QUE NO HAYA RESERVAS PENDIENTES DEL LIBRO
        // (EN UNA IMPLEMENTACION REAL, VERIFICARIA SI OTROS USUARIOS
        // TIENEN RESERVAS PENDIENTES DE ESTE LIBRO)

        // RENUEVO EL PRESTAMO
        if (loan.renew()) {
            loan.setStatus(LoanStatus.RENEWED);
            Loan updatedLoan = loanRepository.save(loan);
            return updatedLoan;
        } else {
            throw new RuntimeException("NO SE PUDO RENOVAR EL PRESTAMO");
        }
    }

    /**
     * BUSCA UN PRESTAMO POR SU ID
     *
     * LOCALIZO UN PRESTAMO ESPECIFICO MEDIANTE SU IDENTIFICADOR UNICO.
     * METODO FUNDAMENTAL PARA OPERACIONES QUE REQUIEREN ACCESO
     * A DATOS COMPLETOS DE UN PRESTAMO.
     *
     * @param id IDENTIFICADOR UNICO DEL PRESTAMO
     * @return OPTIONAL CON EL PRESTAMO ENCONTRADO O VACIO SI NO EXISTE
     */
    @Transactional(readOnly = true)
    public Optional<Loan> findLoanById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("EL ID DEL PRESTAMO NO PUEDE SER NULO");
        }

        return loanRepository.findById(id);
    }

    /**
     * OBTIENE TODOS LOS PRESTAMOS ACTIVOS DE UN USUARIO
     *
     * RECUPERO LA LISTA DE PRESTAMOS QUE UN USUARIO TIENE ACTUALMENTE
     * SIN DEVOLVER. FUNDAMENTAL PARA MOSTRAR EL ESTADO ACTUAL
     * DE CADA USUARIO EN LA INTERFAZ.
     *
     * @param userId ID DEL USUARIO A CONSULTAR
     * @return LISTA DE PRESTAMOS ACTIVOS DEL USUARIO
     */
    @Transactional(readOnly = true)
    public List<Loan> findActiveLoansForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        return loanRepository.findActiveByUser(userOptional.get());
    }

    /**
     * OBTIENE EL HISTORIAL COMPLETO DE PRESTAMOS DE UN USUARIO
     *
     * RECUPERO TODOS LOS PRESTAMOS REALIZADOS POR UN USUARIO ESPECIFICO,
     * INCLUYENDO ACTIVOS, DEVUELTOS Y VENCIDOS. UTIL PARA MOSTRAR
     * EL HISTORIAL COMPLETO EN PERFILES DE USUARIO.
     *
     * @param userId ID DEL USUARIO A CONSULTAR
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON EL HISTORIAL DE PRESTAMOS
     */
    @Transactional(readOnly = true)
    public Page<Loan> findLoanHistoryForUser(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        return loanRepository.findByUser(userOptional.get(), pageable);
    }

    /**
     * OBTIENE TODOS LOS PRESTAMOS VENCIDOS DEL SISTEMA
     *
     * IDENTIFICO PRESTAMOS QUE HAN SUPERADO SU FECHA DE VENCIMIENTO
     * Y NO HAN SIDO DEVUELTOS. CRITICO PARA PROCESOS DE NOTIFICACION
     * Y APLICACION DE MULTAS.
     *
     * @return LISTA DE PRESTAMOS VENCIDOS
     */
    @Transactional(readOnly = true)
    public List<Loan> findOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }

    /**
     * OBTIENE PRESTAMOS QUE VENCEN HOY
     *
     * LOCALIZO PRESTAMOS CUYA FECHA DE VENCIMIENTO ES LA FECHA ACTUAL.
     * FUNDAMENTAL PARA SISTEMAS DE NOTIFICACION Y ALERTAS
     * DE VENCIMIENTOS INMINENTES.
     *
     * @return LISTA DE PRESTAMOS QUE VENCEN HOY
     */
    @Transactional(readOnly = true)
    public List<Loan> findLoansDueToday() {
        return loanRepository.findLoansDueToday();
    }

    /**
     * OBTIENE PRESTAMOS QUE VENCEN EN LOS PROXIMOS DIAS
     *
     * IDENTIFICO PRESTAMOS QUE VENCERAN DENTRO DE UN NUMERO
     * ESPECIFICADO DE DIAS. UTIL PARA RECORDATORIOS PREVENTIVOS
     * Y NOTIFICACIONES ANTICIPADAS.
     *
     * @param days NUMERO DE DIAS DE ANTICIPACION
     * @return LISTA DE PRESTAMOS QUE VENCEN PRONTO
     */
    @Transactional(readOnly = true)
    public List<Loan> findLoansDueSoon(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("EL NUMERO DE DIAS DEBE SER MAYOR A 0");
        }

        return loanRepository.findLoansDueSoon(days);
    }

    /**
     * OBTIENE PRESTAMOS CON MULTAS PENDIENTES DE PAGO
     *
     * IDENTIFICO PRESTAMOS QUE TIENEN MULTAS APLICADAS PERO NO PAGADAS.
     * CRITICO PARA CONTROL FINANCIERO Y GESTION DE COBRANZAS.
     *
     * @return LISTA DE PRESTAMOS CON MULTAS PENDIENTES
     */
    @Transactional(readOnly = true)
    public List<Loan> findLoansWithUnpaidFines() {
        return loanRepository.findLoansWithUnpaidFines();
    }

    /**
     * CALCULA EL MONTO TOTAL DE MULTAS PENDIENTES
     *
     * SUMO TODAS LAS MULTAS NO PAGADAS EN EL SISTEMA.
     * METRICA FINANCIERA IMPORTANTE PARA REPORTES
     * Y CONTROL DE INGRESOS PENDIENTES.
     *
     * @return MONTO TOTAL DE MULTAS PENDIENTES
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalUnpaidFines() {
        BigDecimal totalFines = loanRepository.calculateTotalUnpaidFines();
        return totalFines != null ? totalFines : BigDecimal.ZERO;
    }

    /**
     * PROCESA EL PAGO DE UNA MULTA
     *
     * OPERACION QUE MARCA UNA MULTA COMO PAGADA Y ACTUALIZA
     * EL ESTADO DEL PRESTAMO CORRESPONDIENTE. PERMITE AL USUARIO
     * VOLVER A REALIZAR PRESTAMOS SI ESTABA BLOQUEADO.
     *
     * @param loanId ID DEL PRESTAMO CON MULTA A PAGAR
     * @return PRESTAMO CON LA MULTA MARCADA COMO PAGADA
     * @throws RuntimeException SI EL PRESTAMO NO EXISTE O NO TIENE MULTAS
     */
    public Loan payFine(Long loanId) {
        // VALIDO PARAMETRO DE ENTRADA
        if (loanId == null) {
            throw new IllegalArgumentException("EL ID DEL PRESTAMO ES REQUERIDO");
        }

        // BUSCO EL PRESTAMO
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new RuntimeException("PRESTAMO NO ENCONTRADO CON ID: " + loanId);
        }

        Loan loan = loanOptional.get();

        // VERIFICO QUE TENGA MULTAS PENDIENTES
        if (!loan.hasUnpaidFines()) {
            throw new RuntimeException("EL PRESTAMO NO TIENE MULTAS PENDIENTES");
        }

        // MARCO LA MULTA COMO PAGADA
        loan.payFine();

        // GUARDO LOS CAMBIOS
        return loanRepository.save(loan);
    }

    /**
     * ACTUALIZA AUTOMATICAMENTE EL ESTADO DE PRESTAMOS VENCIDOS
     *
     * PROCESO BATCH QUE SE EJECUTA PERIODICAMENTE PARA IDENTIFICAR
     * PRESTAMOS VENCIDOS, CALCULAR MULTAS Y ACTUALIZAR ESTADOS.
     * ESENCIAL PARA MANTENIMIENTO AUTOMATICO DEL SISTEMA.
     *
     * @return NUMERO DE PRESTAMOS ACTUALIZADOS
     */
    public int updateOverdueLoans() {
        // BUSCO TODOS LOS PRESTAMOS ACTIVOS
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        int updatedCount = 0;

        for (Loan loan : activeLoans) {
            // VERIFICO SI EL PRESTAMO ESTA VENCIDO
            if (loan.isOverdue()) {
                // ACTUALIZO EL ESTADO Y CALCULO LA MULTA
                loan.updateStatus();
                loanRepository.save(loan);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    /**
     * VERIFICA SI UN USUARIO PUEDE REALIZAR UN NUEVO PRESTAMO
     *
     * COMPRUEBO SI UN USUARIO CUMPLE TODAS LAS CONDICIONES PARA
     * REALIZAR UN NUEVO PRESTAMO: LIMITE DE PRESTAMOS, MULTAS PENDIENTES,
     * ESTADO ACTIVO, ETC.
     *
     * @param userId ID DEL USUARIO A VALIDAR
     * @return TRUE SI PUEDE REALIZAR PRESTAMO, FALSE EN CASO CONTRARIO
     */
    @Transactional(readOnly = true)
    public boolean canUserBorrow(Long userId) {
        if (userId == null) {
            return false;
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return false;
        }

        User user = userOptional.get();

        // VERIFICO QUE EL USUARIO ESTE ACTIVO
        if (!user.getActive()) {
            return false;
        }

        // UTILIZO EL METODO DEL REPOSITORIO PARA VALIDACION COMPLETA
        return loanRepository.canUserBorrow(user, MAX_LOANS_PER_USER);
    }

    /**
     * OBTIENE ESTADISTICAS DE PRESTAMOS POR ESTADO
     *
     * GENERO ESTADISTICAS AGRUPADAS POR ESTADO PARA REPORTES
     * Y ANALISIS DEL SISTEMA DE PRESTAMOS.
     *
     * @return LISTA CON ESTADISTICAS [ESTADO, CANTIDAD]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getLoanStatisticsByStatus() {
        return loanRepository.getLoanStatsByStatus();
    }

    /**
     * OBTIENE ESTADISTICAS DE PRESTAMOS POR MES
     *
     * GENERO ESTADISTICAS MENSUALES DE PRESTAMOS PARA ANALISIS
     * DE TENDENCIAS TEMPORALES Y PLANIFICACION DE RECURSOS.
     *
     * @return LISTA CON ESTADISTICAS [AÑO, MES, CANTIDAD]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getLoanStatisticsByMonth() {
        return loanRepository.getLoanStatsByMonth();
    }

    /**
     * OBTIENE LOS USUARIOS MAS ACTIVOS DEL SISTEMA
     *
     * IDENTIFICO LOS USUARIOS QUE MAS PRESTAMOS HAN REALIZADO
     * BASANDOME EN EL HISTORIAL COMPLETO. UTIL PARA PROGRAMAS
     * DE FIDELIZACION Y ANALISIS DE USO.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON USUARIOS MAS ACTIVOS
     */
    @Transactional(readOnly = true)
    public Page<Object[]> findMostActiveUsers(Pageable pageable) {
        return loanRepository.findMostActiveUsers(pageable);
    }

    /**
     * OBTIENE LOS LIBROS MAS PRESTADOS DEL SISTEMA
     *
     * IDENTIFICO LOS LIBROS MAS POPULARES BASANDOME EN EL NUMERO
     * TOTAL DE PRESTAMOS. FUNDAMENTAL PARA ANALISIS DE DEMANDA
     * Y PLANIFICACION DE ADQUISICIONES.
     *
     * @param pageable CONFIGURACION DE PAGINACION
     * @return PAGINA CON LIBROS MAS PRESTADOS
     */
    @Transactional(readOnly = true)
    public Page<Object[]> findMostBorrowedBooks(Pageable pageable) {
        return loanRepository.findMostBorrowedBooks(pageable);
    }

    /**
     * BUSCA PRESTAMOS EN UN RANGO DE FECHAS
     *
     * LOCALIZO PRESTAMOS REALIZADOS DENTRO DE UN PERIODO ESPECIFICO.
     * FUNDAMENTAL PARA ANALISIS DE TENDENCIAS Y GENERACION
     * DE REPORTES PERIODICOS.
     *
     * @param startDate FECHA INICIAL DEL RANGO
     * @param endDate FECHA FINAL DEL RANGO
     * @return LISTA DE PRESTAMOS EN EL PERIODO
     */
    @Transactional(readOnly = true)
    public List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("LAS FECHAS DE INICIO Y FIN SON REQUERIDAS");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("LA FECHA DE INICIO NO PUEDE SER POSTERIOR A LA FECHA FIN");
        }

        return loanRepository.findByLoanDateBetween(startDate, endDate);
    }

    /**
     * BUSCA PRESTAMOS DE UN USUARIO EN UN PERIODO
     *
     * LOCALIZO PRESTAMOS DE UN USUARIO ESPECIFICO DENTRO DE UN RANGO
     * DE FECHAS. UTIL PARA GENERAR REPORTES PERSONALIZADOS
     * Y ANALISIS DE ACTIVIDAD INDIVIDUAL.
     *
     * @param userId ID DEL USUARIO A CONSULTAR
     * @param startDate FECHA INICIAL DEL PERIODO
     * @param endDate FECHA FINAL DEL PERIODO
     * @return LISTA DE PRESTAMOS DEL USUARIO EN EL PERIODO
     */
    @Transactional(readOnly = true)
    public List<Loan> findUserLoansInPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("LAS FECHAS DE INICIO Y FIN SON REQUERIDAS");
        }

        return loanRepository.findByUserAndPeriod(userOptional.get(), startDate, endDate);
    }

    /**
     * CUENTA PRESTAMOS ACTIVOS EN EL SISTEMA
     *
     * CALCULO EL NUMERO TOTAL DE PRESTAMOS QUE ACTUALMENTE ESTAN ACTIVOS.
     * INDICADOR CLAVE DEL NIVEL DE ACTIVIDAD ACTUAL DEL SISTEMA.
     *
     * @return NUMERO DE PRESTAMOS ACTIVOS
     */
    @Transactional(readOnly = true)
    public long countActiveLoans() {
        return loanRepository.countActiveLoans();
    }

    /**
     * CUENTA PRESTAMOS TOTALES DEL SISTEMA
     *
     * CALCULO EL NUMERO TOTAL DE PRESTAMOS REGISTRADOS EN EL SISTEMA.
     * METRICA FUNDAMENTAL PARA ESTADISTICAS GENERALES.
     *
     * @return NUMERO TOTAL DE PRESTAMOS
     */
    @Transactional(readOnly = true)
    public long countTotalLoans() {
        return loanRepository.countTotalLoans();
    }

    /**
     * OBTIENE PRESTAMOS VENCIDOS DE UN USUARIO ESPECIFICO
     *
     * LOCALIZO PRESTAMOS VENCIDOS DE UN USUARIO DETERMINADO.
     * IMPORTANTE PARA GESTION PERSONALIZADA DE MOROSOS
     * Y APLICACION DE RESTRICCIONES INDIVIDUALES.
     *
     * @param userId ID DEL USUARIO A CONSULTAR
     * @return LISTA DE PRESTAMOS VENCIDOS DEL USUARIO
     */
    @Transactional(readOnly = true)
    public List<Loan> findOverdueLoansForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        return loanRepository.findOverdueByUser(userOptional.get());
    }

    /**
     * BUSCA EL PRESTAMO MAS RECIENTE DE UN USUARIO
     *
     * OBTENGO EL ULTIMO PRESTAMO REALIZADO POR UN USUARIO ESPECIFICO.
     * UTIL PARA VALIDACIONES DE HISTORIAL Y CONTROL DE FRECUENCIA
     * DE PRESTAMOS.
     *
     * @param userId ID DEL USUARIO A CONSULTAR
     * @return OPTIONAL CON EL PRESTAMO MAS RECIENTE
     */
    @Transactional(readOnly = true)
    public Optional<Loan> findMostRecentLoanForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        return loanRepository.findMostRecentByUser(userOptional.get());
    }

    /**
     * BUSCA EL ULTIMO PRESTAMO DE UN LIBRO
     *
     * LOCALIZO EL PRESTAMO MAS RECIENTE DE UN LIBRO ESPECIFICO.
     * IMPORTANTE PARA RASTREAR EL HISTORIAL DE CIRCULACION
     * Y ESTADO DE CADA EJEMPLAR.
     *
     * @param bookId ID DEL LIBRO A CONSULTAR
     * @return OPTIONAL CON EL PRESTAMO MAS RECIENTE DEL LIBRO
     */
    @Transactional(readOnly = true)
    public Optional<Loan> findMostRecentLoanForBook(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("EL ID DEL LIBRO ES REQUERIDO");
        }

        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (!bookOptional.isPresent()) {
            throw new RuntimeException("LIBRO NO ENCONTRADO CON ID: " + bookId);
        }

        return loanRepository.findMostRecentByBook(bookOptional.get());
    }

    /**
     * OBTIENE PRESTAMOS CON OBSERVACIONES ESPECIALES
     *
     * LOCALIZO PRESTAMOS QUE TIENEN NOTAS O COMENTARIOS REGISTRADOS.
     * IMPORTANTE PARA SEGUIMIENTO DE CASOS ESPECIALES
     * Y SITUACIONES QUE REQUIEREN ATENCION PARTICULAR.
     *
     * @return LISTA DE PRESTAMOS CON OBSERVACIONES
     */
    @Transactional(readOnly = true)
    public List<Loan> findLoansWithNotes() {
        return loanRepository.findLoansWithNotes();
    }

    /**
     * BUSCA PRESTAMOS RENOVADOS EN EL SISTEMA
     *
     * LOCALIZO PRESTAMOS QUE HAN SIDO RENOVADOS AL MENOS UNA VEZ.
     * UTIL PARA ANALIZAR PATRONES DE USO Y DEMANDA
     * DE EXTENSIONES DE PRESTAMO.
     *
     * @return LISTA DE PRESTAMOS RENOVADOS
     */
    @Transactional(readOnly = true)
    public List<Loan> findRenewedLoans() {
        return loanRepository.findRenewedLoans();
    }

    /**
     * BUSCA PRESTAMOS CON MULTIPLES RENOVACIONES
     *
     * IDENTIFICO PRESTAMOS QUE HAN SIDO RENOVADOS VARIAS VECES.
     * IMPORTANTE PARA DETECTAR PATRONES DE USO EXCESIVO
     * Y POSIBLES PROBLEMAS DE DISPONIBILIDAD.
     *
     * @param minRenewals NUMERO MINIMO DE RENOVACIONES
     * @return LISTA DE PRESTAMOS CON MULTIPLES RENOVACIONES
     */
    @Transactional(readOnly = true)
    public List<Loan> findLoansWithMultipleRenewals(int minRenewals) {
        if (minRenewals < 1) {
            throw new IllegalArgumentException("EL NUMERO MINIMO DE RENOVACIONES DEBE SER MAYOR A 0");
        }

        return loanRepository.findLoansWithMultipleRenewals(minRenewals);
    }

    /**
     * CALCULA LA DURACION PROMEDIO DE PRESTAMOS
     *
     * OBTENGO EL PROMEDIO DE DIAS QUE LOS USUARIOS MANTIENEN
     * LOS LIBROS EN PRESTAMO. METRICA UTIL PARA PLANIFICACION
     * Y OPTIMIZACION DE POLITICAS DE PRESTAMO.
     *
     * @return DURACION PROMEDIO EN DIAS
     */
    @Transactional(readOnly = true)
    public Double calculateAverageLoanDuration() {
        Double avgDuration = loanRepository.calculateAverageLoanDuration();
        return avgDuration != null ? avgDuration : 0.0;
    }

    /**
     * MARCA UN PRESTAMO COMO PERDIDO
     *
     * OPERACION ADMINISTRATIVA QUE CAMBIA EL ESTADO DE UN PRESTAMO
     * A PERDIDO CUANDO EL USUARIO REPORTA LA PERDIDA DEL LIBRO
     * O NO LO DEVUELVE DESPUES DE MULTIPLES INTENTOS.
     *
     * @param loanId ID DEL PRESTAMO A MARCAR COMO PERDIDO
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return PRESTAMO CON ESTADO ACTUALIZADO
     */
    public Loan markLoanAsLost(Long loanId, Long adminUserId) {
        // VALIDO PARAMETROS
        if (loanId == null || adminUserId == null) {
            throw new IllegalArgumentException("LOS IDS SON REQUERIDOS");
        }

        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // BUSCO EL PRESTAMO
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new RuntimeException("PRESTAMO NO ENCONTRADO CON ID: " + loanId);
        }

        Loan loan = loanOptional.get();

        // VERIFICO QUE EL PRESTAMO ESTE ACTIVO
        if (!loan.isActive()) {
            throw new RuntimeException("SOLO SE PUEDEN MARCAR COMO PERDIDOS LOS PRESTAMOS ACTIVOS");
        }

        // CAMBIO EL ESTADO Y APLICO PENALIZACION
        loan.setStatus(LoanStatus.LOST);
        loan.setReturnedAt(LocalDateTime.now());

        // APLICO MULTA POR PERDIDA (PRECIO DEL LIBRO O VALOR FIJO)
        Book book = loan.getBook();
        BigDecimal lostBookFine = book.getPrice() != null ?
                book.getPrice() : new BigDecimal("20.00"); // VALOR FIJO SI NO HAY PRECIO

        loan.setFineAmount(lostBookFine);
        loan.setFinePaid(false);

        // NO LIBERO LA COPIA YA QUE EL LIBRO SE PERDIO
        // (LA COPIA SE DESCUENTA DEL INVENTARIO)

        return loanRepository.save(loan);
    }

    /**
     * MARCA UN PRESTAMO COMO DAÑADO
     *
     * OPERACION ADMINISTRATIVA QUE REGISTRA CUANDO UN LIBRO
     * ES DEVUELTO CON DAÑOS SIGNIFICATIVOS QUE AFECTAN
     * SU DISPONIBILIDAD PARA FUTUROS PRESTAMOS.
     *
     * @param loanId ID DEL PRESTAMO A MARCAR COMO DAÑADO
     * @param damageDescription DESCRIPCION DE LOS DAÑOS
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return PRESTAMO CON ESTADO ACTUALIZADO
     */
    public Loan markLoanAsDamaged(Long loanId, String damageDescription, Long adminUserId) {
        // VALIDO PARAMETROS
        if (loanId == null || adminUserId == null) {
            throw new IllegalArgumentException("LOS IDS SON REQUERIDOS");
        }

        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // BUSCO EL PRESTAMO
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new RuntimeException("PRESTAMO NO ENCONTRADO CON ID: " + loanId);
        }

        Loan loan = loanOptional.get();

        // VERIFICO QUE EL PRESTAMO ESTE ACTIVO
        if (!loan.isActive()) {
            throw new RuntimeException("SOLO SE PUEDEN MARCAR COMO DAÑADOS LOS PRESTAMOS ACTIVOS");
        }

        // PROCESO LA DEVOLUCION CON DAÑOS
        loan.setStatus(LoanStatus.DAMAGED);
        loan.setReturnedAt(LocalDateTime.now());

        // REGISTRO LA DESCRIPCION DE DAÑOS
        String notes = loan.getNotes() != null ?
                loan.getNotes() + " | DAÑOS: " + damageDescription :
                "DAÑOS: " + damageDescription;
        loan.setNotes(notes);

        // APLICO MULTA POR DAÑOS (PORCENTAJE DEL PRECIO O VALOR FIJO)
        Book book = loan.getBook();
        BigDecimal damageFine = book.getPrice() != null ?
                book.getPrice().multiply(new BigDecimal("0.5")) : // 50% DEL PRECIO
                new BigDecimal("10.00"); // VALOR FIJO SI NO HAY PRECIO

        loan.setFineAmount(damageFine);
        loan.setFinePaid(false);

        // LIBERO LA COPIA PERO EL LIBRO PODRIA QUEDAR INACTIVO
        bookService.releaseBookCopy(loan.getBook().getId());

        return loanRepository.save(loan);
    }

    /**
     * CANCELA UN PRESTAMO ACTIVO
     *
     * OPERACION ADMINISTRATIVA QUE PERMITE CANCELAR UN PRESTAMO
     * ANTES DE SU FINALIZACION NATURAL. SE USA EN CIRCUNSTANCIAS
     * ESPECIALES O ERRORES EN EL SISTEMA.
     *
     * @param loanId ID DEL PRESTAMO A CANCELAR
     * @param reason MOTIVO DE LA CANCELACION
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return PRESTAMO CANCELADO
     */
    public Loan cancelLoan(Long loanId, String reason, Long adminUserId) {
        // VALIDO PARAMETROS
        if (loanId == null || adminUserId == null) {
            throw new IllegalArgumentException("LOS IDS SON REQUERIDOS");
        }

        // VERIFICO PERMISOS DEL ADMINISTRADOR
        validateAdminPermissions(adminUserId);

        // BUSCO EL PRESTAMO
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new RuntimeException("PRESTAMO NO ENCONTRADO CON ID: " + loanId);
        }

        Loan loan = loanOptional.get();

        // VERIFICO QUE EL PRESTAMO ESTE ACTIVO
        if (!loan.isActive()) {
            throw new RuntimeException("SOLO SE PUEDEN CANCELAR PRESTAMOS ACTIVOS");
        }

        // CANCELO EL PRESTAMO
        loan.setStatus(LoanStatus.CANCELLED);
        loan.setReturnedAt(LocalDateTime.now());

        // REGISTRO EL MOTIVO DE CANCELACION
        String notes = loan.getNotes() != null ?
                loan.getNotes() + " | CANCELADO: " + reason :
                "CANCELADO: " + reason;
        loan.setNotes(notes);

        // LIBERO LA COPIA DEL LIBRO
        bookService.releaseBookCopy(loan.getBook().getId());

        return loanRepository.save(loan);
    }

    /**
     * ENVIA RECORDATORIOS DE VENCIMIENTO
     *
     * PROCESO QUE IDENTIFICA PRESTAMOS PROXIMOS A VENCER
     * Y PREPARA LA INFORMACION PARA ENVIO DE NOTIFICACIONES.
     * RETORNA LA LISTA DE PRESTAMOS QUE REQUIEREN RECORDATORIO.
     *
     * @param daysBeforeDue DIAS DE ANTICIPACION PARA EL RECORDATORIO
     * @return LISTA DE PRESTAMOS QUE REQUIEREN NOTIFICACION
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansForReminder(int daysBeforeDue) {
        if (daysBeforeDue < 1) {
            throw new IllegalArgumentException("LOS DIAS DE ANTICIPACION DEBEN SER MAYOR A 0");
        }

        return findLoansDueSoon(daysBeforeDue);
    }

    /**
     * GENERA REPORTE DE PRESTAMOS MOROSOS
     *
     * PROCESO QUE IDENTIFICA Y AGRUPA PRESTAMOS VENCIDOS
     * PARA GENERAR REPORTES DE COBRANZA Y SEGUIMIENTO.
     * INCLUYE CALCULO DE MULTAS Y DIAS DE RETRASO.
     *
     * @return LISTA DE PRESTAMOS VENCIDOS CON INFORMACION DETALLADA
     */
    @Transactional(readOnly = true)
    public List<Loan> generateOverdueReport() {
        List<Loan> overdueLoans = findOverdueLoans();

        // ACTUALIZO LOS CALCULOS DE MULTAS PARA EL REPORTE
        for (Loan loan : overdueLoans) {
            if (loan.getFineAmount().compareTo(BigDecimal.ZERO) == 0) {
                // CALCULO LA MULTA ACTUALIZADA SI NO ESTA ESTABLECIDA
                BigDecimal calculatedFine = loan.calculateFine();
                // NOTA: EN UN REPORTE SOLO CALCULAMOS, NO ACTUALIZAMOS LA BD
            }
        }

        return overdueLoans;
    }

    /**
     * VALIDA PERMISOS ADMINISTRATIVOS DEL USUARIO
     *
     * VERIFICO QUE EL USUARIO TENGA PERMISOS PARA REALIZAR OPERACIONES
     * ADMINISTRATIVAS EN EL SISTEMA DE PRESTAMOS. SOLO ADMINISTRADORES
     * Y BIBLIOTECARIOS PUEDEN REALIZAR CIERTAS OPERACIONES.
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
        if (!admin.getRole().hasAdminPrivileges()) {
            throw new RuntimeException("NO TIENE PERMISOS PARA REALIZAR OPERACIONES ADMINISTRATIVAS");
        }

        if (!admin.getActive()) {
            throw new RuntimeException("LA CUENTA DEL ADMINISTRADOR ESTA DESACTIVADA");
        }
    }

    /**
     * OBTIENE CONFIGURACION ACTUAL DEL SISTEMA DE PRESTAMOS
     *
     * METODO QUE RETORNA LOS PARAMETROS CONFIGURADOS DEL SISTEMA
     * COMO LIMITES, DURACIONES Y TARIFAS PARA MOSTRAR EN INTERFACES
     * ADMINISTRATIVAS O PARA CONSULTA DE USUARIOS.
     *
     * @return MAPA CON CONFIGURACION DEL SISTEMA
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getSystemConfiguration() {
        java.util.Map<String, Object> config = new java.util.HashMap<>();

        config.put("MAX_LOANS_PER_USER", MAX_LOANS_PER_USER);
        config.put("STANDARD_LOAN_DURATION_DAYS", STANDARD_LOAN_DURATION_DAYS);
        config.put("MAX_RENEWALS_PER_LOAN", MAX_RENEWALS_PER_LOAN);
        config.put("DAILY_FINE_RATE", DAILY_FINE_RATE);

        return config;
    }
}