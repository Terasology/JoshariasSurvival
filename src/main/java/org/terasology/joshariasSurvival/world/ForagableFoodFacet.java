// Copyright 2015 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.joshariasSurvival.world;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;

/**
 * Stores where plants can be placed
 */
public class ForagableFoodFacet extends SparseObjectFacet3D<ForagableFoodType> {

    public ForagableFoodFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
