package com.taskmanager.service;

import com.taskmanager.model.*;
import com.taskmanager.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * SERVICIO QUE SIMULA EL PROCESAMIENTO DE PAGOS CON TARJETAS DE CRÉDITO
 * IMPLEMENTA DIFERENTES ESCENARIOS DE PAGO PARA TESTING Y DEMOSTRACIÓN
 *
 * @author Mario Flores
 * @version 1.0
 */
@Service
@Transactional
public class PaymentSimulatorService {

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    // TARJETAS DE PRUEBA CON COMPORTAMIENTOS ESPECÍFICOS
    private static final Map<String, String> TEST_CARDS = new HashMap<>();

    static {
        // TARJETAS QUE SIEMPRE APRUEBAN
        TEST_CARDS.put("4111111111111111", "VISA_SUCCESS");
        TEST_CARDS.put("5555555555554444", "MASTERCARD_SUCCESS");
        TEST_CARDS.put("378282246310005", "AMEX_SUCCESS");

        // TARJETAS QUE SIEMPRE RECHAZAN
        TEST_CARDS.put("4000000000000002", "VISA_DECLINED");
        TEST_CARDS.put("5555555555554445", "MASTERCARD_DECLINED");

        // TARJETAS CON FONDOS INSUFICIENTES
        TEST_CARDS.put("4000000000009995", "INSUFFICIENT_FUNDS");

        // TARJETAS EXPIRADAS
        TEST_CARDS.put("4000000000000069", "EXPIRED_CARD");

        // TARJETAS CON PROCESAMIENTO LENTO
        TEST_CARDS.put("4000000000000259", "SLOW_PROCESSING");
    }

    /**
     * INICIO UN PROCESO DE PAGO SIMULADO
     *
     * @param user usuario que realiza el pago
     * @param subscriptionPlan plan a pagar
     * @param cardNumber número de tarjeta
     * @param expiryMonth mes de expiración
     * @param expiryYear año de expiración
     * @param cvv código de seguridad
     * @param cardHolderName nombre del titular
     * @return transacción creada
     */
    public PaymentTransaction initiatePayment(User user, SubscriptionPlan subscriptionPlan,
                                              String cardNumber, String expiryMonth, String expiryYear,
                                              String cvv, String cardHolderName) {

        // VALIDACIONES BÁSICAS
        validatePaymentData(cardNumber, expiryMonth, expiryYear, cvv, cardHolderName);

        // CREAR TRANSACCIÓN
        String paymentMethod = detectCardType(cardNumber);
        PaymentTransaction transaction = new PaymentTransaction(
                user, subscriptionPlan, subscriptionPlan.getPrice(), paymentMethod
        );

        // GUARDAR ÚLTIMOS 4 DÍGITOS
        transaction.setCardLastDigits(cardNumber.substring(cardNumber.length() - 4));

        // DATOS ADICIONALES
        Map<String, String> transactionData = new HashMap<>();
        transactionData.put("cardHolderName", cardHolderName);
        transactionData.put("expiryMonth", expiryMonth);
        transactionData.put("expiryYear", expiryYear);
        transaction.setTransactionData(transactionData.toString());

        // GUARDAR COMO PENDIENTE
        transaction = paymentTransactionRepository.save(transaction);

        // PROCESAR ASINCRÓNICAMENTE
        processPaymentAsync(transaction, cardNumber);

        return transaction;
    }

