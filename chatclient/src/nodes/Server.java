package nodes;

import data.Message;
import threads.ClientThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    // a unique ID for each connection
    public static int connectionId;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> allClients;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned of to stop the server
    private boolean stopServer;

    public Server(int port) {
        this.port = port;
        allClients = new ArrayList<ClientThread>();
    }

    public void start() {
        stopServer = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            while(stopServer)
            {
                display(new Message("Server waiting for Clients on port " + port + "."));

                Socket socket = serverSocket.accept();
                if(!stopServer) break;
                ClientThread t = new ClientThread(socket, this);
                allClients.add(t);
                t.start();
            }

            try {
                serverSocket.close();
                for(int i = 0; i < allClients.size(); ++i) {
                    ClientThread client = allClients.get(i);
                    client.close();
                }
            } catch(Exception e) {
                display(new Message(e, "Exception closing the server and clients"));
                e.printStackTrace();
            }
        } catch (IOException e) {
            display(new Message(e, " Exception on new ServerSocket"));
            e.printStackTrace();
        }
    }

    public void stop() {
        stopServer = false;
        try {
            new Socket("localhost", port);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void display(Message msg) {
        System.out.println(msg.toString());
    }

    public synchronized void broadcast(Message message) {
        message.setTime(new Date());
        for(int i = allClients.size(); --i >= 0;) {
            ClientThread client = allClients.get(i);
            if(!client.writeMsg(message)) {
                allClients.remove(i);
                display(new Message("Disconnected Client " + client.getName() + " removed from list."));
            }
        }
    }

    public synchronized void remove(int id) {
        for(int i = 0; i < allClients.size(); ++i) {
            ClientThread client = allClients.get(i);
            if(client.getId() == id) {
                allClients.remove(i);
                return;
            }
        }
    }

    public ArrayList<ClientThread> getAllClients() {
        return allClients;
    }
}


