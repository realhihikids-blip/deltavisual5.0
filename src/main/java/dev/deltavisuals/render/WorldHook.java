package dev.deltavisuals.render;

import dev.deltavisuals.core.Module;
import dev.deltavisuals.core.Modules;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Hooks the world render event once and lets every enabled module draw its geometry through {@link Gfx}. */
public final class WorldHook {
    private static final Logger LOG = LoggerFactory.getLogger("DeltaVisuals/Render");

    private WorldHook() {}

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || mc.player == null) return;
            if (ctx.consumers() == null || ctx.matrices() == null) return;
            Camera cam = mc.gameRenderer.getCamera();
            Gfx.begin(ctx.consumers(), ctx.matrices().peek().getPositionMatrix(), cam, dev.deltavisuals.util.Compat.partialTick(mc));
            try {
                for (Module m : Modules.all()) {
                    if (!m.isEnabled()) continue;
                    try {
                        m.onWorldRender();
                    } catch (Throwable t) {
                        LOG.error("Module {} crashed in onWorldRender and was disabled", m.name, t);
                        m.setEnabled(false);
                    }
                }
            } finally {
                Gfx.end();
            }
        });
    }
}
