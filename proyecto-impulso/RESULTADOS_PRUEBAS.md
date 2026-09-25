# Resultados de verificación del prototipo TP2

Fecha de verificación: 24 de septiembre de 2026.

## Compilación

El código fuente de `src/main/java` fue compilado con Java 17 utilizando
compatibilidad con Java 8 (`--release 8`). Resultado: compilación correcta, sin
errores.

## Pruebas unitarias de la lógica de cobros

Se ejecutó `CobroServicioPrueba` con seis escenarios:

1. aplicación del beneficio estudiantil al pagar en efectivo;
2. uso de tarifa general cuando el medio no admite beneficios;
3. cálculo del recargo del 10 % para tarjeta de crédito;
4. aplicación parcial de saldo a favor;
5. aplicación total de saldo sin producir importes negativos;
6. bloqueo del pago presencial mientras una suscripción no alcanzó el quinto
   intento.

Resultado: **6 pruebas superadas de 6 ejecutadas**.

Las pruebas de integración con MySQL deben repetirse en el equipo de entrega
después de importar `Proyecto_Impulso_BD.sql` y configurar las variables de
entorno de conexión.
