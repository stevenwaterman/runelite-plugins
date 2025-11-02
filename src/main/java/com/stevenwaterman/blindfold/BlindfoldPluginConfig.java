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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup(BlindfoldPluginConfig.GROUP)
public interface BlindfoldPluginConfig extends Config
{
	String GROUP = "blindfold";

	@ConfigItem(
		keyName = "hotkey",
		name = "Toggle hotkey",
		description = "Toggle plugin functionality",
		position = 1
	)
	default Keybind hotKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "enableUI",
			name = "Show UI",
			description = "Disable this to remove ALL interface elements from the screen. Useful as a greenscreen for content creation.",
			position = 1
	)
	default boolean enableUi()
	{
		return true;
	}

	@ConfigItem(
			keyName = "enableTerrain",
			name = "Show Terrain",
			description = "Disable this to hide the terrain / landscape.",
			position = 2
	)
	default boolean enableTerrain()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableScenery",
			name = "Show Scenery",
			description = "Disable this to hide the static scenery.",
			position = 3
	)
	default boolean enableScenery()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableEntities",
			name = "Show Entities",
			description = "Disable this to hide NPCs, Players, projectiles, and ground items.",
			position = 4
	)
	default boolean enableEntities()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableRuneLiteObjects",
			name = "Show RuneLite Objects (broken)",
			description = "Disable this to hide objects spawned in by RuneLite plugins.<br>Currently non-functional",
			position = 5
	)
	default boolean enableRuneLiteObjects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "disableRendering",
		name = "Pause when unfocused (broken)",
		description = "Stops the screen from rendering when client is unfocused.<br>Rendering resumes when a notification is received.<br>Currently non-functional",
		position = 6
	)
	default boolean disableRendering()
	{
		return false;
	}
}
