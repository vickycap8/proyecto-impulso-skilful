# Proyecto Impulso — TP3

La versión completa del TP3 está en [proyecto-impulso-tp3](proyecto-impulso-tp3). Esa carpeta reúne el proyecto Maven con `pom.xml`, `src/main/java` y `src/test/java`. Continúa el TP2 de Skilful e incorpora políticas de tarifa con abstracción, herencia y polimorfismo.

## Abrir y ejecutar en Eclipse

1. Descargar el repositorio con **Code > Download ZIP** y extraerlo.
2. En Eclipse: **File > Import > Maven > Existing Maven Projects**. Seleccionar la carpeta `proyecto-impulso-tp3`, que contiene `pom.xml`.
3. Usar JDK 17. Configurar `IMPULSO_DB_USUARIO` e `IMPULSO_DB_CONTRASENA` con las credenciales de la instalación local de MySQL.
4. Si todavía no existe la base `proyecto_impulso`, ejecutar [Proyecto_Impulso_BD.sql](Proyecto_Impulso_BD.sql) en MySQL. Si ya funciona la base del TP2, no es necesario recrearla.
5. Ejecutar `ar.com.skilful.vista.VentanaLogin` como **Java Application**.

Las cuentas ficticias iniciales de la aplicación son `admin`, `recepcion` y `profesor`, con las contraseñas indicadas en el script SQL, si no fueron modificadas en la base local. Son independientes de las credenciales de MySQL.

## Pruebas y documentación

Las clases `CobroServicioPrueba` y `PoliticasCobroPrueba` se ejecutan desde `src/test/java` con **Run As > Java Application**, sin MySQL. `CorreccionesPersistenciaPrueba` requiere MySQL. Son aplicaciones Java con método `main`, no pruebas JUnit; `mvn test` no reemplaza su ejecución explícita. Consultar [resultados](proyecto-impulso-tp3/RESULTADOS_PRUEBAS.md), [cambios de POO](proyecto-impulso-tp3/CAMBIOS_TP3.md) y el [README del proyecto](proyecto-impulso-tp3/README.md).

La carpeta `proyecto-impulso` corresponde al TP2. Las carpetas `Proyecto_Impulso_TP3_subida_1` y `Proyecto_Impulso_TP3_subida_2`, y los archivos Java sueltos en la raíz, son cargas preliminares. Para revisar el TP3, usar únicamente [proyecto-impulso-tp3](proyecto-impulso-tp3).
