package com.example.blockparty;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * Fabric 1.20.1 (Yarn mappings) + Fabric API.
 * Подсвечивает блоки шерсти того же цвета, что и шерсть в руке.
 */
public class BlockPartyHighlighter implements ClientModInitializer {

    private static final int RADIUS = 20;   // радиус поиска по горизонтали
    private static final int DOWN = 4;      // сколько блоков вниз от игрока
    private static final int UP = 1;        // сколько вверх

    private static final Set<Block> WOOLS = Set.of(
            Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL,
            Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL,
            Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
            Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL
    );

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null || ctx.consumers() == null) return;

            Block target = heldWool(mc.player.getMainHandStack());
            if (target == null) target = heldWool(mc.player.getOffHandStack());
            if (target == null) return;

            MatrixStack matrices = ctx.matrixStack();
            Vec3d cam = ctx.camera().getPos();
            VertexConsumer lines = ctx.consumers().getBuffer(RenderLayer.getLines());

            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            BlockPos p = mc.player.getBlockPos();
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int y = -DOWN; y <= UP; y++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        pos.set(p.getX() + x, p.getY() + y, p.getZ() + z);
                        if (mc.world.getBlockState(pos).getBlock() == target) {
                            Box box = new Box(pos).expand(0.002);
                            WorldRenderer.drawBox(matrices, lines, box, 0f, 1f, 0f, 1f);
                        }
                    }
                }
            }
            matrices.pop();
        });
    }

    private static Block heldWool(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem bi && WOOLS.contains(bi.getBlock())) {
            return bi.getBlock();
        }
        return null;
    }
}
