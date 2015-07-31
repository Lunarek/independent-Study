package nodes;

import data.Message;
import enums.Type;
import threads.MessageReader;
import UI.ClientUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ClientUI clientUI;
    private String server, username;
    private int port;

    public Client(String server, int port, String username, ClientUI clientUI) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.clientUI = clientUI;
    }

    public boolean start() {
        try {
            socket = new Socket(server, port);
        }catch(Exception e) {
            display(new Message(e, "Error connecting to server"));
            return false;
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
            return false;
        }

        new MessageReader(this).start();
        try
        {
            objectOutputStream.writeObject(username);
        }
        catch (IOException ioe) {
            display(new Message(ioe, "Exception doing login"));
            disconnect();
            return false;
        }
        return true;
    }

    public void display(Message msg) {
        clientUI.appendText(msg);
    }

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
        if(clientUI != null)
            clientUI.connectionFailed();

    }

    public ClientUI getClientUI() {
        return clientUI;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }
}

