package jplee.worldmanager.command;

import java.util.Arrays;
import java.util.List;

import jplee.jlib.command.MultiCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandWorldManager extends MultiCommandBase {

	public CommandWorldManager() {
		super();
	}
	
	@Override
	public String getCommandName() {
		return "worldmanager";
	}
	
	@Override
	public List<String> getCommandAliases() {
		return Arrays.<String>asList(new String[] { "wm" });
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.wm.usage";
	}

	@Override
	protected void addSubCommands(List<ICommand> commands) {
		commands.add(new CommandProcessChunk());
		commands.add(new CommandReloadConfig());
		commands.add(new CommandDebug());
		commands.add(new CommandGetState());
		commands.add(new CommandGetOredict());
	}

	@Override
	public void extExecute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		helpExecute(server, sender, "commands.wm.help.header", args);
	}
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
		BlockPos pos) {
		List<String> completions = super.getTabCompletionOptions(server, sender, args, pos);
		if(args.length == 1 && "help".startsWith(args[0]))
			completions.add("help");
		return completions;
	}
}
