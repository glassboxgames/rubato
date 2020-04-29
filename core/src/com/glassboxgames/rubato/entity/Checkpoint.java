package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
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
    activeEffect.scaleEffect(0.01f);
    // these values are the height and width of the pillar / 2
    activeEffect.getEmitters().first().setPosition(x-0.24f, y-0.64f);
    activeEffect.getEmitters().get(1).setPosition(x-0.24f, y-0.64f);
  }

  /**
   * Initializes checkpoint states.
   */
  public static Array<State> initStates() {
    states = State.readStates("Checkpoints/");
    return states;
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
      activeEffect.update(delta);
      if (activeEffect.isComplete()) {
        activeEffect.scaleEffect(0.01f);
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
    return activated;
  }

  @Override
  public void draw(GameCanvas canvas) {
    super.draw(canvas);
    if (activated) {
      canvas.drawParticleEffect(activeEffect);
    }
  }
}
