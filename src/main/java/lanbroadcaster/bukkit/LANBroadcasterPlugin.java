package lanbroadcaster.bukkit;

import lanbroadcaster.LANBroadcaster;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class LANBroadcasterPlugin extends JavaPlugin {
    private LANBroadcaster broadcaster;

    @Override
    public void onDisable() {
        broadcaster.setRunning(false);
        broadcaster = null;
    }

    @Override
    public void onEnable() {
        Server server = getServer();
        broadcaster = new LANBroadcaster(LANBroadcaster.createSocket(), server.getPort(), server.getMotd(), server.getIp(), getLogger());
        getServer().getScheduler().runTaskAsynchronously(this, broadcaster);
    }
}
