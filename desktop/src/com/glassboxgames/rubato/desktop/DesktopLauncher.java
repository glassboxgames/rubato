package com.glassboxgames.rubato.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.glassboxgames.rubato.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		config.forceExit = false;
		// config.fullscreen = true;
		config.vSyncEnabled = true;
		new LwjglApplication(new GDXRoot(), config);
	}
}
