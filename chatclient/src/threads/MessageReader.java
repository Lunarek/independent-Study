package threads;

import data.Message;
import nodes.Client;

import java.io.IOException;

public class MessageReader extends Thread {
    private Client client;

    public MessageReader(Client client) {
        this.client = client;
    }

    public void run() {
        while(true) {
            try {
                Message msg = (Message) client.getObjectInputStream().readObject();
                client.getClientUI().appendText(msg);
            } catch(IOException e) {
                client.display(new Message(e, "Server has close the connection"));
                if(client.getClientUI() != null)
                    client.getClientUI().connectionFailed();
                break;
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
            }
        }
    }
}
