# NubeMedica
Projecto Fullstack
Se utiliza SpringBoot 3.5.14
Se utiliza Java version 21
Se utiliza Mockito y J5UNIT
se utiliza Swagger UI


Les Microservicios entregados en esta experiencia de aprendizaje seran los 10 microservicios.


Api-GateWay -> port 8081
Service-Login -> port 8082
Service-Direccion -> port 8083
Service-Calendario -> 8086
Service-RegistroDoctor -> port 8085
Service-Pacientes -> port 8084
Service-EstadoCita -> port 8087
Service-FichaMedica -> port 8088
Service-reportes -> port 8089
Service-notificaciones -> port 8090
Service-telemedicina -> port 8091

PROBLEMATICA DEL CLIENTE
Problemática: 
Un profesional del área de salud ha tenido dificultades para poder organizar información de sus pacientes, y las horas que tiene ocupadas en el dia, dado que lo hace todo de manera manual en una diario personal, este profesional requiere una solución tecnológica para este problema


El MicroServicio Direccion sirve para mantener todas las Direcciones creadas en un solo lugar para poder recuperarlas o crear nuevas.

El MicroServicio RegistroDoctor Sirve para poder Registrar a los Doctores(Usuarios) de este producto

El MicroServicio Pacientes Sirve para poder que un Doctor(Usuario) pueda crear a su paciente.

El MicroServicio Calendario Sirve para que el usuario pueda Ver tanto sus Citas medicas como Actividades Personales.

El MicroServicio Login Sirve para que el usuario tenga una manera de ingresar a el sistema de manera segura.

El microServicio EstadoCita Sirve para Otorgarle un Tipo de estado a las citas medicas (Agendado,Cancelado,Realizado) para que el usuario sepa si las citas ocurrieron o no.

El microservicio fichamedica se encarga de tener una ficha medica coherente de los pacientes de un medico

el microservicio reportes se encarga de que cada doctor pueda agregar reportes de cada una de sus citas medicas

el microservicio notificaciones se encarga de entregar una notificacion de cuando sera su cita medica o actividad personal

el microservicio telemedicina se encarga de crear un link de telemedicina para la cita medica asociada.

El modelo de base de datos se encuentra dentro de la carpeta Documentos para poder hacer las verificaciones correspondientes.

Se necesitan agregar datos previos que son genericos para las tablas Region ,Comunas, EstadoCitas y tipoEvento
-- ==========================================================
-- INSERTS PARA LA TABLA REGION
-- ==========================================================
INSERT INTO REGION (id, nombre) VALUES (1, 'Región de Arica y Parinacota');
INSERT INTO REGION (id, nombre) VALUES (2, 'Región de Tarapacá');
INSERT INTO REGION (id, nombre) VALUES (3, 'Región de Antofagasta');
INSERT INTO REGION (id, nombre) VALUES (4, 'Región de Atacama');
INSERT INTO REGION (id, nombre) VALUES (5, 'Región de Coquimbo');
INSERT INTO REGION (id, nombre) VALUES (6, 'Región de Valparaíso');
INSERT INTO REGION (id, nombre) VALUES (7, 'Región Metropolitana de Santiago');
INSERT INTO REGION (id, nombre) VALUES (8, 'Región del Libertador Gral. Bernardo O''Higgins');
INSERT INTO REGION (id, nombre) VALUES (9, 'Región del Maule');
INSERT INTO REGION (id, nombre) VALUES (10, 'Región del Biobío');


-- ==========================================================
-- INSERTS PARA LA TABLA COMUNA 
-- (Se asocia cada una a su respectiva region_id)
-- ==========================================================
INSERT INTO COMUNA (nombre, region_id) VALUES ('Arica', 1);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Iquique', 2);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Antofagasta', 3);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Copiapó', 4);
INSERT INTO COMUNA (nombre, region_id) VALUES ('La Serena', 5);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Viña del Mar', 6);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Santiago', 7);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Rancagua', 8);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Talca', 9);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Concepción', 10);
-- Comunas adicionales para la Región Metropolitana (ID 7)
INSERT INTO COMUNA (nombre, region_id) VALUES ('Providencia', 7);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Las Condes', 7);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Maipú', 7);
INSERT INTO COMUNA (nombre, region_id) VALUES ('Puente Alto', 7);

