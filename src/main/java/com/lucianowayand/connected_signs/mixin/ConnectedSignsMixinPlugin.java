package com.lucianowayand.connected_signs.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class ConnectedSignsMixinPlugin implements IMixinConfigPlugin {
    private static final String TFC_MARKER_CLASS = "net.dries007.tfc.TerraFirmaCraft";

    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (mixinClassName.startsWith("com.lucianowayand.connected_signs.mixin.tfc.")) {
            return isClassPresent(TFC_MARKER_CLASS);
        }
        return true;
    }

    private static boolean isClassPresent(final String className) {
        try {
            Class.forName(className, false, ConnectedSignsMixinPlugin.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }
}
