// Copyright 2015 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.joshariasSurvival.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizerPlugin;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

import java.util.List;
import java.util.Map;

@RegisterPlugin
public class ForagableFoodRasterizer implements WorldRasterizerPlugin {
    private final Map<ForagableFoodType, List<Block>> foragableFood = Maps.newEnumMap(ForagableFoodType.class);

    private BlockManager blockManager;

    @Override
    public void initialize() {
        blockManager = CoreRegistry.get(BlockManager.class);

        foragableFood.put(ForagableFoodType.JOSHABERRY, ImmutableList.<Block>of(
                blockManager.getBlock("JoshariasSurvival:JoshaberryBushFull")));
        foragableFood.put(ForagableFoodType.ROCK, ImmutableList.<Block>of(
                blockManager.getBlockFamily("JoshariasSurvival:Rock").getArchetypeBlock()));
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        ForagableFoodFacet facet = chunkRegion.getFacet(ForagableFoodFacet.class);
        Block air = blockManager.getBlock(BlockManager.AIR_ID);

        WhiteNoise noise = new WhiteNoise(chunk.getPosition().hashCode());

        Map<Vector3ic, ForagableFoodType> entries = facet.getRelativeEntries();
        for (Vector3ic pos : entries.keySet()) {

            // check if some other rasterizer has already placed something here
            if (chunk.getBlock(pos).equals(air)) {

                ForagableFoodType type = entries.get(pos);
                List<Block> list = foragableFood.get(type);
                int blockIdx = Math.abs(noise.intNoise(pos.x(), pos.y(), pos.z())) % list.size();
                Block block = list.get(blockIdx);
                chunk.setBlock(pos, block);
            }
        }
    }
}
