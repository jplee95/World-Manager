package jplee.worldmanager.event;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.manager.BlockManager;
import jplee.worldmanager.manager.GenerationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiChunkDebugEvent extends Gui {

	private long debugUpdateTime;
	private String debugInfo;
	
	public GuiChunkDebugEvent() {
		debugUpdateTime = Minecraft.getSystemTime();
		debugInfo = "";
	}
	
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void eventHander(RenderGameOverlayEvent.Text event) {
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		ScaledResolution resolution = event.getResolution();
		int scaledHeight = resolution.getScaledHeight();

		if(Minecraft.getSystemTime() >= this.debugUpdateTime + 1000L) {
			GenerationManager wg = GenerationManager.instance;
			int dim = Minecraft.getMinecraft().thePlayer.dimension;
			String currentName = DimensionManager.getProviderType(dim).getName();
			boolean workable = GenerationManager.instance.isWorldProcessable(dim);
			debugInfo = "=== World Manager ===\nCurrent:\n  " + currentName + " (" + dim + ") > Q: "
				+ (workable ? wg.getQueuedChunkCount(dim) : "No Gen") + " F: " +
				BlockManager.instance.getFallEventCount(Minecraft.getMinecraft().theWorld) + "\n";
			if(wg.getWorldsWithQueues().size() > 1)
				debugInfo += "\nDimensions";
			for(int dimension : wg.getWorldsWithQueues()) {
				String name = DimensionManager.getProviderType(dimension).getName();
				if(dim != dimension) {
					workable = GenerationManager.instance.isWorldProcessable(dimension);
					debugInfo += "\n" + name + " (" + dimension + ") > Q: " + (workable ? wg.getQueuedChunkCount(dimension) : "No Gen")
						+ " F: " + BlockManager.instance.getFallEventCount(DimensionManager.getWorld(dimension));
				}
			}
			this.debugUpdateTime += 1000L;
		}

		String[] debug = debugInfo.split("\n");
		if(WorldManager.isDebugShowing()) {
			int start = scaledHeight / 2 - 5 * debug.length;
			int i = 0;
			for(String str : debug) {
				drawRect(0, start + i * 10 - 1, 4 + fontRenderer.getStringWidth(str), start + i * 10 + fontRenderer.FONT_HEIGHT, 0x77444444);
				drawString(fontRenderer, str, 2, start + i * 10, 0xFFFFFF);
				i++;
			}
		}
	}
}
