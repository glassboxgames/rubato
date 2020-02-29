package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.util.*;

/**
 * Class representing the (circular) hitbox of an attack in Rubato.
 */
public class Hitbox extends Entity {
  /** Number of frames the hitbox lasts */
  private int lifespan;
  /** Damage the hitbox does */
  private float damage;

  /**
   * Instantiate a new hitbox with the given parameters.
   * @param x x-coordinate of center
   * @param y y-coordinate of center
   * @param r radius of hitbox
   * @param l lifespan in frames
   * @param dmg amount of damage to do on contact
   */
  public Hitbox(float x, float y, float r, int l, float dmg) {
    super(x, y);
    fixtureDef.shape = new CircleShape();
    fixtureDef.shape.setRadius(r);
    
    lifespan = l;
    damage = dmg;
  }
}
