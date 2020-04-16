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
  /** The checkpoints in this level */
  private Array<Checkpoint> checkpoints;

  /**
   * Instantiates a level container with the given parameters.
   * @param width the level width
   * @param height the level height
   * @param background the level background texture
   * @param player the main player
   * @param enemies array of enemies
   * @param platforms array of platforms
   * @param checkpoints array of checkpoints
   */
  public LevelContainer(float width, float height, Texture background, Player player,
                        Array<Enemy> enemies, Array<Platform> platforms, Array<Checkpoint> checkpoints) {
    this.width = width;
    this.height = height;
    this.background = background;
    this.player = player;
    this.enemies = enemies;
    this.platforms = platforms;
    this.checkpoints = checkpoints;
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
      float x = enemyData.x;
      float y = enemyData.y;
      Enemy enemy = null;
      switch (enemyData.type) {
      case "spider":
        enemy = new Spider(x, y);
        break;
      case "wisp":
        enemy = new Wisp(x, y);
        break;
      case "wyrm":
        enemy = new Wyrm(x, y);
        break;
      }
      if (enemy != null) {
        enemies.add(enemy);
      }
    }
    platforms = new Array<Platform>();
    for (LevelData.PlatformData platformData : data.platforms) {
      int type = -1;
      switch (platformData.type) {
      case "simple":
        type = Platform.TYPE_SIMPLE;
        break;
      case "crumbling":
        type = Platform.TYPE_CRUMBLING;
        break;
      case "bottom_spikes":
        type = Platform.TYPE_BOTTOM_SPIKES;
        break;
      case "left_spikes":
        type = Platform.TYPE_LEFT_SPIKES;
        break;
      case "top_spikes":
        type = Platform.TYPE_TOP_SPIKES;
        break;
      case "right_spikes":
        type = Platform.TYPE_RIGHT_SPIKES;
        break;
      default:
        Gdx.app.error("LevelContainer", "Found unregistered platform type: " + platformData.type, new RuntimeException());
      }
      if (type != -1) {
        platforms.add(new Platform(platformData.x, platformData.y, type));
      }
    }
    checkpoints = new Array<Checkpoint>();
    for (LevelData.CheckpointData checkpointData : data.checkpoints) {
      checkpoints.add(new Checkpoint(checkpointData.x, checkpointData.y));
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
    for (Checkpoint checkpoint : checkpoints) {
      checkpoint.activatePhysics(world);
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
    for (Checkpoint checkpoint : checkpoints) {
      checkpoint.deactivatePhysics(world);
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
   * Returns the array of checkpoints in this level.
   */
  public Array<Checkpoint> getCheckpoints() {
    return checkpoints;
  }

  /**
   * Draws this level to the given canvas.
   * @param canvas the canvas to draw on
   * @param debug whether to draw collider shapes
   */
  public void draw(GameCanvas canvas, boolean debug) {
    canvas.begin();
    canvas.drawBackground(background, width * Constants.PPM, height * Constants.PPM);
    canvas.end();

    canvas.begin(Constants.PPM, Constants.PPM);
    for (Platform platform : platforms) {
      platform.draw(canvas);
    }
    for (Checkpoint checkpoint : checkpoints) {
      checkpoint.draw(canvas);
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
      for (Checkpoint checkpoint : checkpoints) {
        checkpoint.drawPhysics(canvas);
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