    /**
     * PROCESO ASÍNCRONO DE PAGO (SIMULA LLAMADA A PASARELA REAL)
     *
     * @param transaction transacción a procesar
     * @param cardNumber número de tarjeta para determinar comportamiento
     */
    @Async
    public CompletableFuture<Void> processPaymentAsync(PaymentTransaction transaction, String cardNumber) {
        try {
            // SIMULAR TIEMPO DE PROCESAMIENTO
            Thread.sleep(getProcessingDelay(cardNumber));

            // DETERMINAR RESULTADO BASADO EN LA TARJETA
            PaymentResult result = simulatePaymentProcessing(cardNumber, transaction.getAmount());

            // ACTUALIZAR TRANSACCIÓN
            transaction.setStatus(result.getStatus());
            transaction.setErrorMessage(result.getErrorMessage());

            // GUARDAR RESULTADO
            paymentTransactionRepository.save(transaction);

            // LOG DEL RESULTADO
            logPaymentResult(transaction, result);

        } catch (InterruptedException e) {
            // MANEJO DE ERROR EN PROCESAMIENTO
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setErrorMessage("Error interno en procesamiento");
            paymentTransactionRepository.save(transaction);

            Thread.currentThread().interrupt();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * SIMULO EL PROCESAMIENTO SEGÚN EL NÚMERO DE TARJETA
     *
     * @param cardNumber número de tarjeta
     * @param amount monto a procesar
     * @return resultado de la simulación
     */
    private PaymentResult simulatePaymentProcessing(String cardNumber, BigDecimal amount) {
        String cardType = TEST_CARDS.get(cardNumber);

        if (cardType == null) {
            // TARJETA NO RECONOCIDA - COMPORTAMIENTO ALEATORIO
            return getRandomResult();
        }

        return switch (cardType) {
            case "VISA_SUCCESS", "MASTERCARD_SUCCESS", "AMEX_SUCCESS" ->
                    new PaymentResult(PaymentStatus.COMPLETED, null);

            case "VISA_DECLINED", "MASTERCARD_DECLINED" ->
                    new PaymentResult(PaymentStatus.REJECTED, "Tarjeta rechazada por el banco emisor");

            case "INSUFFICIENT_FUNDS" ->
                    new PaymentResult(PaymentStatus.REJECTED, "Fondos insuficientes");

            case "EXPIRED_CARD" ->
                    new PaymentResult(PaymentStatus.REJECTED, "Tarjeta expirada");

            case "SLOW_PROCESSING" -> {
                // SIMULAR PROCESAMIENTO LENTO
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                yield new PaymentResult(PaymentStatus.COMPLETED, null);
            }

            default -> new PaymentResult(PaymentStatus.FAILED, "Error desconocido");
        };
    }

    /**
     * GENERO RESULTADO ALEATORIO PARA TARJETAS NO PREDEFINIDAS
     *
     * @return resultado aleatorio
     */
    private PaymentResult getRandomResult() {
        double random = Math.random();

        if (random < 0.85) {
            // 85% DE ÉXITO
            return new PaymentResult(PaymentStatus.COMPLETED, null);
        } else if (random < 0.95) {
            // 10% RECHAZADAS
            return new PaymentResult(PaymentStatus.REJECTED, "Tarjeta rechazada");
        } else {
            // 5% ERRORES
            return new PaymentResult(PaymentStatus.FAILED, "Error en procesamiento");
        }
    }

    /**
     * DETERMINO EL TIPO DE TARJETA BASADO EN EL NÚMERO
     *
     * @param cardNumber número de tarjeta
     * @return tipo de tarjeta
     */
    private String detectCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "VISA";
        } else if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) {
            return "MASTERCARD";
        } else if (cardNumber.startsWith("3")) {
            return "AMERICAN_EXPRESS";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * OBTENGO EL DELAY DE PROCESAMIENTO SEGÚN LA TARJETA
     *
     * @param cardNumber número de tarjeta
     * @return milisegundos de delay
     */
    private long getProcessingDelay(String cardNumber) {
        String cardType = TEST_CARDS.get(cardNumber);

        if ("SLOW_PROCESSING".equals(cardType)) {
            return 8000; // 8 segundos
        }

        // DELAY NORMAL ENTRE 1-3 SEGUNDOS
        return 1000 + (long)(Math.random() * 2000);
    }

    /**
     * VALIDO LOS DATOS DE LA TARJETA
     *
     * @param cardNumber número de tarjeta
     * @param expiryMonth mes de expiración
     * @param expiryYear año de expiración
     * @param cvv código de seguridad
     * @param cardHolderName nombre del titular
     */
    private void validatePaymentData(String cardNumber, String expiryMonth, String expiryYear,
                                     String cvv, String cardHolderName) {

        // VALIDAR NÚMERO DE TARJETA
        if (cardNumber == null || !isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }

        // VALIDAR FECHA DE EXPIRACIÓN
        if (!isValidExpiryDate(expiryMonth, expiryYear)) {
            throw new IllegalArgumentException("Fecha de expiración inválida");
        }

        // VALIDAR CVV
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV inválido");
        }

        // VALIDAR NOMBRE
        if (cardHolderName == null || cardHolderName.trim().length() < 2) {
            throw new IllegalArgumentException("Nombre del titular inválido");
        }
    }

