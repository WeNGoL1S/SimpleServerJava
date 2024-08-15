package Server;

import network.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;

public class Server implements TCPConnectionListener {
    Scanner closeserver = new Scanner(System.in);
    public static void main(String [] args){


        System.out.print("Server starting...\n");
        new Server();

    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private Server(){
        try {
            ServerSocket serverSocket = new ServerSocket(8189);
            while (true){
                if(closeserver.nextLine().equals("close")){
                    System.exit(2);
                }
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
        sendToAllConnections(value);
        printMsg(value);
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
