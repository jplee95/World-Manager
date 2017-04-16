package jplee.worldmanager.event;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.manager.EntityManager;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldEntityEvent {

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
	public void onSpawnLiving(WorldEvent.PotentialSpawns event) {
		int height = EntityManager.instance.getMaxHostelSpawnHeight();
		if(height > -1) {
			if(event.getPos() instanceof MutableBlockPos) {
				MutableBlockPos pos = (MutableBlockPos)event.getPos();
				if(event.getType() == EnumCreatureType.MONSTER)
					if(pos.getY() > height)
						event.setCanceled(true);
			}
		}
	}
	
	
}
