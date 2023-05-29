public class Security {
    private static final int SHIFT = 3; // cantidad de desplazamiento para el cifrado de César

    public boolean isLessThanEightChars(String str) {
        return str.length() < 8;
    }
    public static String encrypt(String plaintext) {
        StringBuilder cipherText = new StringBuilder();

        for (char c : plaintext.toCharArray()) {
            if (Character.isUpperCase(c)) {
                char encryptedChar = (char) ((c + SHIFT - 'A') % 26 + 'A');
                cipherText.append(encryptedChar);
            } else if (Character.isLowerCase(c)) {
                char encryptedChar = (char) ((c + SHIFT - 'a') % 26 + 'a');
                cipherText.append(encryptedChar);
            } else {
                cipherText.append(c); // no alterar otros caracteres
            }
        }

        return cipherText.toString();
    }

    public String decrypt(String cipherText) {
        StringBuilder plaintext = new StringBuilder();

        for (char c : cipherText.toCharArray()) {
            if (Character.isUpperCase(c)) {
                char decryptedChar = (char) ((c - SHIFT - 'A' + 26) % 26 + 'A');
                plaintext.append(decryptedChar);
            } else if (Character.isLowerCase(c)) {
                char decryptedChar = (char) ((c - SHIFT - 'a' + 26) % 26 + 'a');
                plaintext.append(decryptedChar);
            } else {
                plaintext.append(c); // no alterar otros caracteres
            }
        }

        return plaintext.toString();
    }


}
