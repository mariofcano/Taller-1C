/**
 * FUNCIONES JAVASCRIPT COMUNES PARA EL PANEL ADMINISTRATIVO
 *
 * IMPLEMENTO AQUI TODAS LAS FUNCIONALIDADES JAVASCRIPT QUE UTILIZO
 * A TRAVES DE MULTIPLES PANTALLAS ADMINISTRATIVAS. ESTO INCLUYE
 * VALIDACIONES, ANIMACIONES, PETICIONES AJAX Y UTILIDADES GENERALES.
 *
 * ORGANIZO EL CODIGO EN MODULOS LOGICOS PARA FACILITAR EL MANTENIMIENTO
 * Y REUTILIZACION ENTRE DIFERENTES COMPONENTES DEL SISTEMA.
 *
 * @author MARIO FLORES
 * @version 1.0
 * @since 2025-05-27
 */

/**
 * OBJETO PRINCIPAL QUE ENCAPSULA TODAS LAS FUNCIONALIDADES ADMINISTRATIVAS
 *
 * UTILIZO UN PATRON DE MODULO PARA EVITAR CONTAMINACION DEL SCOPE GLOBAL
 * Y ORGANIZAR LAS FUNCIONES EN CATEGORIAS LOGICAS CLARAS.
 */
const AdminFunctions = {

    /**
     * CONFIGURACION GLOBAL DEL SISTEMA ADMINISTRATIVO
     *
     * DEFINO AQUI TODAS LAS CONSTANTES Y CONFIGURACIONES QUE UTILIZO
     * A TRAVES DE DIFERENTES FUNCIONALIDADES DEL PANEL ADMINISTRATIVO.
     */
    config: {
        // TIMEOUTS PARA DIFERENTES OPERACIONES
        alertTimeout: 5000,        // TIEMPO ANTES DE OCULTAR ALERTAS AUTOMATICAMENTE
        ajaxTimeout: 10000,        // TIMEOUT PARA PETICIONES AJAX
        animationDuration: 300,    // DURACION DE ANIMACIONES EN MILISEGUNDOS

        // CONFIGURACION DE TABLAS
        pageSize: 20,              // NUMERO DE ELEMENTOS POR PAGINA
        maxVisiblePages: 5,        // NUMERO MAXIMO DE PAGINAS VISIBLES EN PAGINACION

        // CONFIGURACION DE VALIDACIONES
        minPasswordLength: 8,      // LONGITUD MINIMA DE CONTRASEÑAS
        maxFileSize: 5242880,      // TAMAÑO MAXIMO DE ARCHIVO (5MB)

        // URLS BASE PARA PETICIONES AJAX
        baseUrl: window.location.origin,
        apiUrl: '/admin/api'
    },

    /**
     * MODULO DE INICIALIZACION DEL SISTEMA
     *
     * CONTENGO AQUI TODAS LAS FUNCIONES QUE SE EJECUTAN AL CARGAR LA PAGINA
     * PARA CONFIGURAR EVENTOS, VALIDACIONES Y FUNCIONALIDADES INICIALES.
     */
    init: {
        /**
         * FUNCION PRINCIPAL DE INICIALIZACION
         *
         * EJECUTO ESTA FUNCION CUANDO EL DOM ESTA COMPLETAMENTE CARGADO
         * PARA CONFIGURAR TODOS LOS COMPONENTES Y EVENT LISTENERS NECESARIOS.
         */
        setup: function() {
            console.log('INICIALIZANDO PANEL ADMINISTRATIVO...');

            // CONFIGURO LOS DIFERENTES MODULOS
            AdminFunctions.alerts.init();
            AdminFunctions.forms.init();
            AdminFunctions.tables.init();
            AdminFunctions.ajax.init();
            AdminFunctions.animations.init();

            console.log('PANEL ADMINISTRATIVO INICIALIZADO CORRECTAMENTE');
        },

        /**
         * CONFIGURO EVENT LISTENERS GLOBALES
         *
         * ESTABLEZCO AQUI TODOS LOS EVENT LISTENERS QUE APLICAN
         * A MULTIPLES ELEMENTOS O FUNCIONALIADES DEL SISTEMA.
         */
        setupGlobalEvents: function() {
            // PREVENIR ENVIO MULTIPLE DE FORMULARIOS
            document.addEventListener('submit', function(e) {
                const form = e.target;
                if (form.classList.contains('processing')) {
                    e.preventDefault();
                    return false;
                }
                form.classList.add('processing');
            });

            // CONFIRMAR ACCIONES DESTRUCTIVAS
            document.addEventListener('click', function(e) {
                if (e.target.matches('[data-confirm]')) {
                    const message = e.target.getAttribute('data-confirm');
                    if (!confirm(message)) {
                        e.preventDefault();
                        return false;
                    }
                }
            });

            // MANEJO DE TECLAS DE ACCESO RAPIDO
            document.addEventListener('keydown', function(e) {
                AdminFunctions.keyboard.handleGlobalShortcuts(e);
            });
        }
    },

    /**
     * MODULO DE GESTION DE ALERTAS Y NOTIFICACIONES
     *
     * MANEJO AQUI TODAS LAS FUNCIONALIDADES RELACIONADAS CON MOSTRAR
     * MENSAJES AL USUARIO, INCLUYENDO ALERTAS DE EXITO, ERROR Y CONFIRMACION.
     */
    alerts: {
        /**
         * INICIALIZO EL SISTEMA DE ALERTAS
         *
         * CONFIGURO EL COMPORTAMIENTO AUTOMATICO DE LAS ALERTAS Y
         * LOS EVENT LISTENERS PARA INTERACCIONES DEL USUARIO.
         */
        init: function() {
            this.setupAutoDismiss();
            this.setupManualDismiss();
        },

        /**
         * CONFIGURO CIERRE AUTOMATICO DE ALERTAS
         *
         * ESTABLEZCO TIMERS PARA CERRAR AUTOMATICAMENTE LAS ALERTAS
         * DESPUES DEL TIEMPO CONFIGURADO EN EL SISTEMA.
         */
        setupAutoDismiss: function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                if (!alert.hasAttribute('data-no-auto-dismiss')) {
                    setTimeout(() => {
                        this.dismissAlert(alert);
                    }, AdminFunctions.config.alertTimeout);
                }
            });
        },

        /**
         * CONFIGURO CIERRE MANUAL DE ALERTAS
         *
         * ESTABLEZCO EVENT LISTENERS PARA LOS BOTONES DE CIERRE
         * DE ALERTAS QUE PERMITEN AL USUARIO CERRARLAS MANUALMENTE.
         */
        setupManualDismiss: function() {
            const dismissButtons = document.querySelectorAll('[data-bs-dismiss="alert"]');
            dismissButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    const alert = e.target.closest('.alert');
                    this.dismissAlert(alert);
                });
            });
        },

        /**
         * CIERRO UNA ALERTA CON ANIMACION
         *
         * EJECUTO UNA ANIMACION SUAVE ANTES DE REMOVER LA ALERTA
         * DEL DOM PARA PROPORCIONAR FEEDBACK VISUAL AL USUARIO.
         *
         * @param {HTMLElement} alert - ELEMENTO DE ALERTA A CERRAR
         */
        dismissAlert: function(alert) {
            if (!alert) return;

            alert.style.transition = `opacity ${AdminFunctions.config.animationDuration}ms ease`;
            alert.style.opacity = '0';

            setTimeout(() => {
                if (alert.parentNode) {
                    alert.parentNode.removeChild(alert);
                }
            }, AdminFunctions.config.animationDuration);
        },

        /**
         * MUESTRO UNA ALERTA DINAMICA
         *
         * CREO Y MUESTRO UNA NUEVA ALERTA CON EL MENSAJE Y TIPO ESPECIFICADOS.
         * UTILIZO ESTA FUNCION PARA MOSTRAR RESPUESTAS DE PETICIONES AJAX.
         *
         * @param {string} message - MENSAJE A MOSTRAR
         * @param {string} type - TIPO DE ALERTA (success, danger, warning, info)
         * @param {string} container - SELECTOR DEL CONTENEDOR DONDE INSERTAR
         */
        show: function(message, type = 'info', container = '.container') {
            const alertHtml = `
                <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                    <i class="fas fa-${this.getIconByType(type)} me-2"></i>
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            `;

            const containerElement = document.querySelector(container);
            if (containerElement) {
                containerElement.insertAdjacentHTML('afterbegin', alertHtml);
                // CONFIGURO AUTO-CIERRE PARA LA NUEVA ALERTA
                const newAlert = containerElement.querySelector('.alert');
                setTimeout(() => {
                    this.dismissAlert(newAlert);
                }, AdminFunctions.config.alertTimeout);
            }
        },

        /**
         * OBTENGO ICONO APROPIADO SEGUN EL TIPO DE ALERTA
         *
         * RETORNO EL NOMBRE DEL ICONO DE FONT AWESOME QUE CORRESPONDE
         * AL TIPO DE ALERTA ESPECIFICADO PARA CONSISTENCIA VISUAL.
         *
         * @param {string} type - TIPO DE ALERTA
         * @return {string} NOMBRE DEL ICONO
         */
        getIconByType: function(type) {
            const icons = {
                success: 'check-circle',
                danger: 'exclamation-triangle',
                warning: 'exclamation-circle',
                info: 'info-circle',
                primary: 'info-circle'
            };
            return icons[type] || 'info-circle';
        }
    },

    /**
     * MODULO DE GESTION DE FORMULARIOS
     *
     * CONTENGO AQUI TODAS LAS FUNCIONALIDADES RELACIONADAS CON VALIDACION,
     * ENVIO Y MANEJO DE FORMULARIOS EN EL PANEL ADMINISTRATIVO.
     */
    forms: {
        /**
         * INICIALIZO EL SISTEMA DE FORMULARIOS
         *
         * CONFIGURO VALIDACIONES, EVENT LISTENERS Y COMPORTAMIENTOS
         * ESPECIALES PARA TODOS LOS FORMULARIOS DEL SISTEMA.
         */
        init: function() {
            this.setupValidation();
            this.setupSubmitHandlers();
            this.setupFieldEnhancements();
        },

        /**
         * CONFIGURO VALIDACIONES DE FORMULARIOS
         *
         * ESTABLEZCO VALIDACIONES EN TIEMPO REAL PARA DIFERENTES
         * TIPOS DE CAMPOS Y FORMULARIOS DEL SISTEMA.
         */
        setupValidation: function() {
            // VALIDACION DE CONTRASEÑAS
            const passwordFields = document.querySelectorAll('input[type="password"]');
            passwordFields.forEach(field => {
                field.addEventListener('input', (e) => {
                    this.validatePassword(e.target);
                });
            });

            // VALIDACION DE EMAILS
            const emailFields = document.querySelectorAll('input[type="email"]');
            emailFields.forEach(field => {
                field.addEventListener('blur', (e) => {
                    this.validateEmail(e.target);
                });
            });

            // VALIDACION DE CAMPOS REQUERIDOS
            const requiredFields = document.querySelectorAll('[required]');
            requiredFields.forEach(field => {
                field.addEventListener('blur', (e) => {
                    this.validateRequired(e.target);
                });
            });
        },

        /**
         * CONFIGURO MANEJADORES DE ENVIO DE FORMULARIOS
         *
         * ESTABLEZCO COMPORTAMIENTOS ESPECIALES PARA EL ENVIO DE FORMULARIOS
         * INCLUYENDO VALIDACIONES FINALES Y PREVENCION DE ENVIOS DUPLICADOS.
         */
        setupSubmitHandlers: function() {
            const forms = document.querySelectorAll('form');
            forms.forEach(form => {
                form.addEventListener('submit', (e) => {
                    if (!this.validateForm(form)) {
                        e.preventDefault();
                        return false;
                    }
                    this.showSubmitLoading(form);
                });
            });
        },

        /**
         * CONFIGURO MEJORAS PARA CAMPOS ESPECIFICOS
         *
         * AÑADO FUNCIONALIDADES ESPECIALES A CIERTOS TIPOS DE CAMPOS
         * PARA MEJORAR LA EXPERIENCIA DE USUARIO.
         */
        setupFieldEnhancements: function() {
            // AUTO-FOCUS EN PRIMER CAMPO
            const firstInput = document.querySelector('form input:not([type="hidden"]):first-of-type');
            if (firstInput) {
                firstInput.focus();
            }

            // CONFIRMACION DE CONTRASEÑAS
            this.setupPasswordConfirmation();

            // FORMATEO AUTOMATICO DE TELEFONOS
            this.setupPhoneFormatting();
        },

        /**
         * VALIDO UN CAMPO DE CONTRASEÑA
         *
         * VERIFICO QUE LA CONTRASEÑA CUMPLA CON LOS REQUISITOS
         * MINIMOS DE SEGURIDAD ESTABLECIDOS EN EL SISTEMA.
         *
         * @param {HTMLElement} field - CAMPO DE CONTRASEÑA A VALIDAR
         * @return {boolean} TRUE SI ES VALIDA, FALSE EN CASO CONTRARIO
         */
        validatePassword: function(field) {
            const password = field.value;
            const isValid = password.length >= AdminFunctions.config.minPasswordLength &&
                           /[a-zA-Z]/.test(password) &&
                           /[0-9]/.test(password);

            this.setFieldValidation(field, isValid,
                isValid ? '' : 'LA CONTRASEÑA DEBE TENER AL MENOS 8 CARACTERES, LETRAS Y NÚMEROS');

            return isValid;
        },

        /**
         * VALIDO UN CAMPO DE EMAIL
         *
         * VERIFICO QUE EL EMAIL TENGA UN FORMATO VALIDO
         * UTILIZANDO UNA EXPRESION REGULAR ROBUSTA.
         *
         * @param {HTMLElement} field - CAMPO DE EMAIL A VALIDAR
         * @return {boolean} TRUE SI ES VALIDO, FALSE EN CASO CONTRARIO
         */
        validateEmail: function(field) {
            const email = field.value;
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            const isValid = emailRegex.test(email);

            this.setFieldValidation(field, isValid,
                isValid ? '' : 'EL EMAIL DEBE TENER UN FORMATO VÁLIDO');

            return isValid;
        },

        /**
         * VALIDO UN CAMPO REQUERIDO
         *
         * VERIFICO QUE EL CAMPO TENGA UN VALOR NO VACIO
         * Y MUESTRO EL MENSAJE DE ERROR CORRESPONDIENTE.
         *
         * @param {HTMLElement} field - CAMPO REQUERIDO A VALIDAR
         * @return {boolean} TRUE SI TIENE VALOR, FALSE EN CASO CONTRARIO
         */
        validateRequired: function(field) {
            const isValid = field.value.trim() !== '';

            this.setFieldValidation(field, isValid,
                isValid ? '' : 'ESTE CAMPO ES REQUERIDO');

            return isValid;
        },

        /**
         * ESTABLEZCO EL ESTADO DE VALIDACION DE UN CAMPO
         *
         * APLICO LAS CLASES CSS Y MENSAJES CORRESPONDIENTES
         * AL ESTADO DE VALIDACION DEL CAMPO ESPECIFICADO.
         *
         * @param {HTMLElement} field - CAMPO A MARCAR
         * @param {boolean} isValid - SI EL CAMPO ES VALIDO
         * @param {string} message - MENSAJE DE ERROR A MOSTRAR
         */
        setFieldValidation: function(field, isValid, message) {
            // REMUEVO CLASES ANTERIORES
            field.classList.remove('is-valid', 'is-invalid');

            // AÑADO CLASE SEGUN VALIDACION
            field.classList.add(isValid ? 'is-valid' : 'is-invalid');

            // MUESTRO U OCULTO MENSAJE DE ERROR
            let feedback = field.parentNode.querySelector('.invalid-feedback');
            if (!feedback && !isValid) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                field.parentNode.appendChild(feedback);
            }

            if (feedback) {
                feedback.textContent = message;
                feedback.style.display = isValid ? 'none' : 'block';
            }
        },

        /**
         * VALIDO UN FORMULARIO COMPLETO
         *
         * EJECUTO VALIDACIONES EN TODOS LOS CAMPOS DEL FORMULARIO
         * Y RETORNO TRUE SOLO SI TODOS SON VALIDOS.
         *
         * @param {HTMLElement} form - FORMULARIO A VALIDAR
         * @return {boolean} TRUE SI ES VALIDO, FALSE EN CASO CONTRARIO
         */
        validateForm: function(form) {
            let isValid = true;

            // VALIDO TODOS LOS CAMPOS REQUERIDOS
            const requiredFields = form.querySelectorAll('[required]');
            requiredFields.forEach(field => {
                if (!this.validateRequired(field)) {
                    isValid = false;
                }
            });

            // VALIDO CAMPOS DE EMAIL
            const emailFields = form.querySelectorAll('input[type="email"]');
            emailFields.forEach(field => {
                if (field.value && !this.validateEmail(field)) {
                    isValid = false;
                }
            });

            // VALIDO CAMPOS DE CONTRASEÑA
            const passwordFields = form.querySelectorAll('input[type="password"]');
            passwordFields.forEach(field => {
                if (field.value && !this.validatePassword(field)) {
                    isValid = false;
                }
            });

            return isValid;
        },

        /**
         * MUESTRO INDICADOR DE CARGA EN FORMULARIO
         *
         * CAMBIO EL BOTON DE ENVIO PARA MOSTRAR UN SPINNER
         * Y DESHABILITO EL FORMULARIO DURANTE EL PROCESAMIENTO.
         *
         * @param {HTMLElement} form - FORMULARIO EN PROCESAMIENTO
         */
        showSubmitLoading: function(form) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                const originalText = submitBtn.innerHTML;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>PROCESANDO...';
                submitBtn.disabled = true;

                // GUARDO TEXTO ORIGINAL PARA RESTAURAR EN CASO DE ERROR
                submitBtn.setAttribute('data-original-text', originalText);
            }
        },

        /**
         * CONFIGURO CONFIRMACION DE CONTRASEÑAS
         *
         * ESTABLEZCO VALIDACION EN TIEMPO REAL PARA CAMPOS
         * DE CONFIRMACION DE CONTRASEÑA.
         */
        setupPasswordConfirmation: function() {
            const confirmFields = document.querySelectorAll('input[name="confirmPassword"]');
            confirmFields.forEach(confirmField => {
                const passwordField = document.querySelector('input[name="password"]');
                if (passwordField) {
                    confirmField.addEventListener('input', () => {
                        const isMatch = passwordField.value === confirmField.value;
                        this.setFieldValidation(confirmField, isMatch,
                            isMatch ? '' : 'LAS CONTRASEÑAS NO COINCIDEN');
                    });
                }
            });
        },

        /**
         * CONFIGURO FORMATEO AUTOMATICO DE TELEFONOS
         *
         * APLICO FORMATEO VISUAL A CAMPOS DE TELEFONO
         * PARA MEJORAR LA EXPERIENCIA DE ENTRADA DE DATOS.
         */
        setupPhoneFormatting: function() {
            const phoneFields = document.querySelectorAll('input[type="tel"]');
            phoneFields.forEach(field => {
                field.addEventListener('input', (e) => {
                    // REMUEVO CARACTERES NO NUMERICOS EXCEPTO + Y ESPACIOS
                    let value = e.target.value.replace(/[^\d+\s()-]/g, '');
                    e.target.value = value;
                });
            });
        }
    },

    /**
     * MODULO DE GESTION DE TABLAS
     *
     * CONTENGO AQUI TODAS LAS FUNCIONALIDADES RELACIONADAS CON
     * TABLAS ADMINISTRATIVAS, INCLUYENDO ORDENAMIENTO, FILTRADO Y PAGINACION.
     */
    tables: {
        /**
         * INICIALIZO EL SISTEMA DE TABLAS
         *
         * CONFIGURO TODAS LAS FUNCIONALIDADES ESPECIALES
         * PARA LAS TABLAS DEL PANEL ADMINISTRATIVO.
         */
        init: function() {
            this.setupSorting();
            this.setupRowActions();
            this.setupBulkActions();
        },

        /**
         * CONFIGURO ORDENAMIENTO DE COLUMNAS
         *
         * ESTABLEZCO FUNCIONALIDAD DE CLICK EN HEADERS
         * PARA ORDENAR TABLAS POR DIFERENTES COLUMNAS.
         */
        setupSorting: function() {
            const sortableHeaders = document.querySelectorAll('th[data-sortable]');
            sortableHeaders.forEach(header => {
                header.style.cursor = 'pointer';
                header.addEventListener('click', () => {
                    this.sortTable(header);
                });
            });
        },

        /**
         * CONFIGURO ACCIONES DE FILA
         *
         * ESTABLEZCO EVENT LISTENERS PARA BOTONES DE ACCION
         * EN CADA FILA DE LAS TABLAS ADMINISTRATIVAS.
         */
        setupRowActions: function() {
            // BOTONES DE TOGGLE DE ESTADO
            document.querySelectorAll('.toggle-status-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    this.toggleRowStatus(e.target);
                });
            });

            // BOTONES DE ELIMINACION
            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    this.deleteRow(e.target);
                });
            });
        },

        /**
         * CONFIGURO ACCIONES EN LOTE
         *
         * ESTABLEZCO FUNCIONALIDAD PARA SELECCIONAR MULTIPLES
         * FILAS Y EJECUTAR ACCIONES EN LOTE SOBRE ELLAS.
         */
        setupBulkActions: function() {
            // CHECKBOX MAESTRO PARA SELECCIONAR TODO
            const masterCheckbox = document.querySelector('#selectAll');
            if (masterCheckbox) {
                masterCheckbox.addEventListener('change', (e) => {
                    this.toggleAllRows(e.target.checked);
                });
            }

            // CHECKBOXES INDIVIDUALES
            document.querySelectorAll('.row-checkbox').forEach(checkbox => {
                checkbox.addEventListener('change', () => {
                    this.updateBulkActionsVisibility();
                });
            });
        },

        /**
         * ORDENO UNA TABLA POR COLUMNA ESPECIFICA
         *
         * EJECUTO ORDENAMIENTO CLIENT-SIDE DE LA TABLA
         * BASADO EN LA COLUMNA SELECCIONADA.
         *
         * @param {HTMLElement} header - HEADER DE LA COLUMNA A ORDENAR
         */
        sortTable: function(header) {
            const table = header.closest('table');
            const tbody = table.querySelector('tbody');
            const rows = Array.from(tbody.querySelectorAll('tr'));
            const columnIndex = Array.from(header.parentNode.children).indexOf(header);
            const currentOrder = header.getAttribute('data-order') || 'asc';
            const newOrder = currentOrder === 'asc' ? 'desc' : 'asc';

            // ORDENO LAS FILAS
            rows.sort((a, b) => {
                const aValue = a.children[columnIndex].textContent.trim();
                const bValue = b.children[columnIndex].textContent.trim();

                if (newOrder === 'asc') {
                    return aValue.localeCompare(bValue);
                } else {
                    return bValue.localeCompare(aValue);
                }
            });

            // REINSERTO LAS FILAS ORDENADAS
            rows.forEach(row => tbody.appendChild(row));

            // ACTUALIZO INDICADORES VISUALES
            header.setAttribute('data-order', newOrder);
            this.updateSortIndicators(header, newOrder);
        },

        /**
         * ACTUALIZO INDICADORES VISUALES DE ORDENAMIENTO
         *
         * MUESTRO ICONOS EN LOS HEADERS PARA INDICAR
         * LA DIRECCION DEL ORDENAMIENTO ACTUAL.
         *
         * @param {HTMLElement} activeHeader - HEADER ACTIVO
         * @param {string} order - DIRECCION DEL ORDENAMIENTO
         */
        updateSortIndicators: function(activeHeader, order) {
            // REMUEVO INDICADORES EXISTENTES
            const allHeaders = activeHeader.closest('tr').querySelectorAll('th');
            allHeaders.forEach(header => {
                const icon = header.querySelector('.sort-icon');
                if (icon) icon.remove();
            });

            // AÑADO INDICADOR AL HEADER ACTIVO
            const icon = document.createElement('i');
            icon.className = `fas fa-sort-${order === 'asc' ? 'up' : 'down'} sort-icon ms-2`;
            activeHeader.appendChild(icon);
        },

        /**
         * CAMBIO EL ESTADO DE UNA FILA
         *
         * EJECUTO UNA PETICION AJAX PARA CAMBIAR EL ESTADO
         * DE UN ELEMENTO Y ACTUALIZO LA INTERFAZ EN CONSECUENCIA.
         *
         * @param {HTMLElement} button - BOTON QUE TRIGGERO LA ACCION
         */
        toggleRowStatus: function(button) {
            const userId = button.getAttribute('data-user-id');
            const currentStatus = button.getAttribute('data-current-status') === 'true';
            const newStatus = !currentStatus;

            const actionText = newStatus ? 'activar' : 'desactivar';

            if (confirm(`¿ESTÁ SEGURO DE ${actionText.toUpperCase()} ESTE USUARIO?`)) {
                AdminFunctions.ajax.post(`/admin/users/${userId}/toggle-status`, {
                    active: newStatus
                }, {
                    success: (response) => {
                        AdminFunctions.alerts.show(response, 'success');
                        setTimeout(() => location.reload(), 1000);
                    },
                    error: (error) => {
                        AdminFunctions.alerts.show(error, 'danger');
                    }
                });
            }
        }
    },

    /**
     * MODULO DE PETICIONES AJAX
     *
     * MANEJO AQUI TODAS LAS PETICIONES ASINCRONAS AL SERVIDOR
     * CON MANEJO ROBUSTO DE ERRORES Y RETROALIMENTACION AL USUARIO.
     */
    ajax: {
        /**
         * INICIALIZO EL SISTEMA AJAX
         *
         * CONFIGURO HEADERS GLOBALES Y MANEJADORES
         * DE ERROR PARA TODAS LAS PETICIONES AJAX.
         */
        init: function() {
            this.setupGlobalHandlers();
        },

        /**
         * CONFIGURO MANEJADORES GLOBALES
         *
         * ESTABLEZCO CONFIGURACIONES QUE APLICAN A TODAS
         * LAS PETICIONES AJAX DEL SISTEMA.
         */
        setupGlobalHandlers: function() {
            // CONFIGURO TIMEOUT GLOBAL
            this.defaultTimeout = AdminFunctions.config.ajaxTimeout;
        },

        /**
         * EJECUTO UNA PETICION POST
         *
         * REALIZO UNA PETICION POST AL SERVIDOR CON MANEJO
         * AUTOMATICO DE ERRORES Y RETROALIMENTACION.
         *
         * @param {string} url - URL DE DESTINO
         * @param {Object} data - DATOS A ENVIAR
         * @param {Object} callbacks - CALLBACKS DE SUCCESS Y ERROR
         */
        post: function(url, data, callbacks = {}) {
            fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(data)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ERROR ${response.status}`);
                }
                return response.text();
            })
            .then(data => {
                if (callbacks.success) {
                    callbacks.success(data);
                }
            })
            .catch(error => {
                console.error('ERROR EN PETICION AJAX:', error);
                if (callbacks.error) {
                    callbacks.error(error.message);
                } else {
                    AdminFunctions.alerts.show('ERROR EN LA OPERACIÓN: ' + error.message, 'danger');
                }
            });
        }
    },

    /**
     * MODULO DE ANIMACIONES
     *
     * CONTENGO AQUI FUNCIONES PARA ANIMACIONES SUAVES
     * Y EFECTOS VISUALES QUE MEJORAN LA EXPERIENCIA DE USUARIO.
     */
    animations: {
        /**
         * INICIALIZO SISTEMA DE ANIMACIONES
         *
         * CONFIGURO ANIMACIONES DE ENTRADA PARA ELEMENTOS
         * QUE SE CARGAN DINAMICAMENTE EN LA PAGINA.
         */
        init: function() {
            this.setupFadeInAnimations();
            this.setupHoverEffects();
        },

        /**
         * CONFIGURO ANIMACIONES DE FADE-IN
         *
         * APLICO EFECTOS DE APARICION GRADUAL A ELEMENTOS
         * COMO TARJETAS Y FILAS DE TABLA PARA SUAVIZAR LA CARGA.
         */
        setupFadeInAnimations: function() {
            const elements = document.querySelectorAll('.stats-card, .admin-table tbody tr');
            elements.forEach((element, index) => {
                element.style.opacity = '0';
                element.style.transform = 'translateY(20px)';

                setTimeout(() => {
                    element.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                    element.style.opacity = '1';
                    element.style.transform = 'translateY(0)';
                }, index * 100);
            });
        },

        /**
         * CONFIGURO EFECTOS DE HOVER
         *
         * ESTABLEZCO ANIMACIONES SUAVES PARA INTERACCIONES
         * DE HOVER EN ELEMENTOS INTERACTIVOS.
         */
        setupHoverEffects: function() {
            // EFECTO DE ELEVACION PARA TARJETAS
            document.querySelectorAll('.stats-card').forEach(card => {
                card.addEventListener('mouseenter', () => {
                    card.style.transform = 'translateY(-5px)';
                });

                card.addEventListener('mouseleave', () => {
                    card.style.transform = 'translateY(0)';
                });
            });
        }
    },

    /**
     * MODULO DE ATAJOS DE TECLADO
     *
     * MANEJO AQUI TODAS LAS FUNCIONALIDADES DE NAVEGACION
     * Y ACCIONES RAPIDAS MEDIANTE TECLADO.
     */
    keyboard: {
        /**
         * MANEJO ATAJOS GLOBALES DE TECLADO
         *
         * PROCESAO COMBINACIONES DE TECLAS PARA ACCIONES
         * RAPIDAS EN EL PANEL ADMINISTRATIVO.
         *
         * @param {KeyboardEvent} e - EVENTO DE TECLADO
         */
        handleGlobalShortcuts: function(e) {
            // CTRL/CMD + K = BUSCAR
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                const searchInput = document.querySelector('input[type="search"], input[name="searchTerm"]');
                if (searchInput) {
                    searchInput.focus();
                    searchInput.select();
                }
            }

            // ESC = CERRAR MODALES/ALERTAS
            if (e.key === 'Escape') {
                const openModal = document.querySelector('.modal.show');
                if (openModal) {
                    const closeBtn = openModal.querySelector('.btn-close');
                    if (closeBtn) closeBtn.click();
                }
            }
        }
    }
};

/**
 * INICIALIZO EL SISTEMA CUANDO EL DOM ESTA LISTO
 *
 * ESPERO A QUE EL DOM ESTE COMPLETAMENTE CARGADO ANTES
 * DE INICIALIZAR TODOS LOS MODULOS DEL SISTEMA ADMINISTRATIVO.
 */
document.addEventListener('DOMContentLoaded', function() {
    AdminFunctions.init.setup();
    AdminFunctions.init.setupGlobalEvents();
});

/**
 * EXPONGO EL OBJETO GLOBAL PARA USO EN OTRAS SCRIPTS
 *
 * PERMITO QUE OTRAS SCRIPTS ACCEDAN A LAS FUNCIONALIDADES
 * ADMINISTRATIVAS CUANDO SEA NECESARIO.
 */
window.AdminFunctions = AdminFunctions;