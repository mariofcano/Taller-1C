package com.biblioteca.digital.repository;

import com.biblioteca.digital.model.Loan;
import com.biblioteca.digital.model.LoanStatus;
import com.biblioteca.digital.model.User;
import com.biblioteca.digital.model.Book;
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
 * REPOSITORIO JPA PARA LA GESTION DE ENTIDADES LOAN
 *
 * ESTA INTERFAZ PROPORCIONA OPERACIONES CRUD ESPECIALIZADAS Y CONSULTAS AVANZADAS
 * PARA LA GESTION COMPLETA DEL SISTEMA DE PRESTAMOS DE LA BIBLIOTECA DIGITAL.
 * INCLUYE METODOS OPTIMIZADOS PARA CONTROL DE VENCIMIENTOS, CALCULO DE MULTAS,
 * ESTADISTICAS DE PRESTAMOS Y SEGUIMIENTO DE DISPONIBILIDAD.
 *
 * LAS CONSULTAS ESTAN DISEÑADAS PARA SOPORTAR TODAS LAS OPERACIONES CRITICAS
 * DEL SISTEMA DE PRESTAMOS, DESDE LA GESTION DIARIA HASTA REPORTES GERENCIALES.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see Loan
 * @see LoanStatus
 * @see User
 * @see Book
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * BUSCA PRESTAMOS POR USUARIO ESPECIFICO
     */
    List<Loan> findByUser(User user);

    /**
     * BUSCA PRESTAMOS POR LIBRO ESPECIFICO
     */
    List<Loan> findByBook(Book book);

    /**
     * BUSCA PRESTAMOS POR ESTADO ESPECIFICO
     */
    List<Loan> findByStatus(LoanStatus status);

    /**
     * BUSCA PRESTAMOS ACTIVOS DE UN USUARIO
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL")
    List<Loan> findActiveByUser(@Param("user") User user);

    /**
     * CUENTA PRESTAMOS ACTIVOS DE UN USUARIO
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL")
    long countActiveByUser(@Param("user") User user);

    /**
     * BUSCA PRESTAMOS VENCIDOS EN EL SISTEMA
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    /**
     * BUSCA PRESTAMOS QUE VENCEN HOY
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.dueDate = CURRENT_DATE")
    List<Loan> findLoansDueToday();

    /**
     * BUSCA PRESTAMOS QUE VENCEN EN LOS PROXIMOS DIAS - VERSION CORREGIDA
     *
     * CORRIJO LA CONSULTA PARA SER COMPATIBLE CON H2 DATABASE
     * USANDO DATEADD EN LUGAR DE DATE_ADD PARA SUMAR DIAS.
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.dueDate BETWEEN CURRENT_DATE AND DATEADD('DAY', :days, CURRENT_DATE)")
    List<Loan> findLoansDueSoon(@Param("days") int days);

    /**
     * BUSCA PRESTAMOS DEVUELTOS EN UN RANGO DE FECHAS
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt BETWEEN :startDate AND :endDate")
    List<Loan> findReturnedBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * BUSCA PRESTAMOS REALIZADOS EN UN RANGO DE FECHAS
     */
    List<Loan> findByLoanDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * BUSCA PRESTAMOS CON MULTAS PENDIENTES DE PAGO
     */
    @Query("SELECT l FROM Loan l WHERE l.fineAmount > 0 AND l.finePaid = false")
    List<Loan> findLoansWithUnpaidFines();

    /**
     * CALCULA EL MONTO TOTAL DE MULTAS PENDIENTES
     */
    @Query("SELECT SUM(l.fineAmount) FROM Loan l WHERE l.fineAmount > 0 AND l.finePaid = false")
    BigDecimal calculateTotalUnpaidFines();

    /**
     * BUSCA PRESTAMOS POR USUARIO CON PAGINACION
     */
    Page<Loan> findByUser(User user, Pageable pageable);

    /**
     * BUSCA PRESTAMOS RENOVADOS
     */
    @Query("SELECT l FROM Loan l WHERE l.renewals > 0")
    List<Loan> findRenewedLoans();

    /**
     * BUSCA PRESTAMOS CON MULTIPLES RENOVACIONES
     */
    @Query("SELECT l FROM Loan l WHERE l.renewals >= :minRenewals")
    List<Loan> findLoansWithMultipleRenewals(@Param("minRenewals") int minRenewals);

    /**
     * BUSCA PRESTAMOS DE UN LIBRO EN UN PERIODO
     */
    @Query("SELECT l FROM Loan l WHERE l.book = :book AND l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findByBookAndPeriod(@Param("book") Book book,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * BUSCA PRESTAMOS ACTIVOS POR LIBRO
     */
    @Query("SELECT l FROM Loan l WHERE l.book = :book AND l.returnedAt IS NULL")
    List<Loan> findActiveByBook(@Param("book") Book book);

    /**
     * CUENTA PRESTAMOS ACTIVOS POR LIBRO
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.book = :book AND l.returnedAt IS NULL")
    long countActiveByBook(@Param("book") Book book);

    /**
     * BUSCA PRESTAMOS POR ESTADO CON PAGINACION
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * BUSCA PRESTAMOS DEVUELTOS TARDE - VERSION SIMPLIFICADA
     */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NOT NULL AND l.status = 'RETURNED_LATE'")
    List<Loan> findLateReturns();

    /**
     * CALCULA LA DURACION PROMEDIO DE PRESTAMOS - VERSION SIMPLIFICADA
     */
    @Query("SELECT AVG(CAST(l.loanDate AS double)) FROM Loan l WHERE l.returnedAt IS NOT NULL")
    Double calculateAverageLoanDuration();

    /**
     * BUSCA USUARIOS CON MAS PRESTAMOS REALIZADOS
     */
    @Query("SELECT l.user, COUNT(l) as loanCount FROM Loan l " +
            "GROUP BY l.user ORDER BY loanCount DESC")
    Page<Object[]> findMostActiveUsers(Pageable pageable);

    /**
     * BUSCA LIBROS MAS PRESTADOS
     */
    @Query("SELECT l.book, COUNT(l) as loanCount FROM Loan l " +
            "GROUP BY l.book ORDER BY loanCount DESC")
    Page<Object[]> findMostBorrowedBooks(Pageable pageable);

    /**
     * OBTIENE ESTADISTICAS DE PRESTAMOS POR ESTADO
     */
    @Query("SELECT l.status, COUNT(l) FROM Loan l GROUP BY l.status")
    List<Object[]> getLoanStatsByStatus();

    /**
     * OBTIENE ESTADISTICAS DE PRESTAMOS POR MES - VERSION SIMPLIFICADA
     */
    @Query("SELECT YEAR(l.loanDate), MONTH(l.loanDate), COUNT(l) FROM Loan l " +
            "GROUP BY YEAR(l.loanDate), MONTH(l.loanDate) ORDER BY YEAR(l.loanDate), MONTH(l.loanDate)")
    List<Object[]> getLoanStatsByMonth();

    /**
     * BUSCA PRESTAMOS DE USUARIO EN UN RANGO DE FECHAS
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.loanDate BETWEEN :startDate AND :endDate")
    List<Loan> findByUserAndPeriod(@Param("user") User user,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * BUSCA PRESTAMOS CON OBSERVACIONES ESPECIALES
     */
    @Query("SELECT l FROM Loan l WHERE l.notes IS NOT NULL AND l.notes != ''")
    List<Loan> findLoansWithNotes();

    /**
     * ACTUALIZA EL ESTADO DE UN PRESTAMO
     */
    @Modifying
    @Query("UPDATE Loan l SET l.status = :status WHERE l.id = :loanId")
    void updateLoanStatus(@Param("loanId") Long loanId, @Param("status") LoanStatus status);

    /**
     * MARCA UNA MULTA COMO PAGADA
     */
    @Modifying
    @Query("UPDATE Loan l SET l.finePaid = true WHERE l.id = :loanId")
    void markFineAsPaid(@Param("loanId") Long loanId);

    /**
     * ACTUALIZA LA FECHA DE DEVOLUCION DE UN PRESTAMO
     */
    @Modifying
    @Query("UPDATE Loan l SET l.returnedAt = :returnDate WHERE l.id = :loanId")
    void updateReturnDate(@Param("loanId") Long loanId, @Param("returnDate") LocalDateTime returnDate);

    /**
     * CUENTA PRESTAMOS TOTALES DEL SISTEMA
     */
    @Query("SELECT COUNT(l) FROM Loan l")
    long countTotalLoans();

    /**
     * CUENTA PRESTAMOS ACTIVOS DEL SISTEMA
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnedAt IS NULL")
    long countActiveLoans();

    /**
     * BUSCA PRESTAMOS VENCIDOS DE UN USUARIO ESPECIFICO
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueByUser(@Param("user") User user);

    /**
     * BUSCA EL PRESTAMO MAS RECIENTE DE UN USUARIO
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user ORDER BY l.loanDate DESC")
    Optional<Loan> findMostRecentByUser(@Param("user") User user);

    /**
     * BUSCA EL ULTIMO PRESTAMO DE UN LIBRO
     */
    @Query("SELECT l FROM Loan l WHERE l.book = :book ORDER BY l.loanDate DESC")
    Optional<Loan> findMostRecentByBook(@Param("book") Book book);

    /**
     * VERIFICA SI UN USUARIO PUEDE REALIZAR UN NUEVO PRESTAMO - VERSION SIMPLIFICADA
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL")
    long countActiveLoansForUser(@Param("user") User user);

    /**
     * METODO AUXILIAR PARA VERIFICAR SI PUEDE HACER PRESTAMO
     */
    default boolean canUserBorrow(User user, int maxLoans) {
        long activeLoans = countActiveLoansForUser(user);
        return activeLoans < maxLoans;
    }
}