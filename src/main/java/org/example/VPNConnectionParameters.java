package org.example;
public class VPNConnectionParameters {
    private String serverAddress;
    private int serverPort;
    private String privateKey;
    private String publicKey;
    private String endpointAddress;
    private int endpointPort;
    private String allowedIPs;
    private int mtu;
    private int keepalive;

    public VPNConnectionParameters(String serverAddress, int serverPort, String privateKey, String publicKey,
                                   String endpointAddress, int endpointPort, String allowedIPs, int mtu, int keepalive) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.endpointAddress = endpointAddress;
        this.endpointPort = endpointPort;
        this.allowedIPs = allowedIPs;
        this.mtu = mtu;
        this.keepalive = keepalive;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public int getEndpointPort() {
        return endpointPort;
    }

    public String getAllowedIPs() {
        return allowedIPs;
    }

    public int getMTU() {
        return mtu;
    }

    public int getKeepalive() {
        return keepalive;
    }
}
