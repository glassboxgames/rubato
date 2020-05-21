package com.glassboxgames.rubato.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.glassboxgames.rubato.GDXRoot;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Rubato";
		config.forceExit = true;
		config.vSyncEnabled = false;
    config.width = 1200;
    config.height = 675;
    config.x = -1;
    config.y = -1;
    config.resizable = false;
		new LwjglApplication(new GDXRoot(), config);
	}
}
