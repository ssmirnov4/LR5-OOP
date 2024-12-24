import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class Server extends JFrame {
    private DefaultListModel<String> clientModel = new DefaultListModel<>();
    private Map<String, PrintWriter> clients = new HashMap<>();
    private JTextArea logArea = new JTextArea(10, 40);

    public Server() {
        super("Сервер");
        setupGUI();
        loadClients("C:/Users/Пользователь/IdeaProjects/untitled8/src/clients.txt");
        new Thread(this::startServer).start();
    }

    private void setupGUI() {
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Отправить");

        setLayout(new BorderLayout());
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(new JScrollPane(new JList<>(clientModel)), BorderLayout.EAST);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            sendMessage(message);
            messageField.setText("");
        });

        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void startServer() {
        Socket s = null;
        try {
            ServerSocket server = new ServerSocket(5000);
            log("Сервер запущен на порте: 5000");
            while (true) {
                s = server.accept();
                new ClientHandler(s).start();
            }
        } catch (IOException e) {
            log("ошибка " + e);
        }
    }

    private void loadClients(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) clientModel.addElement(line.trim());
            log("Клиенты загружены");
        } catch (IOException e) {
            log("Не удалось загрузить клиентов из файла " + e);
        }
    }

    private void sendMessage(String message) {
        List<String> clientList = Collections.list(clientModel.elements());
        for (String client : clientList) {
            PrintWriter writer = clients.get(client);
            if (writer != null) {
                writer.println(message);
                log("Отправлено клиенту " + client + ": " + message);
            }
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }


        private class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                String clientName = reader.readLine();
                if (!clientModel.contains(clientName)) {
                    writer.println("INVALID_NAME");
                    log("Имя клиента не найдено " + clientName);
                } else {
                    writer.println("OK");
                    clients.put(clientName, writer);
                    log(clientName + " connected.");
                    while (reader.readLine() != null);
                }
            } catch (IOException e) {
                log("Ошибка клиента: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
