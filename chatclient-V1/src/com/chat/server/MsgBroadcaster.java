package com.chat.server;

import com.chat.common.data.Message;
import com.chat.server.impl.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

/** One instance of this thread will run for each client */
public class MsgBroadcaster extends Thread {
    // the socket where this client will listen to
    Socket socket;
    //The clients input stream where the server will put data
    ObjectInputStream objectInputStream;
    //The server output stream where this client will put data
    ObjectOutputStream objectOutputStream;
    // this is the unique clientId for this connection
    int clientId;
    // the name of the user connected
    String username;
    // the message received
    Message message;
    //The date since the client is active
    String date;
    //The server this client is connected to
    Server server;

    public int getClientId() {
        return clientId;
    }

    public MsgBroadcaster(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientId = ++Server.connectionId;

        System.out.println("Thread trying to create Object Input/Output Streams");
        try
        {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            username = (String) objectInputStream.readObject();
            this.server.display(new Message(username + " just connected."));
        } catch (IOException e) {
            this.server.display(new Message(e, "Exception creating new Input/output Streams"));
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        date = new Date().toString() + "\n";
    }

    public void run() {
        // to loop until SIGNOUT
        boolean isClientActive = true;
        while(isClientActive) {
            try {
                message = (Message) objectInputStream.readObject();
            } catch (IOException e) {
                server.display(new Message(e, username + " Exception reading Streams"));
                e.printStackTrace();
                break;
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
                break;
            }

            switch(this.message.getType()) {
                case MSG:
                    server.broadcast(this.message);
                    break;
                case SIGNOUT:
                    server.display(new Message(username + " disconnected with a LOGOUT message."));
                    isClientActive = false;
                    break;
                case ALL_USERS:
                    writeMsg(new Message("List of the users connected"));
                    // scan al the users connected
                    for(int i = 0; i < server.getAllClients().size(); ++i) {
                        MsgBroadcaster ct = server.getAllClients().get(i);
                        writeMsg(new Message((i+1) + ") " + ct.username + " since " + ct.date));
                    }
                    break;
            }
        }
        server.remove(clientId);
        close();
        if(server.getAllClients().size()<=0) server.stop();
    }

    public void close() {
        try {
            if(objectOutputStream != null) objectOutputStream.close();
        } catch(Exception e) {e.printStackTrace();}
        try {
            if(objectInputStream != null) objectInputStream.close();
        } catch(Exception e) {e.printStackTrace();};
        try {
            if(socket != null) socket.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    public boolean writeMsg(Message msg) {
        if(!socket.isConnected()) {
            close();
            return false;
        }
        try {
            objectOutputStream.writeObject(msg);
        } catch(IOException e) {
            server.display(new Message(e, "Error sending message to " + username));
            return false;
        }
        return true;
    }
}
