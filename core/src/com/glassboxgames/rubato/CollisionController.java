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
    startCollision((Entity.Collider)d1, (Entity.Collider)d2);
  }
  
  @Override
  public void endContact(Contact contact) {
    Fixture f1 = contact.getFixtureA();
    Fixture f2 = contact.getFixtureB();
    Object d1 = f1.getUserData();
    Object d2 = f2.getUserData();
    endCollision((Entity.Collider)d1, (Entity.Collider)d2);
  }

  @Override
  public void preSolve(Contact contact, Manifold manifold) {}

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {}

  /**
   * Process the start of a collision between the two given entities (with metadata).
   */
  private void startCollision(Entity.Collider o1, Entity.Collider o2) {
    if (o1.entity instanceof Player && o2.entity instanceof Enemy) {
      startCollision((Player)o1.entity, o1, (Enemy)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Enemy) {
      startCollision((Player)o2.entity, o2, (Enemy)o1.entity, o1);
    } else if (o1.entity instanceof Player && o2.entity instanceof Platform) {
      startCollision((Player)o1.entity, o1, (Platform)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Platform) {
      startCollision((Player)o2.entity, o2, (Platform)o1.entity, o1);
    } else if (o1.entity instanceof Enemy && o2.entity instanceof Platform) {
      startCollision((Enemy)o1.entity, o1, (Platform)o2.entity, o2);
    } else if (o2.entity instanceof Enemy && o1.entity instanceof Platform) {
      startCollision((Enemy)o2.entity, o2, (Platform)o1.entity, o1);
    } else if (o1.entity instanceof Player && o2.entity instanceof Checkpoint) {
      startCollision((Player)o1.entity, o1, (Checkpoint)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Checkpoint) {
      startCollision((Player)o2.entity, o2, (Checkpoint)o1.entity, o1);
    }
  }

  /**
   * Process the end of a collision between the two given entities (with metadata).
   */
  private void endCollision(Entity.Collider o1, Entity.Collider o2) {
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
    } else if (o1.entity instanceof Player && o2.entity instanceof Checkpoint) {
      endCollision((Player)o1.entity, o1, (Checkpoint)o2.entity, o2);
    } else if (o2.entity instanceof Player && o1.entity instanceof Checkpoint) {
      endCollision((Player)o2.entity, o2, (Checkpoint)o1.entity, o1);
    }
  }

  /**
   * Attacks the given enemy with the given player.
   */
  private void attack(Player player, Enemy enemy) {
    ObjectSet<Enemy> enemiesHit = player.getEnemiesHit();
    if (enemiesHit.add(enemy) && !enemy.isSuspended()) {
      enemy.lowerHealth(Player.ATTACK_DAMAGE);
    }
  }
  
  /**
   * Handles a collision starting between a player and an enemy.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Enemy enemy, Entity.Collider enemyCollider) {

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
        player.setAlive(false);
      }
    } else if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      if (enemy.isSuspended()) {
        player.addUnderfoot(enemy);
      }
    } else if (playerCollider.isWallSensor() && enemyCollider.isHurtbox()) {
      if (enemy.isSuspended()) {
        player.addAdjacent(enemy);
      }
    } else if (playerCollider.isHurtbox() && enemyCollider.isVisionSensor()) {
      enemy.setTarget(player.getPosition());
    }
  }

  /**
   * Handles a collision ending between a player and an enemy.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Enemy enemy, Entity.Collider enemyCollider) {
    if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      player.removeUnderfoot(enemy);
    } else if (playerCollider.isWallSensor() && enemyCollider.isHurtbox()) {
      player.removeAdjacent(enemy);
    } else if (playerCollider.isHurtbox() && enemyCollider.isVisionSensor()) {
      enemy.setTarget(null);
    }
  }

  /**
   * Handles a collision starting between a player and a platform.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Platform platform, Entity.Collider platformCollider) {
    if (playerCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      player.addUnderfoot(platform);
      platform.visit();
    } else if (playerCollider.isWallSensor() && platformCollider.isHurtbox()) {
      player.addAdjacent(platform);
    } else if (playerCollider.isHurtbox() && platformCollider.isHitbox()) {
      player.setAlive(false);
    }
  }

  /**
   * Handles a collision ending between a player and a platform.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Platform platform, Entity.Collider platformCollider) {
    if (playerCollider.isGroundSensor() && platformCollider.isHurtbox()) {
      player.removeUnderfoot(platform);
    } else if (playerCollider.isWallSensor() && platformCollider.isHurtbox()) {
      player.removeAdjacent(platform);
    }
  }

  /**
   * Handles a collision starting between an enemy and a platform.
   */
  private void startCollision(Enemy enemy, Entity.Collider enemyCollider,
                              Platform platform, Entity.Collider platformCollider) {
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
   * Handles a collision ending between an enemy and a platform.
   */
  private void endCollision(Enemy enemy, Entity.Collider enemyCollider,
                            Platform platform, Entity.Collider platformCollider) {
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

  /**
   * Handles a collision starting between a player and a checkpoint.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Checkpoint checkpoint, Entity.Collider checkpointCollider) {
    if (playerCollider.isHurtbox() && checkpointCollider.isCenterSensor()) {
      checkpoint.activate();
    }
  }

  /**
   * Handles a collision ending between a player and a checkpoint.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Checkpoint checkpoint, Entity.Collider checkpointCollider) {}
}
