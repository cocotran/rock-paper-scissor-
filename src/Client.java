import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    private final static String SEPARATOR = "&";

    private static Socket clientSocket;
    private static String clientName = "";

    private static JFrame frame;
    private static JLabel clientNameLabel, playWithLabel;
    private static JTextField clientNameTextField;
    private static JButton connectButton, playButton;
    private static JComboBox playWithBox;


    public static void main(String[] args) throws Exception {

        // Create the GUI frame and components
        frame = new JFrame ("RPS Game Client");
        frame.setLayout(null);
        frame.setBounds(100, 100, 480, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // CONNECTION ROW STARTS
        // Client Name label
        clientNameLabel = new JLabel("Client Name");
        clientNameLabel.setBounds(20, 40, 150, 30);
        frame.getContentPane().add(clientNameLabel);

        // Client name input field
        clientNameTextField = new JTextField();
        clientNameTextField.setBounds(130, 40, 150, 30);
        frame.getContentPane().add(clientNameTextField);

        // Connect/Disconnect button
        connectButton = new JButton("Connect");
        connectButton.setBounds(330, 40, 100, 30);
        frame.getContentPane().add(connectButton);
    // CONNECTION ROW ENDS

    // PLAY WITH ROW STARTS
        // Play With label
        playWithLabel = new JLabel("Play With: ");
        playWithLabel.setBounds(20, 90, 150, 30);
        frame.getContentPane().add(playWithLabel);

        // Play With Combo Box
        playWithBox = new JComboBox();
        playWithBox.setBounds(130, 90, 150, 30);
        frame.getContentPane().add(playWithBox);

        // Play button
        playButton = new JButton("Play");
        playButton.setBounds(330, 90, 100, 30);
        frame.getContentPane().add(playButton);

    // PLAY WITH ROW ENDS


    // BUTTONS ACTION LISTENER FUNCTIONS STARTS
        // Action listener when connect button is pressed
        connectButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {

                    if (connectButton.getText().equals("Connect")) { //if pressed to connect

                        clientName = clientNameTextField.getText();

                        if (! (clientName.equals(""))) {
                            // create a new socket to connect with the server application
                            clientSocket = new Socket ("localhost", 6789);

                            // call function StartThread
                            StartThread();

                            //make the GUI components visible, so the client can send and receive messages
                            clientNameTextField.setEditable(false);

                            // change the Connect button text to disconnect
                            connectButton.setText("Disconnect");
                        }

                    } else { // if pressed to disconnect
                        disconnect();
                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }});


        // Disconnect on close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    disconnect();

                    System.exit(0);

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        });


        frame.setVisible(true);
    }


    private static void disconnect() throws IOException {
        // create an output stream and send a Remove message to disconnect from the server
        DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());

        outToServer.writeBytes("-Remove\n");

        // close the client's socket
        clientSocket.close();

        clientName = "";

        // make the GUI components invisible
        clientNameTextField.setEditable(true);

        // change the Connect button text to connect
        connectButton.setText("Connect");
    }


    // Thread to always read messages from the server and print them in the textArea
    private static void StartThread() {

        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                // create a buffer reader and connect it to the socket's input stream
                BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
                String receivedSentence;

                //create an output stream
                DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());

                // always read received messages and append them to the textArea
                while (true) {

                    receivedSentence = inFromServer.readLine();
                    System.out.println(receivedSentence);

                    if (receivedSentence.startsWith("-Connected")) {
                        outToServer.writeBytes("-Join" + SEPARATOR + clientName + "\n");
                    }

                    else if (receivedSentence.startsWith("-NameTaken")) { // Name is already taken
                        disconnect();
                    }

                    else if (receivedSentence.startsWith("-Joined")) {
                        outToServer.writeBytes("-PlayersList" + "\n");
                    }

                    else if (receivedSentence.startsWith("-PlayersList")) {
                        String []data = receivedSentence.split(SEPARATOR);
                        String []playersList = data[1].split(",");

                        playWithBox.removeAllItems();

                        for (String player: playersList) {
                            if (!player.equals(clientName)) {
                                playWithBox.addItem(player);
                            }
                        }
                    }




                }

            }
            catch(Exception ex) {

            }
        }}).start();
    }

}
