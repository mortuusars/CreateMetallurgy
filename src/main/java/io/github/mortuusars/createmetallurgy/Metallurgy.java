package io.github.mortuusars.createmetallurgy;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressRenderer;
import com.simibubi.create.content.kinetics.press.PressInstance;
import com.simibubi.create.content.kinetics.saw.SawBlock;
import com.simibubi.create.content.kinetics.saw.SawGenerator;
import com.simibubi.create.content.kinetics.saw.SawMovementBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.redstone.displayLink.source.ItemNameDisplaySource;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.mortuusars.createmetallurgy.block.CastingTableBlock;
import io.github.mortuusars.createmetallurgy.block.CastingTableBlockEntity;
import io.github.mortuusars.createmetallurgy.block.CastingTableRenderer;
import io.github.mortuusars.createmetallurgy.block.SpoutCastingBehaviour;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours.assignDataBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

@Mod(Metallurgy.MODID)
public class Metallurgy
{
    public static final String MODID = "createmetallurgy";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    public Metallurgy()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(modEventBus);

        Blocks.register();
        BlockEntities.register();

        BlockSpoutingBehaviour.addCustomSpoutInteraction(resource("table_casting"), new SpoutCastingBehaviour());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static class Blocks {
        static {
            REGISTRATE.creativeModeTab(() -> AllCreativeModeTabs.BASE_CREATIVE_TAB);
        }

        public static final BlockEntry<CastingTableBlock> CASTING_TABLE = REGISTRATE.block("casting_table", CastingTableBlock::new)
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.color(MaterialColor.COLOR_GRAY))
                .transform(pickaxeOnly())
                .blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
                .onRegister(assignDataBehaviour(new ItemNameDisplaySource(), "combine_item_names"))
                .item()
                .transform(customItemModel("_", "block"))
                .register();

        public static void register() {}
    }

    public static class BlockEntities {
        public static final BlockEntityEntry<CastingTableBlockEntity> CASTING_TABLE = REGISTRATE
                .blockEntity("casting_table", CastingTableBlockEntity::new)
                .validBlocks(Blocks.CASTING_TABLE)
                .renderer(() -> CastingTableRenderer::new)
                .register();

        public static void register() {}
    }
}
