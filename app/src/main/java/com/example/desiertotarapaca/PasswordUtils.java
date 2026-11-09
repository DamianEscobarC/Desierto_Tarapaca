package com.example.desiertotarapaca;

import java.security.MessageDigest;
import java.security.SecureRandom;
import android.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtils {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA1";
    private static final String PREFIJO = "pbkdf2_sha1";
    private static final int ITERACIONES = 600000;
    private static final int LONGITUD_SAL = 16;
    private static final int LONGITUD_HASH = 256;

    private PasswordUtils() {
    }

    public static String generarHash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Contraseña inválida");
        }

        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);

        byte[] hash = derivar(password, sal, ITERACIONES);

        return PREFIJO + ":" + ITERACIONES + ":" +
                Base64.encodeToString(sal, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public static boolean verificarPassword(
            String password, String hashGuardado) {

        if (password == null || hashGuardado == null) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split(":");

            if (partes.length != 4 ||
                    !PREFIJO.equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);

            if (iteraciones < 1 || iteraciones > 1000000) {
                return false;
            }

            byte[] sal = Base64.decode(partes[2], Base64.NO_WRAP);
            byte[] hashOriginal = Base64.decode(partes[3], Base64.NO_WRAP);

            if (sal.length != LONGITUD_SAL ||
                    hashOriginal.length != LONGITUD_HASH / 8) {
                return false;
            }

            byte[] hashCalculado = derivar(
                    password, sal, iteraciones
            );

            return MessageDigest.isEqual(
                    hashOriginal, hashCalculado
            );

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derivar(
            String password, byte[] sal, int iteraciones) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                sal,
                iteraciones,
                LONGITUD_HASH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITMO);

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al procesar contraseña", e
            );
        } finally {
            spec.clearPassword();
        }
    }
}
