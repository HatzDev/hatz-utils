package net.hasuya.hutil.mixin;

import net.hasuya.hutil.HutilMain;
import net.hasuya.hutil.blocks.CopycatBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Shadow public abstract void addParticle(Particle particle);
    @Shadow protected ClientWorld world;
    @Shadow @Final private Random random;

    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity) world.getBlockEntity(pos);
        if(copycatBlockEntity != null){
            BlockState copycatBlockState = copycatBlockEntity.getCachedState();
            final Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
            final Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
            final BlockState blockToCopyState = blockToCopy.getDefaultState();

            if(state.getBlock().equals(copycatBlockState.getBlock())){
                VoxelShape voxelShape = state.getOutlineShape(world, pos);
                voxelShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    double d = Math.min(1.0, maxX - minX);
                    double e = Math.min(1.0, maxY - minY);
                    double f = Math.min(1.0, maxZ - minZ);
                    int i = Math.max(2, MathHelper.ceil(d / 0.25));
                    int j = Math.max(2, MathHelper.ceil(e / 0.25));
                    int k = Math.max(2, MathHelper.ceil(f / 0.25));

                    for(int l = 0; l < i; ++l) {
                        for(int m = 0; m < j; ++m) {
                            for(int n = 0; n < k; ++n) {
                                double g = ((double)l + 0.5) / (double)i;
                                double h = ((double)m + 0.5) / (double)j;
                                double o = ((double)n + 0.5) / (double)k;
                                double p = g * d + minX;
                                double q = h * e + minY;
                                double r = o * f + minZ;
                                this.addParticle(new BlockDustParticle(world, (double)pos.getX() + p, (double)pos.getY() + q, (double)pos.getZ() + r, g - 0.5, h - 0.5, o - 0.5, blockToCopyState, pos));
                            }
                        }
                    }
                });
            }
            ci.cancel();
        }
    }
    @Inject(method = "addBlockBreakingParticles", at = @At("HEAD"), cancellable = true)
    private void addBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity) world.getBlockEntity(pos);
        if(copycatBlockEntity != null){
            BlockState copycatBlockState = copycatBlockEntity.getCachedState();
            final Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
            final Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
            final BlockState blockToCopyState = blockToCopy.getDefaultState();

            if (copycatBlockState.getRenderType() != BlockRenderType.INVISIBLE) {
                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();
                float f = 0.1F;
                Box box = copycatBlockState.getOutlineShape(this.world, pos).getBoundingBox();
                double d = (double)i + this.random.nextDouble() * (box.maxX - box.minX - 0.20000000298023224) + 0.10000000149011612 + box.minX;
                double e = (double)j + this.random.nextDouble() * (box.maxY - box.minY - 0.20000000298023224) + 0.10000000149011612 + box.minY;
                double g = (double)k + this.random.nextDouble() * (box.maxZ - box.minZ - 0.20000000298023224) + 0.10000000149011612 + box.minZ;
                if (direction == Direction.DOWN) {
                    e = (double)j + box.minY - 0.10000000149011612;
                }

                if (direction == Direction.UP) {
                    e = (double)j + box.maxY + 0.10000000149011612;
                }

                if (direction == Direction.NORTH) {
                    g = (double)k + box.minZ - 0.10000000149011612;
                }

                if (direction == Direction.SOUTH) {
                    g = (double)k + box.maxZ + 0.10000000149011612;
                }

                if (direction == Direction.WEST) {
                    d = (double)i + box.minX - 0.10000000149011612;
                }

                if (direction == Direction.EAST) {
                    d = (double)i + box.maxX + 0.10000000149011612;
                }
                this.addParticle((new BlockDustParticle(this.world, d, e, g, 0.0, 0.0, 0.0, blockToCopyState, pos)).move(0.2F).scale(0.6F));
            }
            ci.cancel();
        }
    }
}