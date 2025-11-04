/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.stevenwaterman.blindfold;

import com.google.inject.Provides;
import java.util.Objects;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Projectile;
import net.runelite.api.Renderable;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
	name = "Blindfold",
	description = "Stops things rendering (requires GPU)",
	tags = {"blindfold", "blind", "black", "greenscreen", "render"}
)
@Slf4j
public class BlindfoldPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BlindfoldPluginConfig config;

	@Inject
	private BlindfoldOverlay overlay;

	@Inject
	private RenderCallbackManager renderCallbackManager;

	private boolean enabled;

	@Inject
    private KeyManager keyManager;
    private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotKey()) {
        public void hotkeyPressed() {
			if (enabled){
				turnOff();
			}
			else {
				turnOn();
			}
		}
    };

	@Provides
	BlindfoldPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BlindfoldPluginConfig.class);
	}

	private final RenderCallback DISABLE_RENDERING = new RenderCallback(){
		@Override
		public boolean addEntity(Renderable renderable, boolean ui)
		{
			return false;
		}

		@Override
		public boolean drawTile(Scene scene, Tile tile)
		{
			return false;
		}

		@Override
		public boolean drawObject(Scene scene, TileObject object)
		{
			return false;
		}
	};

	private final RenderCallback rcb = new RenderCallback()
	{
		@Override
		public boolean drawTile(Scene scene, Tile tile)
		{
			return config.enableTerrain();
		}

		@Override
		public boolean drawObject(Scene scene, TileObject object)
		{
			if (object instanceof GameObject)
			{
				Renderable renderable = ((GameObject) object).getRenderable();

				if (renderable == client.getLocalPlayer())
				{
					return true;
				}

				if (renderable instanceof Projectile ||
					renderable instanceof TileItem ||
					renderable instanceof Actor)
				{
					return config.enableEntities();
				}

				if (renderable instanceof RuneLiteObject)
				{
					return config.enableRuneLiteObjects();
				}

				if (renderable instanceof Model ||
					renderable instanceof ModelData ||
					renderable instanceof DynamicObject ||
					renderable instanceof GraphicsObject)
				{
					return config.enableScenery();
				}
				return true;
			}
			else {
				return config.enableScenery();
			}
		}
	};

	@Override
	protected void startUp()
	{
		turnOn();
		keyManager.registerKeyListener(hotkeyListener);
	}

	private void turnOn()
	{
		enabled = true;
		overlayManager.add(overlay);
		clientThread.invokeLater(() ->
			{
				renderCallbackManager.register(rcb);

				if (client.getGameState() == GameState.LOGGED_IN)
					client.setGameState(GameState.LOADING);
			}
		);
	}

	@Override
	protected void shutDown()
	{
		turnOff();
		keyManager.unregisterKeyListener(hotkeyListener);
	}

	private void turnOff()
	{
		enabled = false;
		overlayManager.remove(overlay);

		clientThread.invoke(() ->
			{
				renderCallbackManager.unregister(rcb);
//				renderCallbackManager.unregister(DISABLE_RENDERING);

				if (!config.enableScenery() || !config.enableTerrain()){
					if (client.getGameState() == GameState.LOGGED_IN)
						client.setGameState(GameState.LOADING);
				}
			}
		);
	}

//	@Subscribe
//	public void onGameStateChanged(GameStateChanged e){
//		if (e.getGameState() == GameState.LOGGED_IN){
//
//		}
//	}

//	@Subscribe
//	public void onFocusChanged(FocusChanged event)
//	{
//		if (!event.isFocused() && config.disableRendering() && client.getGameState() == GameState.LOGGED_IN){
//			clientThread.invoke(() ->
//				{
//					renderCallbackManager.unregister(rcb);
//					renderCallbackManager.register(DISABLE_RENDERING);
//				}
//			);
//			log.debug("Focus lost: rendering disabled");
//		}
//		else
//		{
//			clientThread.invoke(() ->
//				{
//					renderCallbackManager.register(rcb);
//					renderCallbackManager.unregister(DISABLE_RENDERING);
//					client.setGameState(GameState.LOADING);
//				}
//			);
//			log.debug("Focus gained: rendering reenabled");
//		}
//	}

//	@Subscribe
//	public void onNotificationFired(NotificationFired event){
//		if (config.disableRendering())
//		{
//			clientThread.invoke(() ->
//				{
//					renderCallbackManager.register(rcb);
//					renderCallbackManager.unregister(DISABLE_RENDERING);
//					client.setGameState(GameState.LOADING);
//				}
//			);
//			log.debug("notification sent: rendering reenabled");
//		}
//	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event){
		if (!Objects.equals(event.getGroup(), BlindfoldPluginConfig.GROUP)){
			return;
		}
//		if (Objects.equals(event.getKey(), "disableRendering")){
//			if (Objects.equals(event.getNewValue(), "false")){
//				clientThread.invoke(() ->
//					{
//						renderCallbackManager.register(rcb);
//						renderCallbackManager.unregister(DISABLE_RENDERING);
//						client.setGameState(GameState.LOADING);
//					}
//				);
//			}
//			else {
//				clientThread.invoke(() ->
//					{
//						renderCallbackManager.unregister(rcb);
//						renderCallbackManager.register(DISABLE_RENDERING);
//						client.setGameState(GameState.LOADING);
//					}
//				);
//			}
//		}

		if (Objects.equals(event.getKey(), "enableTerrain") || Objects.equals(event.getKey(), "enableScenery"))
		{
			clientThread.invokeLater(() -> {
				if (client.getGameState() == GameState.LOGGED_IN)
					client.setGameState(GameState.LOADING);
			});
		}
	}
}
