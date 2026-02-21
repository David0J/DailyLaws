package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.Law;
import me.davidjawhar.dailylaws.LawContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class PotionLaw implements Law {
    public enum EffectType { SPEED, SLOWNESS, SLOW_FALLING, NIGHT_VISION }

    private final String id;
    private final String desc;
    private final EffectType type;
    private final int amplifier;
    private final int refreshSeconds;

    private BukkitTask task;

    public PotionLaw(String id, String desc, EffectType type, int amplifier, int refreshSeconds) {
        this.id = id;
        this.desc = desc;
        this.type = type;
        this.amplifier = amplifier;
        this.refreshSeconds = refreshSeconds;
    }

    @Override public String id() { return id; }
    @Override public String description() { return desc; }

    @Override
    public void onActivate(LawContext ctx) {
        PotionEffectType pet = switch (type) {
            case SPEED -> PotionEffectType.SPEED;
            case SLOWNESS -> PotionEffectType.SLOWNESS;
            case SLOW_FALLING -> PotionEffectType.SLOW_FALLING;
            case NIGHT_VISION -> PotionEffectType.NIGHT_VISION;
        };
        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!affects(p, ctx)) continue;
                p.addPotionEffect(new PotionEffect(Objects.requireNonNull(pet), refreshSeconds * 20 + 40, amplifier, true, false, true));
            }
        }, 1L, refreshSeconds * 20L);
    }

    @Override
    public void onDeactivate(LawContext ctx) {
        if (task != null) task.cancel();
    }
}