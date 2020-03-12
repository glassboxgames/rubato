package com.glassboxgames.rubato.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.glassboxgames.rubato.GDXRoot;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.forceExit = false;
		config.vSyncEnabled = true;
    config.width = 1200;
    config.height = 675;
    config.x = 0;
    config.y = 0;
		new LwjglApplication(new GDXRoot(), config);
	}
}
