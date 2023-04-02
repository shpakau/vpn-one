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
                // Запуск туннеля WireGuard
                tunnel = new Tunnel(configPath, new FileConfigStore(configPath), new WgQuickBackend(wgQuickPath));
                tunnel.setState(Tunnel.State.UP);

                // Установите DNS-резольвер
                RootShell.run("ndc resolver setnetdns vpn0 8.8.8.8 8.8.4.4");

                // Добавляем правило маршрутизации
                RootShell.run("ip rule add table 51820 priority 1000");

                // Добавляем правила брандмауэра
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
                // Удалить правила брандмауэра
                RootShell.run("iptables -D OUTPUT -o vpn0 -j ACCEPT");
                RootShell.run("iptables -D INPUT -i vpn0 -j ACCEPT");
                RootShell.run("iptables -D FORWARD -i vpn0 -j ACCEPT");
                RootShell.run("iptables -D FORWARD -o vpn0 -j ACCEPT");
                RootShell.run("iptables -t nat -D POSTROUTING -o vpn0 -j MASQUERADE");

                // Удалить правило маршрутизации
                RootShell.run("ip rule del table 51820");

                // Неустановленный DNS-резольвер
                RootShell.run("ndc resolver setnetdns vpn0 \"\"");

                // Остановить туннель WireGuard
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
        // Настройка VPN-клиента
        String configPath = "config/wg0.conf"; // путь к конфигурационному файлу WireGuard
        String wgQuickPath = "/usr/bin/wg-quick"; // путь к исполняемому файлу wg-quick
        VPNClient client = new VPNClient(configPath, wgQuickPath);
        // Начать VPN-соединение
        client.start();

        // Подождите, пока соединение будет установлено
        while (!client.isRunning()) {
            Thread.sleep(1000);
        }

        // Статус
        System.out.println("VPN-соединение запущено");

        // Ожидаем, пока пользователь прекратит VPN-соединение
        System.out.println("Нажмите Enter для остановки VPN-соединения");
        System.in.read();

        // Остановить VPN-соединение
        client.stop();

        // Статус
        System.out.println("VPN-соединение было остановлено");
    }
}
