package cn.nukkit.event.block;

import cn.nukkit.block.Block;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * Event for leaves Decay.
 * @author MagicDroidX
 */
public class LeavesDecayEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Event for leaves decaying.
     * @param block Leaves block. Contains Block data.
     */
    public LeavesDecayEvent(Block block) {
        super(block);
    }
}
