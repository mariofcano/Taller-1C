package com.biblioteca.digital.service;

import com.biblioteca.digital.model.User;
import com.biblioteca.digital.model.UserRole;
import com.biblioteca.digital.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO DE NEGOCIO PARA LA GESTION COMPLETA DE USUARIOS
 *
 * ESTA CLASE IMPLEMENTA TODA LA LOGICA DE NEGOCIO RELACIONADA CON LA GESTION
 * DE USUARIOS EN EL SISTEMA DE BIBLIOTECA DIGITAL. PROPORCIONA OPERACIONES
 * SEGURAS Y VALIDADAS PARA EL REGISTRO, AUTENTICACION, ACTUALIZACION
 * Y ADMINISTRACION DE CUENTAS DE USUARIO.
 *
 * EL SERVICIO MANEJA TODAS LAS REGLAS DE NEGOCIO, VALIDACIONES DE DATOS,
 * CIFRADO DE CONTRASEÑAS Y CONTROL DE ACCESO SEGUN LOS ROLES ASIGNADOS.
 * IMPLEMENTA TRANSACCIONES PARA GARANTIZAR LA CONSISTENCIA DE LOS DATOS.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-26
 *
 * @see User
 * @see UserRole
 * @see UserRepository
 * @see org.springframework.stereotype.Service
 */
@Service
@Transactional
public class UserService {

    /**
     * REPOSITORIO PARA ACCESO A DATOS DE USUARIOS
     *
     * INYECTO EL REPOSITORIO QUE PROPORCIONA TODAS LAS OPERACIONES
     * DE ACCESO A DATOS PARA LA ENTIDAD USER. UTILIZO SPRING DEPENDENCY
     * INJECTION PARA GESTIONAR LA DEPENDENCIA AUTOMATICAMENTE.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * ENCODER PARA CIFRADO SEGURO DE CONTRASEÑAS
     *
     * INYECTO EL COMPONENTE RESPONSABLE DEL CIFRADO DE CONTRASEÑAS
     * UTILIZANDO ALGORITMOS SEGUROS COMO BCRYPT. GARANTIZA QUE NUNCA
     * SE ALMACENEN CONTRASEÑAS EN TEXTO PLANO EN LA BASE DE DATOS.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * REGISTRA UN NUEVO USUARIO EN EL SISTEMA
     *
     * PROCESO COMPLETO DE REGISTRO QUE INCLUYE VALIDACION DE DATOS,
     * VERIFICACION DE UNICIDAD DE CREDENCIALES, CIFRADO DE CONTRASEÑA
     * Y ASIGNACION DE ROL POR DEFECTO. APLICA TODAS LAS REGLAS DE NEGOCIO
     * PARA GARANTIZAR LA INTEGRIDAD DE LOS DATOS.
     *
     * @param username NOMBRE DE USUARIO UNICO
     * @param email DIRECCION DE CORREO ELECTRONICO UNICA
     * @param password CONTRASEÑA EN TEXTO PLANO (SE CIFRARA AUTOMATICAMENTE)
     * @param fullName NOMBRE COMPLETO DEL USUARIO
     * @return USUARIO REGISTRADO CON TODOS LOS CAMPOS CONFIGURADOS
     * @throws IllegalArgumentException SI LOS DATOS NO SON VALIDOS
     * @throws RuntimeException SI EL USERNAME O EMAIL YA EXISTEN
     */
    public User registerUser(String username, String email, String password, String fullName) {
        // VALIDO QUE TODOS LOS CAMPOS REQUERIDOS ESTEN PRESENTES
        validateRequiredFields(username, email, password, fullName);

        // VERIFICO QUE EL USERNAME NO ESTE EN USO
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("EL NOMBRE DE USUARIO YA ESTA EN USO: " + username);
        }

