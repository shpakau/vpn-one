package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GUI extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JPanel statusTab;
    private JPanel serverTab;
    private JPanel accountTab;

    private JLabel incomingTrafficLabel;
    private JLabel outgoingTrafficLabel;
    private JComboBox<String> serverComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private ArrayList<String> serverList;

    public GUI() {
        // настройки окна
        setTitle("MyVPN Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // инициализация списка серверов
        serverList = new ArrayList<>();
        serverList.add("Сервер 1");
        serverList.add("Сервер 2");
        serverList.add("Сервер 3");

        // создаем главную панель и панель с вкладками
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();

        // создаем вкладку состояния
        statusTab = new JPanel();
        statusTab.setLayout(new FlowLayout());
        incomingTrafficLabel = new JLabel("Входящий трафик: 0");
        outgoingTrafficLabel = new JLabel("Исходящий трафик: 0");
        statusTab.add(incomingTrafficLabel);
        statusTab.add(outgoingTrafficLabel);

        // создаем сервер
        serverTab = new JPanel();
        serverTab.setLayout(new FlowLayout());
        serverComboBox = new JComboBox<>(serverList.toArray(new String[0]));
        serverTab.add(serverComboBox);

        // создаем учетную запись
        accountTab = new JPanel();
        accountTab.setLayout(new GridLayout(2, 2));
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        accountTab.add(usernameLabel);
        accountTab.add(usernameField);
        accountTab.add(passwordLabel);
        accountTab.add(passwordField);

        // Добавление вкладок в панель вкладок и добавление панели вкладок в основную панель
        tabbedPane.addTab("Статус", statusTab);
        tabbedPane.addTab("Сервер", serverTab);
        tabbedPane.addTab("Аккаунт", accountTab);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // добавляем основную панель в окно
        add(mainPanel);

        // показать окно
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // обработка нажатий кнопок
    }

    public static void main(String[] args) {
        new GUI();
    }
}
