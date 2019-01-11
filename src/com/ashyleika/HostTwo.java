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
    private static String path = "D:/TORrent_2";
    private static String fileNameToSaveOnThisHost = null;
    private static String fileNameToDownloadFromThisHost = null;
    private static long skipBytesWhenDownloadFromThisHost = 0L;

    public static void main(String[] args) {
        createFileCheckerForUploadToThisHost();
        createFileCheckerForDownloadFromThisHost();
        createUploadListener();
        createDownloadFileSkipBytesListener();
        createContentSharer();

        while (true) {
            showMenu();
            Scanner scanner = new Scanner(System.in);
            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n\tWprowadź liczbę");
                continue;
            }
            switch (choice) {
                case 0:
                    System.exit(0);
                case 1:
                    uploadFile();
                    break;
                case 2: downloadFromAnotherHost();
                    break;
                case 3:
                    getContentFromServer();
                    break;
                default:
                    System.out.println("TRY AGAIN");
                    break;
            }
        }
    }

    private static void showMenu() {
        System.out.println("Choose action: HOST_TWO\n" +
                "0. Exit\n" +
                "1. Upload File to HostOne\n" +
                "2. Download File from HostOne\n" +
                "3. Show Content From HostOne");
    }

    private static void uploadFile() {
        Scanner scanner2 = new Scanner(System.in);
        System.out.println("Enter file name to be uploaded:");
        String fileToUpload = scanner2.nextLine();
        up(fileToUpload);
    }

    private static void downloadFromAnotherHost() {
        Scanner scanner2 = new Scanner(System.in);
        System.out.println("Enter file name to be downloaded:");
        String fileToDownload = scanner2.nextLine();
        String response = null;
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10003)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println(fileToDownload);
            response = input.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
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

    private static void createFileCheckerForUploadToThisHost() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11001)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    fileNameToSaveOnThisHost = input.readLine();
                    File file = new File(path + "/" + fileNameToSaveOnThisHost);
                    if (file.exists()) output.println(Long.toString(file.length()));
                    else output.println("OK");
                }
            } catch (IOException e) {

            }
        }).start();
    }

    private static void createFileCheckerForDownloadFromThisHost() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11003)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    fileNameToDownloadFromThisHost = input.readLine();
                    File file = new File(path + "/" + fileNameToDownloadFromThisHost);
                    if (file.exists()) {
                        up(fileNameToDownloadFromThisHost);
                        output.println("OK");
                    }
                }
            } catch (IOException e) {

            }
        }).start();
    }

    private static void createUploadListener() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11000)) {
                while (true) {
                    DataInputStream dis = new DataInputStream(serverSocket.accept().getInputStream());
                    FileOutputStream fos = new FileOutputStream(path + "/" + fileNameToSaveOnThisHost, true);
                    byte[] buffer = new byte[4096];
                    int read = 0;
                    while ((read = dis.read(buffer, 0, buffer.length)) > 0) {
                        fos.write(buffer, 0, read);
                    }
                    fos.close();
                    fileNameToSaveOnThisHost = null;
                }
            } catch (IOException e) {

            }
        }).start();
    }

    private static void createDownloadFileSkipBytesListener() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11005)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                    skipBytesWhenDownloadFromThisHost = Long.parseLong(input.readLine());
                    output.println("SKIP_BYTES_RECEIVED");
                    try (Socket socket1 = new Socket(InetAddress.getLocalHost(), 10000)) {
                        FileInputStream fis = new FileInputStream(path + "/" + fileNameToDownloadFromThisHost);
                        DataOutputStream dos = new DataOutputStream(socket1.getOutputStream());
                        fis.skip(skipBytesWhenDownloadFromThisHost);
                        byte[] bytes = new byte[2048];

                        while (fis.read(bytes) > 0) {
                            dos.write(bytes);
                        }
                        dos.flush();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();
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
            if (new File(file).length() >= 2e+9) return null;
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

    private static void up(String fileToUpload) {
        String response = null;
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10001)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println(fileToUpload);
            response = input.readLine();
        } catch (IOException e) {

        }
        if (response == null) {
            System.out.println("Can't upload file");
            return;
        }
        File file = new File(path + "/" + fileToUpload);
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10000)) {
            FileInputStream fis = new FileInputStream(file);
            if (!response.equals("OK")) {
                if (Long.parseLong(response) >= file.length()) {
                    System.out.println("File already exists");
                    return;
                }
                fis.skip(Long.parseLong(response));
            }
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            byte[] bytes = new byte[2048];

            while (fis.read(bytes) > 0) {
                dos.write(bytes);
            }
            fis.close();
            dos.close();
        } catch (IOException e) {
            System.out.println("Error upload file " + fileToUpload);
        }
    }
}
