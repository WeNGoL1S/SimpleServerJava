package Server;

import network.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;

public class Server implements TCPConnectionListener {
    static String adminpass;
    static String adminusername;

    public static void main(String [] args){

        System.out.print("Server starting...\n");
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
            ServerSocket serverSocket = new ServerSocket(8189);
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
            System.out.print("Closing server...");
            System.exit(2);
        }
        sendToAllConnections(value);
        printMsg(value);
    }

    private static void readPass() throws IOException {
        File adminpassfile = new File("adminpass.txt");
        File adminusernamefile = new File("adminusername.txt");
        FileReader read = new FileReader(adminpassfile);
        FileReader read2 = new FileReader(adminusernamefile);
        BufferedReader buffer = new BufferedReader(read);
        BufferedReader buffer2 = new BufferedReader(read2);

        adminpass = buffer.readLine();
        adminusername = buffer2.readLine();

        read.close();
        read2.close();
        buffer.close();
        buffer2.close();

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
