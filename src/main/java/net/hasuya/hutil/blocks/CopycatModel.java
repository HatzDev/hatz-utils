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
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
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
            new Identifier("hutil:block/framed_block")
    };
    private final Identifier[] overlayTextureNames = new Identifier[]{
            new Identifier("hutil:block/framed_block_overlay_down"),
            new Identifier("hutil:block/framed_block_overlay_top"),
            new Identifier("hutil:block/framed_block_overlay")
    };

    private final SpriteAtlasTexture atlas;
    private final BakedModelManager modelManager;
    private final Renderer renderer;

    public CopycatModel(BakedModel originalModel) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.originalModel = originalModel;
        this.atlas = client.getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        this.modelManager = client.getBakedModelManager();
        this.renderer = RendererAccess.INSTANCE.getRenderer();

    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        final CopycatBlockEntity copycatBlockEntity = (CopycatBlockEntity)blockView.getBlockEntity(pos);
        if(copycatBlockEntity == null)
            return;

        final BlockState copycatBlockState = copycatBlockEntity.getCachedState();

        final Identifier blockToCopyID = Identifier.tryParse(copycatBlockEntity.getBlockNBT());
        final Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
        final BlockState blockToCopyState = blockToCopy.getDefaultState();
        final BakedModel blockToCopyModel = modelManager.getBlockModels().getModel(blockToCopyState);
        Sprite[] blockToCopySprites = new Sprite[3];
        Sprite[] blockToCopyOverlaySprites = new Sprite[3];

        final int blockLayer = copycatBlockEntity.getBlockLayer();
        final Sprite[] spriteLayers = new Sprite[]{
                atlas.getSprite(new Identifier("hutil:block/framed_block_overlay")),
                atlas.getSprite(new Identifier("hutil:block/grass_top_overlay")),
                atlas.getSprite(new Identifier("hutil:block/grass_side_overlay")),
                atlas.getSprite(new Identifier("hutil:block/snow_top_overlay")),
                atlas.getSprite(new Identifier("hutil:block/snow_side_overlay")),
                atlas.getSprite(new Identifier("hutil:block/moss_top_overlay")),
                atlas.getSprite(new Identifier("hutil:block/moss_side_overlay")),
                atlas.getSprite(new Identifier("hutil:block/moss_full_overlay")),
        };

        for (Direction direction : Direction.values()) {
            List<BakedQuad> blockToCopyQuads = getBlockToCopyQuads(blockToCopyModel, blockToCopyState, direction, copycatBlockState, randomSupplier);

            if (direction.ordinal() < 3) {
                if (blockToCopyQuads.size() > 1) {
                    blockToCopyOverlaySprites[direction.ordinal()] = blockToCopyQuads.get(1).getSprite();
                }
                if (!blockToCopyQuads.isEmpty()) {
                    blockToCopySprites[direction.ordinal()] = blockToCopyQuads.get(0).getSprite();
                }
            }

            switch (blockLayer) {
                case 1 -> {
                    blockToCopyOverlaySprites[0] = spriteLayers[0];
                    blockToCopyOverlaySprites[1] = spriteLayers[1];
                    blockToCopyOverlaySprites[2] = spriteLayers[2];
                }
                case 2 -> {
                    blockToCopyOverlaySprites[0] = spriteLayers[0];
                    blockToCopyOverlaySprites[1] = spriteLayers[3];
                    blockToCopyOverlaySprites[2] = spriteLayers[4];
                }
                case 3 -> {
                    blockToCopyOverlaySprites[0] = spriteLayers[0];
                    blockToCopyOverlaySprites[1] = spriteLayers[5];
                    blockToCopyOverlaySprites[2] = spriteLayers[6];
                }
                case 4 -> {
                    blockToCopyOverlaySprites[0] = spriteLayers[7];
                    blockToCopyOverlaySprites[1] = spriteLayers[7];
                    blockToCopyOverlaySprites[2] = spriteLayers[7];
                }
            }

            List<BakedQuad> copycatQuads = originalModel.getQuads(copycatBlockState, direction, randomSupplier.get());
            renderQuad(context, blockToCopyState, blockView, pos, renderer, direction, copycatQuads, blockToCopySprites, textureNames, 0, blockToCopyQuads.get(0).getColorIndex());
            if(copycatQuads.size() > 1){
                int colorIndex = (blockLayer == 0 && blockToCopyQuads.size() > 1) ? blockToCopyQuads.get(1).getColorIndex() : (blockLayer == 1) ? copycatQuads.get(1).getColorIndex() : -1;
                renderQuad(context, blockToCopyState, blockView, pos, renderer, direction, copycatQuads, blockToCopyOverlaySprites, overlayTextureNames, 1, colorIndex);
            }
        }
    }

    private void renderQuad(RenderContext context, BlockState blockToCopyState, BlockRenderView blockView, BlockPos pos, Renderer renderer, Direction direction, List<BakedQuad> copycatQuads, Sprite[] sprites, Identifier[] textureNames, int index, int colorIndex) {
        BakedQuad copycatQuad = copycatQuads.get(index);
        Sprite spriteForQuad = getSpriteForQuad(copycatQuad, sprites, textureNames);
        if (spriteForQuad != null) {
            boolean isTransparent = blockToCopyState.isTransparent(blockView, pos);
            BlendMode blendMode = isTransparent ? BlendMode.TRANSLUCENT : BlendMode.CUTOUT_MIPPED;
            RenderMaterial material = renderer.materialFinder().blendMode(blendMode).find();
            context.getEmitter().fromVanilla(new BakedQuad(getTextureUV(copycatQuad, spriteForQuad), colorIndex, direction, spriteForQuad, true), material, direction).emit();
        }
    }

    private List<BakedQuad> getBlockToCopyQuads(BakedModel blockToCopyModel, BlockState blockToCopyState, Direction direction, BlockState copycatBlockState, Supplier<Random> randomSupplier) {
        List<BakedQuad> blockToCopyQuads = blockToCopyModel.getQuads(blockToCopyState, direction, randomSupplier.get());
        return blockToCopyQuads.isEmpty() ? originalModel.getQuads(copycatBlockState, direction, randomSupplier.get()) : blockToCopyQuads;
    }

    private Sprite getSpriteForQuad(BakedQuad quad, Sprite[] sprites, Identifier[] textureNames) {
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
