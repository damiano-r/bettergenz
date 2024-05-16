package it.better.genz;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OreGenerateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Block block;

    public OreGenerateEvent(Block b){
        this.block = b;
    }

    @Override
    public HandlerList getHandlers() { return HANDLERS ; }

    public static HandlerList getHandlerList() { return HANDLERS; }

    public Block getBlock() { return block; }

}
