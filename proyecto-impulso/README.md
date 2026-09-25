# Proyecto Impulso

Prototipo de escritorio desarrollado en Java para apoyar la gestión de socios,
beneficios, cuotas, cobros y suscripciones de las sedes de Skilful. La
persistencia se realiza en una base de datos relacional MySQL.

## Alcance del prototipo

La versión del TP2 permite:

- autenticar empleados y seleccionar una sede autorizada;
- consultar, registrar y actualizar socios;
- gestionar beneficios y suscripciones;
- generar cuotas mensuales;
- calcular descuentos, recargos y saldos a favor con `BigDecimal`;
- registrar pagos dentro de una transacción;
- controlar hasta cinco intentos de cobro de Mercado Pago;
- registrar notificaciones y consultar alertas e historiales.

## Organización del código

- `vista`: ventanas y formularios Swing;
- `servicio`: reglas del negocio y coordinación de operaciones;
- `dao`: consultas y transacciones de persistencia;
- `modelo`: objetos utilizados por las demás capas;
- `conexion`: apertura centralizada de conexiones JDBC;
- `sesion`: usuario y sede activos durante la ejecución.

Los flujos principales de autenticación, socios, cobros y suscripciones siguen
la secuencia `vista -> servicio -> DAO -> MySQL`. Algunas consultas auxiliares
de lectura se conservan en sus ventanas por tratarse de un prototipo incremental.

## Requisitos

- JDK 8 o superior;
- MySQL 8;
- Maven 3.8 o superior, o una IDE Java compatible con Maven;
- MySQL Connector/J, administrado mediante `pom.xml`.

## Base de datos

1. Ejecutar `Proyecto_Impulso_BD.sql` en MySQL.
2. Crear un usuario de base de datos con permisos sobre `proyecto_impulso`.
3. Definir las variables de entorno:

```text
IMPULSO_DB_USUARIO=usuario_mysql
IMPULSO_DB_CONTRASENA=contraseña_mysql
```

Las credenciales no están guardadas en el repositorio.

## Ejecución

Importar el proyecto como proyecto Maven en Eclipse o IntelliJ IDEA y ejecutar:

```text
ar.com.skilful.vista.VentanaLogin
```

También puede compilarse desde una terminal con:

```text
mvn clean compile
```

## Pruebas

La clase `CobroServicioPrueba` verifica sin conexión a MySQL seis reglas del
cálculo de cobros: aplicación de beneficios, tarifa general, recargo de tarjeta,
saldo parcial, saldo total y bloqueo por suscripción activa.

Las pruebas de integración restantes requieren una instancia local de MySQL con
el script provisto y datos de prueba.
