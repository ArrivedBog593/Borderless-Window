package com.github.arrivedbog593.borderlesswindow.visual;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Which nametags are drawn above entities (see NametagState).
 * <p>
 * - ALL: vanilla (default).
 * - PLAYERS: only other players' names.
 * - MOBS: only the names of everything that is not a player: named mobs
 *   and pets, and also named armor stands (server holograms are often
 *   made of them), minecarts and the like. The tooltip says so.
 * - NONE: no nametags at all.
 */
public enum NametagMode {
    ALL("borderlesswindow.nametag_mode.all"),
    PLAYERS("borderlesswindow.nametag_mode.players"),
    MOBS("borderlesswindow.nametag_mode.mobs"),
    NONE("borderlesswindow.nametag_mode.none");

    private final String translationKey;

    NametagMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public boolean shows(Entity entity) {
        return switch (this) {
            case ALL -> true;
            case PLAYERS -> entity instanceof Player;
            case MOBS -> !(entity instanceof Player);
            case NONE -> false;
        };
    }
}
