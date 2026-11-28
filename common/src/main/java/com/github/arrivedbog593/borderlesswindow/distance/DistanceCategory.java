package com.github.arrivedbog593.borderlesswindow.distance;

import java.util.ArrayList;
import java.util.List;

/**
 * Every distance slider, in the order both UIs show them. A category with
 * no parent is a "General" slider (all entities, all block entities, all
 * particles), whose leftmost slot is "Default": vanilla's own distance.
 * A category with a parent is one kind inside it, whose leftmost slot is
 * "General": it follows its parent; any other value replaces the parent's
 * for that kind, higher or lower. See DistanceState for which entity or
 * block entity falls into which category.
 * <p>
 * The config key doubles as the Sodium option id and the stem of the
 * translation keys, so adding a category here is all the plumbing a new
 * slider needs, besides its translations and its line in DistanceState's
 * (or ParticleCategories') classification.
 */
public enum DistanceCategory {

    /**
     * The common 10-chunk tracking range. Steps of 8, so the smallest cap
     * is 8 blocks: a 1-block cap on every entity would hide the mob
     * attacking the player. Same steps for every mob category.
     */
    ENTITIES("distance_entities", 160, 8, null),
    HOSTILE_MOBS("distance_hostile_mobs", 160, 8, ENTITIES),
    LAND_ANIMALS("distance_land_animals", 160, 8, ENTITIES),
    WATER_MOBS("distance_water_mobs", 160, 8, ENTITIES),
    VILLAGERS_GOLEMS("distance_villagers_golems", 160, 8, ENTITIES),
    /** Harmless up close, so single blocks. */
    ITEMS("distance_items", 64, 1, ENTITIES),
    XP_ORBS("distance_xp_orbs", 64, 1, ENTITIES),
    ARMOR_STANDS("distance_armor_stands", 160, 1, ENTITIES),
    FRAMES("distance_frames", 160, 1, ENTITIES),
    /**
     * New in 26.3. Tracked 10 chunks out like frames, and as harmless, so
     * single blocks too.
     */
    CUSHIONS("distance_cushions", 160, 1, ENTITIES),
    VEHICLES("distance_vehicles", 160, 8, ENTITIES),
    /** Primed TNT is no harmless sight up close, so steps of 8 like vehicles. */
    FALLING_BLOCKS("distance_falling_blocks", 160, 8, ENTITIES),
    /** Projectiles are tracked 4 chunks out: nothing to cap past 64. */
    PROJECTILES("distance_projectiles", 64, 8, ENTITIES),

    /** Vanilla's fixed block entity distance; nothing to cap past 64. */
    BLOCK_ENTITIES("distance_block_entities", 64, 8, null),
    CHESTS("distance_chests", 64, 8, BLOCK_ENTITIES),
    SIGNS("distance_signs", 64, 8, BLOCK_ENTITIES),
    BANNERS("distance_banners", 64, 8, BLOCK_ENTITIES),
    HEADS("distance_heads", 64, 8, BLOCK_ENTITIES),
    BOOKS("distance_books", 64, 8, BLOCK_ENTITIES),
    BELLS("distance_bells", 64, 8, BLOCK_ENTITIES),
    CAMPFIRES("distance_campfires", 64, 8, BLOCK_ENTITIES),
    POTS("distance_pots", 64, 8, BLOCK_ENTITIES),
    SPAWNERS("distance_spawners", 64, 8, BLOCK_ENTITIES),

    /**
     * Vanilla's own particle limit; there is nothing to cap above it. The
     * key predates the categories and stays, so older configs keep their
     * value. See ParticleCategories for which type falls where.
     */
    PARTICLES("distance_particles", 32, 1, null),
    SMOKE_FIRE("distance_particles_smoke", 32, 1, PARTICLES),
    REDSTONE("distance_particles_redstone", 32, 1, PARTICLES),
    EXPLOSIONS("distance_particles_explosions", 32, 1, PARTICLES),
    EFFECTS("distance_particles_effects", 32, 1, PARTICLES),
    COMBAT("distance_particles_combat", 32, 1, PARTICLES),
    WATER_DRIPS("distance_particles_water", 32, 1, PARTICLES),
    MOB_PARTICLES("distance_particles_mobs", 32, 1, PARTICLES),
    PORTALS("distance_particles_portals", 32, 1, PARTICLES);

    private final String configKey;
    private final int max;
    private final int step;
    private final DistanceCategory parent;

    DistanceCategory(String configKey, int max, int step, DistanceCategory parent) {
        this.configKey = configKey;
        this.max = max;
        this.step = step;
        this.parent = parent;
    }

    /** Key in config/borderlesswindow.json. */
    public String configKey() {
        return this.configKey;
    }

    /** Sodium option id, "borderlesswindow:distance_...". */
    public String optionId() {
        return "borderlesswindow:" + this.configKey;
    }

    public String nameKey() {
        return "borderlesswindow.options." + this.configKey + ".name";
    }

    public String tooltipKey() {
        return "borderlesswindow.options." + this.configKey + ".tooltip";
    }

    public int max() {
        return this.max;
    }

    public int step() {
        return this.step;
    }

    /** The General slider this one follows, or null for a General slider. */
    public DistanceCategory parent() {
        return this.parent;
    }

    public boolean isGeneral() {
        return this.parent == null;
    }

    /** A General slider followed by its own categories, in UI order. */
    public List<DistanceCategory> withChildren() {
        List<DistanceCategory> group = new ArrayList<>();
        group.add(this);
        for (DistanceCategory category : values()) {
            if (category.parent == this) {
                group.add(category);
            }
        }
        return group;
    }
}
