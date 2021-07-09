// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.joshariasSurvival.world;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facetProviders.PositionFilters;
import org.terasology.core.world.generator.facetProviders.SurfaceObjectProvider;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.properties.Range;

import java.util.List;
import java.util.Map;


@RegisterPlugin
@Produces(ForagableFoodFacet.class)
@Requires({
        @Facet(SeaLevelFacet.class),
        @Facet(value = SurfacesFacet.class, border = @FacetBorder(bottom = 1)),
        @Facet(BiomeFacet.class)
})
public class ForagableFoodProvider extends SurfaceObjectProvider<Biome, ForagableFoodType> implements ConfigurableFacetProvider, FacetProviderPlugin {

    private Noise densityNoiseGen;

    private ForagableFoodDensityConfiguration configuration = new ForagableFoodDensityConfiguration();

    private Map<ForagableFoodType, Float> typeProbs = ImmutableMap.of(
            ForagableFoodType.JOSHABERRY, 0.005f,
            ForagableFoodType.ROCK, 0.2f);

    private Map<Biome, Float> biomeProbs = ImmutableMap.<Biome, Float>builder()
            .put(CoreBiome.FOREST, 0.3f)
            .put(CoreBiome.PLAINS, 0.2f)
            .put(CoreBiome.MOUNTAINS, 0.2f)
            .put(CoreBiome.SNOW, 0.001f)
            .put(CoreBiome.BEACH, 0.001f)
            .put(CoreBiome.OCEAN, 0f)
            .put(CoreBiome.DESERT, 0.001f).build();

    public ForagableFoodProvider() {

        for (CoreBiome biome : CoreBiome.values()) {
            float biomeProb = biomeProbs.get(biome);
            for (ForagableFoodType type : typeProbs.keySet()) {
                float typeProb = typeProbs.get(type);
                float prob = biomeProb * typeProb;
                register(biome, type, prob);
            }
        }
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        densityNoiseGen = new WhiteNoise(seed + 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfacesFacet surface = region.getRegionFacet(SurfacesFacet.class);
        BiomeFacet biomeFacet = region.getRegionFacet(BiomeFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        ForagableFoodFacet facet = new ForagableFoodFacet(region.getRegion(), region.getBorderForFacet(ForagableFoodFacet.class));

        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        filters.add(PositionFilters.minHeight(seaLevel.getSeaLevel()));
        filters.add(PositionFilters.probability(densityNoiseGen, configuration.density));

        populateFacet(facet, surface, biomeFacet, filters);

        region.setRegionFacet(ForagableFoodFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Foragable Food";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (ForagableFoodDensityConfiguration) configuration;
    }

    private static class ForagableFoodDensityConfiguration implements Component<ForagableFoodDensityConfiguration> {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall amount of foragable food")
        private float density = 0.4f;

    }

}
