package org.example;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.android.backend.WgQuickBackend;
import com.wireguard.android.configStore.FileConfigStore;
import com.wireguard.android.configStore.WgQuickConfigStore;
import com.wireguard.android.model.TunnelManager;
import com.wireguard.android.util.RootShell;

public class VPNClient {
    private final String configPath;
    private final String wgQuickPath;
    private final AtomicBoolean running;
    private Tunnel tunnel;

    public VPNClient(String configPath, String wgQuickPath) {
        this.configPath = configPath;
        this.wgQuickPath = wgQuickPath;
        running = new AtomicBoolean(false);
    }

    public synchronized void start() throws Exception {
        if (running.compareAndSet(false, true)) {
            try {
                // Start WireGuard tunnel
                tunnel = new Tunnel(configPath, new FileConfigStore(configPath), new WgQuickBackend(wgQuickPath));
                tunnel.setState(Tunnel.State.UP);

                // Set DNS resolver
                RootShell.run("ndc resolver setnetdns vpn0 8.8.8.8 8.8.4.4");

                // Add routing rule
                RootShell.run("ip rule add table 51820 priority 1000");

                // Add firewall rules
                RootShell.run("iptables -I OUTPUT -o vpn0 -j ACCEPT");
                RootShell.run("iptables -I INPUT -i vpn0 -j ACCEPT");
                RootShell.run("iptables -I FORWARD -i vpn0 -j ACCEPT");
                RootShell.run("iptables -I FORWARD -o vpn0 -j ACCEPT");
                RootShell.run("iptables -t nat -I POSTROUTING -o vpn0 -j MASQUERADE");

            } catch (IOException e) {
                stop();
                throw new Exception("Failed to start VPN connection", e);
            }
        }
    }

    public synchronized void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                // Remove firewall rules
                RootShell.run("iptables -D OUTPUT -o vpn0 -j ACCEPT");
                RootShell.run("iptables -D INPUT -i vpn0 -j ACCEPT");
                RootShell.run("iptables -D FORWARD -i vpn0 -j ACCEPT");
                RootShell.run("iptables -D FORWARD -o vpn0 -j ACCEPT");
                RootShell.run("iptables -t nat -D POSTROUTING -o vpn0 -j MASQUERADE");

                // Remove routing rule
                RootShell.run("ip rule del table 51820");

                // Unset DNS resolver
                RootShell.run("ndc resolver setnetdns vpn0 \"\"");

                // Stop WireGuard tunnel
                if (tunnel != null) {
                    tunnel.setState(Tunnel.State.DOWN);
                    tunnel = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public static void main(String[] args) throws Exception {
        // Set up VPN client
        String configPath = "config/wg0.conf"; // путь к конфигурационному файлу WireGuard
        String wgQuickPath = "/usr/bin/wg-quick"; // путь к исполняемому файлу wg-quick
        VPNClient client = new VPNClient(configPath, wgQuickPath);
        // Start VPN connection
        client.start();

        // Wait for connection to be established
        while (!client.isRunning()) {
            Thread.sleep(1000);
        }

        // Print status
        System.out.println("VPN connection is running");

        // Wait for user to stop VPN connection
        System.out.println("Press Enter to stop VPN connection");
        System.in.read();

        // Stop VPN connection
        client.stop();

        // Print status
        System.out.println("VPN connection has been stopped");
    }
}
