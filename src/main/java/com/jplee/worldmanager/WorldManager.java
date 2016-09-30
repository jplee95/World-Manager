package com.jplee.worldmanager;

import org.apache.logging.log4j.Logger;

import com.jplee.worldmanager.command.CommandWorldManager;
import com.jplee.worldmanager.config.GenConfig;
import com.jplee.worldmanager.gen.WorldGeneration;
import com.jplee.worldmanager.gui.GuiChunkDebugEvent;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid=WorldManager.MODID, name=WorldManager.NAME, version=WorldManager.VERSION, dependencies=WorldManager.FORGE_VERSION,
	 acceptedMinecraftVersions=WorldManager.MINECRAFT_VERSION, useMetadata=false)
public class WorldManager {
	public static final String NAME = "World Manager";
	public static final String MODID = "worldmanager";
	public static final String VERSION = "1.0.0";
	public static final String FORGE_VERSION = "required-after:Forge@[12.18.1.2039,)";
	public static final String MINECRAFT_VERSION = "[1.9.4,1.10.2]";
	
	public static final String CHUNK_REPLACE_TAG = "wmReplace";
	
	private static Logger logger;
	private static GenConfig config;
	
	private static boolean showDebugInfo = false;
	
	public static void showDebug(boolean show) {
		showDebugInfo = show;
	}
	
	public static boolean isDebugShowing() {
		return showDebugInfo;
	}
	
	@Mod.Instance
	public static WorldManager instance;
	
	public static String[] getReplaceables() {
		return config.getReplaceables();
	}
	
	public static int getMaxProcesses() {
		return config.getMaxProcesses();
	}
	
	public static void reloadConfig() {
		config.reloadConfig();
		WorldGeneration.instance.loadReplacables();
	}
	
	public static void info(String message, Object...args) {
		logger.info(String.format(message, args), new Object[0]);
	}

	public static void warning(String message, Object...args) {
		logger.warn(String.format(message, args), new Object[0]);
	}

	public static void error(String message, Object...args) {
		logger.error(String.format(message, args), new Object[0]);
	}
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new GenConfig(event.getSuggestedConfigurationFile());
		
		WorldGeneration.instance.loadReplacables();
		WorldGeneration.instance.registerWorldGenerators();
		
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new WorldEventManager());
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if(event.getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new GuiChunkDebugEvent());
		}
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandWorldManager());
	}
	
	@Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event) {
		info("Server Stoping", new Object[0]);
		WorldGeneration.instance.clearQueuedChunks();
	}
}
