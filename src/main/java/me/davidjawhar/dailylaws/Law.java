package me.davidjawhar.dailylaws;

import org.bukkit.entity.Player;

public interface Law {
    String id();
    String description();

    default void onActivate(LawContext ctx) {}
    default void onDeactivate(LawContext ctx) {}

    default boolean affects(Player p, LawContext ctx) {
        return ctx.isPlayerAffected(p);
    }
}