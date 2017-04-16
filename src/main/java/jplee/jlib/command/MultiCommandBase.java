package jplee.jlib.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public abstract class MultiCommandBase extends CommandBase {

	private List<ICommand> subCommands;
	private List<String> subCommandNames;
	private Map<List<String>, Integer> subCommandIndex;

	public MultiCommandBase() {
		subCommands = Lists.newArrayList();
		subCommandNames = Lists.newArrayList();
		subCommandIndex = Maps.newHashMap();
		addSubCommands(subCommands);
		updateSubCommandIndex();
		updateSubCommandNames();
	}

	protected abstract void addSubCommands(List<ICommand> commands);

	public abstract void extExecute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;
	
	private void updateSubCommandIndex() {
		int i = 0;
		for(ICommand command : subCommands) {
			List<String> names = Lists.newArrayList();
			names.add(command.getCommandName());
			names.addAll(command.getCommandAliases());
			subCommandIndex.put(names, i++);
		}
	}
	
	private void updateSubCommandNames() {
		this.subCommands.forEach(command -> {
			subCommandNames.add(command.getCommandName());
			subCommandNames.addAll(command.getCommandAliases());
		});
		Collections.sort(subCommandNames);
	}
	
	public int getSubCommandIndex(String name) {
		for(List<String> names : subCommandIndex.keySet()) {
			if(names.contains(name))
				return subCommandIndex.get(names);
		}
		return -1;
	}
	
	public List<String> getSubCommandNames() {
		return subCommandNames;
	}

	public void helpExecute(MinecraftServer server, ICommandSender sender, String headerUnlocal, String[] args) throws CommandException {
		if(args.length > 0 && args[0].equals("help")) {
			if(sender instanceof CommandBlockBaseLogic) {
				sender.addChatMessage(new TextComponentString("There is nothing here to look at"));
			} else {
				List<ICommand> commands =  Lists.newArrayList(subCommands);
				Collections.sort(commands);
				
				int count = 7;
				int pages = (commands.size() - 1) / count;
				int page = 0;

				TextComponentTranslation texttranslation = new TextComponentTranslation(headerUnlocal, new Object[]
					{ Integer.valueOf(page + 1), Integer.valueOf(pages + 1) });
				texttranslation.getStyle().setColor(TextFormatting.DARK_GREEN);
				sender.addChatMessage(texttranslation);
				
				try {
					page = args.length == 1 ? 0 : parseInt(args[0], 1, pages + 1) - 1;
				} catch(NumberInvalidException e) {
					int index = getSubCommandIndex(args[1]);
					if(index != -1) {
						ICommand icommmand = subCommands.get(index);
						throw new WrongUsageException(icommmand.getCommandUsage(sender), new Object[0]);
					}
					if(MathHelper.parseIntWithDefault(args[1], -1) != -1){
						throw e;
					}
					throw new CommandNotFoundException();
				}
				int l = Math.min((page + 1) * count, commands.size());
				for(int i = page * count; i < l; i++) {
					ICommand icommand = commands.get(i);
					TextComponentTranslation texttranslation1 = new TextComponentTranslation(icommand.getCommandUsage(sender), new Object[0]);
					texttranslation1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + icommand.getCommandName() + " "));
					sender.addChatMessage(texttranslation1);
				}
				if(page == 0 && sender instanceof EntityPlayer) {
					TextComponentTranslation texttranslation1 = new TextComponentTranslation("commands.help.footer", new Object[0]);
					texttranslation1.getStyle().setColor(TextFormatting.GREEN);
					sender.addChatMessage(texttranslation1);
				}
			}
		} else {
			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
		}
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		boolean executed = false;
		if(args.length >= 1) {
			String[] subArgs = new String[args.length - 1];
			for(int i = 0; i < subArgs.length; i++)
				subArgs[i] = args[i + 1];
			int index = getSubCommandIndex(args[0]);
			if(index != -1) {
				ICommand command = subCommands.get(index);
				if(!command.checkPermission(server, sender))
	                throw new CommandException("commands.generic.permission");
				command.execute(server, sender, subArgs);
				executed = true;
			}
		}
		if(!executed)
			extExecute(server, sender, args);
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
		BlockPos pos) {
		if(args.length == 1) {
			return getListOfStringsMatchingLastWord(args, getSubCommandNames());
		} if(args.length >= 2) {
			String[] subArgs = new String[args.length - 1];
			for(int i = 0; i < subArgs.length; i++)
				subArgs[i] = args[i + 1];
			return this.subCommands.get(getSubCommandIndex(args[0])).getTabCompletionOptions(server, sender, subArgs,
				pos);
			
		} else
			return Collections.<String>emptyList();
	}
}
