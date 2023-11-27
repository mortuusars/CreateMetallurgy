package io.github.mortuusars.createmetallurgy.block;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import io.github.mortuusars.createmetallurgy.Metallurgy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CastingTableBlock extends Block implements IBE<CastingTableBlockEntity>, IWrenchable, ProperWaterloggedBlock {

    public CastingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<CastingTableBlockEntity> getBlockEntityClass() {
        return CastingTableBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CastingTableBlockEntity> getBlockEntityType() {
        return Metallurgy.BlockEntities.CASTING_TABLE.get();
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter getter, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CastingTableBlockEntity castingTableBlockEntity))
            return InteractionResult.FAIL;

        if (hit.getDirection() != Direction.UP)
            return InteractionResult.PASS;

        if (player.getItemInHand(hand).isEmpty() && !castingTableBlockEntity.outputInventory.getStackInSlot(0).isEmpty()) {
            player.getInventory().placeItemBackInInventory(castingTableBlockEntity.outputInventory.extractItem(0, 64, false));
            level.playSound(player, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1f + level.getRandom().nextFloat());
            return InteractionResult.SUCCESS;
        }

        if (castingTableBlockEntity.moldStack.isEmpty()) {
            if (player.getItemInHand(hand).is(Items.GLASS_PANE)) {
                castingTableBlockEntity.moldStack = player.getItemInHand(hand).split(1);
                castingTableBlockEntity.notifyUpdate();
                AllSoundEvents.DEPOT_PLOP.play(level, player, pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive() && player.getItemInHand(hand).isEmpty() && castingTableBlockEntity.solidifyingTicks == Integer.MIN_VALUE) {
            player.setItemInHand(hand, castingTableBlockEntity.moldStack);
            castingTableBlockEntity.moldStack = ItemStack.EMPTY;
            castingTableBlockEntity.notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
    }
}
