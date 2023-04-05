package org.example;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class VPNClient {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "MySecretKey12345";
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int serverPort = 9000;

        try {
            // подключение к VPN-серверу
            Socket socket = new Socket(serverAddress, serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

            // отправка запроса на подключение
            writer.write("CONNECT\n");
            writer.flush();

            // получение ответа на запрос
            String response = reader.readLine();
            if (!response.equals("OK")) {
                System.out.println("Failed to connect to VPN server");
                socket.close();
                return;
            }

            // генерация ключа шифрования
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // создание буферов для чтения/записи данных
            byte[] inputBuffer = new byte[BUFFER_SIZE];
            byte[] encryptedBuffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // чтение данных из стандартного ввода и отправка их на сервер
            while ((bytesRead = System.in.read(inputBuffer)) != -1) {
                // шифрование данных
                encryptedBuffer = cipher.doFinal(inputBuffer, 0, bytesRead);

                // отправка зашифрованных данных на сервер
                writer.write(new String(encryptedBuffer));
                writer.flush();

                // чтение ответа сервера
                response = reader.readLine();
                System.out.println(response);
            }

            // закрытие соединения
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}