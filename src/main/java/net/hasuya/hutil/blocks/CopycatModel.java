package net.hasuya.hutil.blocks;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class CopycatModel implements BakedModel {
    private final BakedModel originalModel;
    private final Identifier[] textureNames = new Identifier[]{
            new Identifier("hutil:block/framed_block_down"),
            new Identifier("hutil:block/framed_block_top"),
            new Identifier("hutil:block/framed_block"),
            new Identifier("hutil:block/framed_block_overlay")
    };

    public CopycatModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        final CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity)blockView.getBlockEntity(pos);
        if(copycatBlockEntity == null)
            return;

        final MinecraftClient client = MinecraftClient.getInstance();
        final BakedModelManager modelManager = client.getBakedModelManager();
        final Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        final Direction[] directions = Direction.values();

        final BlockState copycatBlockState = copycatBlockEntity.getCachedState();

        final Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
        final Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
        final BlockState blockToCopyState = blockToCopy.getDefaultState();
        final BakedModel blockToCopyModel = modelManager.getBlockModels().getModel(blockToCopyState);
        Sprite[] blockToCopySprites = new Sprite[4];

        for (Direction direction : Direction.values()) {
            List<BakedQuad> blockToCopyQuads = getBlockToCopyQuads(blockToCopyModel, blockToCopyState, direction, copycatBlockState, randomSupplier);

            if (blockToCopyQuads != null && !blockToCopyQuads.isEmpty() && direction.ordinal() < 4) {
                blockToCopySprites[direction.ordinal()] = blockToCopyQuads.get(direction.ordinal() > 2 && blockToCopyQuads.size() > 1 ? 1 : 0).getSprite();
            }
        }

        for (Direction direction : directions) {
            List<BakedQuad> blockToCopyQuads = getBlockToCopyQuads(blockToCopyModel, blockToCopyState, direction, copycatBlockState, randomSupplier);
            List<BakedQuad> copycatQuads = originalModel.getQuads(copycatBlockState, direction, randomSupplier.get());

            if (blockToCopyQuads != null) {
                int maxQuadsToEmit = Math.min(2, blockToCopyQuads.size());
                for (int j = 0; j < maxQuadsToEmit && j < copycatQuads.size(); j++) {
                    BakedQuad copycatQuad = copycatQuads.get(j);
                    Sprite spriteForQuad = getSpriteForQuad(copycatQuad, blockToCopySprites);
                    if (spriteForQuad != null) {
                        int colorIndex = blockToCopyQuads.get(j).getColorIndex();
                        boolean isTransparent = blockToCopyState.isTransparent(blockView, pos);
                        BlendMode blendMode = isTransparent ? BlendMode.TRANSLUCENT : BlendMode.CUTOUT_MIPPED;

                        RenderMaterial material = renderer.materialFinder().blendMode(blendMode).find();
                        context.getEmitter().fromVanilla(new BakedQuad(getTextureUV(copycatQuad, spriteForQuad), colorIndex, direction, spriteForQuad, true), material, direction).emit();
                    }
                }
            }
        }
    }

    private List<BakedQuad> getBlockToCopyQuads(BakedModel blockToCopyModel, BlockState blockToCopyState, Direction direction, BlockState copycatBlockState, Supplier<Random> randomSupplier) {
        List<BakedQuad> blockToCopyQuads = blockToCopyModel.getQuads(blockToCopyState, direction, randomSupplier.get());
        return blockToCopyQuads.isEmpty() ? originalModel.getQuads(copycatBlockState, direction, randomSupplier.get()) : blockToCopyQuads;
    }

    private Sprite getSpriteForQuad(BakedQuad quad, Sprite[] sprites) {
        Identifier quadId = quad.getSprite().getContents().getId();
        for (int i = 0; i < textureNames.length; i++) {
            if(quadId.equals(textureNames[i])){
                return sprites[i];
            }
        }
        return null;
    }

    private int[] getTextureUV(BakedQuad quad, Sprite newSprite) {
        int[] vertexData = quad.getVertexData().clone();
        Sprite oldSprite = quad.getSprite();

        for (int i = 0; i < 4; i++) {
            int j = 8 * i;

            float u = Float.intBitsToFloat(vertexData[j + 4]);
            float v = Float.intBitsToFloat(vertexData[j + 5]);

            float relU = (u - oldSprite.getMinU()) / (oldSprite.getMaxU() - oldSprite.getMinU());
            float relV = (v - oldSprite.getMinV()) / (oldSprite.getMaxV() - oldSprite.getMinV());

            vertexData[j + 4] = Float.floatToRawIntBits(newSprite.getMinU() + relU * (newSprite.getMaxU() - newSprite.getMinU()));
            vertexData[j + 5] = Float.floatToRawIntBits(newSprite.getMinV() + relV * (newSprite.getMaxV() - newSprite.getMinV()));
        }
        return vertexData;
    }

    @Override
    public Sprite getParticleSprite() {
        return originalModel.getParticleSprite();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return null;
    }
}
