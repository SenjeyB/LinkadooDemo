package org.poltanov.forums.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Утилитный класс для шифрования и дешифрования строк с использованием алгоритма AES.
 * Предоставляет методы для безопасного преобразования текстовых данных.
 */
public class EncryptionUtil {
    /**
     * Алгоритм шифрования.
     */
    private static final String ALGORITHM = "AES";

    /**
     * Секретный ключ для шифрования и дешифрования.
     * <p>
     * <strong>Важно:</strong> В данном примере ключ хранится в коде для простоты.
     * В реальных приложениях необходимо использовать безопасные методы хранения и управления ключами.
     * </p>
     */
    private static final String KEY = "MySuperSecretKey";

    /**
     * Шифрует заданную строку с использованием AES алгоритма.
     *
     * @param value Строка для шифрования.
     * @return Зашифрованная строка в формате Base64.
     * @throws Exception Если возникает ошибка при шифровании.
     */
    public static String encrypt(String value) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Дешифрует заданную зашифрованную строку с использованием AES алгоритма.
     *
     * @param encrypted Зашифрованная строка в формате Base64.
     * @return Расшифрованная исходная строка.
     * @throws Exception Если возникает ошибка при дешифровании.
     */
    public static String decrypt(String encrypted) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getDecoder().decode(encrypted);
        byte[] original = cipher.doFinal(decoded);

        return new String(original, "UTF-8");
    }
}
