package net.hasuya.hutil.blocks;

import net.hasuya.hutil.utils.Hutil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
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
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CopycatBlock extends Block implements BlockEntityProvider, BlockColorProvider {

    private final Hutil hutilCopycat = new Hutil();

    public CopycatBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopycatBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return hutilCopycat.setCopycatByItem(pos, world, player.getStackInHand(hand));
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity) builder.get(LootContextParameters.BLOCK_ENTITY);

        if (copycatBlockEntity != null) {
            Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
            Block blockToCopy = Registries.BLOCK.get(blockToCopyID);

            if (!blockToCopyID.equals(Hutil.COPYCAT_BLOCK_ID)) {
                return List.of(new ItemStack(blockToCopy.asItem()), new ItemStack(this.asItem()));
            }

            return List.of(new ItemStack(this.asItem()));
        }

        return Collections.emptyList();
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity) world.getBlockEntity(pos);

        if (copycatBlockEntity != null && !Identifier.tryParse(copycatBlockEntity.getBlockNBT()).equals(Hutil.COPYCAT_BLOCK_ID)) {
            Block blockToCopy = Registries.BLOCK.get(Identifier.tryParse(copycatBlockEntity.getBlockNBT()));
            return blockToCopy.getAmbientOcclusionLightLevel(state, world, pos);
        }

        return 0f;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null) {
            CopycatBlockEntity blockEntity = (CopycatBlockEntity) world.getBlockEntity(pos);
            return (blockEntity != null) ? blockEntity.getBlockColor() : -1;
        }

        return -1;
    }
}
