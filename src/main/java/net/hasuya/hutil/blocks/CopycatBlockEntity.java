package net.hasuya.hutil.blocks;

import net.hasuya.hutil.utils.HutilRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class CopycatBlockEntity extends BlockEntity {

    private String blockNBT = "hutil:copycat_block";
    private int blockColor = 0xFFFFFFFF;

    public CopycatBlockEntity(BlockPos pos, BlockState state) {
        super(HutilRegistry.COPYCAT_BLOCKENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putString("BlockNBT", this.blockNBT);
        nbt.putInt("BlockColor", this.blockColor);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        blockNBT = nbt.getString("BlockNBT");
        blockColor = nbt.getInt("BlockColor");
    }

    public void setBlockNBT(String blockNBT) {
        this.blockNBT = blockNBT;
        markDirty();
    }

    public String getBlockNBT() {
        return blockNBT;
    }

    public void setBlockColor(int color){
        this.blockColor = color;
        markDirty();
    }

    public int getBlockColor() {
        return blockColor;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
