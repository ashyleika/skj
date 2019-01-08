package com.ashyleika;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HostTwo {
    private static String path = "C:/TORrent_2";

    public static void main(String[] args) {
        createListener();
        createUploadListener();
        createContentSharer();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Choose action: HOST_TWO");
            System.out.println(
                    "0. Exit\n" +
                            "1. Download File\n" +
                            "2. Echo from server\n" +
                            "3. Show Content From HostOne");
            int choice = scanner.nextInt();
            switch (choice) {
                case 0:
                    System.exit(0);
                case 1:
                    downloadFile();
                    break;
                case 2:
                    echoFromServer();
                    break;
                case 3:
                    getContentFromServer();
                    break;
                default:
                    continue;
            }
        }
    }


    private static void echoFromServer() {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10000)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter string:");
                String s = scanner.nextLine();
                if (s.equals("exit")) return;
                output.println(s);
                System.out.println(input.readLine());
            }
        } catch (IOException e) {

        }
    }

    private static void downloadFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name to be uploaded:");
        String fileNameUpload = scanner.nextLine();
        File file = new File(path + "/" + fileNameUpload);
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10001)) {
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            byte[] bytes = new byte[(int) file.length()];

            while (fis.read(bytes) > 0) {
                dos.write(bytes);
            }
            dos.flush();
        } catch (IOException e) {

        }
    }

    private static void getContentFromServer() {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10002)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("showTheContent");
            bufferedReader.lines().forEach(System.out::println);
            System.out.println();
        } catch (IOException e) {

        }
    }

    private static void createListener() {
        (new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11001)) {
                Socket socket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    String s = input.readLine();
                    output.println(s.toUpperCase());
                }
            } catch (IOException e) {

            }
        })).start();
    }

    private static void createUploadListener() {
        (new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11000)) {
                while (true) {
                    DataInputStream dis = new DataInputStream(serverSocket.accept().getInputStream());
                    FileOutputStream fos = new FileOutputStream(path + "/" + "toHostTwo");
                    byte[] buffer = new byte[4096];
                    int filesize = 15123; // Send file size in separate msg
                    int read = 0;
                    int totalRead = 0;
                    int remaining = filesize;
                    while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;
                        System.out.println("read " + totalRead + " bytes.");
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                }
            } catch (IOException e) {

            }
        })).start();
    }

    private static void createContentSharer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11002)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                    String str = input.readLine();

                    if (str != null && str.equals("showTheContent")) {
                        List<String> files = getContent(path);
                        for (String s : files) {
                            output.write(s);
                        }
                        output.close();
                    }
                }
            } catch (IOException e) {

            }
        }).start();
    }

    private static List<String> getContent(String path) {
        File serverDirectory = new File(path);
        if (!serverDirectory.exists()) {
            serverDirectory.mkdirs();
        }
        List<File> files = Arrays.asList(serverDirectory.listFiles());

        return files
                .stream()
                .map(n -> n.getAbsolutePath() + ": " + getMD5(n.getAbsolutePath()) + "\n")
                .collect(Collectors.toList());
    }

    private static String getMD5(String file) {
        String HEXES = "0123456789ABCDEF";
        try {
            byte[] b = Files.readAllBytes(Paths.get(file));
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            if (hash == null) {
                return null;
            }
            final StringBuilder hex = new StringBuilder(2 * hash.length);
            for (final byte bb : hash) {
                hex.append(HEXES.charAt((bb & 0xF0) >> 4))
                        .append(HEXES.charAt((bb & 0x0F)));
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
