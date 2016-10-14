package jplee.worldmanager.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;

public class GuiConfirmClean extends GuiScreen {

	private GuiScreen lastGui;
	private String worldId;
	
	public GuiConfirmClean(GuiScreen lastGui, String worldId) {
		this.lastGui = lastGui;
		this.worldId = worldId;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(1, this.width / 2 - 130, this.height / 2 + 12, 125, 20, I18n.format("gui.yes", new Object[0])));
		this.buttonList.add(new GuiButton(0, this.width / 2 + 5, this.height / 2 + 12, 125, 20, I18n.format("gui.no", new Object[0])));
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == 0) {
			this.mc.displayGuiScreen(this.lastGui);
		} else if(button.id == 1) {
			ISaveFormat iSaveFormater = this.mc.getSaveLoader();
			WorldInfo worldInfo = iSaveFormater.getWorldInfo(this.worldId);
			NBTBase compound = worldInfo.getAdditionalProperty("FML");
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, I18n.format("wm.selectworld.edit.worldoptions.cleanregistry", new Object[0]), this.width / 2, 20, 16777215);
		this.drawCenteredString(this.fontRendererObj, I18n.format("wm.selectworld.edit.worldoptions.cleanregistry.confirm", new Object[0]), this.width / 2, this.height / 2 - 8, 15132160);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
