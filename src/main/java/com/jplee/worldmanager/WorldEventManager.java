package jplee.worldmanager;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.jplee.worldmanager.gen.WorldGeneration;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
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
				Collection<ChunkPos> completion = Lists.newArrayList(WorldGeneration.instance.getLoadedPendingForWorld(w));
				if(completion == null) return;
				int count = 0;
				for(ChunkPos pos : completion) {
					Chunk chunk = w.getChunkFromChunkCoords(pos.chunkXPos, pos.chunkZPos);
					if(chunk.isPopulated() && chunk.isLoaded()) {
						boolean chunkModified = WorldGeneration.instance.runProcessChunk(w, pos);
						if(chunkModified) {
							chunk.resetRelightChecks();
						}
						WorldGeneration.instance.removePendingForWorld(w, chunk, true);
						count++;
					}
					if(count >= WorldManager.getMaxProcesses() && WorldManager.getMaxProcesses() != -1) {
						WorldManager.info("Maximum amount of proccesses have been reached this tick %s", count);
						break;
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
				WorldGeneration.instance.addPendingForWorld(world, event.getChunk(), true);
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
				WorldGeneration.instance.removePendingForWorld(world, event.getChunk(), false);
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