    /**
     * VALIDO EL NÚMERO DE TARJETA USANDO ALGORITMO LUHN
     *
     * @param cardNumber número a validar
     * @return true si es válido
     */
    private boolean isValidCardNumber(String cardNumber) {
        // REMOVER ESPACIOS Y VALIDAR SOLO NÚMEROS
        cardNumber = cardNumber.replaceAll("\\s", "");
        if (!cardNumber.matches("\\d{13,19}")) {
            return false;
        }

        // ALGORITMO LUHN
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    /**
     * VALIDO LA FECHA DE EXPIRACIÓN
     *
     * @param month mes
     * @param year año
     * @return true si es válida
     */
    private boolean isValidExpiryDate(String month, String year) {
        try {
            int monthInt = Integer.parseInt(month);
            int yearInt = Integer.parseInt(year);

            // VALIDAR RANGO DE MES
            if (monthInt < 1 || monthInt > 12) {
                return false;
            }

            // VALIDAR QUE NO ESTÉ EXPIRADA
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            // CONVERTIR AÑO DE 2 DÍGITOS SI ES NECESARIO
            if (yearInt < 100) {
                yearInt += 2000;
            }

            // VERIFICAR EXPIRACIÓN
            return yearInt > currentYear || (yearInt == currentYear && monthInt >= currentMonth);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * OBTENGO EL ESTADO DE UNA TRANSACCIÓN
     *
     * @param referenceCode código de referencia
     * @return transacción si existe
     */
    public PaymentTransaction getTransactionStatus(String referenceCode) {
        return paymentTransactionRepository.findByReferenceCode(referenceCode).orElse(null);
    }

    /**
     * OBTENGO EL HISTORIAL DE PAGOS DE UN USUARIO
     *
     * @param user el usuario
     * @return lista de transacciones
     */
    public List<PaymentTransaction> getUserPaymentHistory(User user) {
        return paymentTransactionRepository.findAllTransactionsByUser(user);
    }

    /**
     * OBTENGO TARJETAS DE PRUEBA PARA MOSTRAR AL USUARIO
     *
     * @return mapa con tarjetas de prueba
     */
    public Map<String, String> getTestCards() {
        Map<String, String> cards = new HashMap<>();
        cards.put("4111111111111111", "Visa - Siempre aprueba");
        cards.put("5555555555554444", "Mastercard - Siempre aprueba");
        cards.put("4000000000000002", "Visa - Siempre rechaza");
        cards.put("4000000000009995", "Visa - Fondos insuficientes");
        cards.put("4000000000000259", "Visa - Procesamiento lento");
        return cards;
    }

    /**
     * LOG DEL RESULTADO DEL PAGO
     *
     * @param transaction transacción procesada
     * @param result resultado obtenido
     */
    private void logPaymentResult(PaymentTransaction transaction, PaymentResult result) {
        String message = String.format(
                "PAGO PROCESADO - TXN: %s, Usuario: %s, Monto: %s, Estado: %s",
                transaction.getReferenceCode(),
                transaction.getUser().getUsername(),
                transaction.getFormattedAmount(),
                result.getStatus()
        );

        System.out.println("💳 " + message);

        if (result.getErrorMessage() != null) {
            System.out.println("❌ Error: " + result.getErrorMessage());
        }
    }

    // ==================== CLASE AUXILIAR PARA RESULTADOS ====================

    /**
     * CLASE PARA ENCAPSULAR RESULTADOS DE PAGO
     */
    private static class PaymentResult {
        private final PaymentStatus status;
        private final String errorMessage;

        public PaymentResult(PaymentStatus status, String errorMessage) {
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public PaymentStatus getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
    }
}