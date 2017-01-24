package jplee.worldmanager.event;

import java.util.Collection;

import com.google.common.collect.Lists;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.entity.EntityManager;
import jplee.worldmanager.gen.WorldGeneration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

public class WorldEventManager {

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
				if(WorldGeneration.instance.isWorldProcessable(world)) {
					if(!WorldGeneration.instance.getQueuedForWorld(world).isEmpty()) {
						Collection<ChunkPos> completion = Lists.newArrayList(WorldGeneration.instance.getQueuedForWorld(world));
						if(completion == null) return;
						for(ChunkPos pos : completion) {
							Chunk chunk = world.getChunkFromChunkCoords(pos.chunkXPos, pos.chunkZPos);
							if(chunk.isPopulated() && chunk.isLoaded()) {
								if(WorldGeneration.instance.processChunk(world, pos)) {
//									chunk.resetRelightChecks();
									chunk.setChunkModified();

									world.playerEntities.forEach(player -> {
										if(player instanceof EntityPlayerMP) {
											EntityPlayerMP playerMp = (EntityPlayerMP) player;
											playerMp.connection.sendPacket(new SPacketChunkData(chunk, 65535));
										}
									});
									
//									for(EntityPlayer player : world.playerEntities) {
//										if(player instanceof EntityPlayerMP) {
//											WorldManager.packet.sendToPlayer(new ChunkUpdateMessage(world, pos), (EntityPlayerMP) player);
//										}
//									}
								}
								WorldGeneration.instance.removeFromQueue(world, chunk, 1);
							}
						}
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
			if(compound.hasKey(WorldManager.CHUNK_REPLACE_TAG) || WorldGeneration.instance.isWorldProcessable(world)) {
				WorldGeneration.instance.addToQueue(world, event.getChunk(), false);
			}
		}
	}
	
	@SubscribeEvent
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		World world = event.getWorld();
		if(WorldGeneration.instance.isWorldProcessable(world) || WorldGeneration.instance.getQueuedChunkCount(world.provider.getDimension()) > 0) {
			
			if(WorldGeneration.instance.isQueuedChunk(world, event.getChunk())) {
				NBTTagCompound compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
				if(compound == null) {
					event.getData().setTag(WorldManager.MODID, new NBTTagCompound());
					compound = (NBTTagCompound) event.getData().getTag(WorldManager.MODID);
				}
				if(!compound.hasKey(WorldManager.CHUNK_REPLACE_TAG)) {
					compound.setBoolean(WorldManager.CHUNK_REPLACE_TAG, true);
					WorldGeneration.instance.removeFromQueue(world, event.getChunk(), 2);
				}
			}
		}
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if(WorldGeneration.instance.isQueuedChunk(event.getWorld(), event.getChunk(), 2)) {
			WorldGeneration.instance.queueChunk(event.getWorld(), event.getChunk());
		}
	}
	
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		if(WorldGeneration.instance.isQueuedChunk(event.getWorld(), event.getChunk(), 1)) {
			WorldGeneration.instance.unqueueChunk(event.getWorld(), event.getChunk());
		}
	}

//	@SubscribeEvent
//	public void onPlayerLoad(PlayerEvent.LoadFromFile event) {
//		if(WorldManager.isStartInvEnabled()) {
//			EntityPlayer player = event.getEntityPlayer();
//			NBTTagCompound compound = (NBTTagCompound) player.getEntityData().getTag(WorldManager.MODID);
//			
//			if(compound != null) {
//				if(compound.hasKey(WorldManager.PLAYER_START_TAG)) {
//					EntityManager.instance.addPlayerStarted(player);
//				}
//			}
//			
//			if(!EntityManager.instance.hasPlayerStarted(player)) {
//				EntityManager.instance.givePlayerStartingInventory(player);
//				EntityManager.instance.addPlayerStarted(player);
//			}
//		}
//	}

	@SubscribeEvent
	public void onPlayerLogIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		if(EntityManager.instance.startingInventory()) {
			NBTTagCompound compound = (NBTTagCompound) event.player.getEntityData().getTag(WorldManager.MODID);
			
			if(compound != null) {
				if(compound.hasKey(WorldManager.PLAYER_START_TAG)) {
					EntityManager.instance.addPlayerStarted(event.player);
				}
			}
			
			if(!EntityManager.instance.hasPlayerStarted(event.player)) {
				EntityManager.instance.givePlayerStartingInventory(event.player);
				EntityManager.instance.addPlayerStarted(event.player);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerSave(PlayerEvent.SaveToFile event) {
		if(EntityManager.instance.startingInventory()) {
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
		WorldManager.logger.info("%s", event.getOre());
	}

	@SubscribeEvent
	public void onOreGenerate(OreGenEvent.GenerateMinable event) {
		WorldManager.logger.info("%s", event.getType());
	}
}
