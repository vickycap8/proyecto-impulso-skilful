# Cambios incorporados en TP4

La versión TP4 parte del prototipo TP3 y mantiene el esquema MySQL y el flujo de cobros existente.

- Se trasladaron las consultas y modificaciones SQL de las ventanas a `AlertaDAO`, `BeneficioDAO`, `HistorialCobrosDAO`, `SocioDAO` y `SuscripcionDAO`.
- Se conservaron `CuotaDAO`, `PagoDAO` y `UsuarioDAO` para los flujos correspondientes.
- Se actualizaron las ventanas de alertas, beneficios, socios, historial y suscripciones para llamar a los DAO sin ejecutar SQL.
- Las renovaciones de beneficios y el registro de resultados de suscripciones usan transacciones y revierten cambios ante errores.
- La selección de tarifas usa el patrón Strategy con la clase abstracta `PoliticaTarifa` y tres políticas concretas; el precio se consulta en MySQL.
- Se usan arreglos de tamaño fijo para configurar columnas de la interfaz y `ArrayList` para reunir resultados de consultas cuyo número de filas varía.
- Se conservaron las clases de pruebas de lógica y persistencia en `src/test/java`.

La aplicación registra resultados que el personal verifica en Mercado Pago y comunicaciones que realiza por separado; no hace débitos ni envía WhatsApp automáticamente. La instalación probada es local y no implementa la infraestructura VPN/TLS propuesta para las cuatro sedes.
