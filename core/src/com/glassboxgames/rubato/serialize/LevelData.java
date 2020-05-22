package com.glassboxgames.rubato.serialize;

import com.badlogic.gdx.utils.*;

/**
 * Simple level data serialization class.
 */
public class LevelData {
  /** Dimensions of the level */
  public float width, height;
  /** Chapter name */
  public String chapter;
  /** Player data */
  public PlayerData player;
  /** Enemy data array */
  public Array<EnemyData> enemies;
  /** Platform data array */
  public Array<PlatformData> platforms;
  /** Checkpoint data */
  public CheckpointData checkpoint;
  /** Altar data, optionally */
  public AltarData altar;
  /** Tooltips data array */
  public Array<TooltipData> tooltips;
  /** Whether this level is a completion level */
  public boolean completion;
}
