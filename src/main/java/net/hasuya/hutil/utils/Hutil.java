package net.hasuya.hutil.utils;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hasuya.hutil.HutilClient;
import net.hasuya.hutil.blocks.CopycatBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Hutil {
    private static List<String> copycatsRegistry = new ArrayList<>();
    public static List<Identifier> copycats = new ArrayList<>();

    public ActionResult setCopycatByItem(BlockPos pos, World world, ItemStack handItem) {
        if (!(handItem.getItem() instanceof BlockItem blockItem))
            return ActionResult.FAIL;

        CopycatBlockEntity be = (CopycatBlockEntity) world.getBlockEntity(pos);
        if (be == null)
            return ActionResult.FAIL;

        Block blockOfItem = blockItem.getBlock();
        Identifier blockOfItemId = Registries.BLOCK.getId(blockOfItem);

        if (!blockOfItem.getDefaultState().isFullCube(world, pos) || copycats.contains(blockOfItemId))
            return ActionResult.FAIL;

        Identifier blockToCopyID = Identifier.tryParse(be.getBlockNBT());
        Block blockToCopy = Registries.BLOCK.get(blockToCopyID);

        if (blockOfItem == blockToCopy)
            return ActionResult.FAIL;

        if (!world.isClient) {
            BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
            PacketByteBuf buf = PacketByteBufs.create();

            be.setBlockNBT(blockOfItemId.toString());
            be.setBlockColor(blockColors.getColor(blockOfItem.getDefaultState(), world, pos, 0));

            buf.writeBlockPos(pos);
            buf.writeString(be.getBlockNBT());
            buf.writeInt(be.getBlockColor());

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, pos)) {
                ServerPlayNetworking.send(player, HutilClient.COPYCAT_PACKET, buf);
            }

            if (!copycats.contains(blockOfItemId)) {
                ItemStack droppedItemStack = new ItemStack(blockToCopy.asItem());
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, droppedItemStack);
                world.spawnEntity(itemEntity);
            }

            world.playSound(null, pos, blockOfItem.getSoundGroup(blockOfItem.getDefaultState()).getPlaceSound(), SoundCategory.BLOCKS, 1, 0.8f);
            world.updateListeners(pos, be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);
            handItem.decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    public static void registerCopycat(String modID, String blockName){
        copycatsRegistry.add(blockName);
        copycats.add(new Identifier(modID, blockName));
    }

    public static List<String> getCopycatsRegistry(){
        return copycatsRegistry;
    }
}
