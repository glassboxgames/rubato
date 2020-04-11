package com.glassboxgames.rubato;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;

public class CollisionController implements ContactListener {
  /** The singleton instance of the collision controller */
  private static CollisionController controller = null;

  /**
   * Returns the singleton instance of the collision controller.
   */
  public static CollisionController getInstance() {
    if (controller == null) {
      controller = new CollisionController();
    }
    return controller;
  }

  /**
   * Create a new collision controller. Only used to create the singleton.
   */
  private CollisionController() {}

  @Override
  public void beginContact(Contact contact) {
    Fixture f1 = contact.getFixtureA();
    Fixture f2 = contact.getFixtureB();
    Object d1 = f1.getUserData();
    Object d2 = f2.getUserData();
    startCollision((Collider)d1, (Collider)d2);
  }
  
  @Override
  public void endContact(Contact contact) {
    Fixture f1 = contact.getFixtureA();
    Fixture f2 = contact.getFixtureB();
    Object d1 = f1.getUserData();
    Object d2 = f2.getUserData();
    endCollision((Collider)d1, (Collider)d2);
  }

  @Override
  public void preSolve(Contact contact, Manifold manifold) {}

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {}

  /**
   * Process the start of a collision between the two given entities (with metadata).
   */
  private void startCollision(Collider o1, Collider o2) {
    if (o1.entity instanceof Player && o2.entity instanceof Enemy) {
      startCollision((Player)o1.entity, o1, (Enemy)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Enemy) {
      startCollision((Player)o2.entity, o2, (Enemy)o1.entity, o1);
    } else if (o1.entity instanceof Player && o2.entity instanceof Platform) {
      startCollision((Player)o1.entity, o1, (Platform)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Platform) {
      startCollision((Player)o2.entity, o2, (Platform)o1.entity, o1);
    }
  }

  /**
   * Process the end of a collision between the two given entities (with metadata).
   */
  private void endCollision(Collider o1, Collider o2) {
    if (o1.entity instanceof Player && o2.entity instanceof Enemy) {
      endCollision((Player)o1.entity, o1, (Enemy)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Enemy) {
      endCollision((Player)o2.entity, o2, (Enemy)o1.entity, o1);
    } else if (o1.entity instanceof Player && o2.entity instanceof Platform) {
      endCollision((Player)o1.entity, o1, (Platform)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Platform) {
      endCollision((Player)o2.entity, o2, (Platform)o1.entity, o1);
    }
  }

  /**
   * Handles a collision starting between a player and an enemy.
   */
  private void startCollision(Player player, Collider playerCollider,
                              Enemy enemy, Collider enemyCollider) {
    if (playerCollider.isHitbox() && enemyCollider.isHurtbox()) {
      Array<Enemy> enemiesHit = player.getEnemiesHit();
      if (!enemiesHit.contains(enemy, true)) {
        enemy.lowerHealth(Player.ATTACK_DAMAGE);
        enemiesHit.add(enemy);
        if (!enemy.isSuspended()) {
          player.addParry(Player.parryGain);
        }
      }
    } else if (playerCollider.isHurtbox() && enemyCollider.isHitbox()) {
      if (!enemy.isSuspended()) {
        if (!player.isParrying()) {
          player.setAlive(false);
        } else {
          // TODO handle what happens when enemies touch adagio while she's parrying
        }
      }
    } else if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      player.addUnderfoot(enemy);
    }
  }

  /**
   * Handles a collision ending between a player and an enemy.
   */
  private void endCollision(Player player, Collider playerCollider,
                            Enemy enemy, Collider enemyCollider) {
    if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      player.removeUnderfoot(enemy);
    }
  }

  /**
   * Handles a collision starting between a player and a platform.
   */
  private void startCollision(Player player, Collider playerCollider,
                              Platform platform, Collider platformCollider) {
    if (playerCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      player.addUnderfoot(platform);
    }
  }

  /**
   * Handles a collision ending between a player and a platform.
   */
  private void endCollision(Player player, Collider playerCollider,
                            Platform platform, Collider platformCollider) {
    if (playerCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      player.removeUnderfoot(platform);
    }
  }
}
