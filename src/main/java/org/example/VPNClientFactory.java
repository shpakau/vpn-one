package org.example;
import java.io.File;

public class VPNClientFactory {

    private static VPNClientFactory instance;

    private final String configPath;
    private final String wgQuickPath;

    private VPNClientFactory(String configPath, String wgQuickPath) {
        this.configPath = configPath;
        this.wgQuickPath = wgQuickPath;
    }

    public static VPNClientFactory getInstance(String configPath, String wgQuickPath) {
        if (instance == null) {
            instance = new VPNClientFactory(configPath, wgQuickPath);
        }
        return instance;
    }

    public VPNClient createVPNClient(VPNConnectionParameters parameters) {
        // Construct full config file path
        String configFile = new File(configPath, parameters.getServerName() + ".conf").getAbsolutePath();

        // Create VPN client instance
        return new VPNClient(configFile, wgQuickPath);
    }

}
