/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.whoami.authme.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordSecurity {

    private static SecureRandom rnd = new SecureRandom();

    private static String getMD5(String message) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.reset();
        md5.update(message.getBytes());
        byte[] digest = md5.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                                                                               digest));
    }

    private static String getSHA1(String message) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.reset();
        sha1.update(message.getBytes());
        byte[] digest = sha1.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                                                                               digest));
    }

    private static String getSHA256(String message) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        sha256.reset();
        sha256.update(message.getBytes());
        byte[] digest = sha256.digest();

        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,
                                                                               digest));
    }

    public static String getWhirlpool(String message) {
        Whirlpool w = new Whirlpool();
        byte[] digest = new byte[Whirlpool.DIGESTBYTES];
        w.NESSIEinit();
        w.NESSIEadd(message);
        w.NESSIEfinalize(digest);
        return Whirlpool.display(digest);
    }

    private static String getSaltedHash(String message, String salt) throws NoSuchAlgorithmException {
        return "$SHA$" + salt + "$" + getSHA256(getSHA256(message) + salt);
    }
    
    //
    // VBULLETIN 3.X 4.X METHOD
    //
    
    private static String getSaltedMd5(String message, String salt) throws NoSuchAlgorithmException {
        return "$MD5vb$" + salt + "$" + getMD5(getMD5(message) + salt);
    }
    
    private static String getXAuth(String message, String salt) {
        String hash = getWhirlpool(salt + message).toLowerCase();
        int saltPos = (message.length() >= hash.length() ? hash.length() - 1 : message.length());
        return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
    }

    private static String createSalt(int length) throws NoSuchAlgorithmException {
        byte[] msg = new byte[40];
        rnd.nextBytes(msg);

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.reset();
        byte[] digest = sha1.digest(msg);
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,digest)).substring(0, length);
    }

    public static String getHash(HashAlgorithm alg, String password) throws NoSuchAlgorithmException {
        switch (alg) {
            case MD5:
                return getMD5(password);
            case SHA1:
                return getSHA1(password);
            case SHA256:
                String salt = createSalt(16);
                return getSaltedHash(password, salt);
            case MD5VB:
                String salt2 = createSalt(16);
                return getSaltedMd5(password, salt2);
            case WHIRLPOOL:
                return getWhirlpool(password);
            case XAUTH:
                String xsalt = createSalt(12);
                return getXAuth(password, xsalt);
            case PHPBB:
                return getPhpBB(password);
            case PLAINTEXT:
                return getPlainText(password);
            default:
                throw new NoSuchAlgorithmException("Unknown hash algorithm");
        }
    }

    public static boolean comparePasswordWithHash(String password, String hash) throws NoSuchAlgorithmException {
        //System.out.println("[Authme Debug] debug hashString"+hash);
        if(hash.contains("$H$")) {
           PhpBB checkHash = new PhpBB();
            return checkHash.phpbb_check_hash(password, hash);
        }
        // PlainText Password
        if(hash.length() < 32 ) {
            return hash.equals(password);
        }
        
        if (hash.length() == 32) {
            return hash.equals(getMD5(password));
        }

        if (hash.length() == 40) {
            return hash.equals(getSHA1(password));
        }

        if (hash.length() == 140) {
            int saltPos = (password.length() >= hash.length() ? hash.length() - 1 : password.length());
            String salt = hash.substring(saltPos, saltPos + 12);
            return hash.equals(getXAuth(password, salt));
        }

        if (hash.contains("$")) {
            //System.out.println("[Authme Debug] debug hashString"+hash);
            String[] line = hash.split("\\$");
            if (line.length > 3 && line[1].equals("SHA")) {
                return hash.equals(getSaltedHash(password, line[2]));
            } else {
                if(line[1].equals("MD5vb")) {
                    //System.out.println("[Authme Debug] password hashed from Authme"+getSaltedMd5(password, line[2]));
                    //System.out.println("[Authme Debug] salt from Authme"+line[2]);
                    //System.out.println("[Authme Debug] equals? Authme: "+hash);
                    //hash = "$MD5vb$" + salt + "$" + hash;
                    return hash.equals(getSaltedMd5(password, line[2]));
                }
            }
        }
        return false;
    }

    private static String getPhpBB(String password) {
        PhpBB hash = new PhpBB();
        String phpBBhash = hash.phpbb_hash(password);
        return phpBBhash;
    }

    private static String getPlainText(String password) {
        return password;
    }

    public enum HashAlgorithm {

        MD5, SHA1, SHA256, WHIRLPOOL, XAUTH, MD5VB, PHPBB, PLAINTEXT
    }
}
