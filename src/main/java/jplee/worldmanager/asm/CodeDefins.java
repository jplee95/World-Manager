package jplee.worldmanager.asm;

import java.util.List;

import jplee.worldmanager.gen.WorldGeneration;
import jplee.worldmanager.gui.GuiWorldOptions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public final class CodeDefins {

	public static void init() { }
	
	private CodeDefins() { }
	
	public static void addWorldOptionButton(GuiWorldEdit worldEdit, List<GuiButton> buttonList) {
		buttonList.add(new GuiButton(5, worldEdit.width / 2 - 100, worldEdit.height / 4 + 72 + 12, I18n.format("wm.selectworld.edit.worldoptions", new Object[0])));
	}
	
	public static void worldOptionAction(int buttonId, GuiScreen screen, String worldId) {
		if(buttonId == 5) {
			screen.mc.displayGuiScreen(new GuiWorldOptions(screen, worldId));
		}
	}
	
	public static IBlockState getReplacement(World world, Chunk chunk, IBlockState state) {
		WorldGeneration.instance.getQueuedForWorld(world);
		
		return Blocks.AIR.getDefaultState();
	}
	
	public static final CodeDefinition WORLD = new CodeDefinition("aid", "net/minecraft/world/World");
	public static final CodeDefinition CHUNK_PRIMER = new CodeDefinition("", "net/minecraft/world/chunk/ChunkPrimer");
	public static final CodeDefinition CHUNK = new CodeDefinition("asv", "net/minecraft/world/chunk/Chunk")
		.addMethod("<init>", "");
	public static final CodeDefinition I_BLOCK_STATE = new CodeDefinition("ars", "net/minecraft/block/state/IBlockState");
	
	public static final CodeDefinition GUI_BUTTON = new CodeDefinition("bdr", "net/minecraft/client/gui/GuiButton")
		.addField("k", "id", "I");
	
	public static final CodeDefinition GUI_SCREEN = new CodeDefinition("bft", "net/minecraft/client/gui/GuiScreen")
		.addField("n", "buttonList", "Ljava/util/List;")
		.addField("m", "height", "I")
		.addField("l", "width", "I");
	
	public static final CodeDefinition GUI_WORLD_EDIT = new CodeDefinition("bid", "net/minecraft/client/gui/GuiWorldEdit")
		.addField("g", "worldId", "Ljava/lang/String;")
		.addMethod("a", "actionPerformed", "("+ GUI_BUTTON.asClass() +")V")
		.addMethod("b", "initGui", "()V")
		.setParent(GUI_SCREEN);
	
	public static final CodeDefinition I18N = new CodeDefinition("bxl", "net/minecraft/client/resources/I18n")
		.addMethod("a", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
	
	public static final CodeDefinition CODE_DEFINES = new CodeDefinition("jplee/worldmanager/asm/CodeDefins")
		.addMethod("addWorldOptionButton", "(" + GUI_WORLD_EDIT.asClass() + "Ljava/util/List;)V")
		.addMethod("worldOptionAction", "(I" + GUI_SCREEN.asClass() + "Ljava/lang/String;)V")
		.addMethod("getReplacement", "(" + WORLD.asClass() + CHUNK.asClass() + I_BLOCK_STATE.asClass() + ")" + I_BLOCK_STATE.asClass());

}
