package it.better.genz;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GeneratorEvents implements Listener {
    private Main plugin;
    public GeneratorEvents(Main plugin) { this.plugin = plugin; }
    public static HashMap<Island, List<SuperiorPlayer>> membersCounter = new HashMap<>();
    public static List<Generator> listofgenerators = new LinkedList<>();
    public static HashMap<Location, Generator> listofchestless= new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block block = e.getBlock();
        ItemStack itemBlock = e.getItemInHand();

        if(listofchestless.containsKey(block.getLocation())){
            Generator chestlessGen = listofchestless.get(block.getLocation());
            chestlessGen.start();
            listofchestless.remove(block.getLocation());
            p.sendMessage("§f§lBetter§6§lGenerators§7> §aChest collegata!");
            return;
        }

        if(plugin.getGeneratorsMaterial().containsValue(block.getType()) && block.getWorld().getName().equals("SuperiorWorld")){
            if(itemBlock.getItemMeta().getDisplayName() != null) {
                //plugin.getLogger().info("displayName: " + itemBlock.getItemMeta().getDisplayName());
                if(itemBlock.getItemMeta().getDisplayName().contains("Generatore")) {
                   // plugin.getLogger().info("ok");
                    if (Utilities.hasPermissionToPlace(p)) {
                        Block placed = e.getBlockPlaced();
                        if(membersCounter.get(SuperiorSkyblockAPI.getIslandAt(placed.getLocation())) != null || p.hasPermission("*")) {
                            if (membersCounter.get(SuperiorSkyblockAPI.getIslandAt(placed.getLocation())).contains(SuperiorSkyblockAPI.getPlayer(p)) || p.hasPermission("*")) {

                                String type = null;
                                for(Map.Entry<String, Material> entry : plugin.getGeneratorsMaterial().entrySet()){
                                    if(entry.getValue().equals(block.getType()))
                                        type = entry.getKey();
                                }
                                if (type != null) {
                                    Generator generator = new Generator(placed, type);

                                    BlockFace targetFace;
                                    switch (Utilities.getPlayerDirection(p)) {
                                        case "N":
                                            targetFace = BlockFace.NORTH;
                                            break;
                                        case "E":
                                            targetFace = BlockFace.EAST;
                                            break;
                                        case "W":
                                            targetFace = BlockFace.WEST;
                                            break;
                                        case "S":
                                            targetFace = BlockFace.SOUTH;
                                            break;
                                        default:
                                            throw new IllegalStateException("Unexpected value: " + Utilities.getPlayerDirection(p));
                                    }

                                    //plugin.getLogger().info("targetFace: " + Utilities.getPlayerDirection(p));
                                    Location signLocation = getSignLocation(placed, targetFace);

                                    if (!Utilities.existsGeneratorOn(SuperiorSkyblockAPI.getIslandAt(signLocation), signLocation)) {
                                        p.getWorld().getBlockAt(signLocation).setType(Material.OAK_WALL_SIGN);
                                        Block signBlock = p.getWorld().getBlockAt(signLocation);

                                        if (!Utilities.existsSign(signBlock)) {
                                            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlock.getState();
                                            org.bukkit.block.data.type.WallSign wallSign = (org.bukkit.block.data.type.WallSign) sign.getBlockData();
                                            wallSign.setFacing(targetFace.getOppositeFace());

                                            signBlock.getState().setData(new MaterialData(Material.OAK_WALL_SIGN));

                                            String top = "&b&lGeneratore";
                                            String bottom = Utilities.getByTypeAndColor(type);

                                            sign.setLine(1, ChatColor.translateAlternateColorCodes('&', top));
                                            sign.setLine(2, ChatColor.translateAlternateColorCodes('&', bottom));
                                            sign.setBlockData(wallSign);
                                            sign.update();


                                            File f = Utilities.getFile("data.yml");
                                            YamlConfiguration data = YamlConfiguration.loadConfiguration(f);

                                            listofgenerators.add(generator);
                                            generator.checkAutoBreakEnabled(plugin.getGeneratorsWithAutoBreak());
                                            generator.checkWhereToGenerate();
                                            generator.start();

                                            List<String> list = new LinkedList<>();
                                            if (data.isSet("Generators." + generator.getOwnerName()))
                                                list = data.getStringList(("Generators." + generator.getOwnerName()));
                                            list.add(generator.getString());
                                            data.set("Generators." + generator.getOwnerName(), list);

                                            try {
                                                data.save(f);
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                        } else {
                                            p.sendMessage("§f§lBetter§6§lGenerators§7> §cNon puoi piazzare qui il generatore! Err: 0x2");
                                            e.setCancelled(true);
                                        }
                                    } else {
                                        p.sendMessage("§f§lBetter§6§lGenerators§7> §cNon puoi piazzare qui il generatore! Err: 0x1");
                                        e.setCancelled(true);
                                    }
                                } else
                                    p.sendMessage("§f§lBetter§6§lGenerators§7> §eQuesto generatore non è valido! Err: 0x2");
                            } else {
                                p.sendMessage("§f§lBetter§6§lGenerators§7> §cNon hai i permessi per piazzare!");
                                e.setCancelled(true);
                            }
                        } else {
                            p.sendMessage("§f§lBetter§6§lGenerators§7> §cNon hai i permessi per piazzare!");
                            e.setCancelled(true);
                        }
                    } else
                        e.setCancelled(true);
                } else
                    p.sendMessage("§f§lBetter§6§lGenerators§7> §eQuesto generatore non è valido! Err: 0x1");
            }
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Block b = e.getBlock();
        Player p = e.getPlayer();

        if(b.getType().equals(Material.OAK_WALL_SIGN)){
            Sign sign = (Sign) b.getState();
            if(ChatColor.stripColor(sign.getLine(1)).equals("Generatore"))
                e.setCancelled(true);
        }
        Island island = SuperiorSkyblockAPI.getIslandAt(b.getLocation());
        if(island != null){
            if(membersCounter.get(island) != null) {
                if (membersCounter.get(island).contains(SuperiorSkyblockAPI.getPlayer(p)) || p.hasPermission("*")) {
                    if (plugin.getGeneratorsMaterial().containsValue(b.getType()) && b.getWorld().getName().equals("SuperiorWorld")) {
                        plugin.getLogger().info("BLOCKBREAKTRIGGERED ISOLA DI ( " + island.getOwner().getName() + " )");
                        Generator brokenGenerator = null;
                        for (Generator g : listofgenerators) {
                            if (g.getLocation().equals(b.getLocation())) {
                                plugin.getLogger().info("TROVATO GENERATORE ( " + island.getOwner().getName() + ")  AD X: " + g.getLocation().getBlockX() + " Y: " + g.getLocation().getBlockY() + " Z: " + g.getLocation().getBlockZ());
                                brokenGenerator = g;
                                break;
                            }
                        }
                        if (brokenGenerator != null) {

                            listofgenerators.remove(brokenGenerator);

                            brokenGenerator.stop();

                            File f = Utilities.getFile("data.yml");
                            YamlConfiguration data = YamlConfiguration.loadConfiguration(f);

                            List<String> list = data.getStringList("Generators." + brokenGenerator.getOwnerName());

                            //plugin.getLogger().info("broken: " + brokenGenerator.getString());
                            list.remove(brokenGenerator.getString());
                            data.set("Generators." + brokenGenerator.getOwnerName(), list);
                            plugin.getLogger().info("GENERATORE RIMOSSO ( " + island.getOwner().getName() + ") AD X: " + brokenGenerator.getLocation().getBlockX() + " Y: " + brokenGenerator.getLocation().getBlockY() + " Z: " + brokenGenerator.getLocation().getBlockZ());

                            try {
                                data.save(f);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            for (BlockFace faces : BlockFace.values()) {
                                if (b.getRelative(faces).getType().equals(Material.OAK_WALL_SIGN)) {
                                    Sign sign = (Sign) b.getRelative(faces).getState();
                                    if (ChatColor.stripColor(sign.getLine(1)).equals("Generatore"))
                                        b.getRelative(faces).setType(Material.AIR);
                                    break;
                                }
                            }
                            e.setDropItems(false);
                            ItemStack is = Utilities.getGenerator(brokenGenerator.getType());
                            Bukkit.getWorld(b.getWorld().getName()).dropItem(b.getLocation(), is);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void onIslandEnter(IslandEnterEvent e){
        SuperiorPlayer superiorPlayer = e.getPlayer();
        Island is = e.getIsland();
        if(is != null) {
            if (is.equals(superiorPlayer.getIsland())) {
                List<SuperiorPlayer> superiorPlayerList = new LinkedList<>();
                if (membersCounter.get(is) != null)
                    superiorPlayerList = membersCounter.get(is);

                if(!superiorPlayerList.contains(superiorPlayer))
                    superiorPlayerList.add(superiorPlayer);

                membersCounter.put(is, superiorPlayerList);

                if (membersCounter.get(is).size() == 1) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "ieStarting generators.. ( " + is.getOwner().getName() + " )");
                    startGenerators(superiorPlayer.getIsland().getOwner().getName());
                    Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "ieGenerators started! ( " + is.getOwner().getName() + " )");
                }
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "isenterMembers (" + superiorPlayer.getIsland().getOwner().getName() + "): "+ membersCounter.get(superiorPlayer.getIsland()).size() + " - membri: "  + membersCounter.get(superiorPlayer.getIsland()));
            }
        }
    }

    @EventHandler
    public void onLeaveIsland(IslandLeaveEvent e){
        plugin.getLogger().info("ISLANDLEAVE TRIGGERATO");
        SuperiorPlayer superiorPlayer = e.getPlayer();
        if(superiorPlayer.getIsland() != null){
            Island is = superiorPlayer.getIsland();
            if(e.getIsland().equals(is)) {
                if (membersCounter.get(is) != null) {
                    List<SuperiorPlayer> superiorPlayerList = membersCounter.get(is);
                    superiorPlayerList.remove(superiorPlayer);
                    membersCounter.put(is, superiorPlayerList);

                    if (membersCounter.get(is).size() == 0) {
                        membersCounter.remove(is);
                        stopGenerators(is.getOwner().getName());
                    }
                    int counter;
                    if(membersCounter.get(is) != null)
                        counter = membersCounter.get(is).size();
                    else
                        counter = 0;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "isleaveCOUNTER ( " + is.getOwner().getName() + " ): " + counter);
                }

            }
        }
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        plugin.getLogger().info("WORLDCHANGE TRIGGERATO - " + e.getFrom().getName());
        Player p = e.getPlayer();
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(p);
        if(superiorPlayer.getIsland() != null) {
            if (e.getFrom().getName().equals("SuperiorWorld")) {
                Island is = superiorPlayer.getIsland();
                if(membersCounter.get(is) != null){
                    List<SuperiorPlayer> superiorPlayerList = membersCounter.get(is);
                    superiorPlayerList.remove(superiorPlayer);
                    membersCounter.put(is, superiorPlayerList);

                    if (membersCounter.get(is).size() == 0) {
                        membersCounter.remove(is);
                        stopGenerators(is.getOwner().getName());
                    }
                    int counter;
                    if(membersCounter.get(is) != null)
                        counter = membersCounter.get(is).size();
                    else
                        counter = 0;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "worldchangeCOUNTER ( " + is.getOwner().getName() + " ): " + counter);
                }
            } else if(e.getFrom().getName().equals("SuperiorWorld_nether")){
                Island is = SuperiorSkyblockAPI.getIslandAt(superiorPlayer.getLocation());
                if(is != null) {
                    if (is.equals(superiorPlayer.getIsland())) {
                        List<SuperiorPlayer> superiorPlayerList = new LinkedList<>();
                        if (membersCounter.get(is) != null)
                            superiorPlayerList = membersCounter.get(is);

                        if(!superiorPlayerList.contains(superiorPlayer))
                            superiorPlayerList.add(superiorPlayer);

                        membersCounter.put(is, superiorPlayerList);

                        if (membersCounter.get(is).size() == 1) {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "wcStarting generators.. ( " + is.getOwner().getName() + " )");
                            startGenerators(superiorPlayer.getIsland().getOwner().getName());
                            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "wcGenerators started! ( " + is.getOwner().getName() + " )");
                        }
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "worldchangeMembers (" + superiorPlayer.getIsland().getOwner().getName() + "): " + membersCounter.get(superiorPlayer.getIsland()).size() + " - membri: " + membersCounter.get(superiorPlayer.getIsland()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLeaderChange(IslandTransferEvent e){
        plugin.getLogger().info("LEADERCHANGE TRIGGERATO - old:" + e.getOldOwner().getName() + " - new:" + e.getNewOwner().getName());
        File f = Utilities.getFile("data.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(f);

        if(data.isSet("Generators." + e.getOldOwner().getName())){
            List<String> list = data.getStringList("Generators." + e.getOldOwner().getName());
            data.set("Generators." + e.getOldOwner().getName(), null);

            data.set("Generators." + e.getNewOwner().getName(), list);

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "leaderChanged (" + e.getOldOwner() + ") - ( " + e.getNewOwner() + ")");
            for(String s : list){
                for(Generator g : listofgenerators){
                    if(g.getString().equals(s)) {
                        g.setOwnerName(e.getNewOwner().getName());
                        break;
                    }
                }
            }

            try {
                data.save(f);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onIslandDisband(IslandDisbandEvent e){
        plugin.getLogger().info("ISDISBAND TRIGGERATO - " + e.getPlayer().getName());
        File f = Utilities.getFile("data.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(f);
        String player = e.getIsland().getOwner().getName();
        if(data.isSet("Generators." + player)){
            stopGenerators(player);
            deleteGenerators(player);
            membersCounter.remove(e.getIsland());
            data.set("Generators." + player, null);

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "isDisband (" + player + ")");
            try {
                data.save(f);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(p);
        if(superiorPlayer.getIsland() != null) {
            List<SuperiorPlayer> superiorPlayerList = new LinkedList<>();
            superiorPlayerList.add(superiorPlayer);
            membersCounter.putIfAbsent(superiorPlayer.getIsland(), superiorPlayerList);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(p);
        if(superiorPlayer.getIsland() != null){
            Island is = superiorPlayer.getIsland();
            if(membersCounter.get(is) != null) {
                List<SuperiorPlayer> superiorPlayerList = membersCounter.get(is);
                superiorPlayerList.remove(superiorPlayer);
                membersCounter.put(is, superiorPlayerList);

                if (membersCounter.get(is).size() == 0) {
                    membersCounter.remove(is);
                    stopGenerators(is.getOwner().getName());
                }
                int counter;
                if(membersCounter.get(is) != null)
                    counter = membersCounter.get(is).size();
                else
                    counter = 0;
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "isquitCOUNTER ( " + is.getOwner().getName() + " ): " + counter);
            }
        }
    }

    private void startGenerators(String player){
        for(Generator g : listofgenerators){
            if(g.getOwnerName().equalsIgnoreCase(player)) {
                g.start();
            }
        }
    }

    private void stopGenerators(String player){
        for(Generator g : listofgenerators){
            if(g.getOwnerName().equalsIgnoreCase(player))
                if(g.getTaskID() != -1)
                    g.stop();
        }
    }

    private void deleteGenerators(String player){
        listofgenerators.removeIf(g -> g.getOwnerName().equalsIgnoreCase(player));
    }

    private Location getSignLocation(Block b, BlockFace targetFace){
        double x = b.getX();
        double y = b.getY();
        double z = b.getZ();

        if (targetFace.equals(BlockFace.NORTH))
            z += 1;
        else if (targetFace.equals(BlockFace.EAST))
            x -= 1;
        else if (targetFace.equals(BlockFace.SOUTH))
            z -= 1;
        else if (targetFace.equals(BlockFace.WEST))
            x += 1;

        return new Location(b.getWorld(), x, y, z);
    }

    private static boolean isAtLeastOneOnline(Block b){
        Island island = SuperiorSkyblockAPI.getIslandAt(b.getLocation());
        List<SuperiorPlayer> members = island.getIslandMembers(true);
        for(SuperiorPlayer superiorPlayer : members)
            if(superiorPlayer.isOnline())
                return true;
        return false;
    }

    public static void updateMembersCounter(){
        for(Player all : Bukkit.getOnlinePlayers()){
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(all);
            Island island = superiorPlayer.getIsland();
            if(SuperiorSkyblockAPI.getIslandAt(all.getLocation()) != null){
                if(SuperiorSkyblockAPI.getIslandAt(all.getLocation()).getIslandMembers(true).contains(superiorPlayer)){
                    List<SuperiorPlayer> superiorPlayerList = new LinkedList<>();
                    if (membersCounter.get(island) != null)
                        superiorPlayerList = membersCounter.get(island);

                    if(!superiorPlayerList.contains(superiorPlayer))
                        superiorPlayerList.add(superiorPlayer);

                    membersCounter.put(island, superiorPlayerList);
                }

            }
            else
                membersCounter.putIfAbsent(island, null);
        }
        //Main.getInstance().getLogger().info("memberscounter: " + membersCounter.toString());
    }
    private String Color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent e){

        for (Block b : e.getBlocks()) {
            if(plugin.getGeneratorsMaterial().containsValue(b.getType()) && b.getWorld().getName().equals("SuperiorWorld")) {
                for (Generator g : listofgenerators) {
                    if (b.getLocation().equals(g.getLocation())) {
                        e.setCancelled(true);
                    }
                }
            }
        }

    }

    @EventHandler
    public void onBlockExplode(EntityExplodeEvent e){
        for (Block b : e.blockList()) {
            if(plugin.getGeneratorsMaterial().containsValue(b.getType()) && b.getWorld().getName().equals("SuperiorWorld")) {
                for (Generator g : listofgenerators) {
                    if (b.getLocation().equals(g.getLocation())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

}
