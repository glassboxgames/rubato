package com.glassboxgames.rubato.serialize;

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
  /** Checkpoint data, optionally */
  public CheckpointData checkpoint;
}
