package io.github.mortuusars.createmetallurgy.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class CastingTableRenderer extends SafeBlockEntityRenderer<CastingTableBlockEntity> {
    public CastingTableRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void renderSafe(CastingTableBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {

        if (!be.moldStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            poseStack.pushPose();
            poseStack.translate(0.5, 13/16f + 1/16f/2f, 0.5);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
            poseStack.scale(14/16f, 14/16f, 14/16f);
            itemRenderer.renderStatic(be.moldStack, ItemTransforms.TransformType.FIXED, light, overlay, poseStack, bufferSource, 0);
            poseStack.popPose();
        }

        if (!be.fluidStack.isEmpty()) {
            FluidRenderer.renderFluidBox(be.fluidStack, 2 / 16f, 12/16f, 2/16f, 14/16f, 14/16f, 14/16f,
                    bufferSource, poseStack, light, true);
        }

        if (!be.outputInventory.getStackInSlot(0).isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            poseStack.pushPose();
            poseStack.translate(0.5, 13/16f + 3/16f/2f, 0.5);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
            poseStack.scale(12/16f, 12/16f, 12/16f);
            itemRenderer.renderStatic(be.outputInventory.getStackInSlot(0), ItemTransforms.TransformType.FIXED, light, overlay, poseStack, bufferSource, 0);
            poseStack.popPose();
        }
    }
}
