package jplee.worldmanager.asm;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.resources.I18n;

public class CodeDefins {

	public static boolean cactusDoesDamage() {
		return false;
	}
	
	public static void addWorldOptionButton(GuiWorldEdit worldEdit, List<GuiButton> buttonList) {
		buttonList.add(new GuiButton(5, worldEdit.width / 2 - 100, worldEdit.height / 4 + 72 + 12, I18n.format("wm.selectworld.edit.worldoptions", new Object[0])));
	}
	
	public static void worldOptionAction(int buttonId, GuiScreen screen, String worldId) {
		
	}
	
	public static final CodeDefinition GUI_BUTTON = new CodeDefinition("bdr", "net/minecraft/client/gui/GuiButton", WorldManagerPlugin.isObf);
	
	public static final CodeDefinition GUI_SCREEN = new CodeDefinition("bft", "net/minecraft/client/gui/GuiScreen", WorldManagerPlugin.isObf);
	static {
		GUI_SCREEN.addField("n", "buttonList", "Ljava/util/List;");
		GUI_SCREEN.addField("m", "height", "I");
		GUI_SCREEN.addField("l", "width", "I");
	}
	
	public static final CodeDefinition GUI_WORLD_EDIT = new CodeDefinition("bid", "net/minecraft/client/gui/GuiWorldEdit", WorldManagerPlugin.isObf);
	static {
		GUI_WORLD_EDIT.addMethod("a", "actionPerformed", "(L"+ GUI_BUTTON.name +";)V");
		GUI_WORLD_EDIT.addMethod("b", "initGui", "()V");
		GUI_WORLD_EDIT.setParent(GUI_SCREEN);
	}
	
	public static final CodeDefinition I18N = new CodeDefinition("bxl", "net/minecraft/client/resources/I18n", WorldManagerPlugin.isObf);
	static {
		I18N.addMethod("a", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
	}
	
	public static final CodeDefinition BLOCK_CACTUS = new CodeDefinition("akn", "net/minecraft/block/BlockCactus", WorldManagerPlugin.isObf);
	static {
		BLOCK_CACTUS.addMethod("a", "onEntityCollidedWithBlock", "(Laid;Lcm;Lars;Lrw;)V", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V");
	}
}
