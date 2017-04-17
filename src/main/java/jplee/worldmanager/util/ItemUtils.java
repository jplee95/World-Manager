package jplee.worldmanager.util;

import jplee.worldmanager.WorldManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

public class ItemUtils {

	public static ItemStack parsItem(String item) throws NBTException {
		String[] parts = item.trim().split(" +", 3);
		
		int amount = 1;
		if(parts.length >= 2) {
			WorldManager.logger.info(parts[1], new Object[0]);
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
	
}
