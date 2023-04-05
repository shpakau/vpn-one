package org.example;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.wireguard.config.Config;
import com.wireguard.config.ConfigParser;
import com.wireguard.config.Interface;
import com.wireguard.crypto.Key;
import com.wireguard.crypto.KeyFormatException;
import com.wireguard.crypto.KeyPair;
import com.wireguard.crypto.KeyPairFormatException;
import com.wireguard.crypto.Noise;
import com.wireguard.crypto.NoiseException;
import com.wireguard.util.NonNull;
import com.wireguard.util.NonNullByDefault;
import com.wireguard.util.Pair;

@NonNullByDefault
public class WireGuardVPNClient implements Runnable {

    private static final int MAX_PACKET_SIZE = 65535;

    private String serverAddress;
    private int serverPort;
    private String privateKey;
    private String publicKey;
    private KeyPair keyPair;
    private Socket socket;
    private Map<String, Interface> interfaces;

    public WireGuardVPNClient(String serverAddress, int serverPort, String privateKey) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.privateKey = privateKey;
        try {
            keyPair = KeyPair.parse(privateKey);
            publicKey = keyPair.getPublicKey().toString();
        } catch (KeyPairFormatException e) {
            e.printStackTrace();
        }
        interfaces = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            // Connect to server
            socket = new Socket(serverAddress, serverPort);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create and send handshake message
            byte[] clientEphemeralPublicKeyBytes = Noise.getPublicKey();
            String clientEphemeralPublicKey = Base64.getEncoder().encodeToString(clientEphemeralPublicKeyBytes);
            byte[] clientStaticPublicKeyBytes = keyPair.getPublicKey().getBytes();
            String clientStaticPublicKey = Base64.getEncoder().encodeToString(clientStaticPublicKeyBytes);
            byte[] handshakeMessageBytes = ("{\"client_ephemeral_public_key\":\"" + clientEphemeralPublicKey + "\",\"client_static_public_key\":\"" + clientStaticPublicKey + "\"}\n").getBytes(StandardCharsets.UTF_8);
            out.write(handshakeMessageBytes);

            // Receive and parse server response
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            int readResult;
            byte[] readBuffer = new byte[MAX_PACKET_SIZE];
            while ((readResult = in.read(readBuffer)) != -1) {
                responseBuffer.write(readBuffer, 0, readResult);
                if (readResult < MAX_PACKET_SIZE) {
                    break;
                }
            }
            String responseString = responseBuffer.toString(StandardCharsets.UTF_8);
            ConfigParser parser = new ConfigParser();
            Pair<Config, String> parseResult = parser.parse(responseString);
            if (parseResult == null) {
                System.err.println("Error: Failed to parse configuration.");
                return;
            }
            Config config = parseResult.getLeft();
            String serverEphemeralPublicKeyString = parseResult.getRight();

            // Derive shared keys and start WireGuard threads
            try {
                Key serverEphemeralPublicKey = new Key(serverEphemeralPublicKeyString);
                byte[] clientEphemeralSecretKeyBytes = Noise.getPrivateKey(clientEphemeralPublicKeyBytes);
                Key clientEphemeralSecretKey = new Key(clientEphemeralSecretKeyBytes);
                byte[] sharedSecretBytes = Noise.getSharedSecret(clientEphemeralSecretKey, serverEphemeralPublicKey);
                byte[] clientToServerKeyBytes = Noise.getSymmetricmetricKey(sharedSecretBytes, 0);
                byte[] serverToClientKeyBytes = Noise.getSymmetricKey(sharedSecretBytes, 32);
                WireGuardThread clientThread = new WireGuardThread(out, clientToServerKeyBytes, interfaces);
                clientThread.start();
                WireGuardThread serverThread = new WireGuardThread(in, serverToClientKeyBytes, interfaces);
                serverThread.start();
            } catch (NoiseException | KeyFormatException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close socket and all interfaces
            try {
                if (socket != null) {
                    socket.close();
                }
                for (Interface intf : interfaces.values()) {
                    intf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java WireGuardVPNClient <server_address> <server_port> <private_key>");
            return;
        }
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String privateKey = args[2];
        WireGuardVPNClient client = new WireGuardVPNClient(serverAddress, serverPort, privateKey);
        client.run();
    }
}
