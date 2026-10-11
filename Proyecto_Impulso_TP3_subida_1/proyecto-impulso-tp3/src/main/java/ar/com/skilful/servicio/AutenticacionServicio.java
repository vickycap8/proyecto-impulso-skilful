package ar.com.skilful.servicio;

import ar.com.skilful.dao.UsuarioDAO;
import ar.com.skilful.modelo.Sede;
import ar.com.skilful.modelo.Usuario;

import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AutenticacionServicio {

    private final UsuarioDAO usuarioDAO;

    public AutenticacionServicio() {
        this(new UsuarioDAO());
    }

    public AutenticacionServicio(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String nombreUsuario, char[] contrasena)
            throws Exception {

        Usuario usuario = usuarioDAO.buscarActivoPorNombre(nombreUsuario);

        if (usuario == null || !verificarContrasena(contrasena, usuario)) {
            return null;
        }

        return usuario;
    }

    public List<Sede> listarSedesAutorizadas(int idUsuario)
            throws SQLException {

        return usuarioDAO.listarSedesAutorizadas(idUsuario);
    }

    private boolean verificarContrasena(char[] contrasena, Usuario usuario)
            throws Exception {

        byte[] salt = Base64.getDecoder().decode(usuario.getClaveSalt());
        byte[] hashEsperado =
            Base64.getDecoder().decode(usuario.getClaveHash());

        PBEKeySpec especificacion = new PBEKeySpec(
            contrasena,
            salt,
            usuario.getClaveIteraciones(),
            hashEsperado.length * 8
        );

        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            );

            byte[] hashCalculado =
                fabrica.generateSecret(especificacion).getEncoded();

            return MessageDigest.isEqual(hashEsperado, hashCalculado);
        } finally {
            especificacion.clearPassword();
        }
    }
}
