/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.joshariasSurvival.systems;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.inGameHelpAPI.components.ItemHelpComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RegisterSystem
public class WorkstationDiagnosticsSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(WorkstationDiagnosticsSystem.class);

    @In
    WorkstationRegistry workstationRegistry;
    @In
    private NetworkSystem networkSystem;

    @Command(shortDescription = "Logs item without an output process")
    public String logItemsWithoutOutputProcess() {
        logger.info("--- logging items without an output process");

        List<String> processTypes = Assets.list(Prefab.class).stream()
                .map(x -> Assets.get(x, Prefab.class).get())
                .filter(x -> x.hasComponent(WorkstationComponent.class))
                .flatMap(x -> x.getComponent(WorkstationComponent.class).supportedProcessTypes.keySet().stream())
                .collect(Collectors.toList());

        Set<ResourceUrn> knownInputs = Sets.newHashSet();
        Set<ResourceUrn> knownOutputs = Sets.newHashSet();

        for (WorkstationProcess process : workstationRegistry.getWorkstationProcesses(processTypes)) {
            if (process instanceof DescribeProcess) {
                DescribeProcess describeProcess = (DescribeProcess) process;
                for (ProcessPartDescription description : describeProcess.getInputDescriptions()) {
                    if (description.getResourceUrn() != null) {
                        knownInputs.add(description.getResourceUrn());
                    }
                }
                for (ProcessPartDescription description : describeProcess.getOutputDescriptions()) {
                    if (description.getResourceUrn() != null) {
                        knownOutputs.add(description.getResourceUrn());
                    }
                }
            }
        }

        List<ResourceUrn> helpItems = Assets.list(Prefab.class).stream()
                .filter(x -> Assets.get(x, Prefab.class).get().hasComponent(ItemHelpComponent.class))
                .collect(Collectors.toList());
        knownInputs.addAll(helpItems);

        int count = 0;
        for (ResourceUrn input : knownInputs) {
            if (!knownOutputs.contains(input)) {
                logger.info(input.toString() + " does not have a known output process");
                count++;
            }
        }

        logger.info("--- finished logging items without an output process (" + count + " items)");

        return "Logged as info all items without assembly processes defined";
    }

    @Command(shortDescription = "Logs item crafting complexity")
    public String logItemCraftingComplexity() {
        logger.info("--- logging item crafting complexity");

        List<String> processTypes = Assets.list(Prefab.class).stream()
                .map(x -> Assets.get(x, Prefab.class).get())
                .filter(x -> x.hasComponent(WorkstationComponent.class))
                .flatMap(x -> x.getComponent(WorkstationComponent.class).supportedProcessTypes.keySet().stream())
                .collect(Collectors.toList());

        Multimap<ResourceUrn, List<ResourceUrn>> relatedInputs = HashMultimap.create();

        for (WorkstationProcess process : workstationRegistry.getWorkstationProcesses(processTypes)) {
            if (process instanceof DescribeProcess) {
                DescribeProcess describeProcess = (DescribeProcess) process;

                List<ResourceUrn> inputs = describeProcess.getInputDescriptions().stream()
                        .map(ProcessPartDescription::getResourceUrn)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                for (ProcessPartDescription description : describeProcess.getOutputDescriptions()) {
                    if (description.getResourceUrn() != null) {
                        relatedInputs.put(description.getResourceUrn(), inputs);
                    }
                }
            }
        }

        List<ResourceUrn> helpItems = Assets.list(Prefab.class).stream()
                .filter(x -> Assets.get(x, Prefab.class).get().hasComponent(ItemHelpComponent.class))
                .collect(Collectors.toList());
        for (ResourceUrn helpItem : helpItems) {
            relatedInputs.put(helpItem, Collections.EMPTY_LIST);
        }


        Map<ResourceUrn, Integer> maxDepthMap = Maps.newHashMap();
        Map<ResourceUrn, Integer> maxComplexityMap = Maps.newHashMap();

        for (Map.Entry<ResourceUrn, List<ResourceUrn>> entry : relatedInputs.entries()) {
            int maxDepth = 0;
            if (maxDepthMap.containsKey(entry.getKey())) {
                maxDepth = maxDepthMap.get(entry.getKey());
            }

            int maxComplexity = 0;
            if (maxComplexityMap.containsKey(entry.getKey())) {
                maxComplexity = maxComplexityMap.get(entry.getKey());
            }

            for (ResourceUrn input : entry.getValue()) {
                maxDepth = Math.max(maxDepth, getMaxDepth(input, relatedInputs, Lists.newArrayList()));
                maxComplexity = Math.max(maxComplexity, getMaxComplexity(input, relatedInputs, Lists.newArrayList()));
            }
            maxDepthMap.put(entry.getKey(), maxDepth);
            maxComplexityMap.put(entry.getKey(), maxComplexity);
        }

        for (Map.Entry<ResourceUrn, Integer> entry : maxDepthMap.entrySet()) {
            logger.info(entry.getKey().toString() + " MaxDepth=" + entry.getValue() + " MaxComplexity=" + maxComplexityMap.get(entry.getKey()));
        }

        logger.info("--- finished logging item crafting complexity");

        return "Logged as info all item crafting depth";
    }

    private int getMaxDepth(ResourceUrn input, Multimap<ResourceUrn, List<ResourceUrn>> relatedInputs, List<ResourceUrn> alreadyVisited) {
        int depth = 0;
        if (!relatedInputs.containsKey(input) || alreadyVisited.contains(input)) {
            return 0;
        }

        alreadyVisited.add(input);

        for (ResourceUrn childInput : relatedInputs.get(input).stream().flatMap(Collection::stream).collect(Collectors.toList())) {
            depth = Math.max(depth, getMaxDepth(childInput, relatedInputs, alreadyVisited));
        }

        return 1 + depth;
    }

    private int getMaxComplexity(ResourceUrn input, Multimap<ResourceUrn, List<ResourceUrn>> relatedInputs, List<ResourceUrn> alreadyVisited) {
        int complexity = 0;
        if (!relatedInputs.containsKey(input) || alreadyVisited.contains(input)) {
            return 1;
        }

        alreadyVisited.add(input);

        for (List<ResourceUrn> childInputs : relatedInputs.get(input)) {
            int childComplexity = 0;
            List<ResourceUrn> childAlreadyVisited = Lists.newArrayList(alreadyVisited);
            for (ResourceUrn childInput : childInputs) {
                childComplexity += getMaxComplexity(childInput, relatedInputs, childAlreadyVisited);
            }
            complexity = Math.max(complexity, childComplexity);
        }

        return 1 + complexity;
    }


    @Override
    public void postBegin() {
        // only do this in headless mode to avoid bothering too many
        if (networkSystem.getMode().isAuthority() && !networkSystem.getMode().hasLocalClient()) {
            logItemsWithoutOutputProcess();
            logItemCraftingComplexity();
        }
    }
}
