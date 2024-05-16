package it.better.genz;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static File getFile(String name) { return getFile(null, name); }

    public static File getFile(File parent, String name) {
        if (parent == null) parent = Main.getInstance().getDataFolder();
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("File name cannot be null or empty");
        if (!name.toLowerCase().endsWith(".yml")) name = String.valueOf(name) + ".yml";
        File file = new File(parent, name);
        if (!file.exists())
            try { file.createNewFile(); }
            catch (IOException e) { e.printStackTrace(); }

        return file;
    }

    public static String getByTypeAndColor(String type){
        ChatColor blockColor = null;
        Material m = Main.getInstance().getGeneratorsMaterial().get(type);
        switch(m){
            case WHITE_TERRACOTTA:
                blockColor = ChatColor.WHITE;
                break;
            case ORANGE_TERRACOTTA:
                blockColor = ChatColor.GOLD;
                break;
            case MAGENTA_TERRACOTTA:
                blockColor = ChatColor.LIGHT_PURPLE;
                break;
            case LIGHT_BLUE_TERRACOTTA:
                blockColor = ChatColor.AQUA;
                break;
            case YELLOW_TERRACOTTA:
                blockColor = ChatColor.YELLOW;
                break;
            case LIME_TERRACOTTA:
                blockColor = ChatColor.GREEN;
                break;
            case PINK_TERRACOTTA:
                blockColor = ChatColor.RED;
                break;
            case GRAY_TERRACOTTA:
                blockColor = ChatColor.DARK_GRAY;
                break;
            case LIGHT_GRAY_TERRACOTTA:
                blockColor = ChatColor.GRAY;
                break;
            case CYAN_TERRACOTTA:
                blockColor = ChatColor.DARK_AQUA;
                break;
            case PURPLE_TERRACOTTA:
                blockColor = ChatColor.DARK_PURPLE;
                break;
            case BLUE_TERRACOTTA:
                blockColor = ChatColor.BLUE;
                break;
            case BROWN_TERRACOTTA:
                blockColor = ChatColor.DARK_RED;
                break;
            case GREEN_TERRACOTTA:
                blockColor = ChatColor.DARK_GREEN;
                break;
            case RED_TERRACOTTA:
                blockColor = ChatColor.DARK_RED;
                break;
            case BLACK_TERRACOTTA:
                blockColor = ChatColor.BLACK;
                break;
        }
        return blockColor + type;
    }

    public static Material getGeneratorMaterial(String color){
        Material m = null;
        switch(color){
            case "0":
                m = Material.WHITE_TERRACOTTA;
                break;
            case "1":
                m = Material.ORANGE_TERRACOTTA;
                break;
            case "2":
                m = Material.MAGENTA_TERRACOTTA;
                break;
            case "3":
                m = Material.LIGHT_BLUE_TERRACOTTA;
                break;
            case "4":
                m = Material.YELLOW_TERRACOTTA;
                break;
            case "5":
                m = Material.LIME_TERRACOTTA;
                break;
            case "6":
                m = Material.PINK_TERRACOTTA;
                break;
            case "7":
                m = Material.GRAY_TERRACOTTA;
                break;
            case "8":
                m = Material.LIGHT_GRAY_TERRACOTTA;
                break;
            case "9":
                m = Material.CYAN_TERRACOTTA;
                break;
            case "10":
                m = Material.PURPLE_TERRACOTTA;
                break;
            case "11":
                m = Material.BLUE_TERRACOTTA;
                break;
            case "12":
                m = Material.BROWN_TERRACOTTA;
                break;
            case "13":
                m = Material.GREEN_TERRACOTTA;
                break;
            case "14":
                m = Material.RED_TERRACOTTA;
                break;
            case "15":
                m = Material.BLACK_TERRACOTTA;
                break;
        }
        return m;
    }

    public static String getPlayerDirection(Player playerSelf){
        String dir = "";
        float y = playerSelf.getLocation().getYaw();
        if( y < 0 ){y += 360;}
        y %= 360;
        int i = (int)((y+8) / 22.5);
        //Main.getInstance().getLogger().info("i: " + i);
        if(i == 0){dir = "S";}
        else if(i == 1){dir = "S";}  //ovest nord ovest
        else if(i == 2){dir = "W";}   //nord ovest
        else if(i == 3){dir = "W";}  //nord nord ovest
        else if(i == 4){dir = "W";}    //nord
        else if(i == 5){dir = "N";}  //nord nord est
        else if(i == 6){dir = "N";}   //nord est
        else if(i == 7){dir = "N";}  //est nord est
        else if(i == 8){dir = "N";}    //est
        else if(i == 9){dir = "N";}  //est sud est
        else if(i == 10){dir = "E";}  // sud est
        else if(i == 11){dir = "E";} // sud sud est
        else if(i == 12){dir = "E";}   // sud
        else if(i == 13){dir = "E";} // sud sud ovest
        else if(i == 14){dir = "S";}  // sud ovest
        else if(i == 15){dir = "S";} // ovest sud ovest
        else {dir = "S";}              // ovest
        return dir;
    }

    public static boolean existsGeneratorOn(Island is, Location l){
        File f = Utilities.getFile("data.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(f);

        String[] split;
        if(data.isSet("Generators." + is.getOwner().getName())) {
            for (String s : data.getStringList("Generators." + is.getOwner().getName())) {
                split = s.split("/");

                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                int z = Integer.parseInt(split[2]);

                if (l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z) {
                    return true;
                }

            }
        }
        return false;
    }

    public static boolean existsSign(Block block){
        if(block.getType().equals(Material.OAK_WALL_SIGN)) {
            Sign sign = (Sign) block.getState();
            if(ChatColor.stripColor(sign.getLine(1)).equals("Generatore"))
                return true;
        }
        return false;
    }

    public static boolean isGenerator(Block block){
        if(Main.getInstance().getGeneratorsMaterial().containsValue(block.getType()) && block.getWorld().getName().equalsIgnoreCase("SuperiorWorld")){
            //Main.getInstance().getLogger().info("SONO UN BLOCCO DI CLAY E SONO DENTRO IL MONDO SUPERIORWORLD");
            for(BlockFace faces : BlockFace.values()){
                if(block.getRelative(faces).getType().equals(Material.OAK_WALL_SIGN)){
                    //Main.getInstance().getLogger().info("HO UN CARTELLO ATTACCATO AD UNA MIA FACCIA");
                    Sign sign = (Sign) block.getRelative(faces).getState();
                    if(ChatColor.stripColor(sign.getLine(1)).equals("Generatore")) {
                        //Main.getInstance().getLogger().info("HO LA SCRITTA GENERATORE");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ItemStack getGenerator(String type){
        /*
        File f = Utilities.getFile("config.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
        Main.getInstance().getLogger().info("type: " + type);
         */

        ItemStack is = new ItemStack(Main.getInstance().getGeneratorsMaterial().get(type), 1);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.AQUA + "Generatore " + Utilities.getByTypeAndColor(type));
        List<String> lore = Main.getInstance().getConfig().getStringList("Lore");
        List<String> coloredLore = new ArrayList<>();
        for(int k = 0; k < lore.size(); k++){
            coloredLore.add(Color(lore.get(k)));
        }
        isMeta.setLore(coloredLore);
        is.setItemMeta(isMeta);
        return is;
    }

    private static String Color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }

    public static boolean hasPermissionToPlace(Player p){
        boolean power = false;

        if(p.isOp() || p.hasPermission("*")){
            power = true;
        }

        return power;
    }

}
