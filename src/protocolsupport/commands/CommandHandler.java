package protocolsupport.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import net.minecraft.server.v1_10_R1.PropertyManager;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.utils.Utils;

public class CommandHandler implements CommandExecutor, TabCompleter {

	private static final String DEBUG_PROPERTY = "debug";

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("protocolsupport.admin")) {
			sender.sendMessage(ChatColor.RED + "You have no power here!");
			return true;
		}
		if ((args.length == 1) && args[0].equalsIgnoreCase("list")) {
			for (ProtocolVersion version : ProtocolVersion.values()) {
				if (version.getName() != null) {
					sender.sendMessage(ChatColor.GOLD+"["+version.getName()+"]: "+ChatColor.GREEN+getPlayersStringForProtocol(version));
				}
			}
			return true;
		}
		if ((args.length == 1) && args[0].equalsIgnoreCase("debug")) {
			PropertyManager manager = Utils.getServer().getPropertyManager();
			if (!manager.getBoolean(DEBUG_PROPERTY, false)) {
				manager.setProperty(DEBUG_PROPERTY, Boolean.TRUE);
				sender.sendMessage(ChatColor.GOLD + "Enabled debug");
			} else {
				manager.setProperty(DEBUG_PROPERTY, Boolean.FALSE);
				sender.sendMessage(ChatColor.GOLD + "Disabled debug");
			}
			return true;
		}
		if ((args.length == 1) && args[0].equalsIgnoreCase("leakdetector")) {
			if (ResourceLeakDetector.isEnabled()) {
				ResourceLeakDetector.setLevel(Level.DISABLED);
				sender.sendMessage(ChatColor.GOLD + "Disabled leak detector");
			} else {
				ResourceLeakDetector.setLevel(Level.PARANOID);
				sender.sendMessage(ChatColor.GOLD + "Enabled leak detector");
			}
			return true;
		}
		return false;
	}

	private String getPlayersStringForProtocol(ProtocolVersion version) {
		StringBuilder sb = new StringBuilder();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ProtocolSupportAPI.getProtocolVersion(player) == version) {
				sb.append(player.getName());
				sb.append(", ");
			}
		}
		if (sb.length() > 2) {
			sb.delete(sb.length() - 2, sb.length());
		}
		return sb.toString();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		ArrayList<String> completions = new ArrayList<>();
		if ("list".startsWith(args[0])) {
			completions.add("list");
		}
		if ("debug".startsWith(args[0])) {
			completions.add("debug");
		}
		if ("leakdetector".startsWith(args[0])) {
			completions.add("leakdetector");
		}
		return completions;
	}

}
