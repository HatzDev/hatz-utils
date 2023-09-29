package net.hasuya.hutil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hasuya.hutil.blocks.CopycatBlockEntity;
import net.hasuya.hutil.blocks.CopycatModel;
import net.hasuya.hutil.utils.Hutil;
import net.minecraft.block.Block;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.hasuya.hutil.HutilMain.MODID;

public class HutilClient implements ClientModInitializer {
    public static final Identifier COPYCAT_TEXTURE_PACKET = new Identifier(MODID, "copycat_texture_packet");
    public static final Identifier COPYCAT_LAYER_PACKET = new Identifier(MODID, "copycat_layer_packet");

    @Override
    public void onInitializeClient() {
        Hutil.registerCopycatModel("copycat_block");
        Hutil.registerCopycatBlock(MODID, "copycat_block");
        for (String name: Hutil.getCopycatsRegistry()) {
            ModelLoadingPlugin.register(pluginContext -> {
                pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                    return context.id().getPath().equals("block/"+ name) ? new CopycatModel(model) : model;
                });
            });
        }

        ClientPlayNetworking.registerGlobalReceiver(COPYCAT_TEXTURE_PACKET, (client, handler, buf, responseSender) -> {
            final BlockPos pos = buf.readBlockPos();
            final String nbt = buf.readString();
            final int color = buf.readInt();
            final Identifier soundEvent = buf.readIdentifier();

            client.execute(() -> {
                if (client.world == null) {
                    return;
                }

                final CopycatBlockEntity be = (CopycatBlockEntity) client.world.getBlockEntity(pos);
                if (be == null || be.getCachedState() == null) {
                    return;
                }

                be.setBlockNBT(nbt);
                be.setBlockColor(color);

                client.world.updateListeners(pos, be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);
                client.world.playSound(null, pos, SoundEvent.of(soundEvent), SoundCategory.BLOCKS, 1, 0.8f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(COPYCAT_LAYER_PACKET, (client, handler, buf, responseSender) -> {
            final BlockPos pos = buf.readBlockPos();
            final int blockLayer = buf.readInt();
            final Identifier soundEvent = buf.readIdentifier();

            client.execute(() -> {
                if (client.world == null) {
                    return;
                }

                final CopycatBlockEntity be = (CopycatBlockEntity) client.world.getBlockEntity(pos);
                if (be == null || be.getCachedState() == null) {
                    return;
                }

                be.setBlockLayer(blockLayer);

                client.world.updateListeners(pos, be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);
                client.world.playSound(null, pos, SoundEvent.of(soundEvent), SoundCategory.BLOCKS, 1, 0.8f);
            });
        });
    }
}
