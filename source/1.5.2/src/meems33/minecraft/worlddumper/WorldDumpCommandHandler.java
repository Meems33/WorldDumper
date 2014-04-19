package meems33.minecraft.worlddumper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import meems33.minecraft.worlddumper.common.ItemInfoDumper;
import meems33.minecraft.worlddumper.common.WorldDumperUtils;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * 
 * Commands:
 * worlddump item
 * worlddump world [<mod>|all] [<x> <y> <z>]
 * 
 * @author Mark
 *
 */
public class WorldDumpCommandHandler implements ICommand {
	// Needs to be in order!
	private List<String> subCommands0 = Arrays.asList( "items", "listmods", "world" );
	private static final String USAGE_LISTMODS = "worlddump listmods";
	private static final String USAGE_ITEMS = "worlddump items";
	private static final String USAGE_WORLD_1 = "worlddump world <mod_name|ALL>";
	private static final String USAGE_WORLD_2 = "worlddump world <mod_name|ALL> <x> <y> <z>";
	
	@Override
	public int compareTo(Object par1Obj) {
		return this.getCommandName().compareTo(((ICommand)par1Obj).getCommandName());
	}

	@Override
	public String getCommandName() {
		return "worlddump";
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		var1.sendChatToPlayer(USAGE_ITEMS);
		var1.sendChatToPlayer(USAGE_LISTMODS);
		var1.sendChatToPlayer(USAGE_WORLD_1);
		var1.sendChatToPlayer(USAGE_WORLD_2);
		
		return "";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList("worlddump", "wd");
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		if (var2 != null && var2.length > 0) {
			String[] pseudoArgs = WorldDumperUtils.getPseudoArgs(var2);
			
			if ("listmods".equalsIgnoreCase(pseudoArgs[0])) {
				List<String> modList = WorldDumper.getItemInfoHelper().getModsWithItems();
				
				var1.sendChatToPlayer("Mods: " + modList);
			} else if ("world".equalsIgnoreCase(pseudoArgs[0])) {
				String mod;
				int chunkXBase, chunkZBase, yBase;
				
				if (pseudoArgs.length == 2) {
					mod = pseudoArgs[1];
					chunkXBase = var1.getPlayerCoordinates().posX / 16;
					chunkZBase = var1.getPlayerCoordinates().posZ / 16;
					yBase = var1.getPlayerCoordinates().posY;
				} else if (pseudoArgs.length == 5) {
					mod = pseudoArgs[1];
					try {
						chunkXBase = Integer.parseInt(pseudoArgs[2]) / 16;
						chunkZBase = Integer.parseInt(pseudoArgs[4]) / 16;
						yBase = Integer.parseInt(pseudoArgs[3]);
					} catch (NumberFormatException e) {
						printUsage(var1, USAGE_WORLD_2);
						return;
					}
				} else {
					printUsage(var1, USAGE_WORLD_2);
					return;
				}
				
				// Verify mod exists
				if (!WorldDumpGenerator.ALL_MODS.equalsIgnoreCase(mod) 
						&& !WorldDumperUtils.containsCaseInsensitive(WorldDumper.getItemInfoHelper().getModsWithItems(), mod)) {
					var1.sendChatToPlayer("Invalid mod name: " + mod + ". To see a list of mods type /worlddump listmods");
					return;
				}
				
				// TODO make work in diff dimensions?
				World world = DimensionManager.getWorld(0);
				
				EntityPlayer proxyPlayer = world.getPlayerEntityByName(var1.getCommandSenderName());
				
				var1.sendChatToPlayer("Attempting to generate world dump. Please be patient...");
				
				new WorldDumpGenerator(WorldDumper.getItemInfoHelper(), proxyPlayer)
					.generate(world, mod, chunkXBase, yBase, chunkZBase);
			} else if ("item".equalsIgnoreCase(pseudoArgs[0])
					|| "items".equalsIgnoreCase(pseudoArgs[0])) {
				World world = DimensionManager.getWorld(0);
				
				String fileName = "ItemDump_";
				fileName += world.getWorldInfo().getWorldName();
				fileName += "_";
				fileName += WorldDumperUtils.DATE_FORMATER.format(Calendar.getInstance().getTime());
				fileName += ".csv";
				
				new ItemInfoDumper(fileName).doDump(WorldDumper.getItemInfoHelper());
			} else {
				printUsage(var1, null);
				return;
			}
		} else {
			printUsage(var1, null);
			return;
		}
	}
	
	private void printUsage(ICommandSender var1, String usage) {
		if (usage == null) {
			var1.sendChatToPlayer("Invalid command entered.");
		} else {
			var1.sendChatToPlayer(usage);
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender var1) {
		return MinecraftServer.getServer().getConfigurationManager()
				.areCommandsAllowed(var1.getCommandSenderName());
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender var1, String[] var2) {
		List<String> rt = new ArrayList<String>();
		String[] pseudoArgs = WorldDumperUtils.getPseudoArgs(var2);
		
		int level = (var2 != null && var2.length > 0)? pseudoArgs.length - 1 : -1;
		String fringe = (var2 != null && var2.length > 0)? pseudoArgs[pseudoArgs.length - 1] : null;
		
		if (level == 0) {
			rt.addAll(WorldDumperUtils.getPotentialMatches(subCommands0, fringe));
		} else if (level == 1) {
			if ("world".equalsIgnoreCase(var2[0])) {
				List<String> modList = WorldDumper.getItemInfoHelper().getModsWithItems();
				
				rt.addAll(WorldDumperUtils.getPotentialMatches(modList, fringe));
			}
		}
		
		return rt;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}
}
