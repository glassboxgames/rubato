package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
import com.glassboxgames.util.*;

/**
 * This class represents everything inside a single level.
 */
public class LevelContainer {
  /** The dimensions of the level */
  private float width, height;
  /** The level background texture */
  private Texture background;
  /** The player object for this level */
  private Player player;
  /** The enemies in this level */
  private Array<Enemy> enemies;
  /** The platforms in this level */
  private Array<Platform> platforms;

  /**
   * Instantiates a level container with the given parameters.
   * @param width the level width
   * @param height the level height
   * @param background the level background texture
   * @param player the main player
   * @param enemies array of enemies
   * @param platforms array of platforms
   */
  public LevelContainer(float width, float height, Texture background,
                        Player player, Array<Enemy> enemies, Array<Platform> platforms) {
    this.width = width;
    this.height = height;
    this.background = background;
    this.player = player;
    this.enemies = enemies;
    this.platforms = platforms;
  }

  /**
   * Instantiates a LevelContainer from a LevelData object.
   * @param levelData the level data container
   */
  public LevelContainer(LevelData data, AssetManager manager) {
    width = data.width;
    height = data.height;
    background = manager.get(data.background, Texture.class);
    player = new Player(data.player.x, data.player.y);
    enemies = new Array<Enemy>();
    for (LevelData.EnemyData enemyData : data.enemies) {
      // TODO logic for diff enemy types
      enemies.add(new Enemy(enemyData.x, enemyData.y));
    }
    platforms = new Array<Platform>();
    for (LevelData.PlatformData platformData : data.platforms) {
      platforms.add(new Platform(platformData.x, platformData.y,
                                 platformData.type.equals("spikes")
                                 ? Platform.TYPE_SPIKES : Platform.TYPE_SIMPLE));
    }
  }

  /**
   * Activates physics for this level.
   */
  public void activatePhysics(World world) {
    player.activatePhysics(world);
    for (Enemy enemy : enemies) {
      enemy.activatePhysics(world);
    }
    for (Platform platform : platforms) {
      platform.activatePhysics(world);
    }
  }

  /**
   * Deactivates physics for this level.
   */
  public void deactivatePhysics(World world) {
    player.deactivatePhysics(world);
    for (Enemy enemy : enemies) {
      enemy.deactivatePhysics(world);
    }
    for (Platform platform : platforms) {
      platform.deactivatePhysics(world);
    }
  }

  /**
   * Returns the width of this level.
   */
  public float getWidth() {
    return width;
  }

  /**
   * Returns the height of this level.
   */
  public float getHeight() {
    return height;
  }

  /**
   * Returns the player object for this level.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the array of enemies in this level.
   */
  public Array<Enemy> getEnemies() {
    return enemies;
  }

  /**
   * Returns the array of platforms in this level.
   */
  public Array<Platform> getPlatforms() {
    return platforms;
  }

  /**
   * Draws this level to the given canvas.
   * @param canvas the canvas to draw on
   * @param debug whether to draw collider shapes
   */
  public void draw(GameCanvas canvas, boolean debug) {
    canvas.begin();
    canvas.drawBackground(background);
    canvas.end();

    canvas.begin(Constants.PPM, Constants.PPM);
    for (Platform platform : platforms) {
      platform.draw(canvas);
    }
    for (Enemy enemy : enemies) {
      enemy.draw(canvas);
    }
    if (player.isAlive()) {
      player.draw(canvas);
    }
    canvas.end();

    if (debug) {
      canvas.beginDebug(Constants.PPM, Constants.PPM);
      for (Platform platform : platforms) {
        platform.drawPhysics(canvas);
      }
      for (Enemy enemy : enemies) {
        enemy.drawPhysics(canvas);
      }
      if (player.isAlive()) {
        player.drawPhysics(canvas);
      }
      canvas.endDebug();
    }
  }
}
