package server.utility;

import java.security.MessageDigest;
import java.util.Date;

public class Digester {
    private static MessageDigest digester;

    public Digester() {

    }
    static {
        try {
            //Digester set MD5 hashing
            digester = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Method for hashing the password with the salt.
    public String hashWithSalt(String password){
        return Digester.performHashing(password);
    }

    // The logic behind the hashing.
    private static String performHashing(String password) {
        digester.update(password.getBytes());
        byte[] hash = digester.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            if ((0xff & aHash) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & aHash)));
            } else {
                hexString.append(Integer.toHexString(0xFF & aHash));
            }
        }
        return hexString.toString();
    }

}