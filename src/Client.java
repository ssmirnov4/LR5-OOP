import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private JTextArea messageArea;
    private JTextField nameField;
    private JButton connectButton;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public Client() {
        super("Клиент");

        setLayout(new BorderLayout());
        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);

        nameField = new JTextField(10);
        connectButton = new JButton("Присоединиться");

        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Введите Ваше имя: "));
        topPanel.add(nameField);
        topPanel.add(connectButton);
        add(topPanel, BorderLayout.NORTH);

        connectButton.addActionListener(e -> {
            if (connectButton.getText().equals("Присоединиться")) {
                connect();
            } else {
                disconnect();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 5000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println(nameField.getText());

            String response = reader.readLine();
            if (response.equals("INVALID_NAME")) {
                socket.close();
                JOptionPane.showMessageDialog(this, "Имя не в списке!");
                return;
            }

            new Thread(this::receiveMessages).start();
            nameField.setEnabled(false);
            connectButton.setText("Отключиться");
            log("Подключено!");
        } catch (IOException e) {
            log("Ошибка: " + e);
        }
    }

    private void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            nameField.setEnabled(true);
            connectButton.setText("Подключиться");
            log("Отключено");
        } catch (IOException e) {
            log("Ошибка: " + e);
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                log("Получено: " + message);
            }
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                log("Соединение потеряно");
                disconnect();
            }
        }
    }

    private void log(String message) {
        messageArea.append(message + "\n");
    }

    public static void main(String[] args) {
        new Client().setVisible(true);
    }
}
