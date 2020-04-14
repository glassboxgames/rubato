package com.glassboxgames.rubato;

import com.badlogic.gdx.utils.*;

/**
 * Simple level data serialization class.
 */
public class LevelData {
  /** Dimensions of the level */
  public float width, height;
  /** Background path */
  public String background;
  /** Player data */
  public PlayerData player;
  /** Enemy data array */
  public Array<EnemyData> enemies;
  /** Platform data array */
  public Array<PlatformData> platforms;
  /** Checkpoint data array */
  public Array<CheckpointData> checkpoints;

  /**
   * Simple player data serialization class.
   */
  public static class PlayerData {
    public float x, y;
  }

  /**
   * Simple enemy data serialization class.
   */
  public static class EnemyData {
    public String type;
    public float x, y;
  }

  /**
   * Simple platform data serialization class.
   */
  public static class PlatformData {
    public String type;
    public float x, y;
  }

  /**
   * Simple checkpoint data serialization class.
   */
  public static class CheckpointData {
    public float x, y;
  }
}
