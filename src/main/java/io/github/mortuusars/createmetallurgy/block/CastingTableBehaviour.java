package io.github.mortuusars.createmetallurgy.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.item.ItemStack;

public class CastingTableBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<CastingTableBehaviour> TYPE = new BehaviourType<>();

    ItemStack moldStack;


    public CastingTableBehaviour(SmartBlockEntity be) {
        super(be);
        moldStack = ItemStack.EMPTY;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
