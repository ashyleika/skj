package com.ashyleika;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HostOne {

    public static void main(String[] args) {
        createListener();
        createUploadListener();
        createContentSharer();

        while(true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Choose action: HOST_ONE");
            System.out.println(
                    "0. Exit\n" +
                            "1. Download File\n" +
                            "2. Echo from server\n" +
                            "3. Show Content From HostTwo");
            int choice = scanner.nextInt();
            switch (choice) {
                case 0: System.exit(0);
                case 1: downloadFile();
                break;
                case 2: echoFromServer();
                break;
                case 3: getContentFromServer();
                break;
                default:
            }
        }
    }

    private static void downloadFile(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name to be uploaded:");
        String fileNameUpload = scanner.nextLine();
        File file = new File(fileNameUpload);
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10100)){
            FileInputStream fis = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            byte[] bytes = new byte[(int)file.length()];

            while (fis.read(bytes) > 0) {
                dos.write(bytes);
            }
        } catch (IOException e) {

        }
    }

    private static void echoFromServer(){
        try(Socket socket = new Socket(InetAddress.getLocalHost(), 10000)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            while(true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter string:");
                String s = scanner.nextLine();
                if(s.equals("exit")) return;
                output.println(s);
                System.out.println(input.readLine());
            }
        } catch (IOException e) {

        }
    }

    private static void getContentFromServer(){
        try(Socket socket = new Socket(InetAddress.getLocalHost(), 10200)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println("showTheContent");

            bufferedReader.lines().forEach(System.out::println);
        } catch (IOException e) {

        }
    }
    private static void createListener() {
        (new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11000)) {
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
            try (ServerSocket serverSocket = new ServerSocket(11100)) {
                while (true) {
                    DataInputStream dis = new DataInputStream(serverSocket.accept().getInputStream());
                    FileOutputStream fos = new FileOutputStream("demofile.txt");
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
                }
            } catch (IOException e) {

            }
        })).start();
    }

    private static void createContentSharer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(11200)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                    String str = input.readLine();

                    if(str != null && str.equals("showTheContent")) {
                        List<String> files = getContent("D:/TORrent_2");
                        for (String s : files) {
                            output.write(s);
                        }
                        output.println();
                        return;
                    }
                }
            } catch (IOException e) {

            }
        }).start();
    }

    static List<String> getContent(String path) {
        File serverDirectory = new File(path);
        if (!serverDirectory.exists()) {
            serverDirectory.mkdirs();
        }
        List<File> files = Arrays.asList(serverDirectory.listFiles());

        return files
                .stream()
                .map(File::getAbsolutePath)
                .map(n -> n + '\n')
                .collect(Collectors.toList());
    }
}
