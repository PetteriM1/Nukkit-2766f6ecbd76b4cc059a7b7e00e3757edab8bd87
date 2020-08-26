package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityDispenser;
import cn.nukkit.dispenser.DispenseBehavior;
import cn.nukkit.dispenser.DispenseBehaviorRegister;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Faceable;
import cn.nukkit.utils.Utils;

import java.util.Map.Entry;

/**
 * Created by CreeperFace on 15.4.2017.
 */
public class BlockDispenser extends BlockSolidMeta implements Faceable {

    public BlockDispenser() {
        this(0);
    }

    public BlockDispenser(int meta) {
        super(meta);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public String getName() {
        return "Dispenser";
    }

    @Override
    public int getId() {
        return DISPENSER;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0);
    }

    @Override
    public int getComparatorInputOverride() {
        BlockEntity blockEntity = this.level.getBlockEntity(this);

        if (blockEntity instanceof BlockEntityDispenser) {
            return ContainerInventory.calculateRedstone(((BlockEntityDispenser) blockEntity).getInventory());
        }

        return 0;
    }

    public boolean isTriggered() {
        return (this.getDamage() & 8) > 0;
    }

    public void setTriggered(boolean value) {
        int i = 0;
        i |= getBlockFace().getIndex();

        if (value) {
            i |= 8;
        }

        this.setDamage(i);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (player == null) {
            return false;
        }

        BlockEntity blockEntity = this.level.getBlockEntity(this);

        if (!(blockEntity instanceof BlockEntityDispenser)) {
            return false;
        }

        player.addWindow(((BlockEntityDispenser) blockEntity).getInventory());
        return true;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (player != null) {
            if (Math.abs(player.x - this.x) < 2 && Math.abs(player.z - this.z) < 2) {
                double y = player.y + player.getEyeHeight();

                if (y - this.y > 2) {
                    this.setDamage(BlockFace.UP.getIndex());
                } else if (this.y - y > 0) {
                    this.setDamage(BlockFace.DOWN.getIndex());
                } else {
                    this.setDamage(player.getHorizontalFacing().getOpposite().getIndex());
                }
            } else {
                this.setDamage(player.getHorizontalFacing().getOpposite().getIndex());
            }
        }

        this.getLevel().setBlock(block, this, true);

        BlockEntity.createBlockEntity(BlockEntity.DISPENSER, this.getChunk(), BlockEntity.getDefaultCompound(this, BlockEntity.DISPENSER));
        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            this.setTriggered(false);
            this.level.setBlock(this, this, false, false);

            dispense();
            return type;
        } else if (type == Level.BLOCK_UPDATE_REDSTONE) {
            if ((level.isBlockPowered(this) || level.isBlockPowered(this.up())) && !isTriggered()) {
                this.setTriggered(true);
                this.level.setBlock(this, this, false, false);
                level.scheduleUpdate(this, this, 4);
            }

            return type;
        }

        return 0;
    }

    public void dispense() {
        BlockEntity blockEntity = this.level.getBlockEntity(this);

        if (!(blockEntity instanceof BlockEntityDispenser)) {
            return;
        }

        int r = 1;
        int slot = -1;
        Item target = null;

        Inventory inv = ((BlockEntityDispenser) blockEntity).getInventory();
        for (Entry<Integer, Item> entry : inv.getContents().entrySet()) {
            Item item = entry.getValue();

            if (!item.isNull() && Utils.random.nextInt(r++) == 0) {
                target = item;
                slot = entry.getKey();
            }
        }

        if (target == null) {
            //this.level.addLevelSoundEvent(this); //TODO: sound
            return;
        }
        //Item origin = target;
        target = target.clone();

        DispenseBehavior behavior = DispenseBehaviorRegister.getBehavior(target.getId());
        Item result = behavior.dispense(this, getBlockFace(), target);

        if (result == null) {
            target.count--;
            inv.setItem(slot, target);
        } else {
            /*if (result.getId() != origin.getId() || result.getDamage() != origin.getDamage()) {
                Item[] fit = inv.addItem(result);

                if (fit.length > 0) {
                    for (Item drop : fit) {
                        this.level.dropItem(this, drop);
                    }
                }
            } else {
                inv.setItem(slot, result);
            }*/
            inv.setItem(slot, result);
        }
    }

    @Override
    public boolean canHarvestWithHand() {
        return true;
    }

    public Vector3 getDispensePosition() {
        BlockFace facing = getBlockFace();
        return this.add(
                0.5 + 0.7 * facing.getXOffset(),
                0.5 + 0.7 * facing.getYOffset(),
                0.5 + 0.7 * facing.getZOffset()
        );
    }

    @Override
    public BlockFace getBlockFace() {
        return BlockFace.fromIndex(this.getDamage() & 0x07);
    }

    @Override
    public double getHardness() {
        return 0.5;
    }
}
