package com.glassboxgames.rubato;

/**
 * Class controlling all the draw methods for Rubato.
 */

/*
 * GameCanvas.java
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 *
 * This version of GameCanvas only supports both rectangular and polygonal Sprite
 * drawing. It also supports a debug mode that draws polygonal outlines.  However,
 * that mode must be done in a separate begin/end pass.
 */
public class GameCanvas {
  /** Enumeration to track which pass we are in */
  private enum DrawPass {
    /** We are not drawing */
    INACTIVE,
    /** We are drawing sprites */
    STANDARD,
    /** We are drawing outlines */
    DEBUG,
  }

  /**
   * Enumeration of supported BlendStates.
   *
   * For reasons of convenience, we do not allow user-defined blend functions.
   * 99% of the time, we find that the following blend modes are sufficient
   * (particularly with 2D games).
   */
  public enum BlendState {
    /** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
    ALPHA_BLEND,
    /** Alpha blending on, assuming the colors have no pre-multipled alpha */
    NO_PREMULT,
    /** Color values are added together, causing a white-out effect */
    ADDITIVE,
    /** Color values are draw on top of one another with no transparency support */
    OPAQUE
  }


  /** Drawing context to handle textures AND POLYGONS as sprites */
  private PolygonSpriteBatch spriteBatch;

  /** Rendering context for the debug outlines */
  private ShapeRenderer debugRender;

  /** Track whether or not we are active (for error checking) */
  private DrawPass active;

  /** The current color blending mode */
  private BlendState blend;

  /** Camera for the underlying SpriteBatch */
  private OrthographicCamera camera;

  /** Value to cache window width (if we are currently full screen) */
  int width;
  /** Value to cache window height (if we are currently full screen) */
  int height;

  // CACHE OBJECTS
  /** Affine cache for current sprite to draw */
  private Affine2 local;
  /** Affine cache for all sprites this drawing pass */
  private Matrix4 global;
  private Vector2 vertex;
  /** Cache object to handle raw textures */
  private TextureRegion holder;

  /**
   * Creates a new GameCanvas determined by the application configuration.
   *
   * Width, height, and fullscreen are taken from the LWGJApplicationConfig
   * object used to start the application.  This constructor initializes all
   * of the necessary graphics objects.
   */
  public GameCanvas() {
    active = DrawPass.INACTIVE;
    spriteBatch = new PolygonSpriteBatch();
    debugRender = new ShapeRenderer();

    // Set the projection matrix (for proper scaling)
    camera = new OrthographicCamera(getWidth(),getHeight());
    camera.setToOrtho(false);
    spriteBatch.setProjectionMatrix(camera.combined);
    debugRender.setProjectionMatrix(camera.combined);

    // Initialize the cache objects
    holder = new TextureRegion();
    local  = new Affine2();
    global = new Matrix4();
    vertex = new Vector2();
  }

  /**
   * Eliminate any resources that should be garbage collected manually.
   */
  public void dispose() {
    if (active != DrawPass.INACTIVE) {
      Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
      return;
    }
    spriteBatch.dispose();
    spriteBatch = null;
    local  = null;
    global = null;
    vertex = null;
    holder = null;
  }

  /**
   * Returns the width of this canvas
   *
   * This currently gets its value from Gdx.graphics.getWidth()
   *
   * @return the width of this canvas
   */
  public int getWidth() {
    return Gdx.graphics.getWidth();
  }

  /**
   * Changes the width of this canvas
   *
   * This method raises an IllegalStateException if called while drawing is
   * active (e.g. in-between a begin-end pair).
   *
   * @param width the canvas width
   */
  public void setWidth(int width) {
    if (active != DrawPass.INACTIVE) {
      Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
      return;
    }
    this.width = width;
    if (!isFullscreen()) {
      Gdx.graphics.setWindowedMode(width, getHeight());
    }
    resize();
  }

  /**
   * Returns the height of this canvas
   *
   * This currently gets its value from Gdx.graphics.getHeight()
   *
   * @return the height of this canvas
   */
  public int getHeight() {
    return Gdx.graphics.getHeight();
  }

  /**
   * Changes the height of this canvas
   *
   * This method raises an IllegalStateException if called while drawing is
   * active (e.g. in-between a begin-end pair).
   *
   * @param height the canvas height
   */
  public void setHeight(int height) {
    if (active != DrawPass.INACTIVE) {
      Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
      return;
    }
    this.height = height;
    if (!isFullscreen()) {
      Gdx.graphics.setWindowedMode(getWidth(), height);
    }
    resize();
  }

