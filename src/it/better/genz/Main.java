package it.better.genz;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Main extends JavaPlugin {
    private static Main instance;
    private static HashMap<String, Long> generatorsTimer = new HashMap<>();
    private static HashMap<String, Material> generatorsMaterial = new HashMap<>();
    private static List<String> generatorsWithAutoBreak = new LinkedList<>();

    public void onEnable(){
        instance = this;
        loadConfiguration();
        getServer().getPluginManager().registerEvents(new GeneratorEvents(this), this);
        getCommand("ctest").setExecutor(new Commands(this));
        getCommand("bettergenz").setExecutor(new Commands(this));

        for(String type : getConfig().getConfigurationSection("Generatori").getKeys(false)){
            String str = getConfig().getString("Generatori." + type);
            String[] split = str.split(":");
            //getLogger().info("type: " + type + " str: " + str + " - split: " + Arrays.toString(split));
            generatorsMaterial.put(type, Utilities.getGeneratorMaterial(split[0]));
            generatorsTimer.put(type, Long.parseLong(split[1]));
        }

        if(getConfig().isSet("AutoBreak")){
            String[] split = getConfig().getString("AutoBreak").split(",");
            getLogger().info(Arrays.toString(split));
            for(String s : split){
                getLogger().info(s);
                generatorsWithAutoBreak.add(s);
            }
        }

        GeneratorEvents.updateMembersCounter();
        new BukkitRunnable(){

            @Override
            public void run() {
                loadGenerators();
            }
        }.runTaskLater(instance, 100L);
    }

    public void onDisable(){
        super.onDisable();
    }

    public HashMap<String, Material> getGeneratorsMaterial() { return generatorsMaterial; }

    public Long getGeneratorTime(String type) { return generatorsTimer.get(type); }

    public List<String> getGeneratorsWithAutoBreak() { return generatorsWithAutoBreak; }

    public void loadConfiguration(){
        this.saveDefaultConfig();
    }

    public static Main getInstance() { return instance; }

    private void loadGenerators(){

        File f = Utilities.getFile("data.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(f);
        HashMap<String, List<String>> broken = new HashMap<>();
        if(data.isSet("Generators")) {
            for(String player : data.getConfigurationSection("Generators").getKeys(false)){
                List<String> list = data.getStringList("Generators." + player);
                for (String s : list) {

                    String[] split = s.split("/");

                    World w = Bukkit.getWorld("SuperiorWorld");
                    int x = Integer.parseInt(split[0]);
                    int y = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    String where = split[4];

                    try {
                        Block block = w.getBlockAt(x, y, z);
                        //getLogger().info("Block: " + block.toString() + " - - chunk: " + block.getChunk().isLoaded());
                        if(Utilities.isGenerator(block)){
                            //getLogger().info("SONO UN GENERATORE");
                            Generator g = new Generator(block, split[3]);

                            if(generatorsWithAutoBreak.contains(g.getType())){
                                g.setAutobreak(true);
                            }

                            Block toGenerate;
                            if(where.equalsIgnoreCase("down")) {
                                toGenerate = w.getBlockAt(x, y - 1, z);
                                g.setWhere("down");
                            }
                            else {
                                toGenerate = w.getBlockAt(x, y + 1, z);
                                g.setWhere("up");
                            }
                            g.setToGenerate(toGenerate);


                            GeneratorEvents.listofgenerators.add(g);
                            if(GeneratorEvents.membersCounter != null) {
                                if (GeneratorEvents.membersCounter.containsKey(g.getIsland())) {
                                    //getLogger().info("members: " + GeneratorEvents.membersCounter.get(g.getIsland()).toString());
                                    if(GeneratorEvents.membersCounter.get(g.getIsland()) != null) {
                                        if (GeneratorEvents.membersCounter.get(g.getIsland()).size() > 0) {
                                            g.start();
                                        }
                                    }
                                }
                            }
                        } else{
                            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "isGeneratorERRORE NEL CARICAMENTO DI X:" + x + " Y: " + y + " Z: " + z);
                            List<String> dataList = new LinkedList<>();
                            if (broken.containsKey(player)){
                                dataList = broken.get(player);
                            }
                            dataList.add(s);
                            broken.put(player, dataList);
                        }

                    } catch(NullPointerException err) {
                        err.printStackTrace();
                        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "mainERRORE NEL CARICAMENTO DI X:" + x + " Y: " + y + " Z: " + z);
                        List<String> dataList = new LinkedList<>();
                        if (broken.containsKey(player)){
                            dataList = broken.get(player);
                        }
                        dataList.add(s);
                        broken.put(player, dataList);

                        //elimina il generatore dalla lista
                        //aggiusta tutto il codice del file per salvare nel nuovo formato x/y/z/type/color
                    }
                }
                if(broken.containsKey(player)){
                    for(String s : broken.get(player)){
                        String[] split = s.split("/");
                        int x = Integer.parseInt(split[0]);
                        int y = Integer.parseInt(split[1]);
                        int z = Integer.parseInt(split[2]);
                        list.remove(s);
                        data.set("Generators." + player, list);
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "mainRIMOSSO GENERATOR X: " + x + " Y:" + y + " Z: " + z);

                        try{
                            data.save(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loaded " + player + " generators! (" + list.size() + ")");
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded all generators! (" + GeneratorEvents.listofgenerators.size() + ")");
        }
    }
}
