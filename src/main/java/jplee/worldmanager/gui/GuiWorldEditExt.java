package jplee.worldmanager.gui;

import java.io.IOException;
import java.io.PrintStream;

import org.lwjgl.input.Keyboard;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.asm.CodeDefins;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.resources.I18n;

public class GuiWorldEditExt extends GuiWorldEdit {

	private String worldId;
	
	public GuiWorldEditExt(GuiScreen p_i46593_1_, String p_i46593_2_) {
		super(p_i46593_1_, p_i46593_2_);
		this.worldId = p_i46593_2_;
	}
	
//	@Override
//	public void updateScreen() {
//		
//	}
	
	@Override
	public void initGui() {
		if(worldId != null) {
			System.out.println();
		}

		System.out.println();
	}
	
//	@Override
//	public void onGuiClosed() {
//		
//	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		CodeDefins.worldOptionAction(button.id, this, worldId);
	}
	
	
}
