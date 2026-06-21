-- Eliminacion de bases de datos si existen.
DROP DATABASE IF EXISTS db_direccion;
DROP DATABASE IF EXISTS db_registrodoctor;
DROP DATABASE IF EXISTS db_pacientes;
DROP DATABASE IF EXISTS db_login;
DROP DATABASE IF EXISTS db_estadocita;
DROP DATABASE IF EXISTS db_telemedicina;
DROP DATABASE IF EXISTS db_calendario;
DROP DATABASE IF EXISTS db_fichamedica;
DROP DATABASE IF EXISTS db_reportes;
DROP DATABASE IF EXISTS db_notificaciones;

-- ==========================================================
-- 1. DATABASE: db_direccion (Service-Direccion)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_direccion;
USE db_direccion;

CREATE TABLE REGION (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE COMUNA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    region_id BIGINT,
    FOREIGN KEY (region_id) REFERENCES REGION(id)
);

CREATE TABLE DIRECCION (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    comuna_id BIGINT,
    FOREIGN KEY (comuna_id) REFERENCES COMUNA(id)
);

-- Inserts Genericos
INSERT INTO REGION (id, nombre) VALUES (1, 'Región de Arica y Parinacota'), (7, 'Región Metropolitana'), (10, 'Región del Biobío');
INSERT INTO COMUNA (id, nombre, region_id) VALUES (1, 'Arica', 1), (7, 'Santiago', 7), (10, 'Concepción', 10);
-- 3 Datos de Negocio
INSERT INTO DIRECCION (id, nombre, comuna_id) VALUES (1, 'Avenida Providencia 1234', 7), (2, 'Calle Prat 567', 10), (3, 'Azolas 999', 1);


-- ==========================================================
-- 2. DATABASE: db_registrodoctor (Service-RegistroDoctor)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_registrodoctor;
USE db_registrodoctor;

CREATE TABLE doctor (
    run_doctor VARCHAR(20) PRIMARY KEY,
    pri_nombre VARCHAR(100) NOT NULL,
    seg_nombre VARCHAR(100),
    apa_paterno VARCHAR(100) NOT NULL,
    apa_materno VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) UNIQUE NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    id_direccion BIGINT -- Referencia lógica a db_direccion
);

CREATE TABLE atenciones_doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_paciente VARCHAR(20) NOT NULL,
    run_doctor VARCHAR(20),
    UNIQUE (run_paciente, run_doctor),
    FOREIGN KEY (run_doctor) REFERENCES doctor(run_doctor)
);





-- ==========================================================
-- 3. DATABASE: db_pacientes (Service-Pacientes)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_pacientes;
USE db_pacientes;

CREATE TABLE paciente (
    run_paciente VARCHAR(20) PRIMARY KEY,
    correo VARCHAR(100) UNIQUE NOT NULL,
    pri_nombre VARCHAR(100) NOT NULL,
    seg_nombre VARCHAR(100),
    apa_paterno VARCHAR(100) NOT NULL,
    apa_materno VARCHAR(100) NOT NULL,
    num_telefono VARCHAR(20) UNIQUE NOT NULL,
    id_direccion BIGINT
);

INSERT INTO paciente VALUES ('19.666.777-4', 'cata.herrera@email.com', 'Catalina', 'Belén', 'Herrera', 'Castro', '966677774', 1);
INSERT INTO paciente VALUES ('18.111.222-3', 'juan.valdes@email.com', 'Juan', 'Pablo', 'Valdés', 'Rojas', '988887777', 2);
INSERT INTO paciente VALUES ('17.555.444-k', 'marta.sanchez@email.com', 'Marta', 'Elena', 'Sánchez', 'Díaz', '955554444', 3);


-- ==========================================================
-- 4. DATABASE: db_login (Service-Login)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_login;
USE db_login;

CREATE TABLE login_usuario (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100) UNIQUE NOT NULL,
    run_doctor VARCHAR(20) UNIQUE NOT NULL,
    contrasena VARCHAR(255),
    num_telefono VARCHAR(20) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE token (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo_usuario VARCHAR(100),
    token_generado VARCHAR(512),
    refresh_token VARCHAR(512),
    fecha_exp DATETIME,
    fecha_exp_refresh DATETIME,
    activo BOOLEAN,
    run_doctor VARCHAR(20)
);

CREATE TABLE consulta_token (
    id_consulta BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512),
    id_usuario BIGINT,
    nom_api VARCHAR(100),
    fecha_consulta DATETIME,
    FOREIGN KEY (id_usuario) REFERENCES login_usuario(id_usuario)
);




-- ==========================================================
-- 5. DATABASE: db_estadocita (Service-EstadoCita)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_estadocita;
USE db_estadocita;

CREATE TABLE tipo_estadocita (
    id_tipo_estado BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(100)
);

