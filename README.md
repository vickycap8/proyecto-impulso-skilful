# Proyecto Impulso TP3 - primera actualización

Aplicación de escritorio Java Swing con persistencia MySQL. Continúa el TP2 de
Skilful y conserva sus funciones y datos. Esta versión incorpora políticas de
selección de tarifas utilizando abstracción, herencia y polimorfismo.

## Ejecución en Eclipse

1. Descomprimir el ZIP en una carpeta nueva, sin sobrescribir el proyecto TP2.
2. File > Import > Maven > Existing Maven Projects.
3. Seleccionar la carpeta `proyecto-impulso-tp3` que contiene `pom.xml` y finalizar.
4. Usar el JDK que ya ejecutaba el TP2. El código conserva compatibilidad Java 8;
   esta actualización fue compilada con JDK 17, `--release 8`.
5. Configurar en Run Configurations > Java Application > Environment las variables
   `IMPULSO_DB_USUARIO` e `IMPULSO_DB_CONTRASENA` con los valores de la instalación
   local, si no están definidas en el entorno de Windows.
6. Ejecutar `ar.com.skilful.vista.VentanaLogin` como Java Application.

Si la base `proyecto_impulso` del TP2 ya funciona, no hay que ejecutar otra vez el
script SQL. No cambió el esquema. El script de la carpeta superior se incluye para
instalaciones nuevas; `Consultas_TP2.sql` contiene las consultas de referencia.
El controlador Connector/J conserva la versión del proyecto recibido en el POM.
Su descarga por Maven no fue repetida en esta verificación.

Cuentas ficticias iniciales: admin / Admin123!, recepcion / Recepcion123!,
profesor / Profesor123! (si no se modificaron en la base local).
Estas cuentas del programa son independientes de las credenciales de MySQL.

## Paquetes y responsabilidades

- `vista`: formularios y menú de selección gráfico. Aún contiene SQL de beneficios,
  intentos, notificaciones y algunas consultas; la separación en capas es parcial.
- `servicio`: validaciones y coordinación de cobros, autenticación, socios y suscripciones.
- `servicio.tarifa`: clase abstracta y tres políticas concretas de selección de tarifa.
- `modelo`: clases de datos como Cuota, MedioPago, Pago y ResultadoCobro.
- `dao`: consultas y transacciones que ya fueron extraídas de las ventanas.
- `conexion`: acceso JDBC a MySQL local.
- `sesion`: usuario y sede de trabajo.
- `excepcion`: ReglaCobroException para operaciones impedidas por reglas comerciales.

La interfaz sigue siendo Swing. El menú de VentanaPrincipal abre las funciones
según el rol. Mercado Pago y WhatsApp se consultan externamente: la aplicación
registra resultados y comunicaciones, sin ejecutar débitos ni enviar mensajes.

## Verificación

Ejecutar desde `src/test/java` con Run As > Java Application:

- `ar.com.skilful.servicio.CobroServicioPrueba`: seis comprobaciones originales,
  sin MySQL.
- `ar.com.skilful.servicio.PoliticasCobroPrueba`: veinte casos de esta actualización,
  sin MySQL. Debe terminar con `Pruebas de políticas de cobro superadas: 20/20.`
- `ar.com.skilful.vista.CorreccionesPersistenciaPrueba`: siete pruebas de integración;
  requiere MySQL y las variables de conexión. Usa datos ficticios y los revierte.

Estas clases se ejecutan como aplicaciones Java, no con JUnit. `mvn test` no
sustituye la ejecución explícita de sus métodos main. No necesitan activar -ea,
porque comprueban los resultados y lanzan AssertionError directamente.

Ver `RESULTADOS_PRUEBAS.md` para distinguir lo verificado ahora, las evidencias
históricas del TP2 y lo pendiente. `CAMBIOS_TP3.md` explica la aplicación de POO.
