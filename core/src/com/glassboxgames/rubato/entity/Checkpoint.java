package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a time pillar checkpoint in Rubato.
 */
public class Checkpoint extends Entity {
  /** States */
  public static final int STATE_INACTIVE = 0;
  public static final int STATE_ACTIVE = 1;
  
  /** Checkpoint states */
  public static Array<State> states = null;
  
  /** Whether this checkpoint has been activated */
  private boolean activated;
  /** How many frames checkpoint has been activated for */
  public float activatedFrames = 0;

  /** Particle effects */
  private ParticleEffect activeEffect;

  /**
   * Initializes a checkpoint with the specified parameters.
   * @param x x-coordinate of lower left corner
   * @param y y-coordinate of lower left corner
   */
  public Checkpoint(float x, float y) {
    super(x, y, STATE_INACTIVE);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    activeEffect = new ParticleEffect();
    activeEffect.load(Gdx.files.internal("Particles/checkpoint.pe"), Gdx.files.internal("Particles"));
    activeEffect.scaleEffect(1f);
    // these values are the height and width of the pillar / 2
    activeEffect.getEmitters().first().setPosition(x-0.24f, y-0.64f);
    activeEffect.getEmitters().get(1).setPosition(x-0.24f, y-0.64f);
  }

  /**
   * Initializes checkpoint states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Checkpoints/");
  }

  /**
   * Returns whether this checkpoint was just activated.
   */
  public boolean wasJustActivated() {
    return isActivated() && getInternalCount() == 0;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_INACTIVE:
      if (activated) {
        setState(STATE_ACTIVE);
        activeEffect.start();
      }
      break;
    }
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    if (activated) {
      activatedFrames++;
      Vector2 pos = getPosition().sub(0.25f, 0.65f).scl(Shared.PPM);
      for (ParticleEmitter emitter : activeEffect.getEmitters()) {
        emitter.setPosition(pos.x, pos.y);
      }
      activeEffect.update(delta);
      if (activeEffect.isComplete()) {
        activeEffect.reset();
      }
    }
  }

  /**
   * Activates this checkpoint.
   */
  public void activate() {
    activated = true;
  }

  /**
   * Returns whether this checkpoint has been activated.
   */
  public boolean isActivated() {
    return stateIndex == STATE_ACTIVE;
  }

  @Override
  public void draw(GameCanvas canvas) {
    super.draw(canvas);
    if (activated) {
      canvas.drawParticleEffect(activeEffect);
    }
  }
}
