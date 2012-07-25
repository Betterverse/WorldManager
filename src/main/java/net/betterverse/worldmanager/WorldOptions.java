package net.betterverse.worldmanager;

import java.io.File;

import org.bukkit.GameMode;
import org.bukkit.Location;

import net.betterverse.worldmanager.util.YamlFile;

public class WorldOptions {
    private final WorldManager plugin;
    private final YamlFile file;
    private Location spawn;
    private GameMode gameMode;

    public WorldOptions(WorldManager plugin, File file) {
        this.plugin = plugin;
        this.file = new YamlFile(plugin, file);
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Location getSpawnLocation() {
        return spawn;
    }

    public void load() {
        spawn = new Location(plugin.getServer().getWorld(file.getString("spawn.world")), file.getDouble("spawn.x"), file.getDouble("spawn.y"), file.getDouble("spawn.z"), file.getLong("spawn.yaw"),
                file.getLong("spawn.pitch"));
        gameMode = GameMode.valueOf(file.getString("game-mode"));
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        file.set("game-mode", gameMode.name());
        file.save();
    }

    public void setSpawnLocation(Location spawn) {
        this.spawn = spawn;
        file.set("spawn.world", spawn.getWorld().getName());
        file.set("spawn.x", spawn.getX());
        file.set("spawn.y", spawn.getY());
        file.set("spawn.z", spawn.getZ());
        file.set("spawn.pitch", spawn.getPitch());
        file.set("spawn.yaw", spawn.getYaw());

        file.save();
    }
}
