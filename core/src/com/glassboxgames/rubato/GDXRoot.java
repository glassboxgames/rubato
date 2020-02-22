package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.*;

public class GDXRoot extends Game {
  int x, y;
  ShapeRenderer renderer;

	@Override
	public void create() {
    x = 20;
    y = 20;
    renderer = new ShapeRenderer();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      x--;
    } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      x++;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      y++;
    } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      y--;
    }
    System.out.println(x + " " + y);
    renderer.begin(ShapeRenderer.ShapeType.Filled);
    renderer.setColor(0, 0, 1, 1);
    renderer.circle(x, y, 10);
    renderer.end();
	}

	@Override
	public void dispose() {
    renderer.dispose();
	}
}
