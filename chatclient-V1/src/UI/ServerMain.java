package UI;

import nodes.Server;

import javax.swing.*;
import java.util.Scanner;


public class ServerMain {
    private static Scanner console = new Scanner(System.in);
    public static void main(String[] arg) throws InterruptedException {
        Server server;
        while(true){
            String portStr = JOptionPane.showInputDialog(null, "Enter chat server port: ");
            try{
                server = new Server(Integer.parseInt(portStr));
                server.start();
                System.out.println("Enter 'exit' to stop chat server");
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid port entered!");
                e.printStackTrace();
            }
        }

        final Server finalServer = server;
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