  /**
   * Returns the dimensions of this canvas
   *
   * @return the dimensions of this canvas
   */
  public Vector2 getSize() {
    return new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
  }

  /**
   * Changes the width and height of this canvas
   *
   * This method raises an IllegalStateException if called while drawing is
   * active (e.g. in-between a begin-end pair).
   *
   * @param width the canvas width
   * @param height the canvas height
   */
  public void setSize(int width, int height) {
    if (active != DrawPass.INACTIVE) {
      Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
      return;
    }
    this.width = width;
    this.height = height;
    if (!isFullscreen()) {
      Gdx.graphics.setWindowedMode(width, height);
    }
    resize();

  }

  /**
   * Returns whether this canvas is currently fullscreen.
   *
   * @return whether this canvas is currently fullscreen.
   */
  public boolean isFullscreen() {
    return Gdx.graphics.isFullscreen();
  }

  /**
   * Sets whether or not this canvas should change to fullscreen.
   *
   * If desktop is true, it will use the current desktop resolution for
   * fullscreen, and not the width and height set in the configuration
   * object at the start of the application. This parameter has no effect
   * if fullscreen is false.
   *
   * This method raises an IllegalStateException if called while drawing is
   * active (e.g. in-between a begin-end pair).
   *
   * @param fullscreen Whether this canvas should change to fullscreen.
   * @param desktop 	 Whether to use the current desktop resolution
   */
  public void setFullscreen(boolean fullscreen, boolean desktop) {
    if (active != DrawPass.INACTIVE) {
      Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
      return;
    }
    if (fullscreen) {
      Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    } else {
      Gdx.graphics.setWindowedMode(width, height);
    }
  }

  /**
   * Resets the SpriteBatch camera when this canvas is resized.
   *
   * If you do not call this when the window is resized, you will get
   * weird scaling issues.
   */
  public void resize() {
    // Resizing screws up the spriteBatch projection matrix
    spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
  }

  /**
   * Returns the current color blending state for this canvas.
   *
   * Textures draw to this canvas will be composited according
   * to the rules of this blend state.
   *
   * @return the current color blending state for this canvas
   */
  public BlendState getBlendState() {
    return blend;
  }

