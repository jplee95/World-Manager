package com.jplee.worldmanager;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import com.jplee.worldmanager.gen.WorldGeneration;
import com.jplee.worldmanager.util.Replaceable;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class WorldEventManager {

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.side != Side.SERVER) {
			return;
		}
		
		World w = event.world;
		if(!(w instanceof WorldServer)) {
			return;
		} else {
			if(event.phase == TickEvent.Phase.END) {
				Collection<ChunkPos> completion = Lists.newArrayList(WorldGeneration.instance.getPendingForWorld(w));
				if(completion == null) return;
				for(ChunkPos pos : completion) {
					Chunk chunk = w.getChunkFromChunkCoords(pos.chunkXPos, pos.chunkZPos);
					if(chunk.isPopulated()) {
						boolean chunkModified = WorldGeneration.instance.runProcessChunk(w, pos);
						if(chunkModified) {
							chunk.resetRelightChecks();
						}
						WorldGeneration.instance.removePendingForChunk(w, chunk);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		World world = event.getWorld();
		NBTTagCompound compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
		
		if(compound != null) {
			if(compound.hasKey(WorldManager.CHUNK_REPLACE_TAG)) {
				WorldGeneration.instance.addNewPendingChunk(world, event.getChunk());
			}
		}
	}
	
	@SubscribeEvent
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		World world = event.getWorld();
		
		if(WorldGeneration.instance.isQueuedChunk(world, event.getChunk())) {
			NBTTagCompound compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
			if(compound == null) {
				event.getData().setTag(WorldManager.MODID, new NBTTagCompound());
				compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
			}
			if(!compound.hasKey(WorldManager.CHUNK_REPLACE_TAG)) {
				compound.setBoolean(WorldManager.CHUNK_REPLACE_TAG, true);
			}
		}
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if(WorldGeneration.instance.isUnloadedQueuedChunk(event.getWorld(), event.getChunk())) {
			WorldGeneration.instance.requeueChunk(event.getWorld(), event.getChunk());
		}
	}
	
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if(WorldGeneration.instance.isLoadedQueuedChunk(event.getWorld(), event.getChunk())) {
			WorldGeneration.instance.unqueueChunk(event.getWorld(), event.getChunk());
		}
	}
}
