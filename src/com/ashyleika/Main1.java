package com.ashyleika;

import java.io.*;
import java.util.Vector;

public class Main1 {
    public static void main(String[] args) {
        try (
                FileInputStream fis1 = new FileInputStream("part_0");
                FileInputStream fis2 = new FileInputStream("part_1");
                FileInputStream fis3 = new FileInputStream("part_2");
                FileOutputStream fos = new FileOutputStream("result.zip")
        ) {
            Vector<InputStream> vector = new Vector<>();
            vector.add(fis1);
            vector.add(fis2);
            vector.add(fis3);
            SequenceInputStream sis = new SequenceInputStream(vector.elements());
            int read = 0;
            byte[] buffer = new byte[4096];
            while ((read = sis.read(buffer)) > -1) {
                fos.write(buffer);
            }
        } catch (IOException e) {

        }
    }
}
