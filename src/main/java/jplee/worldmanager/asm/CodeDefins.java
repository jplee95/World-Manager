package jplee.worldmanager.asm;

import java.util.List;

import jplee.worldmanager.gui.GuiWorldOptions;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.resources.I18n;

public final class CodeDefins {

	public static boolean cactusDoesDamage() {
		return false;
	}
	
	public static void addWorldOptionButton(GuiWorldEdit worldEdit, List<GuiButton> buttonList) {
		buttonList.add(new GuiButton(5, worldEdit.width / 2 - 100, worldEdit.height / 4 + 72 + 12, I18n.format("wm.selectworld.edit.worldoptions", new Object[0])));
	}
	
	public static void worldOptionAction(int buttonId, GuiScreen screen, String worldId) {
		if(buttonId == 5) {
			screen.mc.displayGuiScreen(new GuiWorldOptions(screen, worldId));
		}
	}
	
	public static final CodeDefinition GUI_BUTTON = new CodeDefinition("bdr", "net/minecraft/client/gui/GuiButton", WorldManagerPlugin.isObf);
	static {
		GUI_BUTTON.addField("k", "id", "I");
	}
	
	public static final CodeDefinition GUI_SCREEN = new CodeDefinition("bft", "net/minecraft/client/gui/GuiScreen", WorldManagerPlugin.isObf);
	static {
		GUI_SCREEN.addField("n", "buttonList", "Ljava/util/List;");
		GUI_SCREEN.addField("m", "height", "I");
		GUI_SCREEN.addField("l", "width", "I");
	}
	
	public static final CodeDefinition GUI_WORLD_EDIT = new CodeDefinition("bid", "net/minecraft/client/gui/GuiWorldEdit", WorldManagerPlugin.isObf);
	static {
		GUI_WORLD_EDIT.addField("g", "worldId", "Ljava/lang/String;");
		GUI_WORLD_EDIT.addMethod("a", "actionPerformed", "("+ GUI_BUTTON.asClass() +")V");
		GUI_WORLD_EDIT.addMethod("b", "initGui", "()V");
		GUI_WORLD_EDIT.setParent(GUI_SCREEN);
	}
	
	public static final CodeDefinition I18N = new CodeDefinition("bxl", "net/minecraft/client/resources/I18n", WorldManagerPlugin.isObf);
	static {
		I18N.addMethod("a", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
	}
	
	public static final CodeDefinition CODE_DEFINES = new CodeDefinition("jplee/worldmanager/asm/CodeDefins", WorldManagerPlugin.isObf);
	static {
		CODE_DEFINES.addField("addWorldOptionButton", "(" + CodeDefins.GUI_WORLD_EDIT.asClass() + "Ljava/util/List;)V");
		CODE_DEFINES.addField("worldOptionAction", "(I" + GUI_SCREEN.asClass() + "Ljava/lang/String;)V");
	}
	
}
