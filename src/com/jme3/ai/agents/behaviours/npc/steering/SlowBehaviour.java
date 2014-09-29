//Copyright (c) 2014, Jesús Martín Berlanga. All rights reserved.
//Distributed under the BSD licence. Read "com/jme3/ai/license.txt".
package com.jme3.ai.agents.behaviours.npc.steering;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviours.npc.steering.SteeringExceptions.ObstacleAvoindanceWithoutTimeIntervalException;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * Slows down the velocity produced by a behaviour container (g.e.
 * CompoundSteeringBehaviour)
 *
 * @see CompoundSteeringBehaviour
 * @see com.jme3.ai.agents.behaviours.npc.SimpleMainBehaviour
 *
 * @author Jesús Martín Berlanga
 * @version 2.1
 */
public class SlowBehaviour extends AbstractStrengthSteeringBehaviour {

    private int timeInterval;
    private float slowPercentage;
    private float maxBrakingFactor = 1;
    private ActionListener slowIteration = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            float newStrength = getBrakingFactorWrapper() * (1 - slowPercentage);

            if (newStrength < maxBrakingFactor) {
                setBrakingFactorWrapper(newStrength);
            }
        }
    };
    private Timer iterationTimer;

    /**
     * Slows a steer behaviour resultant velocity.
     *
     * @param behaviour Steer behaviour
     * @param timeInterval How much time for each slow iteration in ns
     * @param percentajeSlow What percentaje will be reduced the vecocity for
     * each iteration, a float betwen 0 and 1
     *
     * @throws ObstacleAvoindanceWithoutTimeIntervalException If time interval
     * is not a positive integer
     *
     * @see
     * AbstractSteeringBehaviour#AbstractSteeringBehaviour(com.jme3.ai.agents.Agent)
     */
    public SlowBehaviour(Agent agent, int timeInterval, float percentajeSlow) {
        super(agent);
        this.construct(timeInterval, percentajeSlow);
    }

    /**
     * @see SlowBehaviour#SlowBehaviour(com.jme3.ai.agents.Agent, int, float)
     * @see
     * AbstractSteeringBehaviour#AbstractSteeringBehaviour(com.jme3.ai.agents.Agent,
     * com.jme3.scene.Spatial)
     */
    public SlowBehaviour(Agent agent, int timeInterval, float percentajeSlow, Spatial spatial) {
        super(agent, spatial);
        this.construct(timeInterval, percentajeSlow);
    }

    /**
     * @param slowPercentage float in the interval [0, 1]
     */
    public void setSlowPercentage(float slowPercentage) {
        //Auto adjust invalid inputs
        if (slowPercentage > 1) {
            this.slowPercentage = 1;
        } else if (slowPercentage < 0) {
            this.slowPercentage = 0;
        } else {
            this.slowPercentage = slowPercentage;
        }
    }

    private float getBrakingFactorWrapper() {
        return this.getBrakingFactor();
    }

    private void setBrakingFactorWrapper(float brakingFacor) {
        this.setBrakingFactor(brakingFacor);
    }

    /**
     * Turns on or off the slow behaviour
     *
     * @param active
     */
    public void setAcive(boolean active) {
        if (active && !this.iterationTimer.isRunning()) {
            this.iterationTimer.start();
        } else if (!active && this.iterationTimer.isRunning()) {
            this.iterationTimer.stop();
        }
    }

    /**
     * Reset the slow behaviour
     */
    public void reset() {
        this.setBrakingFactor(1);
    }

    private void construct(int timeInterval, float percentajeSlow) {
        if (timeInterval <= 0) {
            throw new ObstacleAvoindanceWithoutTimeIntervalException("The time interval must be postitive. The current value is: " + timeInterval);
        }

        this.timeInterval = timeInterval;

        if (percentajeSlow > 1) {
            this.slowPercentage = 1;
        } else if (percentajeSlow < 0) {
            this.slowPercentage = 0;
        } else {
            this.slowPercentage = percentajeSlow;
        }

        this.iterationTimer = new Timer(this.timeInterval, this.slowIteration);
    }

    @Override
    protected Vector3f calculateRawSteering() {
        return Vector3f.ZERO;
    }

    public void setMaxBrakingFactor(float maxBrakingFactor) {
        this.maxBrakingFactor = maxBrakingFactor;
    }
}