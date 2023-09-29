package net.hasuya.hutil.blocks;

import net.hasuya.hutil.utils.Hutil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CopycatBlock extends Block implements BlockEntityProvider, BlockColorProvider {
    private final Hutil hutilCopycat = new Hutil();
    private final Map<Integer, Item> itemLayers = new HashMap<>();
    private final Map<Integer, SoundEvent> layerSounds = new HashMap<>();

    public CopycatBlock(Settings settings) {
        super(settings);
        itemLayers.put(0, ItemStack.EMPTY.getItem());
        itemLayers.put(1, Items.GRASS);
        itemLayers.put(2, Items.SNOWBALL);
        itemLayers.put(3, Items.MOSS_CARPET);
        itemLayers.put(4, Items.VINE);
        layerSounds.put(1, SoundEvents.BLOCK_GRASS_PLACE);
        layerSounds.put(2, SoundEvents.BLOCK_SNOW_PLACE);
        layerSounds.put(3, SoundEvents.BLOCK_MOSS_PLACE);
        layerSounds.put(4, SoundEvents.BLOCK_VINE_PLACE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopycatBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.SUCCESS;

        ItemStack handItem = player.getStackInHand(hand);
        CopycatBlockEntity be = (CopycatBlockEntity) world.getBlockEntity(pos);

        if (be == null)
            return ActionResult.FAIL;

        for (Map.Entry<Integer, Item> entry : itemLayers.entrySet()) {
            if (handItem.getItem().equals(entry.getValue())) {
                return setLayer(be, pos, world, handItem, layerSounds.get(entry.getKey()), entry.getKey());
            }
        }

        return setTexture(world, pos, handItem, be);
    }

    private ActionResult setTexture(World world, BlockPos pos, ItemStack handItem, CopycatBlockEntity be) {
        if (!(handItem.getItem() instanceof BlockItem blockItem))
            return ActionResult.FAIL;

        Block blockOfItem = blockItem.getBlock();
        Identifier blockOfItemId = Registries.BLOCK.getId(blockOfItem);

        Identifier blockToCopyID = Identifier.tryParse(be.getBlockNBT());
        Block blockToCopy = Registries.BLOCK.get(blockToCopyID);

        if (blockOfItem == blockToCopy || Hutil.copycats.contains(blockOfItemId) || !blockToCopy.getDefaultState().isFullCube(world, pos)) {
            world.playSound(null, pos, blockOfItem.getSoundGroup(blockOfItem.getDefaultState()).getPlaceSound(), SoundCategory.BLOCKS, 1, 0.8f);
            return ActionResult.FAIL;
        }

        if (!Hutil.copycats.contains(Registries.ITEM.getId(blockToCopy.asItem()))) {
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(blockToCopy.asItem())));
        }

        hutilCopycat.setCopycatTexture(be, pos, world, blockOfItem);
        world.playSound(null, pos, blockOfItem.getSoundGroup(blockOfItem.getDefaultState()).getPlaceSound(), SoundCategory.BLOCKS, 1, 0.8f);
        world.updateListeners(pos, be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);

        handItem.decrement(1);
        return ActionResult.SUCCESS;
    }

    private ActionResult setLayer(CopycatBlockEntity be, BlockPos pos, World world, ItemStack handItem, SoundEvent soundEvent, int layer){
        Item layerItem = itemLayers.get(be.getBlockLayer());

        if(layerItem == handItem.getItem() || handItem.isEmpty()){
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1, 0.8f);
            return ActionResult.FAIL;
        }

        ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, layerItem.getDefaultStack());
        world.spawnEntity(itemEntity);

        hutilCopycat.setCopycatLayer(be, pos, world, layer, soundEvent);
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1, 0.8f);
        world.updateListeners(pos, be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);

        handItem.decrement(1);
        return ActionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity) builder.get(LootContextParameters.BLOCK_ENTITY);

        if(copycatBlockEntity == null)
            return Collections.emptyList();

        Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
        Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
        int blockLayer = copycatBlockEntity.getBlockLayer();

        if (Hutil.copycats.contains(blockToCopyID))
            return List.of(new ItemStack(this.asItem()));

        return List.of(new ItemStack(blockToCopy.asItem()), new ItemStack(this.asItem()), new ItemStack(itemLayers.get(blockLayer)));
    }

    @Override public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity) world.getBlockEntity(pos);
        if(copycatBlockEntity == null)
            return 0f;
        if(Hutil.copycats.contains(Identifier.tryParse(copycatBlockEntity.getBlockNBT())))
            return 0f;

        Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
        Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
        return blockToCopy.getAmbientOcclusionLightLevel(state, world, pos);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null) {
            CopycatBlockEntity blockEntity = (CopycatBlockEntity) world.getBlockEntity(pos);
            if(blockEntity == null)
                return -1;

            Biome biome = blockEntity.getWorld().getBiome(pos).value();
            if(blockEntity.getBlockLayer() == 1)
                return biome.getGrassColorAt(pos.getX(), pos.getZ());

            return blockEntity.getBlockColor();
        }

        return -1;
    }

}
