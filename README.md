Proyecto Impulso: transformación digital en Skilful

Prototipo académico desarrollado para optimizar la gestión de cobranzas, beneficios y suscripciones de una cadena de gimnasios con cuatro sedes.

Funciones principales

* Inicio de sesión con usuarios, roles y selección de sede.
* Registro, búsqueda y modificación de socios.
* Administración de beneficios estudiantiles y empresariales.
* Cálculo automático de descuentos y recargos.
* Registro de pagos y saldos a favor.
* Consulta del historial de cobros.
* Creación y seguimiento de suscripciones.
* Registro de intentos aprobados y rechazados.
* Control de notificaciones pendientes.
* Generación automática de cuotas y actualización de vencimientos.
* Consulta de alertas.

Tecnologías utilizadas

* Java
* Java Swing
* Eclipse IDE
* Maven
* JDBC
* MySQL

Contenido del repositorio

* `proyecto-impulso/src/main/java`: código fuente del prototipo.
* `proyecto-impulso/pom.xml`: configuración y dependencias de Maven.
* `Proyecto_Impulso_BD.sql`: creación de la base de datos, tablas, relaciones, vistas y datos ficticios.

Configuración

1. Ejecutar `Proyecto_Impulso_BD.sql` desde MySQL con un usuario administrador.
2. Crear el usuario limitado de la aplicación siguiendo las instrucciones comentadas en el script.
3. Configurar las siguientes variables de entorno en Eclipse:

```text
IMPULSO_DB_USUARIO
IMPULSO_DB_CONTRASENA
```

4. Importar la carpeta `proyecto-impulso` como un proyecto Maven.
5. Ejecutar la clase `VentanaLogin.java`.

Las credenciales privadas de MySQL no se encuentran almacenadas en el código fuente.

Alcance

Esta versión corresponde a un prototipo operativo realizado con información ficticia. El resultado de los cobros de Mercado Pago se consulta externamente y luego se registra en el sistema. Una futura versión podría integrar directamente la API de la plataforma y automatizar el envío de notificaciones.

Autora

Victoria Capozzolo

