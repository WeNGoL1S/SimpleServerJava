package Server;

import network.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;

public class Server implements TCPConnectionListener {
    static String adminpass;
    static String adminusername;
    static File seiitingsfile = new File("settings.ini");

    public static void main(String [] args){

        System.out.print("Server starting...\n");

        if (!seiitingsfile.exists()) {
            createSettings();
        }
        try {
            readPass();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Server();

    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private Server(){
        try {
            ServerSocket serverSocket = new ServerSocket(9021);
            while (true){

                try {
                    new TCPConnection(this,serverSocket.accept());
                }catch (IOException e){
                    printMsg("TCPConnection exception:"+ e);
                }

            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        printMsg("Client connected: "  + tcpConnection);

    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        if(value.equals(adminusername+"> close_server "+ adminpass))
        {
            System.out.print("Closing server...\n");
            System.exit(2);
        }
        sendToAllConnections(value);
        printMsg(value);
    }

    private static void readPass() throws IOException {
        FileReader read = new FileReader(seiitingsfile);
        BufferedReader buffer = new BufferedReader(read);
        buffer.readLine();

        String[] lines;

        ArrayList<String> list = new ArrayList<>();

        boolean isEnd = false;

        while (!isEnd) {
            String readlines = buffer.readLine();
            list.add(readlines);

            isEnd = !buffer.ready();
        }

        lines = new String[list.size()];

        for (int j = 0; j < list.size(); j++) {
            lines[j] = list.get(j);
        }

        for (int i = 0; i < lines.length; i++) {
            String[] args = lines[i].split("=");

            System.out.println(Arrays.toString(args));

            if (args[0].equals("AdminNickname")) {
                adminusername = args[1];
            }else if (args[0].equals("AdminPassword")) {
                adminpass = args[1];
            }
        }

        buffer.close();
        read.close();

    }

    private static void createSettings() {

        try {
            seiitingsfile.createNewFile();
            FileWriter write = new FileWriter(seiitingsfile, false);
            write.write("[Settings]\n" +
                    "AdminNickname=user\n" +
                    "AdminPassword=0");
            write.close();

            System.out.print("File 'settings.ini' is created!\n");

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        printMsg("Client disconnected: "  + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {

    }

    private synchronized void printMsg(String Msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.print(Msg + "\n");

            }
        });
    }
    private void sendToAllConnections(String value){
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
            connections.get(i).sendString(value);
        }
    }

}
