package com.bencvt.minecraft.buildregion;

import java.io.File;

import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.buildregion.ui.CustomKeyBinding;
import com.bencvt.minecraft.buildregion.Controller;

import com.mumfrey.liteloader.Tickable;

/**
 * Front-end class, does the bare minimum of processing. Simply instantiates
 * the Controller and passes ModLoader events to it.
 * 
 * @author bencvt
 */
public class LiteModBuildRegion implements Tickable {
    private Controller controller;

    @Override
    public String getName() {
        return "BuildRegion";
    }

    @Override
    public String getVersion() {
        return Controller.MOD_VERSION + " [" + Controller.MINECRAFT_VERSION + "]";
    }

    @Override
    public void init(File configPath) {
        controller = new Controller(this);
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
        //
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        // include partial ticks
        if (inGame) {
            controller.onRenderTick();

            // (in situ) replacement for keyboardEvent
            for (CustomKeyBinding key : controller.getInputManager().ALL_KEYBINDS) {
                if (key.isPressed()) {
                    controller.getInputManager().handleKeyboardEvent(key, false);
                }
            }
        }
    }
}
