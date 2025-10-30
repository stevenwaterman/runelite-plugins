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
import java.util.HashSet;
import java.util.Objects;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.IntProjection;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Projectile;
import net.runelite.api.Projection;
import net.runelite.api.Renderable;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

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

	@Provides
	BlindfoldPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BlindfoldPluginConfig.class);
	}

	private final RenderCallback DISABLE_RENDERING = new RenderCallback(){
		@Override
		public boolean drawEntity(Renderable renderable, boolean ui)
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
		public boolean drawEntity(Renderable renderable, boolean ui)
		{
			boolean isRuneLiteObject = renderable instanceof RuneLiteObject;
			boolean render =
				renderable == client.getLocalPlayer() ||
				config.enableScenery() && (
					renderable instanceof Model ||
					renderable instanceof ModelData ||
					(!isRuneLiteObject && renderable instanceof GraphicsObject) ||
					renderable instanceof DynamicObject
				) ||
				config.enableEntities() && (
					renderable instanceof Projectile ||
					renderable instanceof TileItem ||
					renderable instanceof Actor
				) ||
				isRuneLiteObject && config.enableRuneLiteObjects();
			// if (!render) check clickbox
			return render;
		}

		@Override
		public boolean drawTile(Scene scene, Tile tile)
		{
			return config.enableTerrain();
		}

		@Override
		public boolean drawObject(Scene scene, TileObject object)
		{
//			if (!render) check clickbox
			return config.enableScenery();
		}
	};

	@Override
	protected void startUp()
	{
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
		overlayManager.remove(overlay);

		clientThread.invoke(() ->
			{
				renderCallbackManager.unregister(rcb);
//				renderCallbackManager.unregister(DISABLE_RENDERING);
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
//		if (client.getGameState() == GameState.LOGGED_IN && config.disableRendering() && !event.isFocused()){
//			clientThread.invoke(() ->
//				{
//					renderCallbackManager.unregister(rcb);
//					renderCallbackManager.register(DISABLE_RENDERING);
//					client.setGameState(GameState.LOADING);
//				}
//			);
//			log.debug("Focus changed: rendering disabled");
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
//			log.debug("Focus changed: rendering reenabled");
//		}
//	}

	@Subscribe
	public void onNotificationFired(NotificationFired event){
		clientThread.invoke(() ->
			{
				renderCallbackManager.register(rcb);
//				renderCallbackManager.unregister(DISABLE_RENDERING);
				client.setGameState(GameState.LOADING);
			}
		);
		log.debug("notification sent: rendering reenabled");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event){
		if (!Objects.equals(event.getGroup(), BlindfoldPluginConfig.GROUP)){
			return;
		}
		if (Objects.equals(event.getKey(), "disableRendering")){
			if (Objects.equals(event.getNewValue(), "false")){
				clientThread.invoke(() ->
					{
						renderCallbackManager.register(rcb);
//						renderCallbackManager.unregister(DISABLE_RENDERING);
						client.setGameState(GameState.LOADING);
					}
				);
			}
			else {
				clientThread.invoke(() ->
					{
						renderCallbackManager.unregister(rcb);
//						renderCallbackManager.register(DISABLE_RENDERING);
						client.setGameState(GameState.LOADING);
					}
				);
			}
		}

		if (Objects.equals(event.getKey(), "enableTerrain") || Objects.equals(event.getKey(), "enableScenery"))
		{
			clientThread.invokeLater(() -> {
				if (client.getGameState() == GameState.LOGGED_IN)
					client.setGameState(GameState.LOADING);
			});
		}
	}

	// Check the clickbox even if not drawn
	public void checkClickbox(Projection projection, Scene scene, Renderable renderable, int orientation, int x, int y, int z, long hash)
	{
		Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
		if (model == null)
			return;

		// Apply height to renderable from the model
		if (model != renderable)
			renderable.setModelHeight(model.getModelHeight());

		model.calculateBoundsCylinder();

		if (projection instanceof IntProjection)
		{
			IntProjection p = (IntProjection) projection;
			if (!isVisible(model, p.getPitchSin(), p.getPitchCos(), p.getYawSin(), p.getYawCos(), x - p.getCameraX(), y - p.getCameraY(), z - p.getCameraZ()))
			{
				return;
			}
		}

		client.checkClickbox(projection, model, orientation, x, y, z, hash);
	}

	/**
	 * Check is a model is visible and should be drawn.
	 */
	private boolean isVisible(Model model, float pitchSin, float pitchCos, float yawSin, float yawCos, int x, int y, int z)
	{
		final int xzMag = model.getXYZMag();
		final int bottomY = model.getBottomY();
		final int zoom = client.get3dZoom();
		final int modelHeight = model.getModelHeight();

		int Rasterizer3D_clipMidX2 = client.getRasterizer3D_clipMidX2(); // width / 2
		int Rasterizer3D_clipNegativeMidX = client.getRasterizer3D_clipNegativeMidX(); // -width / 2
		int Rasterizer3D_clipNegativeMidY = client.getRasterizer3D_clipNegativeMidY(); // -height / 2
		int Rasterizer3D_clipMidY2 = client.getRasterizer3D_clipMidY2(); // height / 2

		float var11 = yawCos * z - yawSin * x;
		float var12 = pitchSin * y + pitchCos * var11;
		float var13 = pitchCos * xzMag;
		float depth = var12 + var13;
		if (depth > 50)
		{
			float rx = z * yawSin + yawCos * x;
			float var16 = (rx - xzMag) * zoom;
			if (var16 / depth < Rasterizer3D_clipMidX2)
			{
				float var17 = (rx + xzMag) * zoom;
				if (var17 / depth > Rasterizer3D_clipNegativeMidX)
				{
					float ry = pitchCos * y - var11 * pitchSin;
					float yheight = pitchSin * xzMag;
					float ybottom = pitchCos * bottomY + yheight; // use bottom height instead of y pos for height
					float var20 = (ry + ybottom) * zoom;
					if (var20 / depth > Rasterizer3D_clipNegativeMidY)
					{
						float ytop = pitchCos * modelHeight + yheight;
						float var22 = (ry - ytop) * zoom;
						return var22 / depth < Rasterizer3D_clipMidY2;
					}
				}
			}
		}
		return false;
	}
}
