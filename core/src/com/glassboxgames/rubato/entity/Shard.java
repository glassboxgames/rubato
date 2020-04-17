package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class to represent the time shard.
 */
public class Shard extends Entity {
  /** State constants */
  public static final int STATE_IDLE = 0;
  /** Shard states */
  public static Array<State> states = null;

  /** Shard angular velocity */
  private static final float SHARD_SPEED = 10f;
  /** Shard distance from center */
  private static final float SHARD_DIST = 0.7f;

  /** Temp vector for calculations */
  private Vector2 temp;

  /**
   * Instantiates a new shard with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Shard(float x, float y) {
    super(x, y, STATE_IDLE);
    bodyDef.gravityScale = 0;
    bodyDef.fixedRotation = false;
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    temp = new Vector2();
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  /**
   * Initializes the time shard's states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Shard/");
  }

  /**
   * Starts a shard swing.
   * @param cwAngle starting angle for clockwise swing
   * @param ccwAngle starting angle for counterclockwise swing
   */
  private void startAttack(float cwAngle, float ccwAngle) {
    body.setTransform(getPosition(), getDirection() > 0 ? cwAngle : ccwAngle);
    body.setAngularVelocity(-getDirection() * SHARD_SPEED);
  }

  /**
   * Starts an upward shard swing.
   */
  public void startUpAttack() {
    startAttack(MathUtils.PI * 3 / 4, MathUtils.PI / 4);
  }

  /**
   * Starts a downward shard swing.
   */
  public void startDownAttack() {
    startAttack(MathUtils.PI * 7 / 4, MathUtils.PI * 5 / 4);
  }

  /**
   * Starts a forward shard swing.
   */
  public void startForwardAttack() {
    startAttack(MathUtils.PI / 4, MathUtils.PI * 3 / 4);
  }

  /**
   * Stops the shard swing.
   */
  public void stopAttack() {
    body.setAngularVelocity(0);
  }

  /**
   * Updates this shard with the given parameters.
   */
  public void update(float delta, Vector2 center, int direction) {
    super.update(delta);
    setPosition(center);
    setDirection(direction);
  }

  @Override
  public void draw(GameCanvas canvas) {
    Texture texture = getState().getTexture(getCount());
    float w = texture.getWidth() / Constants.PPM;
    float h = texture.getHeight() / Constants.PPM;
    temp.set(1, 1).setLength(SHARD_DIST).setAngleRad(getAngle());
    Vector2 pos = getPosition();
    canvas.draw(texture, Color.WHITE,
                0, h / 2,
                temp.x + pos.x, temp.y + pos.y,
                w, h, getAngle() * 180f / MathUtils.PI);
  }
}
