package net.hasuya.hutil.utils;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.hasuya.hutil.HutilMain;
import net.hasuya.hutil.blocks.CopycatBlock;
import net.hasuya.hutil.blocks.CopycatBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HutilRegistry {
    public static final CopycatBlock COPYCAT_BLOCK = new CopycatBlock(FabricBlockSettings.copyOf(Blocks.OAK_WOOD));
    public static BlockEntityType<CopycatBlockEntity> COPYCAT_BLOCKENTITY;

    public static final void registerRegistries(){
        Registry.register(Registries.BLOCK, new Identifier(HutilMain.MODID, "copycat_block"), COPYCAT_BLOCK);
        ColorProviderRegistry.BLOCK.register(COPYCAT_BLOCK, COPYCAT_BLOCK);

        COPYCAT_BLOCKENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(HutilMain.MODID, "copycat_blockentity"),
                FabricBlockEntityTypeBuilder.create(CopycatBlockEntity::new, COPYCAT_BLOCK).build());

    }
}
