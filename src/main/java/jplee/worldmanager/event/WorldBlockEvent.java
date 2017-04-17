package jplee.worldmanager.event;

import java.util.Collection;

import jplee.worldmanager.manager.BlockManager;
import jplee.worldmanager.util.BlockProperty;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WorldBlockEvent {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onHarvestItems(BlockEvent.HarvestDropsEvent event) {
		if(BlockManager.instance.worldHasProperties(event.getWorld())) {
			Collection<BlockProperty> props = BlockManager.instance.getProperties(event.getWorld(),
				event.getState().getBlock());
			if(props != null && !props.isEmpty()) {
				ItemStack stack = null;
				boolean silktouch = false;
				boolean adequate = false;
				for(BlockProperty prop : props) {
					if(prop.hasData("drop") && stack == null) stack = (ItemStack) prop.get("drop");
					if(!silktouch && prop.hasData("drop")) silktouch = prop.getBoolean("silktouch");
					if(!adequate) adequate = prop.isAdequateState("block", event.getState());
				}
				if(stack != null && stack.getItem() != null && adequate) {
					if(!(event.isSilkTouching() && silktouch)) {
						if(!stack.getItem().equals(Item.getItemFromBlock(Blocks.AIR))) {
							event.getDrops().clear();
							event.getDrops().add(stack.copy());
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockNotify(BlockEvent.NeighborNotifyEvent event) {
		if(!event.isCanceled() && BlockManager.instance.worldHasProperties(event.getWorld())) {
			for(EnumFacing face : event.getNotifiedSides()) {
				if(BlockManager.instance.canFall(event.getWorld(), event.getWorld().getBlockState(event.getPos().offset(face)),
					event.getPos().offset(face))) {
					BlockManager.instance.fallBlock(event.getWorld(), event.getPos().offset(face), Blocks.SAND.tickRate(event.getWorld()));
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockPlaced(BlockEvent.PlaceEvent event) {
		if(!event.isCanceled() && BlockManager.instance.worldHasProperties(event.getWorld())) {
			if(BlockManager.instance.canFall(event.getWorld(), event.getWorld().getBlockState(event.getPos()),
				event.getPos()))
				BlockManager.instance.fallBlock(event.getWorld(), event.getPos(),
					Blocks.SAND.tickRate(event.getWorld()));
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		// if(!event.isCanceled() && hasProperties(event.getWorld())) {
		// if(canFall(event.getWorld(),
		// event.getWorld().getBlockState(event.getPos().up())))
		// fallBlock(event.getWorld(), event.getPos().up(),
		// Blocks.SAND.tickRate(event.getWorld()));
		// }
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		BlockManager.instance.processEvent(event);
	}
}
