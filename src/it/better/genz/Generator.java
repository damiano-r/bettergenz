package it.better.genz;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;


public class Generator {

    private Block block;
    private Block toGenerate;
    private String where;
    private Location whereLocation;
    private String type;
    private boolean autobreak;
    private World world;
    private int color;
    private long timer;
    private String ownerName;
    private BukkitTask task;
    private int taskID = -1;
    private Island is;
    private Generator generator;
    private double x;
    private double y;
    private double z;


    Generator(Block _block, String _type){
        this.block = _block;
        this.type = _type;
        this.is = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
        this.ownerName = is.getOwner().getName();
        this.generator = this;
        this.timer = Main.getInstance().getGeneratorTime(type);
        this.autobreak = false;
        this.where = null;
        this.taskID = -1;

        this.world = block.getWorld();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public String getOwnerName(){ return ownerName; }

    public void setOwnerName(String _ownerName){ this.ownerName = _ownerName;}

    public Island getIsland() { return this.is; }

    public Block getBlock(){ return this.block; }

    public void setAutobreak( boolean value ) { autobreak = value; }

    public String getType() { return this.type; }

    public void setWhere(String _where) { this.where = _where; }

    public String getWhere(){ return where; }

    public void setToGenerate(Block _toGenerate) { this.toGenerate = _toGenerate; }

    public Location getLocation(){ return block.getLocation(); }

    public int getTaskID(){ return taskID; }

    public String getString(){
        Location l = getLocation();
        return l.getBlockX() + "/" + l.getBlockY() + "/" + l.getBlockZ() + "/" + type + "/" + where;
    }

    public ItemStack getItemStack(){
        ItemStack is = new ItemStack(Main.getInstance().getGeneratorsMaterial().get(type), 1);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.AQUA + "Generatore " + Utilities.getByTypeAndColor(type));
        is.setItemMeta(isMeta);
        return is;
    }

    public void checkWhereToGenerate(){
        Location up = new Location(world, x, y + 1, z);
        Location down = new Location(world, x, y - 1, z);

        if(autobreak){

            toGenerate = world.getBlockAt(up);
            where = "up";

        }else{
            if (world.getBlockAt(down).getType().equals(Material.AIR)){
                toGenerate = world.getBlockAt(down);
                where = "down";
            }
            else {
                toGenerate = world.getBlockAt(up);
                where = "up";
            }
        }
    }

    public void checkAutoBreakEnabled(List<String> list){
        for(String genType : list){
            if (genType.equalsIgnoreCase(type)) {
                autobreak = true;
                break;
            }
        }
    }

    public void start(){
        //Main.getInstance().getLogger().info("X: " + x + " Y: " + y + " Z: "+ z + " - - - TaskID: " + taskID);
        if(taskID == -1) {
            task = new BukkitRunnable() {
                @Override
                public void run() {

                    if(autobreak){
                        if(toGenerate.getType().equals(Material.CHEST)){
                            OreBreakEvent oreBreakEvent = new OreBreakEvent(toGenerate);
                            Bukkit.getPluginManager().callEvent(oreBreakEvent);
                        } else{
                            if(!GeneratorEvents.listofchestless.containsKey(toGenerate.getLocation())) {
                                Bukkit.getPlayer(ownerName).sendMessage(ChatColor.WHITE +""+ ChatColor.BOLD + "Better" + ChatColor.GOLD + ChatColor.BOLD + "Generators" + ChatColor.GRAY + "> " + ChatColor.RED + "Chest non trovata per il Generatore" + type + " ad X: " + (int)x + " Y: " + (int)y + " Z: " + (int)z);
                                GeneratorEvents.listofchestless.put(toGenerate.getLocation(), generator);
                                stop();
                            }
                        }
                    } else{
                        OreGenerateEvent oreGenerateEvent = new OreGenerateEvent(toGenerate);
                        Bukkit.getPluginManager().callEvent(oreGenerateEvent);
                    }

                }
            }.runTaskTimer(Main.getInstance(), timer, timer);
            taskID = task.getTaskId();
        } else Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "Better" + ChatColor.GOLD + "Generators > " + ChatColor.RED + "duplicateTaskError: x: " + x + " y: " + y + " z: " + z + " tried to start again!");
    }

    public void stop(){
        task.cancel();
        taskID = -1;
    }
}
