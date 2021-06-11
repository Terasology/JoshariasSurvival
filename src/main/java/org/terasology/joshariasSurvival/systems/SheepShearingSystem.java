// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.joshariasSurvival.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.AttackEvent;
import org.terasology.wildAnimals.event.AnimalSpawnEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.In;
import org.terasology.joshariasSurvival.components.ShearableSheepComponent;


import java.util.ArrayList;

@RegisterSystem
public class SheepShearingSystem extends BaseComponentSystem {
    public static final int HITS_TO_SHEAR = 5;
    public static final ArrayList<ItemComponent> SHEARING_ITEMS = null; //TODO yet to decide
    public static final int HAIR_REGROWTH_TIME = 3*60*1000; // In ms
    public static final String HAIR_REGROWTH_ACTION_ID = "Check hair regrowth";
    private static final Logger logger = LoggerFactory.getLogger(SheepShearingSystem.class);

    @In
    protected Time time;

    @In
    private DelayManager delayManager;

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onAnimalSpawn(AnimalSpawnEvent event, EntityRef entityRef) {
        if(entityRef.getParentPrefab().toString().contains("WildAnimals:sheep")) {
            entityRef.addComponent(new ShearableSheepComponent());
        }
    }

    @ReceiveEvent(components = {ShearableSheepComponent.class})
    public void onAnimalSpawn(AttackEvent event, EntityRef entityRef) {
        ShearableSheepComponent component = entityRef.getComponent(ShearableSheepComponent.class);
        if(!component.isSheared()) {
            int clicks = component.getClicks();
            if (clicks < HITS_TO_SHEAR) {
                component.setClicks(++clicks);
            } else {
                component.setClicks(0);
                component.setSheared(true);
                component.setLastShearingTimestamp(time.getGameTimeInMs());
                delayManager.addPeriodicAction(entityRef, HAIR_REGROWTH_ACTION_ID, 0, 10 * 1000);
                //TODO switch model
            }
        }
    }
    @ReceiveEvent
    public void onPeriodicActionTriggered(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(HAIR_REGROWTH_ACTION_ID)) {
            ShearableSheepComponent shearableSheepComponent = entity.getComponent(ShearableSheepComponent.class);
            if(shearableSheepComponent.isSheared()){
                if((time.getGameTimeInMs()-shearableSheepComponent.getLastShearingTimestamp())>HAIR_REGROWTH_TIME){
                    shearableSheepComponent.setSheared(false);
                    delayManager.cancelPeriodicAction(entity,HAIR_REGROWTH_ACTION_ID);
                    //TODO switch model
                }
            }
        }
    }
}
