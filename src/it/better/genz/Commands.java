package it.better.genz;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Commands implements CommandExecutor {
    private Main plugin;
    public Commands(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        boolean power = false;
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if (p.isOp() || p.hasPermission("*")) {
                power = true;
            }
        } else if(sender instanceof ConsoleCommandSender)
            power = true;

        if(command.getName().equalsIgnoreCase("ctest")){
            if(power) {
                int i = 0;
                HashMap<String, Integer> hm = new HashMap<>();

                for (Generator g : GeneratorEvents.listofgenerators) {
                    if (g.getTaskID() > 0){
                        if(hm.containsKey(g.getOwnerName())){
                            i = hm.get(g.getOwnerName());
                            hm.put(g.getOwnerName(), i + 1);
                        } else
                            hm.put(g.getOwnerName(), 1);
                    }
                }

                if(sender instanceof Player) {
                    Player p = (Player) sender;
                    Island is = SuperiorSkyblockAPI.getPlayer(p).getIsland();
                    int counter = GeneratorEvents.membersCounter.get(is) == null ? 0 : GeneratorEvents.membersCounter.get(is).size();
                    p.sendMessage(String.valueOf(counter));
                    i = 0;
                    p.sendMessage("- Task attivi -");
                    for(Map.Entry<String, Integer> entry : hm.entrySet()){
                        p.sendMessage(entry.getKey() + ": " + entry.getValue());
                        i += entry.getValue();
                    }
                    p.sendMessage("Totale: " + i);
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "- Task attivi -");
                    i = 0;
                    for(Map.Entry<String, Integer> entry : hm.entrySet()){
                        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + entry.getKey() + ": " + ChatColor.GOLD + entry.getValue());
                        i += entry.getValue();
                    }
                    Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "Totale: " + i);
                }
            }

        } else if(command.getName().equalsIgnoreCase("bettergenz")){
            if(args.length != 0) {
                if (args[0].equalsIgnoreCase("give")) {
                    if (power) {
                        String msg;
                        if (args.length == 3) {
                            Player pp = Bukkit.getPlayer(args[1]);
                            ItemStack is = Utilities.getGenerator(args[2]);
                            pp.getInventory().addItem(is);
                            pp.sendMessage("§f§lBetter§6§lGenerators§7> §aHai ricevuto x1 " + is.getItemMeta().getDisplayName());
                        } else {
                            msg = "§f§lBetter§6§lGenerators§7> §4Usage: /bettergenz give <Player> <Type>";
                            if(sender instanceof Player) {
                                Player p = (Player) sender;
                                p.sendMessage(msg);
                            } else {
                                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "§f§lBetter§6§lGeneratorsZ§7> §4Usage: /bettergenz give <Player> <Type>");
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
