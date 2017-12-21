package server.utility;

import com.google.gson.Gson;
import server.controller.Config;

public class Crypter {

    //Method for encrypting the data. Takes input as parameter
    public String encrypt(String input) {
        //Check if encryption is true
        if (Config.getEncryption()) {
            //Encryption key
            char[] key = {'L', 'Y', 'N'};
            //New StringBuilder object
            StringBuilder output = new StringBuilder();

            //Loop through input length
            for (int i = 0; i < input.length(); i++) {
                //Create the encrypted string
                output.append((char) (input.charAt(i) ^ key[i % key.length]));
            }

            //Format encryption to JSON
            String isEncrypted = new Gson().toJson(output.toString());

            return isEncrypted;
        } else {
            //If encryption of
            return input;
        }
    }

    //Method for decrypting the data. Takes input as parameter
    public String decrypt(String input) {
        //Check if encryption is true
        if(Config.getEncryption()) {
            //Decryption key
            char[] key = {'L', 'Y', 'N'};
            //New Stringbuilder Object
            StringBuilder output = new StringBuilder();

            //Loop through input length
            for (int i = 0; i < input.length(); i++) {
                //Create the decrypted string
                output.append((char) (input.charAt(i) ^ key[i % key.length]));
            }
            //Format encryption to string
            String isDecrypted = output.toString();

            return isDecrypted;
        } else {
            return input;
        }
    }
}
