package cn.nukkit.event.block;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

/**
 * Event for Block fire (Ignite)
 */
public class BlockIgniteEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final Block source;
    private final Entity entity;
    private final BlockIgniteCause cause;

    /**
     * Block ignite event is called when a block is ignited (lit on fire).
     * @param block Block that has been affected (ignited).
     * @param source Block source of ignition.
     * @param entity Block ignite entity.
     * @param cause Cause of block ignition.
     */
    public BlockIgniteEvent(Block block, Block source, Entity entity, BlockIgniteCause cause) {
        super(block);
        this.source = source;
        this.entity = entity;
        this.cause = cause;
    }

    public Block getSource() {
        return source;
    }

    public Entity getEntity() {
        return entity;
    }

    public BlockIgniteCause getCause() {
        return cause;
    }

    public enum BlockIgniteCause {
        EXPLOSION,
        FIREBALL,
        FLINT_AND_STEEL,
        LAVA,
        LIGHTNING,
        SPREAD
    }
}
