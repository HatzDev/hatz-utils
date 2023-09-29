package net.hasuya.hutil.utils;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hasuya.hutil.HutilClient;
import net.hasuya.hutil.blocks.CopycatBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Hutil {
    private static List<String> copycatsRegistry = new ArrayList<>();
    public static List<Identifier> copycats = new ArrayList<>();

    public void setCopycatLayer(CopycatBlockEntity be, BlockPos pos, World world, int layer, SoundEvent sound) {
        PacketByteBuf buf = PacketByteBufs.create();
        if (!world.isClient) {
            be.setBlockLayer(layer);

            buf.writeBlockPos(pos);
            buf.writeInt(be.getBlockLayer());
            buf.writeIdentifier(Registries.SOUND_EVENT.getId(sound));

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, pos)) {
                ServerPlayNetworking.send(player, HutilClient.COPYCAT_LAYER_PACKET, buf);
            }
        }
    }

    public void setCopycatTexture(CopycatBlockEntity be, BlockPos pos, World world, Block blockOfItem) {
        BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
        PacketByteBuf buf = PacketByteBufs.create();

        Identifier blockOfItemId = Registries.BLOCK.getId(blockOfItem);
        if (!world.isClient) {
            final Identifier blockToCopyID = Identifier.tryParse(be.getBlockNBT());
            final Block blockToCopy = Registries.BLOCK.get(blockToCopyID);
            final BlockState blockToCopyState = blockToCopy.getDefaultState();

            be.setBlockNBT(blockOfItemId.toString());
            be.setBlockColor(blockColors.getColor(blockOfItem.getDefaultState(), world, pos, 0));

            buf.writeBlockPos(pos);
            buf.writeString(be.getBlockNBT());
            buf.writeInt(be.getBlockColor());
            buf.writeIdentifier(Registries.SOUND_EVENT.getId(blockToCopyState.getBlock().getSoundGroup(blockToCopyState).getPlaceSound()));

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, pos)) {
                ServerPlayNetworking.send(player, HutilClient.COPYCAT_TEXTURE_PACKET, buf);
            }
        }
    }

    public static void registerCopycatModel(String modelName){
        copycatsRegistry.add(modelName);
    }

    public static void registerCopycatBlock(String modID, String blockName){
        copycats.add(new Identifier(modID, blockName));
    }


    public static List<String> getCopycatsRegistry(){
        return copycatsRegistry;
    }
}
