/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.joshariasSurvival.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.List;
import java.util.Map;

@RegisterPlugin
public class ForagableFoodRasterizer implements WorldRasterizerPlugin {

    private final Map<ForagableFoodType, List<Block>> foragableFood = Maps.newEnumMap(ForagableFoodType.class);

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        foragableFood.put(ForagableFoodType.BLUEBERRY, ImmutableList.<Block>of(
                blockManager.getBlock("JoshariasSurvival:BlueberryBushFull")));
        foragableFood.put(ForagableFoodType.ROCK, ImmutableList.<Block>of(
                blockManager.getBlock("JoshariasSurvival:Rock")));

    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        ForagableFoodFacet facet = chunkRegion.getFacet(ForagableFoodFacet.class);
        Block air = BlockManager.getAir();

        WhiteNoise noise = new WhiteNoise(chunk.getPosition().hashCode());

        Map<Vector3i, ForagableFoodType> entries = facet.getRelativeEntries();
        for (Vector3i pos : entries.keySet()) {

            // check if some other rasterizer has already placed something here
            if (chunk.getBlock(pos).equals(air)) {

                ForagableFoodType type = entries.get(pos);
                List<Block> list = foragableFood.get(type);
                int blockIdx = Math.abs(noise.intNoise(pos.x, pos.y, pos.z)) % list.size();
                Block block = list.get(blockIdx);
                chunk.setBlock(pos, block);
            }
        }
    }
}
