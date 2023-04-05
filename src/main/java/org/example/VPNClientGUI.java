package org.example;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class VPNClientGUI extends JFrame implements ActionListener {
    private JTextField usernameTextField, passwordTextField, serverAddressTextField;
    private JButton connectButton, disconnectButton;
    private JTextArea statusTextArea;

    public VPNClientGUI() {
        super("VPN Client");

        // Определение компонентов интерфейса
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(new JLabel("Username:"));
        usernameTextField = new JTextField("");
        inputPanel.add(usernameTextField);
        inputPanel.add(new JLabel("Password:"));
        passwordTextField = new JPasswordField("");
        inputPanel.add(passwordTextField);
        inputPanel.add(new JLabel("Server Address:"));
        serverAddressTextField = new JTextField("127.0.0.1");
        inputPanel.add(serverAddressTextField);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);
        buttonPanel.add(connectButton);
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(this);
        disconnectButton.setEnabled(false);
        buttonPanel.add(disconnectButton);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusTextArea = new JTextArea(10, 30);
        statusTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusTextArea);
        statusPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(statusPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton) {
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            String serverAddress = serverAddressTextField.getText();

            // подключение к VPN-серверу с использованием указанных учетных данных и адреса сервера
            boolean connected = connectToVPNServer(username, password, serverAddress);

            if (connected) {
                // обновление текстовой области статуса
                statusTextArea.append("Connected to VPN server at " + serverAddress + "\n");

                // отключение кнопки Connect и включение кнопки Disconnect
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
            } else {
                // обновление текстовой области статуса
                statusTextArea.append("Failed to connect to VPN server at " + serverAddress + "\n");
            }
        } else if (e.getSource() == disconnectButton) {
            // отключение от VPN-сервера
            disconnectFromVPNServer();

            // обновление текстовой области статуса
            statusTextArea.append("Disconnected from VPN-сервер\n");
            // отключение кнопки Disconnect и включение кнопки Connect
            disconnectButton.setEnabled(false);
            connectButton.setEnabled(true);
        }
    }

    private boolean connectToVPNServer(String username, String password, String serverAddress) {
        // код для подключения к VPN-серверу с использованием указанных учетных данных и адреса сервера
        return true; // временный код для демонстрации
    }

    private void disconnectFromVPNServer() {
        // код для отключения от VPN-сервера
        // временный код для демонстрации
    }

    public static void main(String[] args) {
        new VPNClientGUI();
    }
}