        // VERIFICO QUE EL EMAIL NO ESTE REGISTRADO
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("EL EMAIL YA ESTA REGISTRADO: " + email);
        }

        // VALIDO EL FORMATO Y FORTALEZA DE LA CONTRASEÑA
        validatePassword(password);

        // CIFRO LA CONTRASEÑA ANTES DE ALMACENARLA
        String encodedPassword = passwordEncoder.encode(password);

        // CREO EL NUEVO USUARIO CON CONFIGURACION POR DEFECTO
        User newUser = new User(username, email, encodedPassword, fullName);
        newUser.setRole(UserRole.USER); // ROL POR DEFECTO
        newUser.setActive(true); // USUARIO ACTIVO DESDE EL REGISTRO

        // GUARDO EL USUARIO EN LA BASE DE DATOS
        User savedUser = userRepository.save(newUser);

        return savedUser;
    }

    /**
     * AUTENTICA A UN USUARIO VERIFICANDO SUS CREDENCIALES
     *
     * PROCESO DE AUTENTICACION QUE VALIDA LAS CREDENCIALES DEL USUARIO
     * CONTRA LA BASE DE DATOS. PERMITE LOGIN CON USERNAME O EMAIL
     * Y VERIFICA LA CONTRASEÑA UTILIZANDO EL ENCODER CONFIGURADO.
     *
     * @param loginCredential USERNAME O EMAIL DEL USUARIO
     * @param password CONTRASEÑA EN TEXTO PLANO
     * @return USUARIO AUTENTICADO SI LAS CREDENCIALES SON VALIDAS
     * @throws RuntimeException SI LAS CREDENCIALES SON INCORRECTAS O EL USUARIO ESTA INACTIVO
     */
    public User authenticateUser(String loginCredential, String password) {
        // VALIDO QUE SE PROPORCIONEN LAS CREDENCIALES
        if (loginCredential == null || loginCredential.trim().isEmpty()) {
            throw new IllegalArgumentException("EL USUARIO O EMAIL ES REQUERIDO");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("LA CONTRASEÑA ES REQUERIDA");
        }

        // BUSCO EL USUARIO POR USERNAME O EMAIL
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(loginCredential, loginCredential);

        if (!userOptional.isPresent()) {
            throw new RuntimeException("CREDENCIALES INCORRECTAS");
        }

        User user = userOptional.get();

        // VERIFICO QUE EL USUARIO ESTE ACTIVO
        if (!user.getActive()) {
            throw new RuntimeException("LA CUENTA DE USUARIO ESTA DESACTIVADA");
        }

        // VERIFICO LA CONTRASEÑA
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("CREDENCIALES INCORRECTAS");
        }

        return user;
    }

    /**
     * BUSCA UN USUARIO POR SU ID
     *
     * LOCALIZO UN USUARIO ESPECIFICO MEDIANTE SU IDENTIFICADOR UNICO.
     * METODO FUNDAMENTAL PARA OPERACIONES QUE REQUIEREN ACCESO
     * A DATOS COMPLETOS DE UN USUARIO.
     *
     * @param id IDENTIFICADOR UNICO DEL USUARIO
     * @return OPTIONAL CON EL USUARIO ENCONTRADO O VACIO SI NO EXISTE
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO NO PUEDE SER NULO");
        }

        return userRepository.findById(id);
    }

    /**
     * BUSCA UN USUARIO POR SU USERNAME
     *
     * LOCALIZO UN USUARIO MEDIANTE SU NOMBRE DE USUARIO UNICO.
     * UTILIZADO FRECUENTEMENTE EN PROCESOS DE AUTENTICACION
     * Y VALIDACION DE DISPONIBILIDAD DE NOMBRES.
     *
     * @param username NOMBRE DE USUARIO A BUSCAR
     * @return OPTIONAL CON EL USUARIO ENCONTRADO O VACIO SI NO EXISTE
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("EL USERNAME NO PUEDE SER NULO O VACIO");
        }

        return userRepository.findByUsername(username);
    }

    /**
     * BUSCA UN USUARIO POR SU EMAIL
     *
     * LOCALIZO UN USUARIO MEDIANTE SU DIRECCION DE CORREO ELECTRONICO.
     * IMPORTANTE PARA PROCESOS DE RECUPERACION DE CONTRASEÑA
     * Y VERIFICACION DE CUENTAS EXISTENTES.
     *
     * @param email DIRECCION DE EMAIL A BUSCAR
     * @return OPTIONAL CON EL USUARIO ENCONTRADO O VACIO SI NO EXISTE
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("EL EMAIL NO PUEDE SER NULO O VACIO");
        }

        return userRepository.findByEmail(email);
    }

    /**
     * OBTIENE TODOS LOS USUARIOS ACTIVOS DEL SISTEMA
     *
     * RECUPERO LA LISTA COMPLETA DE USUARIOS QUE ESTAN HABILITADOS
     * EN EL SISTEMA. UTILIZADO PARA INTERFACES ADMINISTRATIVAS
     * Y GENERACION DE REPORTES DE USUARIOS ACTIVOS.
     *
     * @return LISTA DE USUARIOS ACTIVOS
     */
    @Transactional(readOnly = true)
    public List<User> findAllActiveUsers() {
        return userRepository.findByActive(true);
    }

    /**
     * OBTIENE USUARIOS POR ROL ESPECIFICO
     *
     * FILTRO USUARIOS SEGUN SU ROL ASIGNADO. FUNDAMENTAL PARA
     * GESTION DE PERMISOS Y ADMINISTRACION DE ACCESOS
     * POR CATEGORIA DE USUARIO.
     *
     * @param role ROL A FILTRAR
     * @return LISTA DE USUARIOS CON EL ROL ESPECIFICADO
     */
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("EL ROL NO PUEDE SER NULO");
        }

        return userRepository.findByRole(role);
    }

    /**
     * OBTIENE USUARIOS ADMINISTRADORES ACTIVOS
     *
     * RECUPERO USUARIOS CON ROLES ADMINISTRATIVOS (ADMIN Y LIBRARIAN)
     * QUE ESTAN ACTIVOS EN EL SISTEMA. UTILIZADO PARA GESTION
     * DE PERSONAL Y ASIGNACION DE TAREAS ADMINISTRATIVAS.
     *
     * @return LISTA DE USUARIOS CON PERMISOS ADMINISTRATIVOS
     */
    @Transactional(readOnly = true)
    public List<User> findAdministrativeUsers() {
        return userRepository.findAdministrativeUsers();
    }

    /**
     * ACTUALIZA LA INFORMACION BASICA DE UN USUARIO
     *
     * PROCESO DE ACTUALIZACION QUE PERMITE MODIFICAR DATOS PERSONALES
     * DEL USUARIO MANTENIENDO LA INTEGRIDAD DE LAS VALIDACIONES.
     * NO PERMITE CAMBIAR CREDENCIALES DE ACCESO (USERNAME/PASSWORD).
     *
     * @param userId ID DEL USUARIO A ACTUALIZAR
     * @param fullName NUEVO NOMBRE COMPLETO
     * @param phone NUEVO TELEFONO (OPCIONAL)
     * @param address NUEVA DIRECCION (OPCIONAL)
     * @return USUARIO ACTUALIZADO
     * @throws RuntimeException SI EL USUARIO NO EXISTE
     */
    public User updateUserProfile(Long userId, String fullName, String phone, String address) {
        // VALIDO QUE EL ID SEA VALIDO
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }

        // BUSCO EL USUARIO EXISTENTE
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        User user = userOptional.get();

        // VALIDO Y ACTUALIZO EL NOMBRE COMPLETO
        if (fullName != null && !fullName.trim().isEmpty()) {
            if (fullName.length() < 2 || fullName.length() > 100) {
                throw new IllegalArgumentException("EL NOMBRE COMPLETO DEBE TENER ENTRE 2 Y 100 CARACTERES");
            }
            user.setFullName(fullName);
        }

        // ACTUALIZO TELEFONO SI SE PROPORCIONA
        if (phone != null && !phone.trim().isEmpty()) {
            validatePhoneFormat(phone);
            user.setPhone(phone);
        }

        // ACTUALIZO DIRECCION SI SE PROPORCIONA
        if (address != null && !address.trim().isEmpty()) {
            if (address.length() > 200) {
                throw new IllegalArgumentException("LA DIRECCION NO PUEDE EXCEDER 200 CARACTERES");
            }
            user.setAddress(address);
        }

        // GUARDO LOS CAMBIOS
        return userRepository.save(user);
    }

    /**
     * CAMBIA LA CONTRASEÑA DE UN USUARIO
     *
     * PROCESO SEGURO DE CAMBIO DE CONTRASEÑA QUE VALIDA LA CONTRASEÑA
     * ACTUAL ANTES DE ESTABLECER LA NUEVA. APLICA TODAS LAS REGLAS
     * DE VALIDACION Y CIFRADO CORRESPONDIENTES.
     *
     * @param userId ID DEL USUARIO
     * @param currentPassword CONTRASEÑA ACTUAL
     * @param newPassword NUEVA CONTRASEÑA
     * @throws RuntimeException SI LA CONTRASEÑA ACTUAL ES INCORRECTA
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // VALIDO PARAMETROS
        if (userId == null) {
            throw new IllegalArgumentException("EL ID DEL USUARIO ES REQUERIDO");
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("LA CONTRASEÑA ACTUAL ES REQUERIDA");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("LA NUEVA CONTRASEÑA ES REQUERIDA");
        }

        // BUSCO EL USUARIO
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        User user = userOptional.get();

        // VERIFICO LA CONTRASEÑA ACTUAL
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("LA CONTRASEÑA ACTUAL ES INCORRECTA");
        }

        // VALIDO LA NUEVA CONTRASEÑA
        validatePassword(newPassword);

        // CIFRO Y GUARDO LA NUEVA CONTRASEÑA
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        userRepository.save(user);
    }

    /**
     * CAMBIA EL ROL DE UN USUARIO
     *
     * OPERACION ADMINISTRATIVA QUE PERMITE MODIFICAR EL ROL ASIGNADO
     * A UN USUARIO. SOLO DEBE SER EJECUTADA POR ADMINISTRADORES
     * CON LOS PERMISOS ADECUADOS.
     *
     * @param userId ID DEL USUARIO A MODIFICAR
     * @param newRole NUEVO ROL A ASIGNAR
     * @param adminUserId ID DEL USUARIO ADMINISTRADOR QUE REALIZA EL CAMBIO
     * @return USUARIO CON EL ROL ACTUALIZADO
     * @throws RuntimeException SI NO TIENE PERMISOS O EL USUARIO NO EXISTE
     */
    public User changeUserRole(Long userId, UserRole newRole, Long adminUserId) {
        // VALIDO PARAMETROS
        if (userId == null || adminUserId == null) {
            throw new IllegalArgumentException("LOS IDS DE USUARIO SON REQUERIDOS");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("EL NUEVO ROL ES REQUERIDO");
        }

        // VERIFICO QUE EL ADMINISTRADOR TENGA PERMISOS
        Optional<User> adminOptional = userRepository.findById(adminUserId);
        if (!adminOptional.isPresent()) {
            throw new RuntimeException("ADMINISTRADOR NO ENCONTRADO");
        }

        User admin = adminOptional.get();
        if (!admin.getRole().canManageUsers()) {
            throw new RuntimeException("NO TIENE PERMISOS PARA CAMBIAR ROLES DE USUARIO");
        }

        // BUSCO EL USUARIO A MODIFICAR
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        User user = userOptional.get();

        // ACTUALIZO EL ROL
        user.setRole(newRole);

        return userRepository.save(user);
    }

    /**
     * ACTIVA O DESACTIVA UN USUARIO
     *
     * OPERACION ADMINISTRATIVA QUE PERMITE HABILITAR O DESHABILITAR
     * UNA CUENTA DE USUARIO. USUARIOS DESACTIVADOS NO PUEDEN ACCEDER
     * AL SISTEMA NI REALIZAR OPERACIONES.
     *
     * @param userId ID DEL USUARIO A MODIFICAR
     * @param active NUEVO ESTADO DE ACTIVACION
     * @param adminUserId ID DEL ADMINISTRADOR QUE REALIZA LA OPERACION
     * @return USUARIO CON EL ESTADO ACTUALIZADO
     */
    public User toggleUserActiveStatus(Long userId, Boolean active, Long adminUserId) {
        // VALIDO PARAMETROS
        if (userId == null || adminUserId == null) {
            throw new IllegalArgumentException("LOS IDS DE USUARIO SON REQUERIDOS");
        }
        if (active == null) {
            throw new IllegalArgumentException("EL ESTADO DE ACTIVACION ES REQUERIDO");
        }

        // VERIFICO PERMISOS DEL ADMINISTRADOR
        Optional<User> adminOptional = userRepository.findById(adminUserId);
        if (!adminOptional.isPresent()) {
            throw new RuntimeException("ADMINISTRADOR NO ENCONTRADO");
        }

        User admin = adminOptional.get();
        if (!admin.getRole().hasAdminPrivileges()) {
            throw new RuntimeException("NO TIENE PERMISOS PARA MODIFICAR USUARIOS");
        }

        // BUSCO EL USUARIO A MODIFICAR
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("USUARIO NO ENCONTRADO CON ID: " + userId);
        }

        User user = userOptional.get();

        // EVITO QUE UN ADMIN SE DESACTIVE A SI MISMO
        if (userId.equals(adminUserId) && !active) {
            throw new RuntimeException("NO PUEDE DESACTIVAR SU PROPIA CUENTA");
        }

        // ACTUALIZO EL ESTADO
        user.setActive(active);

        return userRepository.save(user);
    }

    /**
     * BUSCA USUARIOS CON PRESTAMOS VENCIDOS
     *
     * IDENTIFICO USUARIOS QUE TIENEN LIBROS NO DEVUELTOS DESPUES
     * DE LA FECHA LIMITE. FUNDAMENTAL PARA PROCESOS DE COBRANZA
     * Y GESTION DE MOROSOS.
     *
     * @return LISTA DE USUARIOS CON PRESTAMOS VENCIDOS
     */
    @Transactional(readOnly = true)
    public List<User> findUsersWithOverdueLoans() {
        return userRepository.findUsersWithOverdueLoans();
    }

    /**
     * BUSCA USUARIOS CON PRESTAMOS ACTIVOS
     *
     * LOCALIZO USUARIOS QUE ACTUALMENTE TIENEN LIBROS EN PRESTAMO.
     * UTIL PARA ESTADISTICAS DE USO Y GESTION DE INVENTARIO.
     *
     * @return LISTA DE USUARIOS CON PRESTAMOS ACTIVOS
     */
    @Transactional(readOnly = true)
    public List<User> findUsersWithActiveLoans() {
        return userRepository.findUsersWithActiveLoans();
    }

    /**
     * REALIZA BUSQUEDA GLOBAL DE USUARIOS
     *
     * BUSQUEDA INTEGRAL QUE EXAMINA COINCIDENCIAS EN MULTIPLE CAMPOS
     * (USERNAME, EMAIL, NOMBRE COMPLETO) SIMULTANEAMENTE.
     * PROPORCIONA FUNCIONALIDAD DE BUSQUEDA GENERAL.
     *
     * @param searchTerm TERMINO DE BUSQUEDA
     * @return LISTA DE USUARIOS CON COINCIDENCIAS
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("EL TERMINO DE BUSQUEDA ES REQUERIDO");
        }

        return userRepository.findByGlobalSearch(searchTerm.trim());
    }

    /**
     * OBTIENE ESTADISTICAS DE USUARIOS POR ROL
     *
     * GENERO ESTADISTICAS AGRUPADAS POR ROL PARA REPORTES
     * Y ANALISIS DEMOGRAFICO DEL SISTEMA.
     *
     * @return LISTA CON ESTADISTICAS [ROL, CANTIDAD]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUserStatisticsByRole() {
        return userRepository.getUserStatsByRole();
    }

    /**
     * CUENTA USUARIOS REGISTRADOS HOY
     *
     * CALCULO LA CANTIDAD DE NUEVOS REGISTROS EN LA FECHA ACTUAL.
     * METRICA IMPORTANTE PARA MONITOREO DE CRECIMIENTO DIARIO.
     *
     * @return NUMERO DE USUARIOS REGISTRADOS HOY
     */
    @Transactional(readOnly = true)
    public long countUsersRegisteredToday() {
        return userRepository.countUsersRegisteredToday();
    }

    /**
     * VALIDA CAMPOS REQUERIDOS PARA REGISTRO
     *
     * VERIFICO QUE TODOS LOS CAMPOS OBLIGATORIOS ESTEN PRESENTES
     * Y TENGAN VALORES VALIDOS ANTES DE PROCESAR EL REGISTRO.
     *
     * @param username NOMBRE DE USUARIO
     * @param email DIRECCION DE EMAIL
     * @param password CONTRASEÑA
     * @param fullName NOMBRE COMPLETO
     * @throws IllegalArgumentException SI ALGUN CAMPO NO ES VALIDO
     */
    private void validateRequiredFields(String username, String email, String password, String fullName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("EL NOMBRE DE USUARIO ES REQUERIDO");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("EL EMAIL ES REQUERIDO");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("LA CONTRASEÑA ES REQUERIDA");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("EL NOMBRE COMPLETO ES REQUERIDO");
        }
    }

    /**
     * VALIDA EL FORMATO Y FORTALEZA DE UNA CONTRASEÑA
     *
     * APLICO REGLAS DE VALIDACION PARA GARANTIZAR QUE LAS CONTRASEÑAS
     * CUMPLAN CON LOS ESTANDARES DE SEGURIDAD ESTABLECIDOS.
     *
     * @param password CONTRASEÑA A VALIDAR
     * @throws IllegalArgumentException SI LA CONTRASEÑA NO ES VALIDA
     */
    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("LA CONTRASEÑA DEBE TENER AL MENOS 8 CARACTERES");
        }
        if (password.length() > 50) {
            throw new IllegalArgumentException("LA CONTRASEÑA NO PUEDE EXCEDER 50 CARACTERES");
        }

        // VERIFICO QUE CONTENGA AL MENOS UNA LETRA
        if (!password.matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException("LA CONTRASEÑA DEBE CONTENER AL MENOS UNA LETRA");
        }

        // VERIFICO QUE CONTENGA AL MENOS UN NUMERO
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("LA CONTRASEÑA DEBE CONTENER AL MENOS UN NUMERO");
        }
    }

    /**
     * VALIDA EL FORMATO DE UN NUMERO DE TELEFONO
     *
     * VERIFICO QUE EL TELEFONO TENGA UN FORMATO VALIDO SEGUN
     * LOS PATRONES ESTABLECIDOS EN EL SISTEMA.
     *
     * @param phone NUMERO DE TELEFONO A VALIDAR
     * @throws IllegalArgumentException SI EL FORMATO NO ES VALIDO
     */
    private void validatePhoneFormat(String phone) {
        if (!phone.matches("^[+]?[0-9\\s\\-()]{7,15}$")) {
            throw new IllegalArgumentException("EL TELEFONO DEBE TENER UN FORMATO VALIDO");
        }
    }

    /**
     * VERIFICA SI UN USUARIO EXISTE POR SU ID
     *
     * COMPRUEBO LA EXISTENCIA DE UN USUARIO SIN CARGAR
     * LA ENTIDAD COMPLETA. OPTIMIZA VALIDACIONES RAPIDAS.
     *
     * @param userId ID DEL USUARIO A VERIFICAR
     * @return TRUE SI EXISTE, FALSE EN CASO CONTRARIO
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        if (userId == null) {
            return false;
        }

        return userRepository.existsById(userId);
    }

    /**
     * VERIFICA DISPONIBILIDAD DE UN USERNAME
     *
     * COMPRUEBO SI UN NOMBRE DE USUARIO ESTA DISPONIBLE
     * PARA REGISTRO. UTIL PARA VALIDACIONES EN TIEMPO REAL.
     *
     * @param username NOMBRE DE USUARIO A VERIFICAR
     * @return TRUE SI ESTA DISPONIBLE, FALSE SI YA EXISTE
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        return !userRepository.existsByUsername(username);
    }

    /**
     * VERIFICA DISPONIBILIDAD DE UN EMAIL
     *
     * COMPRUEBO SI UNA DIRECCION DE EMAIL ESTA DISPONIBLE
     * PARA REGISTRO. FUNDAMENTAL PARA EVITAR DUPLICADOS.
     *
     * @param email DIRECCION DE EMAIL A VERIFICAR
     * @return TRUE SI ESTA DISPONIBLE, FALSE SI YA EXISTE
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return !userRepository.existsByEmail(email);
    }
}