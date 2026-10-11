# Resultados de verificación - TP3, primera actualización

Fecha: 08/10/2026.

## Comprobación ejecutada sobre esta versión

Se compilaron correctamente los 45 archivos Java de src/main/java y src/test/java,
con JDK 17.0.20 y compatibilidad --release 8. Se ejecutaron las aplicaciones de
pruebas sin interfaz gráfica ni conexión a MySQL:

- CobroServicioPrueba: 6/6 comprobaciones superadas.
- PoliticasCobroPrueba: 20/20 casos superados.

La salida real de consola se conserva en EVIDENCIA_PRUEBAS_TP3.txt. La compilación
se realizó directamente con el compilador de Java; no se verificó en este entorno
la resolución de dependencias Maven ni el inicio de Swing con MySQL.

## Resultados concretos - PoliticasCobroPrueba

| Caso | Entrada o condición | Resultado esperado | Resultado obtenido |
|---|---|---|---|
| P01 | Sin beneficio, efectivo, sin suscripción | GENERAL | GENERAL |
| P02 | Beneficio null, efectivo, sin suscripción | GENERAL | GENERAL |
| P03 | ESTUDIANTE, efectivo habilitado | ESTUDIANTE | ESTUDIANTE |
| P04 | CONVENIO, efectivo habilitado | CONVENIO | CONVENIO |
| P05 | ESTUDIANTE, transferencia sin beneficios | GENERAL | GENERAL |
| P06 | CONVENIO, crédito sin beneficios | GENERAL | GENERAL |
| P07 | Suscripción activa, 4 intentos, efectivo | ReglaCobroException | Excepción esperada |
| P08 | Suscripción activa, 5 intentos, efectivo | SUSCRIPCION | SUSCRIPCION |
| P09 | Suscripción activa, 5 intentos, transferencia | SUSCRIPCION | SUSCRIPCION |
| P10 | Suscripción activa, 5 intentos, crédito | ReglaCobroException | Excepción esperada |
| P11 | Suscripción activa, 5 intentos, ESTUDIANTE y efectivo | SUSCRIPCION, sin acumular beneficio | SUSCRIPCION |
| P12 | Cuota null | IllegalArgumentException | Excepción esperada |
| P13 | Medio de pago null | IllegalArgumentException | Excepción esperada |
| P14 | Política de beneficio con NINGUNO | ReglaCobroException | Excepción esperada |
| P15 | Precio y tarifa 20000, recargo 10%, sin saldo | Recargo 2000; total 22000 | 2000; 22000 |
| P16 | Precio 20000, tarifa 15000, saldo 3000 | Descuento 5000; saldo aplicado 3000; total 12000 | 5000; 3000; 12000 |
| P17 | Precio 10000, tarifa 7500, saldo 9000 | Saldo aplicado 7500; total 0 | 7500; 0 |
| P18 | Saldo -1 | IllegalArgumentException | Excepción esperada |
| P19 | Recargo -10% | IllegalArgumentException | Excepción esperada |
| P20 | Tres objetos tratados como PoliticaTarifa, con cuotas compatibles | GENERAL, ESTUDIANTE, SUSCRIPCION | GENERAL, ESTUDIANTE, SUSCRIPCION |

Las seis comprobaciones originales verifican ESTUDIANTE en efectivo, GENERAL para
convenio con crédito, 22000 de importe final para 20000 más 10%, 12000 a abonar al
aplicar 3000 de saldo a una tarifa de 15000, total cero para tarifa 7500 con saldo
9000, y bloqueo con tres intentos de suscripción.

## Aclaración de la evidencia histórica del TP2

El archivo anterior estaba fechado el 24/09/2026 y señalaba que las pruebas MySQL
quedaban por repetir. Esa descripción era anterior a la ejecución documentada por
Victoria el 27/09/2026: CorreccionesPersistenciaPrueba terminó con siete pruebas
aprobadas y datos ficticios revertidos. Las capturas se incorporaron al informe
entregado del TP2. No corresponde presentar aquella ejecución como pendiente en
el TP2 ni como una ejecución nueva del TP3.

Los siete escenarios cubren conservación del beneficio anterior, bloqueo de otro
beneficio activo, límite y numeración por cuota, notificación exacta y duplicados,
pago aprobado vinculado a la cuota, rechazo de una cuota ajena y rollback explícito.

## Pendiente de ejecutar sobre esta actualización

Repetir CorreccionesPersistenciaPrueba con MySQL en el equipo local y verificar el
flujo gráfico desde VentanaLogin. Registrar fecha y resultado real antes de marcar
estas verificaciones como aprobadas para el TP3. Las pruebas sin MySQL no acreditan
la persistencia, el control de permisos de toda la interfaz ni la aceptación de
usuarios. El rollback explícito no sustituye todas las pruebas de error provocado
del cobro presencial. Se conservan los CP pendientes declarados en el TP2.
