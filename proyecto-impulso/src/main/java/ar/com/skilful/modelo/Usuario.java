package ar.com.skilful.modelo;

public class Usuario {

    private final int id;
    private final String nombreCompleto;
    private final String nombreUsuario;
    private final String rol;
    private final String claveHash;
    private final String claveSalt;
    private final int claveIteraciones;

    public Usuario(
            int id,
            String nombreCompleto,
            String nombreUsuario,
            String rol,
            String claveHash,
            String claveSalt,
            int claveIteraciones) {

        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.claveHash = claveHash;
        this.claveSalt = claveSalt;
        this.claveIteraciones = claveIteraciones;
    }

    public int getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public String getClaveSalt() {
        return claveSalt;
    }

    public int getClaveIteraciones() {
        return claveIteraciones;
    }
}
