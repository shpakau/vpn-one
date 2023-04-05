package org.example;
public class VPNClientFactory {
    public static VPNClient createVPNClient(String protocol, String serverAddress, int serverPort, String username, String password) throws IllegalArgumentException {
        if (protocol.equalsIgnoreCase("OpenVPN")) {
            return new OpenVPNClient(serverAddress, serverPort, username, password);
        } else if (protocol.equalsIgnoreCase("WireGuard")) {
            return new WireGuardVPNClient(serverAddress, serverPort, username);
        } else {
            throw new IllegalArgumentException("Unsupported VPN protocol: " + protocol);
        }
    }
}
