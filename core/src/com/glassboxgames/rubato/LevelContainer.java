package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
import com.glassboxgames.rubato.serialize.*;
import com.glassboxgames.util.*;

/**
 * This class represents everything inside a single level.
 */
public class LevelContainer {
  /** Width of the left wall fixture */
  private static final float LEFT_WALL_WIDTH = 0.5f;

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
  /** The checkpoint in this level (optional) */
  private Checkpoint checkpoint;
  /** The left wall definition */
  private BodyDef leftWallDef;
  /** The left wall in this level */
  private Body leftWall;

  /**
   * Instantiates a LevelContainer from a LevelData object.
   * @param data the level data container
   */
  public LevelContainer(LevelData data, AssetManager manager) {
    width = data.width;
    height = data.height;
    background = Shared.TEXTURE_MAP.get(data.chapter);
    player = new Player(data.player.x, data.player.y);
    enemies = new Array<Enemy>();
    for (EnemyData enemyData : data.enemies) {
      Enemy enemy = createEnemy(enemyData);
      if (enemy != null) {
        enemies.add(enemy);
      }
    }
    platforms = new Array<Platform>();
    for (PlatformData platformData : data.platforms) {
      Platform platform = createPlatform(platformData);
      if (platform != null) {
        platforms.add(platform);
      }
    }
    checkpoint = new Checkpoint(data.checkpoint.x, data.checkpoint.y);
    leftWallDef = new BodyDef();
    leftWallDef.type = BodyDef.BodyType.StaticBody;
    leftWallDef.position.set(-LEFT_WALL_WIDTH / 2, height / 2);
  }

  /**
   * Creates and returns an enemy from an enemy data object.
   */
  private static Enemy createEnemy(EnemyData data) {
    float x = data.x;
    float y = data.y;
    switch (data.type) {
    case "blob":
      return new Blob(x, y);
    case "spider":
      return new Spider(x, y);
    case "wisp":
      return new Wisp(x, y);
    case "wyrm":
      return new Wyrm(x, y);
    default:
      return null;
    }
  }

  /**
   * Returns the type index associated with the given type string.
   */
  private static int typeStringToIndex(String type) {
    switch (type) {
    case "tb_forest":
      return Platform.TYPE_TB_FOREST;
    case "t_forest":
      return Platform.TYPE_T_FOREST;
    case "m_forest":
      return Platform.TYPE_M_FOREST;
    case "b_forest":
      return Platform.TYPE_B_FOREST;
    case "tb_plains":
      return Platform.TYPE_TB_PLAINS;
    case "t_plains":
      return Platform.TYPE_T_PLAINS;
    case "m_plains":
      return Platform.TYPE_M_PLAINS;
    case "b_plains":
      return Platform.TYPE_B_PLAINS;
    case "tb_desert":
      return Platform.TYPE_TB_DESERT;
    case "t_desert":
      return Platform.TYPE_T_DESERT;
    case "m_desert":
      return Platform.TYPE_M_DESERT;
    case "b_desert":
      return Platform.TYPE_B_DESERT;
    case "tb_mountains":
      return Platform.TYPE_TB_MOUNTAINS;
    case "t_mountains":
      return Platform.TYPE_T_MOUNTAINS;
    case "m_mountains":
      return Platform.TYPE_M_MOUNTAINS;
    case "b_mountains":
      return Platform.TYPE_B_MOUNTAINS;
    case "b_wood_spikes":
      return Platform.TYPE_B_WOOD_SPIKES;
    case "l_wood_spikes":
      return Platform.TYPE_L_WOOD_SPIKES;
    case "t_wood_spikes":
      return Platform.TYPE_T_WOOD_SPIKES;
    case "r_wood_spikes":
      return Platform.TYPE_R_WOOD_SPIKES;
    case "b_stone_spikes":
      return Platform.TYPE_B_STONE_SPIKES;
    case "l_stone_spikes":
      return Platform.TYPE_L_STONE_SPIKES;
    case "t_stone_spikes":
      return Platform.TYPE_T_STONE_SPIKES;
    case "r_stone_spikes":
      return Platform.TYPE_R_STONE_SPIKES;
    case "crumbling":
      return Platform.TYPE_CRUMBLING;
    default:
      return -1;
    }
  }

  /**
   * Creates and returns a platform from a data object.
   */
  private static Platform createPlatform(PlatformData data) {
    int type = typeStringToIndex(data.type);
    return type == -1 ? null : new Platform(data.x, data.y, type);
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
    checkpoint.activatePhysics(world);
    leftWall = world.createBody(leftWallDef);
    FixtureDef def = new FixtureDef();
    def.friction = 0;
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(LEFT_WALL_WIDTH / 2, height);
    def.shape = shape;
    leftWall.createFixture(def);
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
    checkpoint.deactivatePhysics(world);
    if (leftWall != null) {
      world.destroyBody(leftWall);
      leftWall = null;
      leftWallDef.active = false;
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
   * Returns the checkpoint in this level, if there is one.
   */
  public Checkpoint getCheckpoint() {
    return checkpoint;
  }

  /**
   * Draws this level to the given canvas.
   * @param canvas the canvas to draw on
   * @param debug whether to draw collider shapes
   */
  public void draw(GameCanvas canvas, boolean debug) {
    canvas.removeShader();
    canvas.begin(Shared.BACKGROUND_SCALE, Shared.BACKGROUND_SCALE);
    canvas.drawBackground(background);
    canvas.end();
    canvas.begin();
    for (Platform platform : platforms) {
      platform.draw(canvas);
    }
    checkpoint.draw(canvas);
    for (Enemy enemy : enemies) {
      enemy.draw(canvas);
    }
    if (player.isAlive()) {
      player.draw(canvas);
    }
    canvas.end();

    if (debug) {
      canvas.beginDebug();
      for (Platform platform : platforms) {
        platform.drawPhysics(canvas);
      }
      checkpoint.drawPhysics(canvas);
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
