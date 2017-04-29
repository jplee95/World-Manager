package jplee.worldmanager.command;

import java.util.Arrays;
import java.util.List;

import jplee.worldmanager.WorldManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandReloadConfig extends CommandBase {

	@Override
	public String getCommandName() {
		return "reloadconfig";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.<String>asList(new String[] { "reload" });
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.reloadconfig.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			WorldManager.reloadConfig(true);
			notifyCommandListener(sender, this, "commands.wm.reloadconfig.complete", new Object[0]);
		} else {
			notifyCommandListener(sender, this, "commands.wm.reloadconfig.usage", new Object[0]);
		}
	}

}
