// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.joshariasSurvival.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizerPlugin;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.math.geom.BaseVector3i;

import java.util.List;
import java.util.Map;

@RegisterPlugin
public class ForagableFoodRasterizer implements WorldRasterizerPlugin {
    private final Map<ForagableFoodType, List<Block>> foragableFood = Maps.newEnumMap(ForagableFoodType.class);

    private BlockManager blockManager;

    @Override
    public void initialize() {
        blockManager = CoreRegistry.get(BlockManager.class);

        foragableFood.put(ForagableFoodType.JOSHABERRY, ImmutableList.of(
                blockManager.getBlock("JoshariasSurvival:JoshaberryBushFull")));
        foragableFood.put(ForagableFoodType.ROCK, ImmutableList.of(
                blockManager.getBlockFamily("JoshariasSurvival:Rock").getArchetypeBlock()));
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        ForagableFoodFacet facet = chunkRegion.getFacet(ForagableFoodFacet.class);
        Block air = blockManager.getBlock(BlockManager.AIR_ID);

        WhiteNoise noise = new WhiteNoise(chunk.getPosition().hashCode());

        Map<BaseVector3i, ForagableFoodType> entries = facet.getRelativeEntries();
        for (BaseVector3i pos : entries.keySet()) {

            // check if some other rasterizer has already placed something here
            if (chunk.getBlock(pos).equals(air)) {

                ForagableFoodType type = entries.get(pos);
                List<Block> list = foragableFood.get(type);
                int blockIdx = Math.abs(noise.intNoise(pos.getX(), pos.getY(), pos.getZ())) % list.size();
                Block block = list.get(blockIdx);
                chunk.setBlock(pos, block);
            }
        }
    }
}