CREATE TABLE estado_cita (
    id_estado_cita_medica BIGINT AUTO_INCREMENT PRIMARY KEY,
    observacion VARCHAR(255),
    tipoestadocita_id BIGINT,
    FOREIGN KEY (tipoestadocita_id) REFERENCES tipo_estadocita(id_tipo_estado)
);

INSERT INTO tipo_estadocita (id_tipo_estado, nombre_estado) VALUES (1, 'Agendada'), (2, 'Completada'), (3, 'Cancelada');
INSERT INTO estado_cita (observacion, tipoestadocita_id) VALUES ('Pendiente de confirmación', 1), ('Todo OK', 2), ('Paciente no llegó', 3);


-- ==========================================================
-- 6. DATABASE: db_telemedicina (Service-Telemedicina)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_telemedicina;
USE db_telemedicina;

CREATE TABLE sesion_telemedicina (
    id_sesion_telemedicina BIGINT AUTO_INCREMENT PRIMARY KEY,
    link_acceso VARCHAR(255) NOT NULL,
    codigo_acceso VARCHAR(10) NOT NULL
);




-- ==========================================================
-- 7. DATABASE: db_calendario (Service-Calendario)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_calendario;
USE db_calendario;

CREATE TABLE tipo_evento (
    id_tipo_evento BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(100),
    color_tipo VARCHAR(20)
);

CREATE TABLE evento (
    id_evento BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE,
    hora TIME,
    id_tipo_evento BIGINT,
    run_doctor VARCHAR(20),
    FOREIGN KEY (id_tipo_evento) REFERENCES tipo_evento(id_tipo_evento)
);

CREATE TABLE cita_medica (
    id_evento BIGINT PRIMARY KEY,
    run_paciente VARCHAR(20),
    id_estado_cita_medica BIGINT,
    motivo_consulta VARCHAR(255),
    id_sesion_telemedicina BIGINT,
    FOREIGN KEY (id_evento) REFERENCES evento(id_evento)
);

CREATE TABLE actividad_personal (
    id_evento BIGINT PRIMARY KEY,
    nombre_actividad VARCHAR(100),
    descripcion TEXT,
    FOREIGN KEY (id_evento) REFERENCES evento(id_evento)
);



-- ==========================================================
-- 8. DATABASE: db_fichamedica (Service-FichaMedica)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_fichamedica;
USE db_fichamedica;

CREATE TABLE ficha_medica (
    id_ficha_medica BIGINT AUTO_INCREMENT PRIMARY KEY,
    historial_familiar TEXT,
    Diagnostico TEXT,
    run_paciente VARCHAR(20) NOT NULL,
    run_doctor VARCHAR(20) NOT NULL,
    UNIQUE (run_paciente, run_doctor)
);

CREATE TABLE farmacos_recetados (
    id_farmaco BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_farmaco VARCHAR(100),
    dosis FLOAT,
    fichamedica_id BIGINT,
    FOREIGN KEY (fichamedica_id) REFERENCES ficha_medica(id_ficha_medica)
);

CREATE TABLE contacto_profesional (
    id_contacto_pro BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100),
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    id_ficha_medica BIGINT,
    FOREIGN KEY (id_ficha_medica) REFERENCES ficha_medica(id_ficha_medica)
);

CREATE TABLE telefono_emergencia (
    id_telefono BIGINT AUTO_INCREMENT PRIMARY KEY,
    num_telefono VARCHAR(20),
    descripcion VARCHAR(100),
    id_ficha_medica BIGINT,
    FOREIGN KEY (id_ficha_medica) REFERENCES ficha_medica(id_ficha_medica)
);




-- ==========================================================
-- 9. DATABASE: db_reportes (Service-Reportes)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_reportes;
USE db_reportes;

CREATE TABLE detalle_reporte (
    id_detalle_reporte BIGINT AUTO_INCREMENT PRIMARY KEY,
    descripcion TEXT
);

CREATE TABLE reporte (
    id_reporte BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_reporte VARCHAR(100),
    fecha_reporte DATE,
    id_ficha_medica BIGINT,
    id_detalle_reporte BIGINT,
    FOREIGN KEY (id_detalle_reporte) REFERENCES detalle_reporte(id_detalle_reporte)
);



-- ==========================================================
-- 10. DATABASE: db_notificaciones (Service-Notificaciones)
-- ==========================================================
CREATE DATABASE IF NOT EXISTS db_notificaciones;
USE db_notificaciones;

CREATE TABLE notificaciones (
    id_notificacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo_destino VARCHAR(100) NOT NULL,
    asunto VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha_envio DATE NOT NULL,
    hora_envio TIME NOT NULL,
    estado_envio BOOLEAN NOT NULL,
    id_evento BIGINT NOT NULL
);

