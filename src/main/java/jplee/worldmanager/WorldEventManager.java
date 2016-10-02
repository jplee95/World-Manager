package jplee.worldmanager;

import java.util.Collection;

import com.google.common.collect.Lists;

import jplee.worldmanager.entity.EntityManager;
import jplee.worldmanager.gen.WorldGeneration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

public class WorldEventManager {

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) { // TODO: Possibly move stuff inside this to a better spot
		if(event.side != Side.SERVER) {
			return;
		}
		
		World w = event.world;
		if(!(w instanceof WorldServer)) {
			return;
		} else {
			if(event.phase == TickEvent.Phase.END && WorldManager.isReplaceablesEnabled()) {
				if(WorldGeneration.instance.getLoadedPendingForWorld(w).size() != 0) {
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
//						if(count >= WorldManager.getMaxProcesses() && WorldManager.getMaxProcesses() != -1) {
//							WorldManager.warning("Maximum amount of proccesses have been reached this tick %s", count);
//							break;
//						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		if(WorldManager.isReplaceablesEnabled()) {
			World world = event.getWorld();
			NBTTagCompound compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
			
			if(compound != null) {
				if(compound.hasKey(WorldManager.CHUNK_REPLACE_TAG)) {
					WorldGeneration.instance.addPendingForWorld(world, event.getChunk(), true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		if(WorldManager.isReplaceablesEnabled()) {
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

	@SubscribeEvent
	public void onPlayerLoad(PlayerEvent.LoadFromFile event) {
		if(WorldManager.isStartInvEnabled()) {
			EntityPlayer player = event.getEntityPlayer();
			NBTTagCompound compound = (NBTTagCompound) player.getEntityData().getTag(WorldManager.MODID);
			
			if(compound != null) {
				if(compound.hasKey(WorldManager.PLAYER_START_TAG)) {
					EntityManager.instance.addPlayerStarted(player);
				}
			}
			
			if(!EntityManager.instance.hasPlayerStarted(player)) {
				EntityManager.instance.givePlayerStartingInventory(player);
				EntityManager.instance.addPlayerStarted(player);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerSave(PlayerEvent.SaveToFile event) {
		if(WorldManager.isStartInvEnabled()) {
			EntityPlayer player = event.getEntityPlayer();
			NBTTagCompound base = player.getEntityData();

			NBTTagCompound compound = (NBTTagCompound) base.getTag(WorldManager.MODID);
			if(compound == null) {
				base.setTag(WorldManager.MODID, new NBTTagCompound());
				compound = (NBTTagCompound) base.getTag(WorldManager.MODID);
			}
			
			if(EntityManager.instance.hasPlayerStarted(player)) {
				compound.setBoolean(WorldManager.PLAYER_START_TAG, true);
			}
		}
	}

	@SubscribeEvent
	public void onOreRegister(OreRegisterEvent event) {
		WorldManager.info("%s", event.getOre());
	}

	@SubscribeEvent
	public void onOreGenerate(OreGenEvent.GenerateMinable event) {
		WorldManager.info("%s", event.getType());
	}
}
