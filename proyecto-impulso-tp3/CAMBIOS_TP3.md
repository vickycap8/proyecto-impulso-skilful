# TP3: primera actualización del código

## Alcance de esta iteración

Se reemplazó la decisión de tarifa dentro de CobroServicio por objetos de política.
No se cambiaron los importes de MySQL ni se codificaron porcentajes estudiantiles
fijos. La selección devuelve un código; CuotaDAO sigue obteniendo su tarifa vigente.
El cálculo monetario continúa utilizando BigDecimal.

## Evidencia de programación orientada a objetos

| Concepto | Aplicación concreta |
|---|---|
| Abstracción | PoliticaTarifa declara aplica y resolverCodigo, comunes a las tres reglas. No puede instanciarse. |
| Herencia | TarifaGeneral, TarifaConBeneficio y TarifaSuscripcion extienden PoliticaTarifa. Reutilizan seleccionar y la validación común. |
| Polimorfismo | SelectorPoliticaTarifa recorre List<PoliticaTarifa>; aplica y resolverCodigo ejecutan el comportamiento del objeto concreto. CobroServicio utiliza el selector en el flujo real de VentanaCobros. |
| Encapsulamiento | descripcion y politicas son privados y finales; la lista es inmodificable y no se expone. Las clases de modelo ya poseen atributos privados y métodos de acceso. |
| Constructores | Cada política llama a super con su descripción; el selector crea las tres instancias con new. CobroServicio crea su selector al construirse. |
| Condicionales | Cada regla verifica beneficio, suscripción o medio permitido. |
| Repetición y colección | El for-each recorre una lista de reglas. No es necesario introducir pilas o colas artificialmente. |
| Excepciones | ReglaCobroException identifica restricciones comerciales; IllegalArgumentException rechaza datos ausentes. La ventana captura estas excepciones mediante su manejo existente de IllegalStateException e IllegalArgumentException. SQLException sigue representando fallos de persistencia. |
| Menú | VentanaPrincipal conserva la selección gráfica de funciones. |

La regla de suscripción tiene prioridad: antes de cinco intentos bloquea el pago
alternativo y, desde el quinto, solo permite efectivo o transferencia. La política
de beneficio utiliza el beneficio activo y el indicador admiteBeneficio del medio.
Si no corresponden esas modalidades, se selecciona GENERAL. Los datos de medios
provistos en MySQL determinan cuáles admiten beneficios.

## Archivos de código nuevos

- excepcion/ReglaCobroException.java
- servicio/tarifa/PoliticaTarifa.java
- servicio/tarifa/TarifaGeneral.java
- servicio/tarifa/TarifaConBeneficio.java
- servicio/tarifa/TarifaSuscripcion.java
- servicio/tarifa/SelectorPoliticaTarifa.java
- src/test/java/ar/com/skilful/servicio/PoliticasCobroPrueba.java

Se modificó CobroServicio para delegar la selección de tarifa. El artifactId Maven
es proyecto-impulso-tp3 para importarlo junto al proyecto anterior. Se mantuvieron
los paquetes Java, el controlador JDBC y las correcciones de persistencia del TP2.

## Pendientes para completar el TP3

- Repetir las siete pruebas MySQL y los recorridos de interfaz sobre esta versión.
- Continuar la extracción de SQL desde ventanas hacia servicios y DAO.
- Elaborar el diagrama UML de despliegue con nodos y artefactos.
- Actualizar el DER con tipos de datos, PK y FK legibles.
- Presentar los prototipos de ventanas y explicar su relación con la interfaz.
- Preparar el informe conforme a la consigna y actualizar GitHub con la versión final.

Esta entrega de código es la primera iteración del TP3; no declara terminada toda
la arquitectura ni todas las funciones previstas en el sistema.
