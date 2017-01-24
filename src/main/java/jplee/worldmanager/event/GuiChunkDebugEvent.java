package jplee.worldmanager.event;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.gen.WorldGeneration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
		
		if(Minecraft.getSystemTime() >= this.debugUpdateTime + 1000L) {
			WorldGeneration wg = WorldGeneration.instance;
			int dim = Minecraft.getMinecraft().thePlayer.dimension;
			boolean workable = WorldGeneration.instance.isWorldProcessable(dim);
			debugInfo = "=== World Manager ===\nCurrent:\n  " + dim + " > " + (workable ? wg.getQueuedChunkCount(dim) : "No Gen") + "\n";
			if(wg.getWorldsWithQueues().size() > 1)
				debugInfo += "\nDimensions";
			for(int dimension : wg.getWorldsWithQueues()) {
				if(dim != dimension) {
					workable = WorldGeneration.instance.isWorldProcessable(dimension);
					debugInfo += "\n" + dimension + " > " + (workable ? wg.getQueuedChunkCount(dimension) : "No Gen");
				}
			}
			this.debugUpdateTime += 1000L;
		}
	}
}
