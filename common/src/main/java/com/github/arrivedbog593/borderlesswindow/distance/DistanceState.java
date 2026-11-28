package com.github.arrivedbog593.borderlesswindow.distance;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

/**
 * Holds the maximum draw distances of every {@link DistanceCategory} and
 * decides which category an entity or block entity belongs to.
 * EntityRenderDispatcherMixin, BlockEntityRenderDispatcherMixin and
 * ClientLevelMixin consult it.
 * <p>
 * DRAW, NOT EXIST. Every one of these only stops something from being
 * drawn (or, for particles, from being spawned on this client). The
 * entities are still there: items can be picked up, orbs still fly to the
 * player, mobs still move and attack, and nothing changes for the server
 * or other players.
 * <p>
 * WHAT VANILLA ALREADY DOES, verified against 1.21.1 and 1.21.11. Entity.
 * shouldRenderAtSqrDistance draws an entity up to its bounding box size x
 * 64 x the Entity Distance option: 16 blocks for an item (0.25), 32 for
 * an orb (0.5), about 67 for a zombie. ArmorStand overrides it with a far
 * larger reach, and item frames and other players have their own; the
 * server only sends entities within each type's tracking range, 4 to 10
 * chunks for nearly all of them. Block entities with a renderer (chests,
 * signs, banners...) are drawn up to BlockEntityRenderer.getViewDistance(),
 * a fixed 64 blocks that no option changes; beacons and End gateways use
 * 256. Particles are refused past 32 blocks in ClientLevel.doAddParticle
 * unless they are forced. These caps
 * can only LOWER those numbers; above vanilla's own distance a cap simply
 * does nothing.
 * <p>
 * SCALE: {@link #DEFAULT} (0) is the leftmost slot of every slider. On a
 * General slider it means no cap -- vanilla behavior, like the cloud
 * height's "Default". On a category it means "follow the General slider";
 * any other value is a distance in blocks that replaces the General one
 * for that category, higher or lower.
 * <p>
 * Split apply/persist like DistortionState: these are sliders, and
 * AbstractSliderButton fires applyValue on every step of a drag.
 */
public final class DistanceState {

    /** Leftmost slot: "Default" on a General slider, "General" on a category. */
    public static final int DEFAULT = 0;

    private static final DistanceCategory[] CATEGORIES = DistanceCategory.values();

    /** Indexed by ordinal. */
    private static final int[] values = new int[CATEGORIES.length];

    /**
     * True while any entity (block entity, particle) slider holds a
     * value, so the hooks skip the classification entirely in the common
     * all-Default case.
     */
    private static boolean entityCapsActive = false;
    private static boolean blockEntityCapsActive = false;
    private static boolean particleCapsActive = false;

    private DistanceState() {
    }

    /**
     * Called once at startup with the values loaded from disk, indexed by
     * ordinal; a shorter array leaves the missing ones at DEFAULT.
     */
    public static void init(int[] loaded) {
        for (DistanceCategory category : CATEGORIES) {
            int index = category.ordinal();
            values[index] = index < loaded.length ? sanitize(category, loaded[index]) : DEFAULT;
        }
        refreshActive();
    }

    /** The slider's own value: DEFAULT or a distance. */
    public static int get(DistanceCategory category) {
        return values[category.ordinal()];
    }

