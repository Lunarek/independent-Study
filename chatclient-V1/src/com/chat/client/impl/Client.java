package com.chat.client.impl;

import com.chat.client.ClientSvc;
import com.chat.client.MsgBroker;
import com.chat.common.data.Message;
import com.chat.server.ServerSvc;
import com.chat.server.impl.Server;
import com.chat.ui.ChatClient;
import com.chat.common.utils.Type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client implements ClientSvc {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ChatClient chatClient;
    private String server, username;
    private int port;

    public Client(String server, int port, String username, ChatClient chatClient) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.chatClient = chatClient;
    }

    @Override
    public String start() {
        while(socket==null){
            try {
                display(new Message("Connecting to the chat server", Type.INFO));
                socket = new Socket(server, port);
            }catch(Exception e) {
                display(new Message("Chat server not available at " + server + ":" + port,
                        Type.ERROR));
                display(new Message("Starting the chat server on this machine", Type.INFO));
                try {
                    server = Inet4Address.getLocalHost().getHostAddress();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ServerSvc serviceSvc = new Server(port);
                        serviceSvc.start();
                    }
                }).start();
            }
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(new Message(msg, Type.INFO));
	
        try
        {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display(new Message(eIO, "Exception creating new Input/output Streams"));
            return server;
        }

        new MsgBroker(this).start();
        try {
            objectOutputStream.writeObject(username);
        }
        catch (IOException ioe) {
            display(new Message(ioe, "Exception doing login"));
            disconnect();
            return server;
        }
        return server;
    }

    public void display(Message msg) {
        chatClient.appendText(msg);
    }

    @Override
    public void sendMessage(Message msg) {
        try {
            msg.setUsername(username);
            objectOutputStream.writeObject(msg);
        } catch(IOException e) {
            display(new Message(e, "Exception writing to server"));
        }
    }

    private void disconnect() {
        try {
            if(objectInputStream != null) objectInputStream.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(objectOutputStream != null) objectOutputStream.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do

        // inform the GUI
        if(chatClient != null)
            chatClient.connectionFailed();

    }

    public ChatClient getChatClient() {
        return chatClient;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }
}

