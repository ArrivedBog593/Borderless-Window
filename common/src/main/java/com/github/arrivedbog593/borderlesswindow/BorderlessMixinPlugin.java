package com.github.arrivedbog593.borderlesswindow;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * Keeps the optional mods' mixins quiet when those mods are missing.
 * Mixin skips a @Pseudo mixin whose target class does not exist, but only
 * after trying to load it, and that attempt logs "Error loading class" as
 * a warning: every player without Iris would see it in their log, where it
 * reads like a real problem. Mixin asks this plugin before looking up the
 * target, so checking first here avoids the lookup altogether.
 * <p>
 * The check asks Mixin's own bytecode provider, the same one that finds
 * the target when the mod is there, so it works the same on both loaders.
 * It runs transformers because NeoForge's provider refuses the untransformed
 * lookup. Lives outside the mixin package: Mixin refuses to load ordinary
 * classes from there.
 * <p>
 * The empty methods stay on purpose. Mixin made them default methods only
 * in Fabric's 0.17.4 build (the one NeoForge 26.1.2 ships); older builds
 * declare them abstract, and a loader running one of those would crash
 * with an AbstractMethodError without these.
 */
@SuppressWarnings("RedundantMethodOverride")
public class BorderlessMixinPlugin implements IMixinConfigPlugin {

    private static final String IRIS_HAND_RENDERER_MIXIN =
            "com.github.arrivedbog593.borderlesswindow.mixin.IrisHandRendererMixin";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (IRIS_HAND_RENDERER_MIXIN.equals(mixinClassName)) {
            return classExists(targetClassName);
        }
        return true;
    }

    private static boolean classExists(String className) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(className.replace('.', '/'));
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            // Unexpected: let Mixin look it up itself, as without this plugin.
            return true;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
