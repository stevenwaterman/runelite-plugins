package com.stevenwaterman.blindfold;

import net.runelite.api.Projection;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Texture;
import net.runelite.api.hooks.DrawCallbacks;

public class DisableRenderCallbacks implements DrawCallbacks
{
	@Override
	public void draw(Projection projection, Scene scene, Renderable renderable, int i, int i1, int i2, int i3, long l)
	{

	}

	@Override
	public void drawScenePaint(Scene scene, SceneTilePaint sceneTilePaint, int i, int i1, int i2)
	{

	}

	@Override
	public void drawSceneTileModel(Scene scene, SceneTileModel sceneTileModel, int i, int i1)
	{

	}

	@Override
	public void draw(int overlayColor)
	{

	}

	@Override
	public void drawScene(double v, double v1, double v2, double v3, double v4, int i)
	{

	}

	@Override
	public void postDrawScene()
	{

	}

	@Override
	public void animate(Texture texture, int diff)
	{

	}

	@Override
	public void loadScene(Scene scene)
	{

	}

	@Override
	public void swapScene(Scene scene)
	{

	}

	@Override
	public boolean tileInFrustum(Scene scene, float pitchSin, float pitchCos, float yawSin, float yawCos, int cameraX, int cameraY, int cameraZ, int plane, int msx, int msy)
	{
		return false;
	}
}
