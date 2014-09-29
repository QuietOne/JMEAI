//Copyright (c) 2014, Jesús Martín Berlanga. All rights reserved.
//Distributed under the BSD licence. Read "com/jme3/ai/license.txt".
package com.jme3.ai.agents.behaviours.npc.steering;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviours.npc.steering.SteeringExceptions.PathFollowIstinsufficientPointsException;
import com.jme3.ai.agents.behaviours.npc.steering.SteeringExceptions.PathFollowNegativeCohesionStrengthException;
import com.jme3.ai.agents.behaviours.npc.steering.SteeringExceptions.PathFollowNegativeRadiusException;

import com.jme3.math.Plane;
import com.jme3.math.Plane.Side;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import java.util.ArrayList;

/**
 * "Path following behavior enables a character to steer along a predetermined
 * path, such as a roadway, corridor or tunnel. This is distinct from
 * constraining a vehicle rigidly to a path like a train rolling along a track.
 * Rather path following behavior is intended to produce motion such as people
 * moving down a corridor: the individual paths remain near, and often parallel
 * to, the centerline of the corridor, but are free to deviate from it. In the
 * implementation described here, a path will be idealized as a spine and a
 * radius. The spine" is represented as a "poly-line (a series of connected line
 * segments). The path is then a tube or generalized cylinder: a circle of the
 * specified radius, swept along the specified spine. The goal of the path
 * following steering behavior is to move a character along the path while
 * taying within the specified radius of the spine. If the character is
 * initially far away from the path, it must first approach, then follow the
 * path."
 * <br><br>
 *
 * "To compute steering for path following, a velocity-based prediction is made
 * of the character's future position. The predicted future position is
 * projected onto the nearest point on the path spine. If this projection
 * distance (from the predicted position to the nearest on-path point) is less
 * than the path radius, then the character is deemed to be correctly following
 * the path and no corrective steering is required. Otherwise the character is
 * veering away from the path, or is too far away from the path. To steer back
 * towards the path, the seek behavior is used to steer towards the on-path
 * projection of the predicted future position. A null or zero value is returned
 * if no corrective steering is required." <br><br>
 *
 * If you need the agent to pass exactly from the beginning of the first spine
 * you must use seek before activating path follow.
 *
 * @author Jesús Martín Berlanga
 * @version 1.0.1
 */
public class PathFollowBehaviour extends AbstractStrengthSteeringBehaviour {

    private ArrayList<Vector3f> orderedPointsList;
    private float pathRadius;
    private float cohesionStrength = 1;
    private int nextSpineJoint = -1;
    private Plane nextExit = null;
    private boolean active = true;

    /**
     * @param orderedPointsList Ordered points that will set the path spine and
     * consequently the path route. The minimum number of points is two.
     * @param pathRadius Spine radius i.e the path width
     *
     * @throws PathFollowIstinsufficientPointsException If orderedPointsList size is
     * lower than 2
     * @throws PathFollowNegativeRadiusException If path radius is negative
     *
     * @see
     * AbstractStrengthSteeringBehaviour#AbstractStrengthSteeringBehaviour(com.jme3.ai.agents.Agent)
     */
    public PathFollowBehaviour(Agent agent, ArrayList<Vector3f> orderedPointsList, float pathRadius) {
        super(agent);
        PathFollowBehaviour.validateConstruction(orderedPointsList, pathRadius, this.cohesionStrength);
        this.orderedPointsList = orderedPointsList;
        this.pathRadius = pathRadius;
    }

    /**
     * @see PathFollowBehaviour#PathFollowBehaviour(com.jme3.ai.agents.Agent,
     * java.util.ArrayList, float)
     * @see
     * AbstractStrengthSteeringBehaviour#AbstractStrengthSteeringBehaviour(com.jme3.ai.agents.Agent,
     * com.jme3.scene.Spatial)
     */
    public PathFollowBehaviour(Agent agent, ArrayList<Vector3f> orderedPointsList, float pathRadius, Spatial spatial) {
        super(agent, spatial);
        PathFollowBehaviour.validateConstruction(orderedPointsList, pathRadius, this.cohesionStrength);
        this.orderedPointsList = orderedPointsList;
        this.pathRadius = pathRadius;
    }

    /**
     * @param cohesionStrength Cohesion multiplier
     * @throws PathFollowNegativeCohesionStrengthException If cohesionStrength is
     * negative
     * @see PathFollowBehaviour#PathFollowBehaviour(com.jme3.ai.agents.Agent,
     * java.util.ArrayList, float)
     */
    public PathFollowBehaviour(Agent agent, ArrayList<Vector3f> orderedPointsList, float pathRadius, float cohesionStrength) {
        super(agent);
        PathFollowBehaviour.validateConstruction(orderedPointsList, pathRadius, cohesionStrength);
        this.orderedPointsList = orderedPointsList;
        this.pathRadius = pathRadius;
        this.cohesionStrength = cohesionStrength;
    }

