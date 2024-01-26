package io.github.mortuusars.create_metallurgy;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.redstone.displayLink.source.ItemNameDisplaySource;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.mortuusars.create_metallurgy.block.CastingTableBlock;
import io.github.mortuusars.create_metallurgy.block.CastingTableBlockEntity;
import io.github.mortuusars.create_metallurgy.block.CastingTableRenderer;
import io.github.mortuusars.create_metallurgy.block.SpoutCastingBehaviour;
import io.github.mortuusars.create_metallurgy.recipe.CastingRecipe;
import io.github.mortuusars.create_metallurgy.recipe.MetallurgyRecipes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours.assignDataBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

@Mod(Metallurgy.ID)
public class Metallurgy
{
    public static final String ID = "create_metallurgy";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

    public Metallurgy()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(modEventBus);

        Blocks.register();
        BlockEntities.register();
        Items.register();
        MetallurgyRecipes.register(modEventBus);

        BlockSpoutingBehaviour.addCustomSpoutInteraction(resource("table_casting"), new SpoutCastingBehaviour());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
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

    public static class Items {
        public static final ItemEntry<Item> ANDESITE_INGOT_MOLD = REGISTRATE
                .item("andesite_ingot_mold", Item::new)
                .tag(Tags.Items.MOLDS)
                .tab(() -> AllCreativeModeTabs.BASE_CREATIVE_TAB)
                .register();

        public static final ItemEntry<Item> ANDESITE_NUGGET_MOLD = REGISTRATE
                .item("andesite_nugget_mold", Item::new)
                .tag(Tags.Items.MOLDS)
                .tab(() -> AllCreativeModeTabs.BASE_CREATIVE_TAB)
                .register();

        public static void register() { }
    }

    public static class Tags {
        public static class Items {
            public static final TagKey<Item> MOLDS = ItemTags.create(resource("molds"));
        }
    }
}
