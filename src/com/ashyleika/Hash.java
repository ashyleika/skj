package com.ashyleika;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class Hash {
    public static void main(String[] args) throws Exception {
        byte[] b = Files.readAllBytes(Paths.get("1.txt"));
        byte[] hash = MessageDigest.getInstance("MD5").digest(b);
        System.out.println("NEW: " + getHex(hash));
    }

    private static final String HEXES = "0123456789ABCDEF";
    private static String getHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
