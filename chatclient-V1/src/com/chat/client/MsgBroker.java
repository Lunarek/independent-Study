package com.chat.client;

import com.chat.common.data.Message;
import com.chat.client.impl.Client;

import java.io.IOException;

public class MsgBroker extends Thread {
    private Client client;

    public MsgBroker(Client client) {
        this.client = client;
    }

    public void run() {
        while(true) {
            try {
                Message msg = (Message) client.getObjectInputStream().readObject();
                client.getChatClient().appendText(msg);
            } catch(IOException e) {
                client.display(new Message(e, "Server has close the connection"));
                if(client.getChatClient() != null)
                    client.getChatClient().connectionFailed();
                break;
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
            }
        }
    }
}
