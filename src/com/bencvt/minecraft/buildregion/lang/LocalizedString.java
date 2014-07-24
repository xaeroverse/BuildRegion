package com.bencvt.minecraft.buildregion.lang;

import java.io.IOException;
import java.util.IllegalFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;

import com.mumfrey.liteloader.util.ModUtilities;

/**
 * Internationalization (i18n): ideally, every string that the user can see
 * during normal operation should be translated here.
 * 
 * @author bencvt
 */
public abstract class LocalizedString {
    private static Method translateKeyPrivate;
    private static Locale i18nLocale;
    static {
        try {
            // reflect twice from I18n to get translateKeyPrivate
            // TODO: Obfuscation 1.7.10
            Field i18nLocaleField = I18n.class.getDeclaredField(ModUtilities.getObfuscatedFieldName("i18nLocale", "a", "field_135054_a"));
            i18nLocaleField.setAccessible(true);
            i18nLocale = (Locale) i18nLocaleField.get(null);

            translateKeyPrivate = Locale.class.getDeclaredMethod(ModUtilities.getObfuscatedFieldName("translateKeyPrivate", "b", "func_135026_c"), new Class<?>[] { String.class });
            translateKeyPrivate.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            translateKeyPrivate = null;
            i18nLocale = null;
        }
    }

    /**
     * Attempt to translate a string, optionally with String.format arguments.
     * <p>
     * If any of the String.format arguments is an enum value, it is
     * translated as well.
     * 
     * @return a non-null string, translated and formatted if possible.
     */
    public static String translate(String key, Object ... args) {
        String result = lookup(key);
        if (result != null) {
            if (result.startsWith("$MC:")) {
                // The property value is specifying a key from the Minecraft
                // translations. Perform another lookup.
                result = I18n.format(result.substring(4), args);
            } else if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        args[i] = "null";
                    } else if (args[i] instanceof Enum) {
                        args[i] = translate((Enum) args[i]);
                    }
                }
                try {
                    result = I18n.format(result, args);
                } catch (IllegalFormatException e) {
                    // leave result as it was
                }
            }
        }
        return result == null ? key : result;
    }

    /**
     * Attempt to translate an enum value.
     * @return a non-null string, translated if possible.
     */
    public static String translate(Enum<?> e) {
        if (e == null) {
            return "null";
        }
        String propName = "enum." + e.getClass().getSimpleName().toLowerCase();
        if (lookup(propName) != null) {
            return translate(propName + "." + e.toString().toLowerCase());
        } else {
            return e.toString().toLowerCase();
        }
    }

    /**
     * Look up a localized string using Mojang's assets system.
     * @return a string from the localization mappings, or null if there is
     *         no entry.
     */
    private static String lookup(String key) {
        String result;
        try {
            result = (String) translateKeyPrivate.invoke(i18nLocale, key);
        } catch (Exception ex) {
            ex.printStackTrace();
            result = null;
        }
        if (result == null || result.equals(key)) {
            return null;
        }

        // Replace with proper newlines for FontRenderer.listFormattedStringToWidth()
        result = result.replace("\\n", "\n");
        return result;
    }
}
