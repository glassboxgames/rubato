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
  /** Ripple shader */
  private static final ShaderProgram RIPPLE_SHADER =
    new ShaderProgram(Gdx.files.internal("Shaders/ripple.vsr"), Gdx.files.internal("Shaders/ripple.fsr"));
  /** Width of the wall fixture */
  private static final float WALL_WIDTH = 0.5f;
  /** Distance between player and tooltip to trigger drawing */
  private static final float TOOLTIP_DISTANCE = 3.5f;

  /** The dimensions of the level */
  private float width, height;
  /** The chapter of the level */
  private String chapter;
  /** The level background layer textures */
  private Array<Texture> backgroundLayers;
  /** The player object for this level */
  private Player player;
  /** The enemies in this level */
  private Array<Enemy> enemies;
  /** The platforms in this level */
  private Array<Platform> platforms;
  /** The checkpoint in this level */
  private Checkpoint checkpoint;
  /** The optional altar in this level */
  private Altar altar;
  /** The tooltips in this level */
  private Array<Tooltip> tooltips;

  /** The wall definition */
  private BodyDef wallDef;
  /** The walls in this level */
  private Body leftWall, rightWall;
  /** Whether this level is the last level in its chapter */
  private boolean completion;

  /**
   * Instantiates a LevelContainer from a LevelData object.
   * @param data the level data container
   */
  public LevelContainer(LevelData data) {
    width = data.width;
    height = data.height;
    chapter = data.chapter;
    completion = data.completion;
    backgroundLayers = new Array<>();
    for (String key : Shared.TEXTURE_MAP.keys()) {
      if (key.startsWith(chapter + "_layer_")) {
        backgroundLayers.add(Shared.getTexture(key));
      }
    }
    player = new Player(data.player.x, data.player.y);
    enemies = new Array<Enemy>();
    for (EnemyData enemyData : data.enemies) {
      enemies.add(createEnemy(enemyData));
    }
    platforms = new Array<Platform>();
    for (PlatformData platformData : data.platforms) {
      platforms.add(createPlatform(platformData));
    }
    checkpoint = data.checkpoint != null ? new Checkpoint(data.checkpoint.x, data.checkpoint.y) : null;
    altar = data.altar != null ? new Altar(data.altar.x, data.altar.y) : null;
    if (checkpoint == null && altar == null) {
      Gdx.app.error("LevelContainer", "Expected exactly one of checkpoint and altar to be null; got both",
                    new RuntimeException());
      Gdx.app.exit();
    }
    if (checkpoint != null && altar != null) {
      Gdx.app.error("LevelContainer", "Expected exactly one of checkpoint and altar to be null; got neither",
                    new RuntimeException());
      Gdx.app.exit();
    }
    tooltips = new Array<Tooltip>();
    if (data.tooltips != null) {
      for (TooltipData tooltipData : data.tooltips) {
        tooltips.add(createTooltip(tooltipData));
      }
    }
    wallDef = new BodyDef();
    wallDef.type = BodyDef.BodyType.StaticBody;
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
      Gdx.app.error("LevelContainer", "Found unknown enemy type " + data.type, new RuntimeException());
      Gdx.app.exit();
      return null;
    }
  }

  /**
   * Creates and returns a platform from a data object.
   */
  private static Platform createPlatform(PlatformData data) {
    try {
      int type = Platform.Type.valueOf(data.type.toUpperCase()).ordinal();
      return new Platform(data.x, data.y, type);
    } catch (Exception e) {
      Gdx.app.error("LevelContainer", "Found unknown platform type " + data.type, new RuntimeException());
      Gdx.app.exit();
      return null;
    }
  }

  /**
   * Creates and returns a tooltip from a data object.
   */
  private static Tooltip createTooltip(TooltipData data) {
    try {
      String action = data.type;
      int type = Tooltip.Type.valueOf(action.toUpperCase()).ordinal();
      return new Tooltip(data.x, data.y, type, action);
    } catch (Exception e) {
      Gdx.app.error("LevelContainer", "Found unknown tooltip type " + data.type, new RuntimeException());
      Gdx.app.exit();
      return null;
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
    if (checkpoint != null) {
      checkpoint.activatePhysics(world);
    }
    if (altar != null) {
      altar.activatePhysics(world);
    }
    for (Tooltip tooltip : tooltips) {
      tooltip.activatePhysics(world);
    }
    FixtureDef def = new FixtureDef();
    def.friction = 0;
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(WALL_WIDTH / 2, height);
    def.shape = shape;
    leftWall = world.createBody(wallDef);
    leftWall.setTransform(-WALL_WIDTH / 2, height / 2, 0);
    leftWall.createFixture(def);
    rightWall = world.createBody(wallDef);
    rightWall.setTransform(width + WALL_WIDTH / 2, height / 2, 0);
    rightWall.createFixture(def);
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
    if (checkpoint != null) {
      checkpoint.deactivatePhysics(world);
    }
    if (altar != null) {
      altar.deactivatePhysics(world);
    }
    for (Tooltip tooltip : tooltips) {
      tooltip.deactivatePhysics(world);
    }
    if (leftWall != null) {
      world.destroyBody(leftWall);
      leftWall = null;
    }
    if (rightWall != null) {
      world.destroyBody(rightWall);
      rightWall = null;
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
   * Returns the chapter of this level.
   */
  public String getChapter() {
    return chapter;
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
   * Returns the checkpoint in this level.
   */
  public Checkpoint getCheckpoint() {
    return checkpoint;
  }

  /**
   * Returns the altar in this level.
   */
  public Altar getAltar() {
    return altar;
  }

  /**
   * Returns whether this is a completion level.
   */
  public boolean isCompletion() {
    return completion;
  }

  /**
   ** Returns the tooltips in this level.
   */
  public Array<Tooltip> getTooltips() {
    return tooltips;
  }

  /**
   * Removes the right wall in this level.
   */
  public void removeRightWall() {
    rightWall.setActive(false);
  }

  /**
   * Activates the ripple shader for this level.
   */
  private void setRippleShader(GameCanvas canvas) {
    if (!completion && altar == null) {
      RIPPLE_SHADER.begin();
      RIPPLE_SHADER.setUniformf("u_max_length", width * Shared.PPM);
      RIPPLE_SHADER.setUniformf("u_adagio",
                                new Vector2(Shared.PPM * player.getPosition().x
                                            - canvas.getCameraPos().x + canvas.getWidth() / 2,
                                            Shared.PPM * player.getPosition().y
                                            - canvas.getCameraPos().y + canvas.getHeight() / 2));
      RIPPLE_SHADER.setUniformf("u_checkpoint",
                                new Vector2(Shared.PPM * checkpoint.getPosition().x
                                            - canvas.getCameraPos().x + canvas.getWidth() / 2,
                                            Shared.PPM * checkpoint.getPosition().y
                                            - canvas.getCameraPos().y + canvas.getHeight() / 2));
      RIPPLE_SHADER.setUniformf("u_frame", checkpoint.isActivated() ? checkpoint.getInternalCount() : 0);
      RIPPLE_SHADER.end();
      canvas.setShader(RIPPLE_SHADER);
    }
  }

  /**
   * Draws this level's background to the given canvas.
   * @param canvas the canvas to draw on
   */
  public void drawBackground(GameCanvas canvas) {
    canvas.begin();
    setRippleShader(canvas);
    canvas.drawBackground(backgroundLayers);
    canvas.removeShader();
    canvas.end();
  }

  /**
   * Draws this level's entities to the given canvas.
   * @param canvas the canvas to draw on
   */
  public void drawEntities(GameCanvas canvas) {
    canvas.begin();
    setRippleShader(canvas);
    for (Platform platform : platforms) {
      platform.draw(canvas);
    }
    canvas.removeShader();
    if (checkpoint != null) {
      checkpoint.draw(canvas);
    }
    if (altar != null) {
      altar.draw(canvas);
    }
    for (Enemy enemy : enemies) {
      enemy.draw(canvas);
    }
    for (Tooltip tooltip : tooltips) {
      SaveController save = SaveController.getInstance();
      if (save.isDefaultBinding(tooltip.getAction())) {
        tooltip.draw(canvas);
      }
    }
    player.draw(canvas);
    canvas.end();
  }

  /**
   * Draws this level in debug mode.
   */
  public void drawDebug(GameCanvas canvas) {
    canvas.beginDebug();
    for (Tooltip tooltip : tooltips) {
      tooltip.drawPhysics(canvas);
    }
    for (Platform platform : platforms) {
      platform.drawPhysics(canvas);
    }
    if (checkpoint != null) {
      checkpoint.drawPhysics(canvas);
    }
    if (altar != null) {
      altar.drawPhysics(canvas);
    }
    for (Enemy enemy : enemies) {
      enemy.drawPhysics(canvas);
    }
    if (player.isActive()) {
      player.drawPhysics(canvas);
    }
    canvas.endDebug();
  }
}
