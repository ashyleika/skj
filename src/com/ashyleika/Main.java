package com.ashyleika;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

//        try (FileInputStream fis = new FileInputStream("C:/tasks.png");
//             FileOutputStream fos = new FileOutputStream("part_1");) {
//            int read = 0;
//            int written = 0;
//             while ((read = fis.read()) > -1 ) {
//                 fos.write(read);
//                 written++;
//                 if(written == 10000) break;
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try (FileInputStream fis = new FileInputStream("C:/tasks.png");
//             FileOutputStream fos = new FileOutputStream("part_2")) {
//            int read = 0;
//            int written = 0;
//            while ((read = fis.read()) > -1 ) {
//                written++;
//                if(written <= 10000) continue;
//                fos.write(read);
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try (FileInputStream fis = new FileInputStream("part_2");
//            FileOutputStream fos = new FileOutputStream("part_1", true)){
//            int read = 0;
//            while ((read = fis.read()) > -1) {
//                fos.write(read);
//            }
//        } catch (IOException e ) {
//
//        } finally {
//            File file1 = new File("part_1");
//            file1.renameTo(new File("image.png"));
//        }



        List<String> hosts = Arrays.asList("C:/", "D:/TORrent_1/", "D:/TORrent_2/");
        String fileName = "idea.zip";
        long sizeOfEntireFile = new File("C:/idea.zip").length();
        long sizePerHost = sizeOfEntireFile/hosts.size();
        System.out.println("Size: " + sizeOfEntireFile + "\n");
        for(int i = 0; i < hosts.size(); ++i) {
            downLoadUsingThread(fileName, hosts.get(i), sizeOfEntireFile, i, hosts.size());
        }

    }

    static void downLoadUsingThread(String file, String host, long entireFileSize, int counter, int numberOfHosts) {
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(host + file);
                 FileOutputStream fos = new FileOutputStream("part_"+counter)){

                int sizeOfBuffer = 2048;
                long partSize = entireFileSize/numberOfHosts;
                while (partSize%sizeOfBuffer != 0) {
                    partSize++;
                }
                System.out.println("for " + host + " size of part is " + partSize + " and can be divided by 2048: " +
                        ((partSize%sizeOfBuffer) == 0) );
                fis.skip(partSize*counter);
                int read;
                int done = 0;
                byte[] bytes = new byte[sizeOfBuffer];
                while ((read = fis.read(bytes)) > -1) {
                    fos.write(bytes);
                    done += read;
                    if(done == partSize && counter < numberOfHosts-1) return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
