package net.hasuya.hutil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hasuya.hutil.blocks.CopycatBlockEntity;
import net.hasuya.hutil.blocks.CopycatModel;
import net.hasuya.hutil.utils.Hutil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.hasuya.hutil.HutilMain.MODID;

public class HutilClient implements ClientModInitializer {
    public static final Identifier COPYCAT_PACKET = new Identifier(MODID, "copycat_packet");

    @Override
    public void onInitializeClient() {
        Hutil.registerCopycat("copycat_block");

        for (String name: Hutil.getCopycatsRegistry()) {
            ModelLoadingPlugin.register(pluginContext -> {
                pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                    return context.id().getPath().equals("block/"+ name) ? new CopycatModel(model) : model;
                });
            });
        }

        ClientPlayNetworking.registerGlobalReceiver(COPYCAT_PACKET, (client, handler, buf, responseSender) -> {
            final BlockPos pos = buf.readBlockPos();
            final String nbt = buf.readString();
            final int color = buf.readInt();

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

                final Identifier blockToCopyID = Identifier.tryParse(nbt); // Usamos o NBT diretamente
                final Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
                final BlockState blockToCopyState = blockToCopy.getDefaultState();

                client.world.updateListeners(pos, be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);
                client.world.playSound(null, pos, blockToCopy.getSoundGroup(blockToCopyState).getPlaceSound(), SoundCategory.BLOCKS, 1, 0.8f);
            });
        });
    }
}
