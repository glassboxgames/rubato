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
    } else if (o1.entity instanceof Enemy && o2.entity instanceof Platform) {
      startCollision((Enemy)o1.entity, o1, (Platform) o2.entity, o2);
    } else if (o2.entity instanceof Enemy && o1.entity instanceof Platform) {
      startCollision((Enemy)o2.entity, o2, (Platform) o1.entity, o1);
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
    } else if (o1.entity instanceof Enemy && o2.entity instanceof Platform) {
      endCollision((Enemy)o1.entity, o1, (Platform) o2.entity, o2);
    } else if (o2.entity instanceof Enemy && o1.entity instanceof Platform) {
      endCollision((Enemy)o2.entity, o2, (Platform) o1.entity, o1);
    }
  }

  /**
   * Attacks the given enemy with the given player.
   */
  private void attack(Player player, Enemy enemy) {
    ObjectSet<Enemy> enemiesHit = player.getEnemiesHit();
    if (enemiesHit.add(enemy)) {
      if (!enemy.isSuspended()) {
        player.addParry(Player.parryGain);
      }
      enemy.lowerHealth(Player.ATTACK_DAMAGE);
    }
  }
  
  /**
   * Handles a collision starting between a player and an enemy.
   */
  private void startCollision(Player player, Collider playerCollider,
                              Enemy enemy, Collider enemyCollider) {

    if (playerCollider.isForwardSensor() && enemyCollider.isHurtbox()) {
      if (player.isAttackingForward()) {
        attack(player, enemy);
      }
    } else if (playerCollider.isUpSensor() && enemyCollider.isHurtbox()) {
      if (player.isAttackingUp()) {
        attack(player, enemy);
      }
    } else if (playerCollider.isDownSensor() && enemyCollider.isHurtbox()) {
      if (player.isAttackingDown()) {
        attack(player, enemy);
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
    } else if (playerCollider.isHurtbox() && enemyCollider.isVisionSensor()) {
      enemy.setTarget(player.getPosition());
    }
  }

  /**
   * Handles a collision ending between a player and an enemy.
   */
  private void endCollision(Player player, Collider playerCollider,
                            Enemy enemy, Collider enemyCollider) {
    if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      player.removeUnderfoot(enemy);
    } else if (playerCollider.isHurtbox() && enemyCollider.isVisionSensor()) {
      enemy.setTarget(null);
    }
  }

  /**
   * Handles a collision starting between a player and a platform.
   */
  private void startCollision(Player player, Collider playerCollider,
                              Platform platform, Collider platformCollider) {
    if (playerCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      player.addUnderfoot(platform);
    } else if (playerCollider.isHurtbox() && platformCollider.isHitbox()) {
      player.setAlive(false);
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

  /**
   * Handles a collision starting between an Enemy and a platform
   */
  private void startCollision(Enemy enemy, Collider enemyCollider,
                              Platform platform, Collider platformCollider) {
    if (enemyCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).addUnderfoot(platform);
      }
    } else if (enemyCollider.isEdgeSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).setEdge(false);
      }
    }
  }
  /**
   * Handles a collision ending between an Enemy and a platform
   */
  private void endCollision(Enemy enemy, Collider enemyCollider,
                            Platform platform, Collider platformCollider) {
    if (enemyCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).removeUnderfoot(platform);
      }
    } else if (enemyCollider.isEdgeSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).setEdge(true);
      }
    }

  }
}
