package jplee.worldmanager.entity;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import jplee.worldmanager.WorldManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

public class EntityManager {

	public static EntityManager instance = new EntityManager();
	
	private Map<EntityPlayer,Boolean> startedPlayers;
	private List<ItemStack> startingItems;
	
	private EntityManager() {
		 startedPlayers = Maps.newHashMap();
	}
	
	public void loadStartingItems() {
		 String[] startInv = WorldManager.getStartInv();
		startingItems = Lists.newArrayListWithCapacity(startInv.length);
		
		int count = 0;
		for(String item : startInv) {
			if(!item.startsWith("#") && !item.trim().isEmpty()) {
				try {
					startingItems.add(parsItem(item));
					count++;
				} catch (NBTException e) {
					WorldManager.error("NBT tags were incorrect writen for item %s", item);
				}
				if(count >= 18) break;
			}
		}
	}
	
	private ItemStack parsItem(String item) throws NBTException {
		String[] parts = item.trim().split(" ", 2);
		
		int amount = 1;
		if(parts.length >= 2) {
			amount = Integer.parseInt(parts[1]);
		}
		
		String id = parts[0];
		String[] idParts = id.split(":");
		
		int meta = 0;
		Item i = null;
		if(idParts.length == 3) {
			meta = Integer.parseInt(idParts[2]);
			i = Item.getByNameOrId(idParts[0] + ":"  + idParts[1]);
		} else if(idParts.length == 2) {
			try {
				meta = Integer.parseInt(idParts[1]);
				i = Item.getByNameOrId(idParts[0]);
			} catch(NumberFormatException e) {
				i = Item.getByNameOrId(idParts[0] + ":" + idParts[1]);
			}
		} else {
			i = Item.getByNameOrId(idParts[0]);
		}
		
		ItemStack stack = new ItemStack(i, amount, meta);
		if(parts.length == 3) {
			try {
				stack.setTagCompound(JsonToNBT.getTagFromJson(parts[2]));
			} catch(NBTException e) {
				throw e;
			}
		}
		
		return stack;
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
}
