package me.davidjawhar.dailylaws;

import me.davidjawhar.dailylaws.laws.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LawManager {
    private final DailyLawsPlugin plugin;
    private final LawContext ctx;

    private final Map<String, Law> lawsById = new LinkedHashMap<>();
    private final Set<String> enabledPool = new LinkedHashSet<>();

    private List<String> active = new ArrayList<>();
    private List<String> yesterday = new ArrayList<>();
    private boolean paused = false;

    private BukkitTask tickTask;
    private BukkitTask periodicTask;

    public LawManager(DailyLawsPlugin plugin) {
        this.plugin = plugin;
        this.ctx = new LawContext(plugin);
    }

    public void registerLaws() {
        // Add more later easily
        add(new PotionLaw("FEATHER", "Slow Falling I", PotionLaw.EffectType.SLOW_FALLING, 0, 10));
        add(new PotionLaw("HEAVY", "Slowness I", PotionLaw.EffectType.SLOWNESS, 0, 10));
        add(new PotionLaw("SPEED", "Speed I", PotionLaw.EffectType.SPEED, 0, 10));
        add(new PotionLaw("NIGHT_VISION", "Night Vision", PotionLaw.EffectType.NIGHT_VISION, 0, 30));

        add(new GlassCannonLaw());
        add(new VampiricLaw());
        add(new DoubleDropsLaw());
        add(new StoneLotteryLaw());
        add(new CursedSpawnsLaw());
        add(new NoRegenLaw());
        add(new HotbarShuffleLaw());
        add(new RandomTpLaw());

        // Enabled pool from config
        enabledPool.clear();
        List<String> fromCfg = plugin.getConfig().getStringList("enabled_laws");
        if (fromCfg == null || fromCfg.isEmpty()) enabledPool.addAll(lawsById.keySet());
        else fromCfg.forEach(s -> enabledPool.add(s.toUpperCase()));
    }

    private void add(Law law) {
        lawsById.put(law.id().toUpperCase(), law);
    }

    public Collection<Law> getAllLaws() {
        return lawsById.values();
    }

    public String getActiveLawSummary() {
        if (active.isEmpty()) return "None";
        return active.stream()
                .map(id -> {
                    Law l = lawsById.get(id);
                    return l == null ? id : (l.id() + " (" + l.description() + ")");
                })
                .collect(Collectors.joining(ChatColor.GRAY + ", " + ChatColor.YELLOW, ChatColor.YELLOW.toString(), ""));
    }

    public String timeUntilNextRollString() {
        World w = getPrimaryWorld();
        if (w == null) return "Unknown";
        long t = w.getTime(); // 0..23999
        long ticksUntil = (24000 - t) % 24000;
        long seconds = ticksUntil / 20;
        long mins = seconds / 60;
        long sec = seconds % 60;
        return mins + "m " + sec + "s";
    }

    public void startSchedulers() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new SpigotEventBridge(plugin, this), plugin);

        // check time each second
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (paused) return;
            World w = getPrimaryWorld();
            if (w == null) return;
            if (w.getTime() == 0) {
                rollNewDay(false);
            }
        }, 20L, 20L);

        // periodic (every second) laws that need ticking handled internally via their own tasks (we do that in those law classes)
        // this placeholder stays minimal
    }

    public void shutdown() {
        if (tickTask != null) tickTask.cancel();
        if (periodicTask != null) periodicTask.cancel();
        HandlerList.unregisterAll(plugin);
        deactivateAll();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void onConfigReload() {
        // Re-read pool and settings
        enabledPool.clear();
        List<String> fromCfg = plugin.getConfig().getStringList("enabled_laws");
        if (fromCfg == null || fromCfg.isEmpty()) enabledPool.addAll(lawsById.keySet());
        else fromCfg.forEach(s -> enabledPool.add(s.toUpperCase()));
    }

    public void rollNewDay(boolean forced) {
        if (paused && !forced) return;

        // Deactivate old
        deactivateAll();

        yesterday = new ArrayList<>(active);

        // Choose count: 70/30
        int one = plugin.getConfig().getInt("roll_one_percent", 70);
        int roll = new Random().nextInt(100) + 1;
        int count = (roll <= one) ? 1 : 2;

        // Build candidate list
        List<String> candidates = enabledPool.stream()
                .filter(id -> lawsById.containsKey(id))
                .collect(Collectors.toList());

        if (!plugin.getConfig().getBoolean("repeat_allowed", false) && !yesterday.isEmpty()) {
            candidates.removeAll(yesterday);
            if (candidates.isEmpty()) {
                // fallback if pool too small
                candidates = enabledPool.stream().filter(id -> lawsById.containsKey(id)).collect(Collectors.toList());
            }
        }

        Collections.shuffle(candidates);
        active = candidates.stream().limit(count).collect(Collectors.toList());

        // Activate new
        for (String id : active) {
            Law law = lawsById.get(id);
            if (law != null) law.onActivate(ctx);
        }

        saveState();

        if (plugin.getConfig().getBoolean("broadcast_roll", true)) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "📜 New Laws for Today: " + getActiveLawSummary());
        }
    }

    private void deactivateAll() {
        for (String id : active) {
            Law law = lawsById.get(id);
            if (law != null) law.onDeactivate(ctx);
        }
        active.clear();
    }

    public boolean isActive(String id) {
        return active.contains(id.toUpperCase());
    }

    public boolean enableLaw(String id) {
        id = id.toUpperCase();
        if (!lawsById.containsKey(id)) return false;
        enabledPool.add(id);
        plugin.getConfig().set("enabled_laws", new ArrayList<>(enabledPool));
        plugin.saveConfig();
        return true;
    }

    public boolean disableLaw(String id) {
        id = id.toUpperCase();
        if (!lawsById.containsKey(id)) return false;
        enabledPool.remove(id);
        plugin.getConfig().set("enabled_laws", new ArrayList<>(enabledPool));
        plugin.saveConfig();
        return true;
    }

    private World getPrimaryWorld() {
        List<String> worlds = plugin.getConfig().getStringList("enabled_worlds");
        if (worlds != null && !worlds.isEmpty()) {
            World w = Bukkit.getWorld(worlds.get(0));
            if (w != null) return w;
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }

    public void loadState() {
        File f = new File(plugin.getDataFolder(), "state.yml");
        if (!f.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        this.active = y.getStringList("active");
        this.yesterday = y.getStringList("yesterday");
        this.paused = y.getBoolean("paused", false);
    }

    public void saveState() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            File f = new File(plugin.getDataFolder(), "state.yml");
            YamlConfiguration y = new YamlConfiguration();
            y.set("active", active);
            y.set("yesterday", yesterday);
            y.set("paused", paused);
            y.save(f);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed saving state.yml: " + e.getMessage());
        }
    }
}