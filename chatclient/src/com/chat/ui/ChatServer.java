package com.chat.ui;

import com.chat.server.ServerSvc;
import com.chat.server.impl.Server;

import javax.swing.*;
import java.util.Scanner;


public class ChatServer {
    private static Scanner console = new Scanner(System.in);
    public static void main(String[] arg) throws InterruptedException {
        ServerSvc serverSvc;
        while(true){
            String portStr = JOptionPane.showInputDialog(null, "Enter chat server port: ");
            try{
                serverSvc = new Server(Integer.parseInt(portStr));
                serverSvc.start();
                System.out.println("Enter 'exit' to stop chat server");
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid port entered!");
                e.printStackTrace();
            }
        }

        final ServerSvc finalServer = serverSvc;
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                finalServer.stop();
            }
        });

        String exit;
        do{
            exit = console.next();
        } while(!"exit".equalsIgnoreCase(exit));

        System.exit(0);
    }
}

