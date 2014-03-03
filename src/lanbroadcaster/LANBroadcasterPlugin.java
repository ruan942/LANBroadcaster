package lanbroadcaster;

import org.bukkit.plugin.java.JavaPlugin;

public class LANBroadcasterPlugin extends JavaPlugin {
    public Thread thread;

    @Override
    public void onDisable() {
        thread.interrupt();
    }

    @Override
    public void onEnable() {
        thread = new Thread(new LANBroadcaster(this));
        thread.start();
    }
}
