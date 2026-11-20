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
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * Fabric 1.20.1 (Yarn) + Fabric API, Java 21.
 * Пока в руке шерсть: вся шерсть вокруг красится в чёрный,
 * а шерсть того же цвета, что в руке, в белый.
 */
public class BlockPartyHighlighter implements ClientModInitializer {

    private static final int RADIUS = 24;   // радиус поиска по горизонтали
    private static final int DOWN = 5;      // сколько блоков вниз от игрока
    private static final int UP = 1;        // сколько вверх
    private static final double PAD = 0.003; // небольшой отступ, чтобы не было мерцания

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
            VertexConsumer fill = ctx.consumers().getBuffer(RenderLayer.getDebugFilledBox());

            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            BlockPos p = mc.player.getBlockPos();
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int y = -DOWN; y <= UP; y++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        pos.set(p.getX() + x, p.getY() + y, p.getZ() + z);
                        Block b = mc.world.getBlockState(pos).getBlock();
                        if (!WOOLS.contains(b)) continue;

                        float c = (b == target) ? 1f : 0f; // 1 = белый, 0 = чёрный
                        WorldRenderer.renderFilledBox(matrices, fill,
                                pos.getX() - PAD, pos.getY() - PAD, pos.getZ() - PAD,
                                pos.getX() + 1 + PAD, pos.getY() + 1 + PAD, pos.getZ() + 1 + PAD,
                                c, c, c, 1f);
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
