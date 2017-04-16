package jplee.worldmanager.manager;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.config.GenConfig;
import jplee.worldmanager.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;

public class EntityManager {

	public static final EntityManager instance = new EntityManager();
	
	private Map<EntityPlayer,Boolean> startedPlayers;
	private List<ItemStack> startingItems;
	
	private boolean startingInventory;
	
	private int maxHostelSpawnHeight;
	
	private EntityManager() {
		 startedPlayers = Maps.newHashMap();
		 
		 startingInventory = false;
		 maxHostelSpawnHeight = -1;
	}
	
	public void loadStartingItems(GenConfig config) {
		maxHostelSpawnHeight = config.getMaxHostelSpawnHeight();
		
		this.startingInventory = config.isStartInvEnabled();
		if(startingInventory) {
			String[] startInv = config.getStartingInventory();
			startingItems = Lists.newArrayListWithCapacity(startInv.length);
			
			
			int count = 0;
			for(String item : startInv) {
				if(!item.startsWith("#") && !item.trim().isEmpty()) {
					try {
						startingItems.add(ItemUtils.parsItem(item));
						count++;
					} catch (NBTException e) {
						WorldManager.logger.error("NBT tags were incorrect writen for item %s", item);
					}
					if(count >= 18) {
						WorldManager.logger.warning("There was more that 18 items listed for starting inventory", new Object[0]);
						break;
					}
				}
			}
		}
	}
	
	public boolean startingInventory() {
		return this.startingInventory && !startingItems.isEmpty();
	}
	
	public void addPlayerStarted(EntityPlayer player) {
		startedPlayers.put(player, true);
	}
	
	public boolean hasPlayerStarted(EntityPlayer player) {
		return startedPlayers.get(player) != null;
	}

	public void removePlayerStarted(EntityPlayer player) {
		startedPlayers.remove(player);
	}

	public void givePlayerStartingInventory(EntityPlayer player) {
		for(ItemStack stack : startingItems) {
			player.inventory.addItemStackToInventory(stack.copy());
		}
	}
	
	public void clearEntityInfoCatch() {
		startedPlayers.clear();
	}
	
	public int getMaxHostelSpawnHeight() {
		return maxHostelSpawnHeight;
	}
}
