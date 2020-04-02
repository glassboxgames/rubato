package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;
import com.glassboxgames.util.*;

/**
 * Class representing an animation state for an entity.
 */
public class State {
  /**
   * Class to represent state metadata for JSON serialization.
   */
  private static class StateMetadata {
    public String name, path;
    public boolean loop;
  }
  
  /**
   * Class to represent an animation frame for JSON serialization.
   */
  private static class FrameData {
    public String file;
    public Array<Array<Float>> hurtboxes;
  }

  /**
   * Class to represent an in-memory frame.
   */
  private static class Frame {
    public String path;
    public Texture texture;
    public Array<FixtureDef> hurtboxDefs;
  }

  /** Filename of the state master file */
  protected static final String STATE_FILE = "states.json";
  /** Filename of the frame config file */
  protected static final String FRAME_FILE = "frames.json";
  
  /** Whether to loop the animation */
  protected boolean loop;
  /** Array of frame data */
  protected Array<FrameData> frameDataList;
  /** Array of frames */
  protected Array<Frame> frames;

  /**
   * Creates an array of states from the given root path.
   * Requires the presence of a `states.json` file at that path.
   * @param path
   */
  public static Array<State> readStates(String path) {
    Json json = new Json();
    Array<StateMetadata> metadataList = json.fromJson(Array.class, StateMetadata.class,
                                                      Gdx.files.internal(path + STATE_FILE));
    Array<State> states = new Array<State>();
    for (StateMetadata metadata : metadataList) {
      states.add(new State(path + metadata.path, metadata.loop));
    }
    return states;
  }

  /**
   * Instantiates an entity state.
   * @param path path to the directory containing the entity state data
   */
  public State(String path, boolean loop) {
    this.loop = loop;
    frames = new Array<Frame>();
    Json json = new Json();
    frameDataList = json.fromJson(Array.class, FrameData.class,
                                  Gdx.files.internal(path + FRAME_FILE).readString());
    for (FrameData frameData : frameDataList) {
      Frame frame = new Frame();
      frame.path = path + frameData.file;
      frame.hurtboxDefs = new Array<FixtureDef>();
      for (Array<Float> arr : frameData.hurtboxes) {
        FixtureDef def = new FixtureDef();
        def.density = 1f;
        def.friction = 0f;
        if (arr.size == 3) {
          CircleShape shape = new CircleShape();
          shape.setPosition(new Vector2(arr.get(0), arr.get(1)));
          shape.setRadius(arr.get(2));
          def.shape = shape;
        } else if (arr.size == 5) {
          PolygonShape shape = new PolygonShape();
          shape.setAsBox(arr.get(2) / 2, arr.get(3) / 2,
                         new Vector2(arr.get(0), arr.get(1)),
                         arr.get(4));
          def.shape = shape;
        }
        frame.hurtboxDefs.add(def);
      }
      frames.add(frame);
    }
  }

  /**
   * Preloads the textures for this state.
   * @param manager asset manager to use
   */
  public void preloadContent(AssetManager manager) {
    for (Frame frame : frames) {
      manager.load(frame.path, Texture.class);
    }
  }

  /**
   * Loads the textures for this state.
   * @param manager asset manager to use
   */
  public void loadContent(AssetManager manager) {
    for (Frame frame : frames) {
      frame.texture = manager.get(frame.path, Texture.class);
      frame.texture.setFilter(Texture.TextureFilter.Linear,
                              Texture.TextureFilter.Linear);
    }
  }

  /**
   * Unloads the textures for this state.
   * @param manager asset manager to use
   */
  public void unloadContent(AssetManager manager) {
    for (Frame frame : frames) {
      if (manager.isLoaded(frame.path)) {
        manager.unload(frame.path);
      }
    }
  }

  /**
   * Returns the frame object at the given frame index for this state.
   * If looping, takes index mod length; otherwise returns last frame on overflow.
   */
  protected Frame getFrame(int index) {
    return frames.get(loop ? index % frames.size : Math.min(index, frames.size - 1));
  }
  
  /**
   * Returns the length of the state (number of frames in animation).
   */
  public int getLength() {
    return frames.size;
  }

  /**
   * Returns whether this animation state loops.
   */
  public boolean isLooping() {
    return loop;
  }

  /**
   * Returns the current texture of this state.
   */
  public Texture getTexture(int index) {
    return getFrame(index).texture;
  }

  /**
   * Returns the current hurtbox fixture definition array of this state.
   */
  public Array<FixtureDef> getHurtboxDefs(int index) {
    return getFrame(index).hurtboxDefs;
  }
}