    /** Memory only -- the change shows on the next frame. */
    public static void apply(DistanceCategory category, int blocks) {
        values[category.ordinal()] = sanitize(category, blocks);
        refreshActive();
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void set(DistanceCategory category, int blocks) {
        apply(category, blocks);
        persist();
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * The distance that applies to a category: its own value, or its
     * General slider's when it is left at "General". DEFAULT means no cap.
     */
    public static int effective(DistanceCategory category) {
        int own = values[category.ordinal()];
        DistanceCategory parent = category.parent();
        return own != DEFAULT || parent == null ? own : values[parent.ordinal()];
    }

    /**
     * The cap for one entity, or DEFAULT. Some are always drawn, whatever
     * the sliders say:
     * - other players: losing sight of them is a real disadvantage, and
     *   their names would vanish with them;
     * - glowing entities: the glow (a Spectral Arrow hit, the effect, a
     *   team color) exists to keep them visible from afar;
     * - floating text: armor stands with a visible name and text displays,
     *   which servers and maps use for holograms and labels.
     */
    public static int entityCap(Entity entity) {
        if (!entityCapsActive || entity instanceof Player || entity.isCurrentlyGlowing()
                || entity instanceof Display.TextDisplay
                || entity instanceof ArmorStand && entity.isCustomNameVisible()) {
            return DEFAULT;
        }
        return effective(categoryOf(entity));
    }

    /**
     * The cap for one block entity, or DEFAULT. Always drawn: beacons and
     * End portals and gateways (TheEndGatewayBlockEntity extends
     * TheEndPortalBlockEntity), landmarks meant to be seen from far away;
     * and moving piston blocks, which would vanish mid-push.
     */
    public static int blockEntityCap(BlockEntity blockEntity) {
        if (!blockEntityCapsActive || blockEntity instanceof BeaconBlockEntity
                || blockEntity instanceof TheEndPortalBlockEntity
                || blockEntity instanceof PistonMovingBlockEntity) {
            return DEFAULT;
        }
        return effective(categoryOf(blockEntity));
    }

    /**
     * The cap for a particle about to spawn, or DEFAULT. Its type decides
     * the category (ParticleCategories); unknown and modded types follow
     * the General slider. Particles forced by their caller never get here;
     * those forced only by their type do (see LevelRendererMixin).
     */
    public static int particleCap(ParticleOptions options) {
        if (!particleCapsActive) {
            return DEFAULT;
        }
        return effective(ParticleCategories.of(options.getType()));
    }

    /**
     * True when {@code distanceSqr} is past the cap. Always false on
     * {@link #DEFAULT}, so callers can pass any cap straight in.
     */
    public static boolean isBeyond(int cap, double distanceSqr) {
        return cap != DEFAULT && distanceSqr > (double) cap * cap;
    }

    /**
     * Slider label shared by both UIs. Leftmost slot: "Default" on a
     * General slider (the cloud height's key, same meaning), "General" on
     * a category. Elsewhere "N blocks".
     */
    public static Component format(DistanceCategory category, int blocks) {
        if (blocks > DEFAULT) {
            return Component.translatable("borderlesswindow.options.distance.blocks", blocks);
        }
        return category.isGeneral()
                ? Component.translatable("borderlesswindow.options.cloud_height.default")
                : Component.translatable("borderlesswindow.options.distance.general");
    }

    /**
     * Mobs go by the category vanilla (and every mod) gives each mob type
     * for spawning, so modded mobs land in the right slider on their own.
     * NeoForge lets mods add categories, hence the default branch.
     * Anything else without a slider of its own (block and item displays,
     * End crystals, markers...) follows the General one.
     */
    private static DistanceCategory categoryOf(Entity entity) {
        if (entity instanceof ItemEntity) {
            return DistanceCategory.ITEMS;
        }
        if (entity instanceof ExperienceOrb) {
            return DistanceCategory.XP_ORBS;
        }
        // A Mannequin is an Avatar (player-shaped, player skin), not an
        // ArmorStand, but it is decoration all the same.
        if (entity instanceof ArmorStand || entity instanceof Mannequin) {
            return DistanceCategory.ARMOR_STANDS;
        }
        // The leash knot is a BlockAttachedEntity, not a HangingEntity.
        if (entity instanceof HangingEntity || entity instanceof LeashFenceKnotEntity) {
            return DistanceCategory.FRAMES;
        }
        if (entity instanceof VehicleEntity) {
            return DistanceCategory.VEHICLES;
        }
        if (entity instanceof FallingBlockEntity || entity instanceof PrimedTnt) {
            return DistanceCategory.FALLING_BLOCKS;
        }
        if (entity instanceof Projectile) {
            return DistanceCategory.PROJECTILES;
        }
        // Before the MobCategory check: vanilla gives the wandering trader
        // CREATURE, which would put it with the animals.
        if (entity instanceof AbstractVillager) {
            return DistanceCategory.VILLAGERS_GOLEMS;
        }
        if (entity instanceof Mob) {
            // Not a switch: NeoForge makes MobCategory extensible
            // (IExtensibleEnum), so a mod can add categories. A switch
            // listing every vanilla one would throw MatchException on
            // them without a default, which the IDE flags as unnecessary.
            // Here they fall through to the General slider below.
            MobCategory category = entity.getType().getCategory();
            if (category == MobCategory.MONSTER) {
                return DistanceCategory.HOSTILE_MOBS;
            }
            if (category == MobCategory.CREATURE || category == MobCategory.AMBIENT) {
                return DistanceCategory.LAND_ANIMALS;
            }
            if (category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                    || category == MobCategory.UNDERGROUND_WATER_CREATURE || category == MobCategory.AXOLOTLS) {
                return DistanceCategory.WATER_MOBS;
            }
            if (category == MobCategory.MISC) {
                return DistanceCategory.VILLAGERS_GOLEMS;
            }
        }
        return DistanceCategory.ENTITIES;
    }

    /**
     * Subclasses are covered too: trapped chests (ChestBlockEntity) and
     * hanging signs (SignBlockEntity). Conduits, structure blocks,
     * suspicious sand and modded block entities follow the General slider.
     * <p>
     * Since 26.2 a sign's board and post are an ordinary block model, built
     * into the chunk mesh with the terrain; its renderer only draws the
     * text. So the Signs slider (named Sign Text here) hides the text
     * alone. Beds are no block entity at all any more.
     */
    private static DistanceCategory categoryOf(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity
                || blockEntity instanceof ShulkerBoxBlockEntity) {
            return DistanceCategory.CHESTS;
        }
        if (blockEntity instanceof SignBlockEntity) {
            return DistanceCategory.SIGNS;
        }
        if (blockEntity instanceof BannerBlockEntity) {
            return DistanceCategory.BANNERS;
        }
        if (blockEntity instanceof SkullBlockEntity) {
            return DistanceCategory.HEADS;
        }
        if (blockEntity instanceof EnchantingTableBlockEntity || blockEntity instanceof LecternBlockEntity) {
            return DistanceCategory.BOOKS;
        }
        if (blockEntity instanceof BellBlockEntity) {
            return DistanceCategory.BELLS;
        }
        if (blockEntity instanceof CampfireBlockEntity) {
            return DistanceCategory.CAMPFIRES;
        }
        if (blockEntity instanceof DecoratedPotBlockEntity) {
            return DistanceCategory.POTS;
        }
        if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity
                || blockEntity instanceof VaultBlockEntity) {
            return DistanceCategory.SPAWNERS;
        }
        return DistanceCategory.BLOCK_ENTITIES;
    }

    private static void refreshActive() {
        entityCapsActive = anyValueIn(DistanceCategory.ENTITIES);
        blockEntityCapsActive = anyValueIn(DistanceCategory.BLOCK_ENTITIES);
        particleCapsActive = anyValueIn(DistanceCategory.PARTICLES);
    }

    private static boolean anyValueIn(DistanceCategory general) {
        for (DistanceCategory category : CATEGORIES) {
            if ((category == general || category.parent() == general)
                    && values[category.ordinal()] != DEFAULT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Anything at or below DEFAULT is DEFAULT; the rest is clamped and
     * snapped to the slider's step (rounding up), so a hand-edited config
     * never holds a value the slider cannot show.
     */
    private static int sanitize(DistanceCategory category, int value) {
        if (value <= DEFAULT) {
            return DEFAULT;
        }
        int step = category.step();
        return Math.min((value + step - 1) / step * step, category.max());
    }
}
