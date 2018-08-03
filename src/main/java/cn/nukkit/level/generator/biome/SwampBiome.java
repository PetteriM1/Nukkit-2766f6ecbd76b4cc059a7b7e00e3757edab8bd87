package cn.nukkit.level.generator.biome;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFlower;
import cn.nukkit.block.BlockWaterStill;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.level.generator.populator.PopulatorFlower;
import cn.nukkit.level.generator.populator.PopulatorLake;
import cn.nukkit.level.generator.populator.PopulatorLilyPad;
import cn.nukkit.level.generator.populator.tree.SwampTreePopulator;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class SwampBiome extends GrassyBiome {

    public SwampBiome() {
        super();

        PopulatorLilyPad lilypad = new PopulatorLilyPad();
        lilypad.setBaseAmount(4);

        SwampTreePopulator trees = new SwampTreePopulator();
        trees.setBaseAmount(2);

        PopulatorFlower flower = new PopulatorFlower();
        flower.setBaseAmount(2);
        flower.addType(Block.RED_FLOWER, BlockFlower.TYPE_BLUE_ORCHID);
        
        PopulatorLake lakes = new PopulatorLake();
        lakes.setOreTypes(new OreType[]{
                new OreType(new BlockWaterStill(), 1, 40, 62, 65)
        });

        this.addPopulator(lakes);
        this.addPopulator(trees);
        this.addPopulator(flower);
        this.addPopulator(lilypad);

        this.setElevation(62, 63);

        this.temperature = 0.8;
        this.rainfall = 0.9;
    }

    @Override
    public String getName() {
        return "Swamp";
    }

    @Override
    public int getColor() {
        return 0x6a7039;
    }
}
