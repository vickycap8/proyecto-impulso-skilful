# Proyecto Impulso: transformación digital en Skilful — TP4

Prototipo operativo de escritorio desarrollado en Java Swing, con persistencia en MySQL mediante JDBC y MySQL Connector/J. Gestiona socios, beneficios, cuotas, cobros, suscripciones, alertas e historial. El proyecto está delimitado al prototipo descrito en el informe; no se integra con las API de Mercado Pago o WhatsApp.

## Contenido del proyecto

- `src/main/java/ar/com/skilful/modelo`: socios, cuotas, tarifas, pagos y otros datos del negocio.
- `servicio` y `servicio/tarifa`: validaciones, cálculos y selección de tarifa con el patrón Strategy (`PoliticaTarifa` y sus implementaciones).
- `dao`: acceso a MySQL, consultas, actualizaciones y transacciones; las ventanas no contienen sentencias SQL.
- `vista`: menú, ventanas y formularios Swing; presentan resultados y llaman a servicios o DAO.
- `conexion`, `sesion` y `excepcion`: conexión JDBC, contexto del usuario y `ReglaCobroException`.
- `src/test/java`: comprobaciones de reglas de cobro y de operaciones persistentes.
- `Proyecto_Impulso_BD.sql`: creación del esquema y carga de datos ficticios.
- `Consultas_TP2.sql`: consultas SQL complementarias.
- `RESULTADOS_PRUEBAS_TP4.md`: síntesis de las verificaciones de esta entrega.

La separación entre interfaz y persistencia está implementada. Algunas ventanas todavía coordinan operaciones llamando directamente a los DAO; la capa de servicios no cubre todos los flujos.

## Requisitos y ejecución

1. Iniciar una instalación local de MySQL y ejecutar `Proyecto_Impulso_BD.sql` en MySQL Workbench si la base aún no existe. Si ya se usa la base del TP3, no hace falta volver a ejecutar el script.
2. Definir las variables de entorno `IMPULSO_DB_USUARIO` y `IMPULSO_DB_CONTRASENA` con las credenciales de MySQL. La URL en `ConexionBD` apunta a `localhost:3306/proyecto_impulso` y tiene `useSSL=false` para la prueba local.
3. En Eclipse, usar **File > Import > Maven > Existing Maven Projects** y seleccionar esta carpeta. Ejecutar `ar.com.skilful.vista.VentanaLogin` como aplicación Java.

El script incluye cuentas de demostración, independientes de la cuenta de MySQL:

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `Admin123!` |
| Recepcionista | `recepcion` | `Recepcion123!` |
| Profesor | `profesor` | `Profesor123!` |

## Verificación

Las clases `PoliticasCobroPrueba` y `CobroServicioPrueba` se ejecutan como aplicaciones Java y comprueban reglas de negocio sin MySQL. `CorreccionesPersistenciaPrueba` ejecuta siete escenarios de integración con MySQL; crea datos ficticios y revierte cada transacción. La evidencia de ejecución en Eclipse y los resultados que corresponden a la versión entregada se documentan en el informe AP4.

La VPN, TLS y un servidor compartido entre sedes son parte de la propuesta de despliegue, no de esta instalación local.