-- ==========================================================
-- INSERTS PARA LA TABLA TIPO_ESTADOCITA
-- ==========================================================

INSERT INTO tipo_estadocita (nombre_estado) VALUES ('Agendada');
INSERT INTO tipo_estadocita (nombre_estado) VALUES ('Confirmada');
INSERT INTO tipo_estadocita (nombre_estado) VALUES ('En Curso');
INSERT INTO tipo_estadocita (nombre_estado) VALUES ('Completada');
INSERT INTO tipo_estadocita (nombre_estado) VALUES ('Cancelada');
INSERT INTO tipo_estadocita (nombre_estado) VALUES ('No Asistió');
INSERT INTO tipo_estadocita (nombre_estado) VALUES ('Reprogramada');

-- ==========================================================
-- INSERTS PARA LA TABLA TIPO_EVENTO
-- ==========================================================

-- Cita Médica: Usualmente se usa Rojo para resaltar
INSERT INTO tipo_evento (nombre_tipo, color_tipo) 
VALUES ('Cita Médica', '#E53935');

-- Actividad Personal: Usualmente se usa Azul o Verde
INSERT INTO tipo_evento (nombre_tipo, color_tipo) 
VALUES ('Actividad Personal', '#1E88E5');

Pagina ruta swagger: localhost:8081/swagger-ui.hhtml

Service RegistroDoctor prueba de post se realizo con los siguientes datos http://localhost:8081/api/v1/doctores
{
    "runDoctor": "11.111.111-1",
    "priNombre": "Carlos",
    "segNombre": "Alberto",
    "apaPaterno": "Pérez",
    "apaMaterno": "Soto",
    "correo": "carlos.perez@nubemedica.cl",
    "telefono": "911111111",
    "contrasena": "doctor123",
    "direccion": {
        "nombre": "Avenida Providencia 1234",
        "comunaId": 1
    }
}


Service registroDoctor prueba de put doctores localhost:8081/api/v1/doctores/11.111.111-1

{
    "runDoctor": "11.111.111-1",
    "priNombre": "Carlos",
    "segNombre": "Alberto",
    "apaPaterno": "Pérez",
    "apaMaterno": "Soto,
    "telefono": "922222222",
    "correo": "carlos.perez@nubemedica.cl",
    "idDireccion": 2,
    "direccion": {
        "nombre": "Calle Valparaíso 567",
        "comunaId": 4
    }
}

Service Calendario

  Calendario crear cita metodo post localhost:8081/api/v1/calendario/citas

  {
    "runPaciente": "19.666.777-4",
    "fecha": "2026-10-30",
    "hora": "19:00:00",
    "motivoConsulta": "Coso"
}

  Calendario Actualizar Cita Medica  localhost:8081/api/v1/calendario/citas/7
    {
        "idEvento": 7,
        "runPaciente": "18.111.222-3",
        "fecha": "2026-10-25",
        "hora": "14:00:00",
        "motivoConsulta": "Control de rutina anual",
        "estadoCitaMedica": {
            "nombreEstado": "Terminada",
            "observaciones": "el papu"
        }
    }

 Servicio registro paciente POST localhost:8081/api/v1/pacientes

  {
      "runPaciente": "19.666.777-4",
      "priNombre": "Catalina",
      "segNombre": "Belén",
      "apaPaterno": "Herrera",
      "apaMaterno": "Castro",
      "correo": "cata.herrera@email.com",
      "numTelefono": "966677774",
      "direccion": {
          "nombre": "Providencia 1280",
          "comunaId": 2
      }
  }
  
 Servicio registro paciente put localhost:8081/api/v1/pacientes/19.666.777-4
  {
    "runPaciente": "19.666.777-4",
    "correo": "tomas.reyes@email.com",
    "priNombre": "Tomás",
    "segNombre": "Ignacio",
    "apaPaterno": "Reyes",
    "apaMaterno": "Guzman",
    "numTelefono": "777231332",
    "idDireccion": 9,
    "direccion": {
        "id": 9,
        "nombre": "Ricardo Lyon 777",
        "comunaId": 2
    }
}
