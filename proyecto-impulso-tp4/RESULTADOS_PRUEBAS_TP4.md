# Resultados de pruebas de Proyecto Impulso — TP4

## Verificaciones de lógica Java

`PoliticasCobroPrueba` completó 20/20 comprobaciones y `CobroServicioPrueba` completó 6/6. Ambas clases se ejecutan como aplicaciones Java sin conexión a MySQL. La evidencia y los resultados esperados se presentan en el informe AP4.

## Verificaciones de persistencia MySQL

La captura de Eclipse del 04/11/2026 muestra la ejecución de `CorreccionesPersistenciaPrueba` sobre el proyecto TP4 y la salida: `Pruebas de persistencia superadas: 7/7. Datos ficticios revertidos.`

| Caso | Resultado comprobado |
|---|---|
| PI01 | La renovación conserva el historial del certificado y crea el nuevo beneficio. |
| PI02 | Otro beneficio activo impide duplicar la renovación. |
| PI03 | Los intentos se numeran por cuota; se rechaza el sexto. |
| PI04 | La notificación corresponde al intento y período elegidos. |
| PI05 | El intento aprobado paga solo la cuota seleccionada y no duplica el pago. |
| PI06 | Se rechaza una cuota ajena a la suscripción. |
| PI07 | Un `rollback` revierte la renovación, el pago y el intento. |

La clase prepara datos ficticios y ejecuta cada caso dentro de una transacción que revierte al finalizar. Estas pruebas no realizan débitos ni envían mensajes externos. Tampoco sustituyen los casos funcionales CP01–CP12 cuyo estado se detalla por separado en el informe.
