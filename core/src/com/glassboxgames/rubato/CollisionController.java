package com.glassboxgames.rubato;

import com.badlogic.gdx.audio.Sound;
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
    if (d1 != null && d2 != null) {
      Entity.Collider o1 = (Entity.Collider)d1;
      Entity.Collider o2 = (Entity.Collider)d2;
      if (o1.entity instanceof Player && o2.entity instanceof Enemy) {
        startCollision((Player)o1.entity, o1, (Enemy)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Enemy) {
        startCollision((Player)o2.entity, o2, (Enemy)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Platform) {
        startCollision((Player)o1.entity, o1, (Platform)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Platform) {
        startCollision((Player)o2.entity, o2, (Platform)o1.entity, o1);
      } else if (o1.entity instanceof Enemy && o2.entity instanceof Enemy) {
        startCollision((Enemy)o1.entity, o1, (Enemy) o2.entity, o2);
        startCollision((Enemy)o2.entity, o2, (Enemy) o1.entity, o1);
      } else if (o1.entity instanceof Enemy && o2.entity instanceof Platform) {
        startCollision((Enemy)o1.entity, o1, (Platform)o2.entity, o2);
      } else if (o2.entity instanceof Enemy && o1.entity instanceof Platform) {
        startCollision((Enemy)o2.entity, o2, (Platform)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Checkpoint) {
        startCollision((Player)o1.entity, o1, (Checkpoint)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Checkpoint) {
        startCollision((Player)o2.entity, o2, (Checkpoint)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Altar) {
        startCollision((Player)o1.entity, o1, (Altar)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Altar) {
        startCollision((Player)o2.entity, o2, (Altar)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Tooltip) {
        startCollision((Player)o1.entity, o1, (Tooltip)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Tooltip) {
        startCollision((Player) o2.entity, o2, (Tooltip) o1.entity, o1);
      }
    }
  }
  
  @Override
  public void endContact(Contact contact) {
    Fixture f1 = contact.getFixtureA();
    Fixture f2 = contact.getFixtureB();
    Object d1 = f1.getUserData();
    Object d2 = f2.getUserData();
    if (d1 != null && d2 != null) {
      Entity.Collider o1 = (Entity.Collider)d1;
      Entity.Collider o2 = (Entity.Collider)d2;
      if (o1.entity instanceof Player && o2.entity instanceof Enemy) {
        endCollision((Player)o1.entity, o1, (Enemy)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Enemy) {
        endCollision((Player)o2.entity, o2, (Enemy)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Platform) {
        endCollision((Player)o1.entity, o1, (Platform)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Platform) {
        endCollision((Player)o2.entity, o2, (Platform)o1.entity, o1);
      } else if (o1.entity instanceof Enemy && o2.entity instanceof Enemy) {
        endCollision((Enemy)o1.entity, o1, (Enemy) o2.entity, o2);
        endCollision((Enemy)o2.entity, o2, (Enemy) o1.entity, o1);
      } else if (o1.entity instanceof Enemy && o2.entity instanceof Platform) {
        endCollision((Enemy)o1.entity, o1, (Platform) o2.entity, o2);
      } else if (o2.entity instanceof Enemy && o1.entity instanceof Platform) {
        endCollision((Enemy)o2.entity, o2, (Platform) o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Checkpoint) {
        endCollision((Player)o1.entity, o1, (Checkpoint)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Checkpoint) {
        endCollision((Player)o2.entity, o2, (Checkpoint)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Altar) {
        endCollision((Player)o1.entity, o1, (Altar)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Altar) {
        endCollision((Player)o2.entity, o2, (Altar)o1.entity, o1);
      } else if (o1.entity instanceof Player && o2.entity instanceof Tooltip) {
        endCollision((Player)o1.entity, o1, (Tooltip)o2.entity, o2);
      } else if (o2.entity instanceof Player && o1.entity instanceof Tooltip) {
        endCollision((Player)o2.entity, o2, (Tooltip)o1.entity, o1);
      }
    }
  }

  @Override
  public void preSolve(Contact contact, Manifold manifold) {}

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {}

  /**
   * Attacks the given enemy with the given player.
   */
  private void attack(Player player, Enemy enemy) {
    ObjectSet<Enemy> enemiesHit = player.getEnemiesHit();
    if (enemiesHit.add(enemy) && !enemy.isSuspended()) {
      enemy.lowerHealth(Player.ATTACK_DAMAGE);
      String sound = Shared.getSoundPath("attack_hit");
      SoundController.getInstance().play(sound, sound, false);
      if (enemy.isSuspended()) {
        player.startDrain(enemy.getPosition());
      }
    }
  }
  
  /**
   * Handles a collision starting between a player and an enemy.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Enemy enemy, Entity.Collider enemyCollider) {

    if (playerCollider.isHitbox() && enemyCollider.isHurtbox()) {
      attack(player, enemy);
    } else if (playerCollider.isHitbox() && enemyCollider.isHitbox()) {
      if (enemy instanceof Projectile) {
        attack(player, enemy);
      }
    } else if (playerCollider.isHurtbox() && enemyCollider.isHitbox()) {
      if (!player.isInvincible() && !enemy.isSuspended()) {
        player.setAlive(false);
      }
    } else if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      if (enemy.isSuspended()) {
        player.addUnderfoot(enemy);
      }
    } else if (playerCollider.isHurtbox() && enemyCollider.isVisionSensor()) {
      if (!enemy.isSuspended()) {
        enemy.setTarget(player.getPosition());
      }
    }
  }

  /**
   * Handles a collision ending between a player and an enemy.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Enemy enemy, Entity.Collider enemyCollider) {
    if (playerCollider.isGroundSensor() && enemyCollider.isHurtbox()) {
      player.removeUnderfoot(enemy);
    } else if (playerCollider.isHurtbox() && enemyCollider.isVisionSensor()) {
      if (!enemy.isSuspended()) {
        enemy.setTarget(null);
      }
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
    }
  }
  
  /**
   * Handles a collision starting between two enemies.
   */
  private void startCollision(Enemy e1, Entity.Collider collider1,
                              Enemy e2, Entity.Collider collider2) {
    if (collider1.isHitbox() && collider2.isHurtbox()) {
      if (e1 instanceof Projectile && e2.isSuspended()) {
        e1.setRemove(true);
      }
    } else if (collider1.isGroundSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).addUnderfoot(e2);
      }
    } else if (collider1.isFrontEdgeSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).addAtFrontEdge(e2);
      }
    } else if (collider1.isBackEdgeSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).addAtBackEdge(e2);
      }
    } else if (collider1.isAheadSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).addAhead(e2);
      }
    } else if (collider1.isBehindSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).addBehind(e2);
      }
    }    
  }

  /**
   * Handles a collision ending between two enemies.
   */
  private void endCollision(Enemy e1, Entity.Collider collider1,
                            Enemy e2, Entity.Collider collider2) {
    if (collider1.isGroundSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).removeUnderfoot(e2);
      }
    } else if (collider1.isFrontEdgeSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).removeAtFrontEdge(e2);
      }
    } else if (collider1.isBackEdgeSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).removeAtBackEdge(e2);
      }
    } else if (collider1.isAheadSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).removeAhead(e2);
      }
    } else if (collider1.isBehindSensor() && collider2.isHurtbox()) {
      if (e1 instanceof Spider) {
        ((Spider)e1).removeBehind(e2);
      }
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
    } else if (enemyCollider.isFrontEdgeSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).addAtFrontEdge(platform);
      }
    } else if (enemyCollider.isBackEdgeSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).addAtBackEdge(platform);
      }
    } else if (enemyCollider.isAheadSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).addAhead(platform);
      }
    } else if (enemyCollider.isBehindSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).addBehind(platform);
      }
    } else if (enemyCollider.isHurtbox() && platformCollider.isHurtbox()) {
      if (enemy instanceof Wyrm) {
        ((Wyrm)enemy).cancelAttack();
      }
    } else if (enemyCollider.isHitbox() && platformCollider.isHurtbox()) {
      if (enemy instanceof Projectile) {
        enemy.setRemove(true);
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
    } else if (enemyCollider.isFrontEdgeSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).removeAtFrontEdge(platform);
      }
    } else if (enemyCollider.isBackEdgeSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).removeAtBackEdge(platform);
      }
    } else if (enemyCollider.isAheadSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).removeAhead(platform);
      }
    } else if (enemyCollider.isBehindSensor() && platformCollider.isHurtbox()) {
      if (enemy instanceof Spider) {
        ((Spider)enemy).removeBehind(platform);
      }
    }
  }
  
  /**
   * Handles a collision starting between a player and a checkpoint.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Checkpoint checkpoint, Entity.Collider checkpointCollider) {
    if (playerCollider.isHurtbox() && checkpointCollider.isCenterSensor()) {
      if (!checkpoint.isActivated()) {
        checkpoint.activate();
      }
    }
  }

  /**
   * Handles a collision ending between a player and a checkpoint.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Checkpoint checkpoint, Entity.Collider checkpointCollider) {}
  
  /**
   * Handles a collision starting between a player and a altar.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Altar altar, Entity.Collider altarCollider) {
    if (playerCollider.isHurtbox() && altarCollider.isCenterSensor()) {
      altar.setPlayerClose(true);
    } else if (playerCollider.isHurtbox() && altarCollider.isVisionSensor()) {
      altar.setPlayerSeen(true);
    }
  }
  
  /**
   * Handles a collision starting between a player and a tooltip.
   */
  private void startCollision(Player player, Entity.Collider playerCollider,
                              Tooltip tooltip, Entity.Collider tooltipCollider) {
    if (playerCollider.isHurtbox() && tooltipCollider.isCenterSensor()) {
      tooltip.appear();
    }
  }

  /**
   * Handles a collision ending between a player and a altar.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Altar altar, Entity.Collider altarCollider) {}

  /**
   * Handles a collision ending between a player and a tooltip.
   */
  private void endCollision(Player player, Entity.Collider playerCollider,
                            Tooltip tooltip, Entity.Collider tooltipCollider) {
    if (playerCollider.isHurtbox() && tooltipCollider.isCenterSensor()) {
      tooltip.disappear();
    }
  }
}
