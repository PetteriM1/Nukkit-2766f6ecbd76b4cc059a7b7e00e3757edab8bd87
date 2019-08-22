package cn.nukkit.utils.spawners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.passive.EntityTurtle;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.AbstractEntitySpawner;
import cn.nukkit.utils.SpawnResult;
import cn.nukkit.utils.Spawner;

public class TurtleSpawner extends AbstractEntitySpawner {

    public TurtleSpawner(Spawner spawnTask) {
        super(spawnTask);
    }

    public SpawnResult spawn(Player player, Position pos, Level level) {
        SpawnResult result = SpawnResult.OK;

        final int blockId = level.getBlockIdAt((int) pos.x, (int) pos.y, (int) pos.z);

        if (blockId != Block.WATER && blockId != Block.STILL_WATER) {
            result = SpawnResult.WRONG_BLOCK;
        } else if (level.getBiomeId((int) pos.x, (int) pos.z) != 0) {
            result = SpawnResult.WRONG_BIOME;
        } else if (pos.y > 255 || pos.y < 1) {
            result = SpawnResult.POSITION_MISMATCH;
        } else if (level.isNether || level.isEnd) {
            result = SpawnResult.WRONG_BIOME;
        } else {
            if (level.getBlock(pos.add(0, -1, 0)) instanceof BlockWater) {
                this.spawnTask.createEntity("Turtle", pos.add(0, -1, 0));
            } else {
                result = SpawnResult.POSITION_MISMATCH;
            }
        }

        return result;
    }

    @Override
    public final int getEntityNetworkId() {
        return EntityTurtle.NETWORK_ID;
    }
}
