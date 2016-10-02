package jplee.worldmanager;

import org.apache.logging.log4j.Logger;

import jplee.worldmanager.command.CommandWorldManager;
import jplee.worldmanager.config.GenConfig;
import jplee.worldmanager.entity.EntityManager;
import jplee.worldmanager.gen.WorldGeneration;
import jplee.worldmanager.gui.GuiChunkDebugEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid=WorldManager.MODID, name=WorldManager.NAME, version=WorldManager.VERSION, dependencies=WorldManager.FORGE_VERSION,
	 acceptedMinecraftVersions=WorldManager.MINECRAFT_VERSION, useMetadata=false, acceptableRemoteVersions = "*")
public class WorldManager {
	public static final String NAME = "World Manager";
	public static final String MODID = "worldmanager";
	public static final String VERSION = "1.0.1";
	public static final String FORGE_VERSION = "required-after:Forge@[12.18.1.2039,)";
	public static final String MINECRAFT_VERSION = "[1.9.4,1.10.2]";
	
	public static final String CHUNK_REPLACE_TAG = "wmReplace";
	public static final String PLAYER_START_TAG = "wmStart";

	@Mod.Instance
	public static WorldManager instance;
	
	private static Logger logger;
	private static GenConfig config;
	
	private static boolean showDebugInfo = false;
	private static boolean showDebugLog = false;
	
	public static void showDebug(boolean show) {
		showDebugInfo = show;
	}
	public static boolean isDebugShowing() {
		return showDebugInfo;
	}
	
	public static void showDebugLog(boolean show) {
		showDebugLog = show;
	}
	public static boolean isDebugLogShowing() {
		return showDebugLog;
	}
	
	public static String[] getReplaceables() {
		return config.getReplaceables();
	}
	
	public static int getMaxProcesses() {
		return config.getMaxProcesses();
	}
	
	public static String[] getStartInv() {
		return config.getStartingInventory();
	}
	
	public static boolean isReplaceablesEnabled() {
		return config.isReplaceablesEnabled();
	}
	
	public static boolean isStartInvEnabled() {
		return config.isStartInvEnabled();
	}
	
	public static void reloadConfig() {
		config.loadConfig();
		if(config.isReplaceablesEnabled())
			WorldGeneration.instance.loadReplacables();
		if(config.isStartInvEnabled())
			EntityManager.instance.loadStartingItems();
	}
	
	public static void info(String message, Object...args) {
		logger.info(String.format(message, args));
	}

	public static void warning(String message, Object...args) {
		logger.warn(String.format(message, args));
	}

	public static void debug(String message, Object...args) {
		if(logger.isDebugEnabled())
			logger.debug(String.format(message, args));
		else if(showDebugLog) {
			logger.info("[DEBUG]" + String.format(message, args));
		}
	}
	
	public static void error(String message, Object...args) {
		logger.error(String.format(message, args));
	}
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new GenConfig(event.getSuggestedConfigurationFile());

		if(config.isReplaceablesEnabled())
			WorldGeneration.instance.loadReplacables();
		WorldGeneration.instance.registerWorldGenerators();

		if(config.isStartInvEnabled())
			EntityManager.instance.loadStartingItems();
		
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
		WorldGeneration.instance.clearQueuedChunks();
		EntityManager.instance.clearEntityInfoCatch();
	}
}