  /**
   * Sets the color blending state for this canvas.
   *
   * Any texture draw subsequent to this call will use the rules of this blend
   * state to composite with other textures.  Unlike the other setters, if it is
   * perfectly safe to use this setter while  drawing is active (e.g. in-between
   * a begin-end pair).
   *
   * @param state the color blending rule
   */
  public void setBlendState(BlendState state) {
    if (state == blend) {
      return;
    }
    switch (state) {
    case NO_PREMULT:
      spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
      break;
    case ALPHA_BLEND:
      spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA);
      break;
    case ADDITIVE:
      spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE);
      break;
    case OPAQUE:
      spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ZERO);
      break;
    }
    blend = state;
  }

  /**
   * Clear the screen so we can start a new animation frame
   */
  public void clear() {
    // Clear the screen
    Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  /**
   * Start a standard drawing sequence.
   *
   * Nothing is flushed to the graphics card until the method end() is called.
   */
  public void begin() {
    spriteBatch.setProjectionMatrix(camera.combined);
    spriteBatch.begin();
    active = DrawPass.STANDARD;
  }

  /**
   * Ends a drawing sequence, flushing textures to the graphics card.
   */
  public void end() {
    spriteBatch.end();
    active = DrawPass.INACTIVE;
  }

  /**
   * Draw the seamless background image.
   *
   * The background image is drawn (with NO SCALING) at position x, y.  Width-wise,
   * the image is seamlessly scrolled; when we reach the image we draw a second copy.
   *
   * To work properly, the image should be wide and high enough to fill the screen.
   *
   * @param image  Texture to draw as an overlay
   */
  public void drawBackground(Texture image) {
    if (active != DrawPass.STANDARD) {
      Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
      return;
    }

    spriteBatch.draw(image, 0, 0, getWidth(),getHeight());
  }

  /**
   * Draws the tinted texture at the given position.
   *
   * The texture colors will be multiplied by the given color.  This will turn
   * any white into the given color.  Other colors will be similarly affected.
   *
   * Unless otherwise transformed by the global transform (@see begin(Affine2)),
   * the texture will be unscaled.  The bottom left of the texture will be positioned
   * at the given coordinates.
   *
   * @param image The texture to draw
   * @param tint  The color tint
   * @param ox 	The x-coordinate of texture origin (in pixels)
   * @param oy 	The y-coordinate of texture origin (in pixels)
   * @param x 	The x-coordinate of the texture origin (on screen)
   * @param y 	The y-coordinate of the texture origin (on screen)
   * @param width	The texture width
   * @param height The texture height
   */
  public void draw(Texture image, Color tint, float ox, float oy, float x, float y, float width, float height) {
    if (active != DrawPass.STANDARD) {
      Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
      return;
    }

    // Call the master drawing method (more efficient that base method)
    holder.setRegion(image);
    draw(holder, tint, x-ox, y-oy, width, height);
  }

  /**
   * Draws the tinted texture at the given position.
   *
   * The texture colors will be multiplied by the given color.  This will turn
   * any white into the given color.  Other colors will be similarly affected.
   *
   * Unless otherwise transformed by the global transform (@see begin(Affine2)),
   * the texture will be unscaled.  The bottom left of the texture will be positioned
   * at the given coordinates.
   *region
   * @param image The texture to draw
   * @param tint  The color tint
   * @param x 	The x-coordinate of the bottom left corner
   * @param y 	The y-coordinate of the bottom left corner
   * @param width	The texture width
   * @param height The texture height
   */
  public void draw(TextureRegion region, Color tint, float x, float y, float width, float height) {
    if (active != DrawPass.STANDARD) {
      Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
      return;
    }

    // Unlike Lab 1, we can shortcut without a master drawing method
    spriteBatch.setColor(tint);
    spriteBatch.draw(region, x,  y, width, height);
  }

  /**
   * Draws the tinted texture at the given position.
   *
   * The texture colors will be multiplied by the given color.  This will turn
   * any white into the given color.  Other colors will be similarly affected.
   *
   * Unless otherwise transformed by the global transform (@see begin(Affine2)),
   * the texture will be unscaled.  The bottom left of the texture will be positioned
   * at the given coordinates.
   *
   * @param region The texture to draw
   * @param tint  The color tint
   * @param ox 	The x-coordinate of texture origin (in pixels)
   * @param oy 	The y-coordinate of texture origin (in pixels)
   * @param x 	The x-coordinate of the texture origin (on screen)
   * @param y 	The y-coordinate of the texture origin (on screen)
   * @param width	The texture width
   * @param height The texture height
   */
  public void draw(TextureRegion region, Color tint, float ox, float oy, float x, float y, float width, float height) {
    if (active != DrawPass.STANDARD) {
      Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
      return;
    }

    // Unlike Lab 1, we can shortcut without a master drawing method
    spriteBatch.setColor(tint);
    spriteBatch.draw(region, x-ox, y-oy, width, height);
  }

  /**
   * Start the debug drawing sequence.
   *
   * Nothing is flushed to the graphics card until the method end() is called.
   */
  public void beginDebug() {
    debugRender.setProjectionMatrix(camera.combined);
    debugRender.begin(ShapeRenderer.ShapeType.Filled);
    debugRender.setColor(Color.RED);
    debugRender.circle(0, 0, 10);
    debugRender.end();

    debugRender.begin(ShapeRenderer.ShapeType.Line);
    active = DrawPass.DEBUG;
  }

  /**
   * Ends the debug drawing sequence, flushing textures to the graphics card.
   */
  public void endDebug() {
    debugRender.end();
    active = DrawPass.INACTIVE;
  }

  /**
   * Compute the affine transform (and store it in local) for this image.
   *
   * @param ox 	The x-coordinate of texture origin (in pixels)
   * @param oy 	The y-coordinate of texture origin (in pixels)
   * @param x 	The x-coordinate of the texture origin (on screen)
   * @param y 	The y-coordinate of the texture origin (on screen)
   * @param angle The rotation angle (in degrees) about the origin.
   * @param sx 	The x-axis scaling factor
   * @param sy 	The y-axis scaling factor
   */
  private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
    local.setToTranslation(x,y);
    local.rotate(180.0f*angle/(float)Math.PI);
    local.scale(sx,sy);
    local.translate(-ox,-oy);
  }
  /**
   * Draws the outline of the given shape in the specified color
   *
   * @param shape The Box2d shape
   * @param color The outline color
   * @param x  The x-coordinate of the shape position
   * @param y  The y-coordinate of the shape position
   */
  public void drawPhysics(PolygonShape shape, Color color, float x, float y) {
    if (active != DrawPass.DEBUG) {
      Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
      return;
    }

    float x0, y0, x1, y1;
    debugRender.setColor(color);
    for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
      shape.getVertex(ii  ,vertex);
      x0 = x+vertex.x; y0 = y+vertex.y;
      shape.getVertex(ii+1,vertex);
      x1 = x+vertex.x; y1 = y+vertex.y;
      debugRender.line(x0, y0, x1, y1);
    }
    // Close the loop
    shape.getVertex(shape.getVertexCount()-1,vertex);
    x0 = x+vertex.x; y0 = y+vertex.y;
    shape.getVertex(0,vertex);
    x1 = x+vertex.x; y1 = y+vertex.y;
    debugRender.line(x0, y0, x1, y1);
  }

  /**
   * Draws the outline of the given shape in the specified color
   *
   * @param shape The Box2d shape
   * @param color The outline color
   * @param x  The x-coordinate of the shape position
   * @param y  The y-coordinate of the shape position
   * @param angle  The shape angle of rotation
   */
  public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle) {
    if (active != DrawPass.DEBUG) {
      Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
      return;
    }

    local.setToTranslation(x,y);
    local.rotateRad(angle);

    float x0, y0, x1, y1;
    debugRender.setColor(color);
    for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
      shape.getVertex(ii  ,vertex);
      local.applyTo(vertex);
      x0 = vertex.x; y0 = vertex.y;
      shape.getVertex(ii+1,vertex);
      local.applyTo(vertex);
      x1 = vertex.x; y1 = vertex.y;
      debugRender.line(x0, y0, x1, y1);
    }
    // Close the loop
    shape.getVertex(shape.getVertexCount()-1,vertex);
    local.applyTo(vertex);
    x0 = vertex.x; y0 = vertex.y;
    shape.getVertex(0,vertex);
    local.applyTo(vertex);
    x1 = vertex.x; y1 = vertex.y;
    debugRender.line(x0, y0, x1, y1);
  }

  /**
   * Draws the outline of the given shape in the specified color
   *
   * @param shape The Box2d shape
   * @param color The outline color
   * @param x  The x-coordinate of the shape position
   * @param y  The y-coordinate of the shape position
   * @param angle  The shape angle of rotation
   * @param sx The amount to scale the x-axis
   * @param sx The amount to scale the y-axis
   */
  public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle, float sx, float sy) {
    if (active != DrawPass.DEBUG) {
      Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
      return;
    }

    local.setToScaling(sx,sy);
    local.translate(x,y);
    local.rotateRad(angle);

    float x0, y0, x1, y1;
    debugRender.setColor(color);
    for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
      shape.getVertex(ii  ,vertex);
      local.applyTo(vertex);
      x0 = vertex.x; y0 = vertex.y;
      shape.getVertex(ii+1,vertex);
      local.applyTo(vertex);
      x1 = vertex.x; y1 = vertex.y;
      debugRender.line(x0, y0, x1, y1);
    }
    // Close the loop
    shape.getVertex(shape.getVertexCount()-1,vertex);
    local.applyTo(vertex);
    x0 = vertex.x; y0 = vertex.y;
    shape.getVertex(0,vertex);
    local.applyTo(vertex);
    x1 = vertex.x; y1 = vertex.y;
    debugRender.line(x0, y0, x1, y1);
  }

  /**
   * Draws the outline of the given shape in the specified color
   *
   * The position of the circle is ignored.  Only the radius is used. To move the
   * circle, change the x and y parameters.
   *
   * @param shape The Box2d shape
   * @param color The outline color
   * @param x  The x-coordinate of the shape position
   * @param y  The y-coordinate of the shape position
   */
  public void drawPhysics(CircleShape shape, Color color, float x, float y) {
    if (active != DrawPass.DEBUG) {
      Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
      return;
    }

    debugRender.setColor(color);
    debugRender.circle(x, y, shape.getRadius(),12);
  }

  /**
   * Draws the outline of the given shape in the specified color
   *
   * The position of the circle is ignored.  Only the radius is used. To move the
   * circle, change the x and y parameters.
   *
   * @param shape The Box2d shape
   * @param color The outline color
   * @param x  The x-coordinate of the shape position
   * @param y  The y-coordinate of the shape position
   * @param sx The amount to scale the x-axis
   * @param sx The amount to scale the y-axis
   */
  public void drawPhysics(CircleShape shape, Color color, float x, float y, float sx, float sy) {
    if (active != DrawPass.DEBUG) {
      Gdx.app.error("GameCanvas", "Cannot draw without active beginDebug()", new IllegalStateException());
      return;
    }

    float x0 = x*sx;
    float y0 = y*sy;
    float w = shape.getRadius()*sx;
    float h = shape.getRadius()*sy;
    debugRender.setColor(color);
    debugRender.ellipse(x0-w, y0-h, 2*w, 2*h, 12);
  }
}
