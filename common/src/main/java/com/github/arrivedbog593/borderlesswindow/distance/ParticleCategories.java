package com.github.arrivedbog593.borderlesswindow.distance;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Which particle slider each vanilla particle type follows. Built from the
 * full ParticleTypes list of 1.21.1 (about 110 types), grouped by where a
 * player sees them, not by how they are implemented. Anything not
 * listed -- biome ambience (ash, spores, cherry leaves), block breaking
 * dust, sculk, copper wax and sparks, enchanting glyphs, end rods,
 * fireworks, trial spawner and vault effects, and every modded particle --
 * follows the General slider.
 * <p>
 * A class of its own, loaded on the first particle a cap applies to, so
 * that DistanceState (loaded with the config at startup) never touches
 * ParticleTypes before the registries are bootstrapped.
 * <p>
 * Only particles that go through LevelRenderer.addParticleInternal reach
 * any slider at all: block breaking and firework sparks are created
 * straight in the ParticleEngine and are never capped. Tracking emitters
 * (a totem's burst, critical hits) do go through it, one particle at a
 * time.
 */
final class ParticleCategories {

    private static final Map<ParticleType<?>, DistanceCategory> BY_TYPE = build();

    private ParticleCategories() {
    }

    static DistanceCategory of(ParticleType<?> type) {
        return BY_TYPE.getOrDefault(type, DistanceCategory.PARTICLES);
    }

    private static Map<ParticleType<?>, DistanceCategory> build() {
        Map<ParticleType<?>, DistanceCategory> map = new IdentityHashMap<>();

        // Torches, furnaces, campfires, candles, fire, lava pools; and since
        // 26.2 the noxious gas over potent sulfur. Only the gas cloud (an
        // emitter) can be capped: the gas it gives off and the sulfur's
        // bubbles are spawned as always visible, which is honored.
        put(map, DistanceCategory.SMOKE_FIRE,
                ParticleTypes.SMOKE, ParticleTypes.LARGE_SMOKE, ParticleTypes.WHITE_SMOKE,
                ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                ParticleTypes.FLAME, ParticleTypes.SMALL_FLAME, ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.COPPER_FIRE_FLAME, ParticleTypes.LAVA,
                ParticleTypes.NOXIOUS_GAS_CLOUD, ParticleTypes.NOXIOUS_GAS);

        // Powered dust, torches, repeaters, ore; also the colored dust
        // maps and commands use.
        put(map, DistanceCategory.REDSTONE,
                ParticleTypes.DUST, ParticleTypes.DUST_COLOR_TRANSITION);

        // TNT and creepers, wind charges and breezes, the puff a mob
        // leaves when it dies.
        put(map, DistanceCategory.EXPLOSIONS,
                ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, ParticleTypes.POOF,
                ParticleTypes.GUST, ParticleTypes.SMALL_GUST,
                ParticleTypes.GUST_EMITTER_LARGE, ParticleTypes.GUST_EMITTER_SMALL);

        // Potion swirls (on any entity, beacon effects included), splash
        // and lingering potions, witches, omens.
        put(map, DistanceCategory.EFFECTS,
                ParticleTypes.ENTITY_EFFECT, ParticleTypes.EFFECT, ParticleTypes.INSTANT_EFFECT,
                ParticleTypes.WITCH, ParticleTypes.DRAGON_BREATH,
                ParticleTypes.RAID_OMEN, ParticleTypes.TRIAL_OMEN, ParticleTypes.TOTEM_OF_UNDYING);

        put(map, DistanceCategory.COMBAT,
                ParticleTypes.CRIT, ParticleTypes.ENCHANTED_HIT, ParticleTypes.SWEEP_ATTACK,
                ParticleTypes.DAMAGE_INDICATOR, ParticleTypes.SONIC_BOOM);

        // Rain hitting the ground, splashes, bubbles and bubble columns,
        // fishing, conduits, and every drip (water, lava, honey, crying
        // obsidian, dripstone).
        put(map, DistanceCategory.WATER_DRIPS,
                ParticleTypes.RAIN, ParticleTypes.SPLASH, ParticleTypes.UNDERWATER,
                ParticleTypes.BUBBLE, ParticleTypes.BUBBLE_POP,
                ParticleTypes.BUBBLE_COLUMN_UP, ParticleTypes.CURRENT_DOWN,
                ParticleTypes.FISHING, ParticleTypes.NAUTILUS,
                ParticleTypes.DRIPPING_WATER, ParticleTypes.FALLING_WATER,
                ParticleTypes.DRIPPING_LAVA, ParticleTypes.FALLING_LAVA, ParticleTypes.LANDING_LAVA,
                ParticleTypes.DRIPPING_HONEY, ParticleTypes.FALLING_HONEY, ParticleTypes.LANDING_HONEY,
                ParticleTypes.DRIPPING_OBSIDIAN_TEAR, ParticleTypes.FALLING_OBSIDIAN_TEAR,
                ParticleTypes.LANDING_OBSIDIAN_TEAR,
                ParticleTypes.DRIPPING_DRIPSTONE_WATER, ParticleTypes.FALLING_DRIPSTONE_WATER,
                ParticleTypes.DRIPPING_DRIPSTONE_LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA,
                // Since 26.2: potent sulfur's geysers under water (the
                // eruption and the base, plume and poof it gives off) and
                // its bubbles.
                ParticleTypes.GEYSER, ParticleTypes.GEYSER_BASE,
                ParticleTypes.GEYSER_PLUME, ParticleTypes.GEYSER_POOF,
                ParticleTypes.SULFUR_BUBBLES);

        // Breeding hearts, villagers (also bone meal's green sparkles,
        // which share the type), note blocks, and what mobs give off:
        // pandas, llamas, squid, dolphins, slimes, bees, glow squid; and
        // since 26.1 a golden dandelion pausing or resetting a baby mob's
        // growth (AgeableMob).
        put(map, DistanceCategory.MOB_PARTICLES,
                ParticleTypes.HEART, ParticleTypes.ANGRY_VILLAGER, ParticleTypes.HAPPY_VILLAGER,
                ParticleTypes.NOTE, ParticleTypes.SNEEZE, ParticleTypes.SPIT,
                ParticleTypes.SQUID_INK, ParticleTypes.GLOW_SQUID_INK, ParticleTypes.GLOW,
                ParticleTypes.DOLPHIN, ParticleTypes.ITEM_SLIME, ParticleTypes.FALLING_NECTAR,
                ParticleTypes.EGG_CRACK, ParticleTypes.PAUSE_MOB_GROWTH, ParticleTypes.RESET_MOB_GROWTH,
                // Since 26.2, the sulfur cube's goo (like a slime's).
                ParticleTypes.SULFUR_CUBE_GOO);

        // Nether portals, endermen, ender pearls, ender chests; respawn
        // anchors (reverse portal). Crying obsidian drips are Water.
        put(map, DistanceCategory.PORTALS,
                ParticleTypes.PORTAL, ParticleTypes.REVERSE_PORTAL);

        return map;
    }

    private static void put(Map<ParticleType<?>, DistanceCategory> map, DistanceCategory category,
                            ParticleType<?>... types) {
        for (ParticleType<?> type : types) {
            map.put(type, category);
        }
    }
}
