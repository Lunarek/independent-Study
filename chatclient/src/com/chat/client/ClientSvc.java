package com.chat.client;

import com.chat.common.data.Message;


public interface ClientSvc {
    void sendMessage(Message message);
    String start();
}
