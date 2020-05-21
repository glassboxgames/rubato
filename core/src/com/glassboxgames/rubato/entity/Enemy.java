package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.rubato.*;

/**
 * Abstract class to represent any enemy in Rubato.
 */
public abstract class Enemy extends Entity {
  /** Desaturation shader */
  private static final ShaderProgram DESAT_SHADER =
    new ShaderProgram(Gdx.files.internal("Shaders/desat.vsr"), Gdx.files.internal("Shaders/desat.fsr"));

  /** Represent the previous position of the enemy */
  private Vector2 prevPosition = new Vector2(0, 0);
  /** Represent the previous velocity of the enemy */
  private Vector2 prevVelocity = new Vector2(0, 0);
  /** Cache for prevPosition calculation */
  private Vector2 prevPosCache = new Vector2(0, 0);
  /** Cache for prevVelocity calculation */
  private Vector2 prevVelCache = new Vector2(0, 0);
  /** Cache for targeting */
  private Vector2 targetCache = new Vector2(0, 0);

  /** Flag for removing the enemy */
  private boolean remove;
  /** Current health */
  private float health;
  /** Current target coordinates */
  private Vector2 target;

  /**
   * Initializes an enemy with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param i initial state index
   */
  public Enemy(float x, float y, int i) {
    super(x, y, i);
    health = getMaxHealth();
  }

  /**
   * Returns the maximum health of this enemy.
   */
  public abstract float getMaxHealth();

  /**
   * Damage this enemy by the given amount.
   * @param damage damage value
   */
  public void lowerHealth(float damage) {
    health = Math.max(0, health - damage);
  }

  /**
   * Returns whether this enemy is suspended (dead).
   */
  public boolean isSuspended() {
    return health == 0;
  }

  /**
   * Sets the target of this enemy.
   */
  public void setTarget(Vector2 target) {
    this.target = target == null ? null : new Vector2(target);
  }

  /**
   * Returns this enemy's target.
   */
  public Vector2 getTarget() {
    return target == null ? null : targetCache.set(target);
  }

  @Override
  public void update(float delta) {
    float tsf = health / getMaxHealth();
    super.update(delta, tsf);

    if (isSuspended() && body.getType() != BodyDef.BodyType.StaticBody) {
      body.setType(BodyDef.BodyType.StaticBody);
    } else {
      prevPosCache.x = prevPosition.x * (1 - tsf) + getPosition().x * tsf;
      prevPosCache.y = prevPosition.y * (1 - tsf) + getPosition().y * tsf;
      prevVelCache.x = prevVelocity.x * (1 - tsf) + getVelocity().x * tsf;
      prevVelCache.y = prevVelocity.y * (1 - tsf) + getVelocity().y * tsf;

      prevPosition = prevPosCache;
      prevVelocity = prevVelCache;

      body.setTransform(prevPosCache, body.getAngle());
      body.setLinearVelocity(prevVelCache);
    }
  }

  /**
   * Sets whether this enemy should be removed from the game.
   */
  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  /**
   * Returns whether this enemy should be removed.
   */
  public boolean shouldRemove() {
    return remove;
  }

  /**
   * Draws the enemy to the canvas
   */
  public void draw(GameCanvas canvas) {
    if (isSuspended()) {
      ShaderProgram temp = canvas.getShader();
      canvas.setShader(DESAT_SHADER);
      super.draw(canvas);
      canvas.setShader(temp);
    } else {
      super.draw(canvas);
    }
  }
}
