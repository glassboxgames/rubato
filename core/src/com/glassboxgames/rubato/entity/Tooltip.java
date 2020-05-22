package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a tooltip object in Rubato.
 */
public class Tooltip extends Entity {
  /**
   * Type enum
   */
  public enum Type {
    ATTACK_CARD,
    JUMP_CARD,
    RUN_CARD,
    PAUSE_CARD,
    RESET_CARD
  }

  /** Number of frames for tooltip to fully appear */
  public static final int DRAW_TIME = 20;
  /** Tooltip drawing percent */
  private int drawPercent;
  /** Radial rate of expansion */
  private int rate;
  /** Tooltip action */
  private String action;

  /**
   * Tooltip states (with one state per type)
   */
  public static Array<State> states = null;

  /**
   * Initializes a tooltip with the specified parameters.
   *
   * @param x    x-coordinate of lower left corner
   * @param y    y-coordinate of lower left corner
   * @param type the type index of the tooltip
   * @param action the action type of the tooltip
   *
   */
  public Tooltip(float x, float y, int type, String action) {
    super(x, y, type);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    drawPercent = 0;
    rate = 0;
    this.action = action;
  }

  /**
   * Initializes tooltip states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Tooltips/");
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    drawPercent = MathUtils.clamp(drawPercent + rate, 0, DRAW_TIME);
  }

  /**
   * Starts showing the tooltip.
   */
  public void appear() {
    rate = 1;
  }

  /**
   * Starts hiding the tooltip.
   */
  public void disappear() {
    rate = -1;
  }

  /**
   * Gets the action corresponding to the tooltip.
   */
  public String getAction() {
    return action;
  }

  @Override
  public void draw(GameCanvas canvas) {
    Texture texture = getTexture();
    float w = texture.getWidth() * drawPercent / DRAW_TIME;
    float h = texture.getHeight() * drawPercent / DRAW_TIME;
    Vector2 pos = getPosition().scl(Shared.PPM);
    canvas.draw(texture, Color.WHITE,
      dir * w / 2, h / 2,
      pos.x, pos.y,
      dir * w, h);
  }
}
