package com.glassboxgames.rubato;

import com.glassboxgames.rubato.entity.Enemy;
import com.glassboxgames.rubato.entity.Platform;
import com.glassboxgames.rubato.entity.Player;

public class CollisionController {

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

  public void startCollision(Object o1, Object o2) {
    if (o1 instanceof Player && o2 instanceof Platform){
      startCollision((Player) o1, (Platform) o2);
    } else if (o2 instanceof Player && o1 instanceof Platform) {
      startCollision((Player) o2, (Platform) o1);
    } else if (o1 instanceof Player && o2 instanceof Enemy) {
      startCollision((Player) o1, (Enemy) o2);
    } else if (o2 instanceof Player && o1 instanceof Enemy) {
      startCollision((Player) o2, (Enemy) o1);
    } else if (o1 instanceof GroundSensor && o2 instanceof Platform){
      startCollision((GroundSensor) o1, (Platform) o2);
    } else if (o2 instanceof GroundSensor && o1 instanceof Platform) {
      startCollision((GroundSensor) o2, (Platform) o1);
    } else if (o1 instanceof GroundSensor && o2 instanceof Enemy) {
      startCollision((GroundSensor) o1, (Enemy) o2);
    } else if (o2 instanceof GroundSensor && o1 instanceof Enemy) {
      startCollision((GroundSensor) o2, (Enemy) o1);
    }
  }

  public void endCollision(Object o1, Object o2) {
    if (o1 instanceof Player && o2 instanceof Platform){
      endCollision((Player) o1, (Platform) o2);
    } else if (o2 instanceof Player && o1 instanceof Platform) {
      endCollision((Player) o2, (Platform) o1);
    } else if (o1 instanceof Player && o2 instanceof Enemy) {
      endCollision((Player) o1, (Enemy) o2);
    } else if (o2 instanceof Player && o1 instanceof Enemy) {
      endCollision((Player) o2, (Enemy) o1);
    } else if (o1 instanceof GroundSensor && o2 instanceof Platform){
      endCollision((GroundSensor) o1, (Platform) o2);
    } else if (o2 instanceof GroundSensor && o1 instanceof Platform) {
      endCollision((GroundSensor) o2, (Platform) o1);
    } else if (o1 instanceof GroundSensor && o2 instanceof Enemy) {
      endCollision((GroundSensor) o1, (Enemy) o2);
    } else if (o2 instanceof GroundSensor && o1 instanceof Enemy) {
      endCollision((GroundSensor) o2, (Enemy) o1);
    }
  }

  /**
   * Handles the start collision between players and enemies.
   * @param player
   * @param enemy
   */
  public void startCollision(Player player, Enemy enemy) {
    if (!enemy.isSuspended()) {
      player.setAlive(false);
    }
  }

  /**
   * Handles the start collision between players and enemies.
   * @param player
   * @param enemy
   */
  public void endCollision(Player player, Enemy enemy) {}

  /**
   * Handles the start collision between players and platforms.
   * @param player
   * @param platform
   */
  public void startCollision(Player player, Platform platform) {}

  /**
   * Handles the end collision between players and platforms.
   * @param player
   * @param platform
   */
  public void endCollision(Player player, Platform platform) {}

  /**
   * Handles the start collision between ground sensors and enemies.
   * @param sensor
   * @param enemy
   */
  public void startCollision(GroundSensor sensor, Enemy enemy) {
    if (enemy.isSuspended()) {
      sensor.getPlayer().setGrounded(true);
    }
  }

  /**
   * Handles the start collision between ground sensors and enemy.
   * @param sensor
   * @param enemy
   */
  public void endCollision(GroundSensor sensor, Enemy enemy) {
    if (enemy.isSuspended()) {
      sensor.getPlayer().setGrounded(false);
    }
  }

  /**
   * Handles the start collision between ground sensors and platforms.
   * @param sensor
   * @param platform
   */
  public void startCollision(GroundSensor sensor, Platform platform) {
    sensor.getPlayer().setGrounded(true);
  }

  /**
   * Handles the end collision between ground sensors and platforms.
   * @param sensor
   * @param platform
   */
  public void endCollision(GroundSensor sensor, Platform platform) {
    sensor.getPlayer().setGrounded(false);
  }
}
