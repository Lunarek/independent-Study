package UI;

import data.Message;
import nodes.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JLabel lbUsername = new JLabel("Username: ", SwingConstants.CENTER);
    private JTextField tfUsername = new JTextField("Anonymous");
    private JButton btJoin = new JButton("Join");
    private JButton btLeave = new JButton("Leave");
    private JButton ptActiveUsers = new JButton("Active users");
    private JTextArea taChatRoom = new JTextArea("Welcome to the Chat room\n", 80, 60);
    private boolean isConnected;
    private Client client;
    private int port;
    private String host;

    private ActionListener btActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if(o == btLeave) {
                client.sendMessage(new Message("", enums.Type.SIGNOUT));
                ClientUI.this.setTitle("Signed-out");
                return;
            }
            if(o == ptActiveUsers) {
                client.sendMessage(new Message("", enums.Type.ALL_USERS));
                ClientUI.this.setTitle("Active users");
                return;
            }

            if(isConnected) {
                client.sendMessage(new Message(tfUsername.getText(), enums.Type.MSG));
                tfUsername.setText("");
                ClientUI.this.setTitle(tfUsername.getText());
                return;
            }

            if(o == btJoin) {
                String username = tfUsername.getText().trim();

                if(username.length() == 0)
                    return;

                client = new Client(host, port, username, ClientUI.this);
                if(!client.start())
                    return;
                tfUsername.setText("");
                lbUsername.setText("Enter your message below");
                isConnected = true;

                btJoin.setEnabled(false);
                btLeave.setEnabled(true);

                ptActiveUsers.setEnabled(true);

                tfUsername.addActionListener(this);

                ClientUI.this.setTitle(username + " Joined");
            }
        }
    };

    public ClientUI(String host, int port) {

        super("Chat Client");
        this.port = port;
        this.host = host;

        JPanel pnlNorth = new JPanel(new GridLayout(3,1));

        pnlNorth.add(lbUsername);
        tfUsername.setBackground(Color.WHITE);
        pnlNorth.add(tfUsername);
        add(pnlNorth, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(taChatRoom));
        taChatRoom.setEditable(false);
        taChatRoom.setLineWrap(true);
        add(centerPanel, BorderLayout.CENTER);

        btJoin.addActionListener(btActionListener);
        btLeave.addActionListener(btActionListener);
        btLeave.setEnabled(false);
        ptActiveUsers.addActionListener(btActionListener);
        ptActiveUsers.setEnabled(false);

        JPanel southPanel = new JPanel();
        southPanel.add(btJoin);
        southPanel.add(btLeave);
        southPanel.add(ptActiveUsers);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 600);
        setVisible(true);
        tfUsername.requestFocus();
    }

    public void appendText(Message msg) {
        taChatRoom.append(msg.toString());
        taChatRoom.setCaretPosition(taChatRoom.getText().length() - 1);
        ClientUI.this.setTitle(msg.getUsername() + ": " + msg.getMsg());
    }

    public void connectionFailed() {
        btJoin.setEnabled(true);
        btLeave.setEnabled(false);
        ptActiveUsers.setEnabled(false);
        lbUsername.setText("Username: ");
        tfUsername.setText("Anonymous");
        tfUsername.removeActionListener(btActionListener);
        isConnected = false;
    }

    public static void main(String[] args) {
        String address;
        while(true){
            address = JOptionPane.showInputDialog("Chat server host name: ");
            if(address!=null && !address.isEmpty())
                break;
            JOptionPane.showMessageDialog(null, "Invalid server host name entered!");
        }

        while(true){
            String portStr = JOptionPane.showInputDialog("Chat server port: ");
            try{
                new ClientUI(address, Integer.parseInt(portStr));
                break;
            } catch (Exception e){
                JOptionPane.showMessageDialog(null, "Invalid port entered!");
                e.printStackTrace();
            }
        }
    }
}

