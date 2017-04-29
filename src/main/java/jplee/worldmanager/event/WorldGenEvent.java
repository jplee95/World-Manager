package jplee.worldmanager.event;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.Lists;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.manager.GenerationManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class WorldGenEvent {

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.side != Side.SERVER) {
			return;
		}
		
		if(!(event.world instanceof WorldServer)) {
			return;
		} else {
			WorldServer world = (WorldServer) event.world;
			
			if(event.phase == TickEvent.Phase.END) {
				if(GenerationManager.instance.isWorldProcessable(world)) {
					
					if(!GenerationManager.instance.getQueuedForWorld(world).isEmpty()) {
						Collection<ChunkPos> completion = Lists.newArrayList(GenerationManager.instance.getQueuedForWorld(world));
						if(completion == null) return;
						for(ChunkPos pos : completion) {
							Chunk chunk = world.getChunkFromChunkCoords(pos.chunkXPos, pos.chunkZPos);
							if(chunk.isPopulated() && chunk.isLoaded()) {
								if(GenerationManager.instance.processChunk(world, pos)) {
									chunk.resetRelightChecks();
									chunk.setChunkModified();
	
									if(!GenerationManager.instance.isReplacementOverride()) {
										world.playerEntities.forEach(player -> {
											if(player instanceof EntityPlayerMP) {
												EntityPlayerMP playerMp = (EntityPlayerMP) player;
												playerMp.connection.sendPacket(new SPacketChunkData(chunk, 65535));
											}
										});
									}
								}
								GenerationManager.instance.removeFromQueue(world, chunk, 1);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		GameRules rules = event.getWorld().getGameRules();
		for(String rule : rules.getRules()) {
			
		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		File file = new File(event.getWorld().getSaveHandler().getWorldDirectory(), "gamerule.dat");
		
	}
	
	@SubscribeEvent
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		World world = event.getWorld();
		NBTTagCompound compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
		
		if(compound != null) {
			if(compound.hasKey(WorldManager.CHUNK_REPLACE_TAG) && GenerationManager.instance.isWorldProcessable(world)
				&& !GenerationManager.instance.isQueuedChunk(world, event.getChunk())) {
				GenerationManager.instance.addToQueue(world, event.getChunk(), false);
			}
		}
	}
	
	@SubscribeEvent
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		World world = event.getWorld();
		if(GenerationManager.instance.isWorldProcessable(world) || GenerationManager.instance.getQueuedChunkCount(world.provider.getDimension()) > 0) {
			
			if(GenerationManager.instance.isQueuedChunk(world, event.getChunk())) {
				NBTTagCompound compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
				if(compound == null) {
					event.getData().setTag(WorldManager.MODID, new NBTTagCompound());
					compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
				}
				if(!compound.hasKey(WorldManager.CHUNK_REPLACE_TAG)) {
					compound.setBoolean(WorldManager.CHUNK_REPLACE_TAG, true);
					GenerationManager.instance.removeFromQueue(world, event.getChunk(), 2);
				}
			}
		}
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if(GenerationManager.instance.isQueuedChunk(event.getWorld(), event.getChunk(), 2)) {
			GenerationManager.instance.queueChunk(event.getWorld(), event.getChunk());
		}
	}
	
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if(GenerationManager.instance.isQueuedChunk(event.getWorld(), event.getChunk(), 1)) {
			GenerationManager.instance.unqueueChunk(event.getWorld(), event.getChunk());
		}
	}

//	@SubscribeEvent(priority=EventPriority.LOWEST)
//	public void onBiomeDecorate(DecorateBiomeEvent.Decorate event) {
//		event.setResult(Result.DENY);
//	}

//	@SubscribeEvent(priority=EventPriority.LOWEST)
//	public void onPopulateChunk(PopulateChunkEvent.Populate event) {
//		event.setResult(Result.DENY);
//	}
	
//	@SubscribeEvent(priority=EventPriority.LOWEST)
//	public void onChunkGenerator(ChunkGeneratorEvent.ReplaceBiomeBlocks event) {
//		event.setResult(Result.DENY);
//		event.getGen();
//	}
	
//	@SubscribeEvent(priority=EventPriority.LOWEST)
//	public void on(InitMapGenEvent event) {
//		event.setResult(Result.DENY);
//	}

//	@SubscribeEvent
//	public void onOreGenerate(OreGenEvent.GenerateMinable event) {
//	 	// implement ore gen override here
//		GenerationManager.logger.info("%s", event.getType());
//	}
	
//	@SubscribeEvent
//	public void onOreRegister(OreRegisterEvent event) {
//		GenerationManager.logger.info("%s", event.getOre());
//	}

}
