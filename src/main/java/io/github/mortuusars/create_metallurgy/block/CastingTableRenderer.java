package io.github.mortuusars.create_metallurgy.block;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

public class CastingTableRenderer extends SafeBlockEntityRenderer<CastingTableBlockEntity> {
    public CastingTableRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void renderSafe(CastingTableBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {


        if (!be.fluidStack.isEmpty() && be.fillingTicks <= 15) {
            float solidifyingProgress = (be.solidifyingTicks - partialTicks) / ((float)be.totalSolidifyingTicks);
            solidifyingProgress = 1;
            float fillLevel = Mth.clamp((be.fillingTicks - partialTicks) / 15f, 0f, 1f);
//            fillLevel = fillLevel * fillLevel * fillLevel;
            fillLevel = fillLevel < 0.5 ? 4 * fillLevel * fillLevel * fillLevel : (float) (1 - Math.pow(-2 * fillLevel + 2, 3) / 2);
            fillLevel = 1f - fillLevel;

            renderFluidBox(be.fluidStack, 2 / 16f, 11.02f/16f, 2/16f, 14/16f, (11.02f + fillLevel * 0.85f)/16f, 14/16f,
                    FluidRenderer.getFluidBuilder(bufferSource), poseStack, light, true, solidifyingProgress);
        }

        if (!be.moldStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            poseStack.pushPose();
            poseStack.translate(0.5, 11/16f + (0.5/16f), 0.5);
            poseStack.scale(12/16f, 12/16f, 12/16f);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
            itemRenderer.renderStatic(be.moldStack, ItemTransforms.TransformType.FIXED, light, overlay, poseStack, bufferSource, 0);
            poseStack.popPose();
        }

        if (!be.outputInventory.getStackInSlot(0).isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            poseStack.pushPose();
            poseStack.translate(0.5, 11/16f + (0.55/16f), 0.5);
            poseStack.scale(12/16f, 12/16f, 12/16f);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
            itemRenderer.renderStatic(be.outputInventory.getStackInSlot(0), ItemTransforms.TransformType.FIXED, light, overlay, poseStack, bufferSource, 0);
            poseStack.popPose();
        }
    }

    public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax,
                                      float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, float opacity) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);
        FluidType fluidAttributes = fluid.getFluidType();
        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(clientFluid.getStillTexture(fluidStack));

        int color = clientFluid.getTintColor(fluidStack);

        int alpha = color >> 24 & 0xff;
        alpha = (int)Mth.clamp(alpha * opacity, 0, 255);
        color = (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);

        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluidAttributes.getLightLevel(fluidStack));
        light = (light & 0xF00000) | luminosity << 4;

        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        ms.pushPose();
        if (fluidAttributes.isLighterThanAir())
            TransformStack.cast(ms)
                    .translate(center)
                    .rotateX(180)
                    .translateBack(center);

        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis()
                    .isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    FluidRenderer.renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin, builder, ms, light,
                            color, fluidTexture);
                } else {
                    FluidRenderer.renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin, builder, ms, light,
                            color, fluidTexture);
                }
            } else {
                FluidRenderer.renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin, builder, ms, light, color,
                        fluidTexture);
            }
        }

        ms.popPose();
    }
}
