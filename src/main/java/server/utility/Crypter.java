package server.utility;

import com.google.gson.Gson;
import server.controller.Config;

public class Crypter {

    public String encrypt(String input) {
        if (Config.getEncryption()) {
            char[] key = {'L', 'Y', 'N'};
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < input.length(); i++) {
                output.append((char) (input.charAt(i) ^ key[i % key.length]));
            }

            String isEncrypted = new Gson().toJson(output.toString());

            return isEncrypted;
        } else {
            return input;
        }
    }

    public String decrypt(String input) {
        if(Config.getEncryption()) {
            char[] key = {'L', 'Y', 'N'};
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < input.length(); i++) {
                output.append((char) (input.charAt(i) ^ key[i % key.length]));
            }

            String isDecrypted = output.toString();
            return isDecrypted;
        } else {
            return input;
        }
    }
}
