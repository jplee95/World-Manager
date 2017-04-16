package jplee.worldmanager.command;

import java.util.Arrays;
import java.util.List;

import jplee.worldmanager.WorldManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandDebug extends CommandBase {

	@Override
	public String getCommandName() {
		return "showdebug";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[] { "toggledebug", "debug" });
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.debug.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			WorldManager.showDebug(!WorldManager.isDebugShowing());
		} else {
			notifyCommandListener(sender, this, "commands.wm.debug.usage", new Object[0]);
		}
	}

}
