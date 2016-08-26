package com.jplee.worldmanager.gui;

import com.jplee.worldmanager.WorldManager;
import com.jplee.worldmanager.gen.WorldGeneration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.SidedProxy;
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

		String[] debug = debugInfo.split("\\|");
		
		if(WorldManager.isDebugShowing()) {
			int start = scaledHeight / 2 - debug.length * 5;
			int i = 0;
			for(String str : debug) {
				drawString(fontRenderer, str, 2, start + i * 10, 0xFFFFFF);
				i++;
			}
			
			if(Minecraft.getSystemTime() >= this.debugUpdateTime + 1000L) {
				WorldGeneration wg = WorldGeneration.instance;
				int dim = Minecraft.getMinecraft().thePlayer.dimension;
				debugInfo = "=== World Manager ===|Dimension: " + dim + "|Chunks Queued: " + wg.getLoadedQueuedChunkCount(dim);
				this.debugUpdateTime += 1000L;
			}
		}
	}
}
