package com.bencvt.minecraft.buildregion.ui;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;

/**
 * Extends Minecraft's KeyBinding class to include various utility methods.
 * 
 * @author bencvt
 */
public class CustomKeyBinding extends KeyBinding {
    private final boolean allowRepeat;
    private boolean registered;

    public CustomKeyBinding(int defaultKeyCode, boolean allowRepeat, String bindingId, String description) {
        super(bindingId, defaultKeyCode, "buildregion.name");
        this.allowRepeat = allowRepeat;
        // TODO: what about localizations for other languages?
    }

    public void register(LiteMod mod) {
        if (!registered) {
            // allowRepeat?
            LiteLoader.getInput().registerKeyBinding(this);
            registered = true;
        }
    }

    public String getKeyName() {
        if (this.getKeyCode() < 0) {
            return "MOUSE";
        }
        String keyName = Keyboard.getKeyName(this.getKeyCode());
        if (keyName.equals("LBRACKET")) {
            return "[";
        } else if (keyName.equals("RBRACKET")) {
            return "]";
        } else {
            return keyName;
        }
    }

    public String getKeyName(boolean withShiftOrCtrl) {
        if (withShiftOrCtrl) {
            return "shift- " + i18n("or") + " ctrl-" + getKeyName();
        } else {
            return getKeyName();
        }
    }

    public String getKeyNameColored(boolean withShiftOrCtrl) {
        if (withShiftOrCtrl) {
            return getShiftOrCtrlColored() + getKeyName() + "\u00a7r";
        } else {
            return "\u00a7c" + getKeyName() + "\u00a7r";
        }
    }

    public static String getShiftOrCtrlColored() {
        return "\u00a7cshift-\u00a7r " + i18n("or") +" \u00a7cctrl-\u00a7c";
    }

    public static String i18n(String key, Object ... args) {
        return LocalizedString.translate(key, args);
    }
}
