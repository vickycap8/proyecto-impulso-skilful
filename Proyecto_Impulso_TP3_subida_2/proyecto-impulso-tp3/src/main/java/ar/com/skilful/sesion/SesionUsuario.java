package ar.com.skilful.sesion;

public final class SesionUsuario {

    private static int idUsuario;
    private static String nombreCompleto;
    private static String nombreUsuario;
    private static String rol;

    private static int idSede;
    private static String nombreSede;

    private static boolean sesionIniciada;

    private SesionUsuario() {
        // Impide crear objetos de esta clase.
    }

    public static void iniciarSesion(
            int idUsuario,
            String nombreCompleto,
            String nombreUsuario,
            String rol,
            int idSede,
            String nombreSede) {

        SesionUsuario.idUsuario = idUsuario;
        SesionUsuario.nombreCompleto = nombreCompleto;
        SesionUsuario.nombreUsuario = nombreUsuario;
        SesionUsuario.rol = rol;
        SesionUsuario.idSede = idSede;
        SesionUsuario.nombreSede = nombreSede;
        SesionUsuario.sesionIniciada = true;
    }

    public static void cerrarSesion() {
        idUsuario = 0;
        nombreCompleto = null;
        nombreUsuario = null;
        rol = null;
        idSede = 0;
        nombreSede = null;
        sesionIniciada = false;
    }

    public static boolean puedeGestionarSocios() {
        return esAdministrador()
            || esRecepcionista()
            || esProfesor();
    }

    public static boolean puedeRegistrarCobros() {
        return esAdministrador()
            || esRecepcionista()
            || esProfesor();
    }

    public static boolean puedeGestionarBeneficios() {
        return esAdministrador() || esRecepcionista();
    }

    public static boolean puedeGestionarSuscripciones() {
        return esAdministrador() || esRecepcionista();
    }

    public static boolean puedeConsultarAlertas() {
        return esAdministrador() || esRecepcionista();
    }

    public static boolean esAdministrador() {
        return "ADMINISTRADOR".equalsIgnoreCase(rol);
    }

    public static boolean esRecepcionista() {
        return "RECEPCIONISTA".equalsIgnoreCase(rol);
    }

    public static boolean esProfesor() {
        return "PROFESOR".equalsIgnoreCase(rol);
    }

    public static int getIdUsuario() {
        return idUsuario;
    }

    public static String getNombreCompleto() {
        return nombreCompleto;
    }

    public static String getNombreUsuario() {
        return nombreUsuario;
    }

    public static String getRol() {
        return rol;
    }

    public static int getIdSede() {
        return idSede;
    }

    public static String getNombreSede() {
        return nombreSede;
    }

    public static boolean isSesionIniciada() {
        return sesionIniciada;
    }
}
