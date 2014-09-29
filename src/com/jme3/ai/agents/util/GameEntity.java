//Copyright (c) 2014, Jesús Martín Berlanga. All rights reserved. 
//Distributed under the BSD licence. Read "com/jme3/ai/license.txt".
package com.jme3.ai.agents.util;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviours.npc.steering.ObstacleAvoidanceBehaviour;
import com.jme3.ai.agents.util.control.AIAppState;
import com.jme3.ai.agents.util.systems.HPSystem;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * Base class for game objects that are interacting in game, and in general can
 * move and can be destroyed. Not to be used for terrain except for things that
 * can be destroyed. For automaticaly updating them, add them to to AIAppState
 * with addAgent().
 *
 * @see AIAppState#addAgent(com.jme3.ai.agents.Agent)
 * @see AIAppState#addAgent(com.jme3.ai.agents.Agent, com.jme3.math.Vector3f)
 * @see AIAppState#addAgent(com.jme3.ai.agents.Agent, float, float, float) For
 * other GameEntity use:
 * @see AIAppState#addGameObject(com.jme3.ai.agents.util.GameEntity)
 *
 * @author Tihomir Radosavljević
 * @author Jesús Martín Berlanga
 * @version 1.3.0
 */
public abstract class GameEntity extends AbstractControl {

    /**
     * Container for the velocity of the game object.
     */
    protected Vector3f velocity = Vector3f.UNIT_XYZ.clone();
    /**
     * Mass of GameEntity.
     */
    protected float mass;
    /**
     * GameEntity acceleration speed.
     */
    protected Vector3f acceleration;
    /**
     * Maximum move speed of GameEntity
     */
    protected float maxMoveSpeed;
    /**
     * Maximum force that can be applied to this GameEntity.
     */
    protected float maxForce;
    /**
     * HitPoint System that will agent use.
     */
    private HPSystem hpSystem;
    /**
     * Rotation speed of GameEntity.
     */
    protected float rotationSpeed;
    /**
     * Radius of GameEntity. It is needed for object that will be added in list
     * of objects that agent should avoid durring game, like mines etc.
     *
     * @see ObstacleAvoidanceBehaviour
     */
    protected float radius = 0;
    protected int id;

    /**
     * @return The predicted position for this 'frame', taking into account
     * current position and velocity.
     */
    public Vector3f getPredictedPosition() {
        Vector3f predictedPos = new Vector3f();
        if (velocity != null) {
            predictedPos = getLocalTranslation().add(velocity);
        }
        return predictedPos;
    }

    /**
     * @param gameEntity Other game object
     * @return The offset relative to another game object
     */
    public Vector3f offset(GameEntity gameEntity) {
        return gameEntity.getLocalTranslation().subtract(getLocalTranslation());
    }

    /**
     * @param positionVector
     * @return The offset relative to an position vector
     */
    public Vector3f offset(Vector3f positionVector) {
        return positionVector.subtract(getLocalTranslation());
    }

    /**
     * @return The agent forward direction
     */
    public Vector3f fordwardVector() {
        return getLocalRotation().mult(new Vector3f(0, 0, 1)).normalize();
    }

    /**
     * Calculates the forwardness in relation with another game object. That is
     * how "forward" is the direction to the quarry (1 means dead ahead, 0 is
     * directly to the side, -1 is straight back)
     *
     * @param gameEntity Other game object
     * @return The forwardness in relation with another agent
     */
    public float forwardness(GameEntity gameEntity) {
        Vector3f agentLooks = fordwardVector();
        float radiansAngleBetwen = agentLooks.angleBetween(offset(gameEntity).normalize());
        return FastMath.cos(radiansAngleBetwen);
    }

    /**
     * @param positionVector Offset vector.
     * @return The forwardness in relation with a position vector
     */
    public float forwardness(Vector3f offsetVector) {
        Vector3f agentLooks = getLocalRotation().mult(new Vector3f(0, 0, 1)).normalize();
        float radiansAngleBetwen = agentLooks.angleBetween(offsetVector.normalize());
        return FastMath.cos(radiansAngleBetwen);
    }

    /**
     * @param gameEntity Other agent
     * @return Distance relative to another game object
     */
    public float distanceRelativeToGameObject(GameEntity gameEntity) {
        return offset(gameEntity).length();
    }

    /**
     * @param gameEntity Other agent
     * @return Distance from a position
     */
    public float distanceSquaredRelativeToGameObject(GameEntity gameEntity) {
        return offset(gameEntity).lengthSquared();
    }

    /**
     * @param pos Position
     * @return Distance from a position
     */
    public float distanceFromPosition(Vector3f pos) {
        return offset(pos).length();
    }

    /**
     * @param position Position
     * @return Distance squared Distance from a position
     */
    public float distanceSquaredFromPosition(Vector3f position) {
        return offset(position).lengthSquared();
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector3f acceleration) {
        this.acceleration = acceleration;
    }

    public float getMoveSpeed() {
        return velocity.length();
    }

    public void setMoveSpeed(float moveSpeed) {
        if (maxMoveSpeed < moveSpeed) {
            this.maxMoveSpeed = moveSpeed;
        }
        velocity.normalizeLocal().multLocal(moveSpeed);
    }

    public float getMaxForce() {
        return maxForce;
    }

    public void setMaxForce(float maxForce) {
        this.maxForce = maxForce;
    }

    public Quaternion getLocalRotation() {
        return spatial.getLocalRotation();
    }

    public void setLocalRotation(Quaternion rotation) {
        spatial.setLocalRotation(rotation);
    }

    /**
     *
     * @return local translation of agent
     */
    public Vector3f getLocalTranslation() {
        return spatial.getLocalTranslation();
    }

    /**
     *
     * @param position local translation of agent
     */
    public void setLocalTranslation(Vector3f position) {
        this.spatial.setLocalTranslation(position);
    }

    /**
     * Setting local translation of agent
     *
     * @param x x translation
     * @param y y translation
     * @param z z translation
     */
    public void setLocalTranslation(float x, float y, float z) {
        this.spatial.setLocalTranslation(x, y, z);
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public float getMaxMoveSpeed() {
        return maxMoveSpeed;
    }

    public void setMaxMoveSpeed(float maxMoveSpeed) {
        this.maxMoveSpeed = maxMoveSpeed;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public Vector3f getVelocity() {
        return this.velocity;
    }

    public void setRadius(float radius) {
        this.validateRadius(radius);
        this.radius = radius;
    }

    public float getRadius() {
        return this.radius;
    }

    public HPSystem getHpSystem() {
        return hpSystem;
    }

    public void setHpSystem(HPSystem hpSystem) {
        this.hpSystem = hpSystem;
    }

    protected void validateRadius(float radius) {
        if (radius < 0) {
            throw new GameEntityExceptions.NegativeRadiusException("A GameObject can't have a negative radius. You tried to construct the agent with a " + radius + " radius.");
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GameEntity{" + id + '}';
    }
}