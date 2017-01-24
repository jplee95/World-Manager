package jplee.worldmanager;

import java.io.IOException;

import jplee.jlib.server.network.MessageHandler.MessageType;
import jplee.jlib.server.network.PacketHandler;
import jplee.jlib.util.Log;

import jplee.worldmanager.command.CommandWorldManager;
import jplee.worldmanager.config.GenConfig;
import jplee.worldmanager.entity.EntityManager;
import jplee.worldmanager.event.GuiChunkDebugEvent;
import jplee.worldmanager.event.WorldEventManager;
import jplee.worldmanager.gen.WorldGeneration;
import jplee.worldmanager.util.SaveFileUtils;
import jplee.worldmanager.util.message.ChunkUpdateMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid=WorldManager.MODID, name=WorldManager.NAME, version=WorldManager.VERSION, dependencies=WorldManager.DEPENDENCIES,
	 acceptedMinecraftVersions=WorldManager.MINECRAFT_VERSION, useMetadata=false, acceptableRemoteVersions = "*")
public class WorldManager {
	public static final String NAME = "World Manager";
	public static final String MODID = "worldmanager";
	public static final String VERSION = "1.0.3";
	public static final String DEPENDENCIES = "required-after:Forge@[12.18.1.2039,)";
	public static final String MINECRAFT_VERSION = "[1.10.2]";
	
	public static final String CHUNK_REPLACE_TAG = "wmReplace";
	public static final String PLAYER_START_TAG = "wmStart";
	
	@Mod.Instance
	public static WorldManager instance;
	
	public static final Log logger = new Log();
	private static GenConfig config;
	
	public static final PacketHandler packet = new PacketHandler(MODID);
	static {
		packet.registerPacket(ChunkUpdateMessage.class, new ChunkUpdateMessage.Handler(), MessageType.CLIENT);
	}
	
	private static boolean showDebugInfo = false;
	
	public static void showDebug(boolean show) {
		showDebugInfo = show;
	}
	public static boolean isDebugShowing() {
		return showDebugInfo;
	}
	
	public static void reloadConfig() {
		config.loadConfig(false);
		if(config.isReplaceablesEnabled())
			WorldGeneration.instance.loadWorldGenerationInfo(config);
		if(config.isStartInvEnabled())
			EntityManager.instance.loadStartingItems(config);
	}
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger.attachLogger(event.getModLog());
		config = new GenConfig(event.getSuggestedConfigurationFile(), event.getSide() == Side.SERVER);

		if(config.isReplaceablesEnabled())
			WorldGeneration.instance.loadWorldGenerationInfo(config);
		WorldGeneration.instance.registerWorldGenerators();

		if(config.isStartInvEnabled())
			EntityManager.instance.loadStartingItems(config);
		
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new WorldEventManager());
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if(event.getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new GuiChunkDebugEvent());
//			MinecraftForge.EVENT_BUS.register(new GuiEventManager());
		}
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@Mod.EventHandler
	public void serverWillStart(FMLServerAboutToStartEvent event) {
		if(event.getSide() == Side.SERVER) {
			if(config.shouldCleanWorldReg()) {
				MinecraftServer server = event.getServer();
				logger.info("Cleaning %s registires", server.getFolderName());
				try { 
					SaveFileUtils.cleanWorldRegistry(server.getDataDirectory(), server.getFolderName());
				} catch(IOException e) {
					logger.error("Unable to read and write to file for %s", server.getFolderName());
					e.printStackTrace();
				}
			}
		}
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