    /**
     * @param cohesionStrength Cohesion multiplier
     * @see PathFollowBehaviour#PathFollowBehaviour(com.jme3.ai.agents.Agent,
     * java.util.ArrayList, float, com.jme3.scene.Spatial)
     */
    public PathFollowBehaviour(Agent agent, ArrayList<Vector3f> orderedPointsList, float pathRadius, float cohesionStrength, Spatial spatial) {
        super(agent, spatial);
        validateConstruction(orderedPointsList, pathRadius, cohesionStrength);
        this.orderedPointsList = orderedPointsList;
        this.pathRadius = pathRadius;
        this.cohesionStrength = cohesionStrength;
    }

    private static void validateConstruction(ArrayList<Vector3f> orderedPointsList, float pathRadius, float cohesionStrength) {
        if (orderedPointsList.size() < 2) {
            throw new PathFollowIstinsufficientPointsException(
                    "To create the path it is needed at least two points. You have passed  "
                    + orderedPointsList.size() + " points.");
        } else if (pathRadius < 0) {
            throw new PathFollowNegativeRadiusException(
                    "The path radius can not be negative. You have passed  "
                    + pathRadius + " as the radius value.");
        } else if (cohesionStrength < 0) {
            throw new PathFollowNegativeCohesionStrengthException(
                    "The path cohesion strength must be a positive value. You have passed  "
                    + cohesionStrength + " as the cohesion strength argument.");
        }
    }

    /**
     * Reset the behaviour. The agent will start following the path again from
     * the beginning.
     */
    public void reset() {
        this.nextSpineJoint = -1;
        this.nextExit = null;
        this.active = true;
    }

    /**
     * @see AbstractStrengthSteeringBehaviour#calculateRawSteering()
     */
    @Override
    protected Vector3f calculateRawSteering() {
        Vector3f steer = new Vector3f();

        if (active) {
            //Start to follow from the beginning
            if (this.nextSpineJoint < 0) {
                this.nextSpineJoint = 0;
            }

            if (this.nextSpineJoint < this.orderedPointsList.size()) {
                //Calculate the next exit
                Vector3f exitNormal;
                if (this.nextSpineJoint == 0) {
                    exitNormal = this.orderedPointsList.get(this.nextSpineJoint + 1).subtract(this.orderedPointsList.get(this.nextSpineJoint));
                } else {
                    exitNormal = this.orderedPointsList.get(this.nextSpineJoint).subtract(this.orderedPointsList.get(this.nextSpineJoint - 1));
                }

                this.nextExit = new Plane();
                this.nextExit.setOriginNormal(
                        this.orderedPointsList.get(this.nextSpineJoint),
                        exitNormal);

                Vector3f posProjection = this.nextExit.getClosestPoint(this.agent.getLocalTranslation());
                float distanceToCenter = posProjection.subtract(this.orderedPointsList.get(this.nextSpineJoint)).length();

                if (distanceToCenter > this.pathRadius) //The agent is outside the path
                {
                    //Move to the next spine and inside the path
                    Vector3f moveToSpine = this.agent.offset(this.orderedPointsList.get(this.nextSpineJoint)).normalize();
                    Vector3f moveToCenter = this.orderedPointsList.get(this.nextSpineJoint).subtract(posProjection).normalize();
                    steer = moveToSpine.add(moveToCenter);
                }//Move through the path 
                else {
                    Vector3f moveThroughPathSteer = exitNormal.normalize();

                    Vector3f predictedPos = this.agent.getPredictedPosition();
                    Vector3f predictedOffsetFromNextCenter = predictedPos.subtract(this.orderedPointsList.get(this.nextSpineJoint));
                    Vector3f projectionIntoSpine = this.orderedPointsList.get(this.nextSpineJoint).add(
                            exitNormal.mult(predictedOffsetFromNextCenter.dot(exitNormal) / exitNormal.lengthSquared()));

                    Vector3f predictedOffset = predictedPos.subtract(projectionIntoSpine);
                    Vector3f predictedOffsetFromSurface = predictedOffset.subtract(predictedOffset.normalize().mult(this.pathRadius));

                    //Path containment
                    if (predictedOffset.length() > this.pathRadius) {
                        moveThroughPathSteer = moveThroughPathSteer.add(predictedOffsetFromSurface.mult(cohesionStrength));
                    }

                    steer = moveThroughPathSteer;

                    if (this.nextExit.whichSide(this.agent.getLocalTranslation()) == Side.Positive) {
                        this.nextSpineJoint++;
                    }
                }
            } //The path has ended
            else {
                this.active = false;
            }
        }

        return steer;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }
}