package com.bencvt.minecraft.buildregion.ui.window;

import java.util.LinkedHashMap;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;
import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.buildregion.lang.LocalizedString;

/**
 * A multi-select Minecraft GUI control.
 * @param <T> an enum type
 * 
 * @author bencvt
 */
public class GuiSelectEnum<T extends Enum<?>> extends GuiLabeledControl {
    public static final int OPTION_SPACING = 2;
    public static final int PAD_LEFT = 2;
    public static final int PAD_RIGHT = 2;
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;
    public static final int ANIM_DURATION = 350;

    public class Option {
        protected T value;
        protected String text;
        protected String textMouseOver;
        protected ReadonlyColor color;
        protected int xBegin; // relative to the control column's x coordinate
        protected int xEnd;   // relative to the control column's x coordinate
        protected Color alpha;
        protected Option prev;
        protected Option next;

        public boolean isMouseOver(int xMouse, int yMouse) {
            return parent.isMouseOver(
                    xMouse, getControlXOffset() + xBegin, getControlXOffset() + xEnd,
                    yMouse, yPosition, yPosition + height);
        }
    }
    private final boolean enableMousewheel;
    private final LinkedHashMap<T, Option> options;
    private int labelWidth;
    private boolean allowSetNull;
    private T selectedValue;

    public GuiSelectEnum(GuiScreenBase parent, String text, T[] values, ReadonlyColor color) {
        super(parent, text);
        options = new LinkedHashMap<T, Option>();
        int controlWidth = 0;
        Option prevOption = null;
        for (T value : values) {
            if (value == com.bencvt.minecraft.buildregion.region.RegionType.CYLINDER || value == com.bencvt.minecraft.buildregion.region.RegionType.SPHERE) {
                continue; // XXX: temporarily disable region types not ready for release
            }
            Option option = new Option();
            option.value = value;
            option.text = LocalizedString.translate(value);
            option.textMouseOver = "\u00a7n" + option.text;
            option.color = color;
            option.alpha = Color.TRANSPARENT_WHITE.copy();
            if (controlWidth > 0) {
                controlWidth += OPTION_SPACING;
            }
            option.xBegin = controlWidth;
            controlWidth += PAD_LEFT + parent.getFontRenderer().getStringWidth(option.text) + PAD_RIGHT;
            option.xEnd = controlWidth;
            option.prev = prevOption;
            if (prevOption != null) {
                prevOption.next = option;
            }
            prevOption = option;
            options.put(value, option);
        }
        enableMousewheel = options.size() >= 8;
        selectedValue = null;
        setControlWidth(controlWidth); // also sets width
        height = PAD_TOP + parent.getFontRenderer().FONT_HEIGHT + PAD_BOTTOM;
    }

    private Option getOptionForValue(T value) {
        Option option = options.get(value);
        if (value != null && option == null) {
            throw new IllegalArgumentException("invalid value: " + String.valueOf(value));
        }
        return option;
    }

    public boolean isAllowSetNull() {
        return allowSetNull;
    }
    public GuiSelectEnum<T> setAllowSetNull(boolean allowSetNull) {
        this.allowSetNull = allowSetNull;
        return this;
    }

    public GuiSelectEnum<T> setOptionColor(T value, ReadonlyColor color) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        getOptionForValue(value).color = color;
        return this;
    }

    public T getSelectedValue() {
        return selectedValue;
    }
    public GuiSelectEnum<T> setSelectedValue(T value, boolean animate) {
        Option prev = getOptionForValue(selectedValue);
        Option cur = getOptionForValue(value);
        selectedValue = value;

        if (prev == cur) {
            return this;
        }

        if (prev != null) {
            // Fade out previous selection
            if (animate) {
                prev.alpha.animateStart(Color.TRANSPARENT_WHITE, ANIM_DURATION);
            } else {
                prev.alpha.animateStop().set(Color.TRANSPARENT_WHITE);
            }
        }

        if (cur != null) {
            // Fade in current selection
            if (animate) {
                cur.alpha.animateStart(Color.WHITE, ANIM_DURATION);
            } else {
                cur.alpha.animateStop().set(Color.WHITE);
            }
        }

        return this;
    }

    @Override
    protected int getLabelYOffset() {
        return PAD_TOP;
    }

    @Override
    protected void mouseWheelScrolled(boolean back) {
        if (!enableMousewheel) {
            return;
        }
        Option cur = getOptionForValue(selectedValue);
        if (back && cur.prev != null) {
            setSelectedValue(cur.prev.value, true);
            parent.actionPerformedByControl(this);
        } else if (!back && cur.next != null) {
            setSelectedValue(cur.next.value, true);
            parent.actionPerformedByControl(this);
        }
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        int xOffset = getControlXOffset();
        for (Option option : options.values()) {
            // Draw background rectangle.
            tempColor.set(option.color).scaleAlpha(option.alpha.getAlpha());
            drawRect(
                    xOffset + option.xBegin,  yPosition,
                    xOffset + option.xEnd,    yPosition + height,
                    tempColor.getARGB());

            // Draw foreground text.
            if (option.isMouseOver(xMouse, yMouse)) {
                parent.getFontRenderer().drawString(option.textMouseOver,
                        xOffset + option.xBegin + PAD_LEFT, yPosition + PAD_TOP,
                        CONTROL_MOUSEOVER_ARGB);
            } else {
                parent.getFontRenderer().drawString(option.text,
                        xOffset + option.xBegin + PAD_LEFT, yPosition + PAD_TOP,
                        CONTROL_ENABLED_ARGB);
            }
        }
    }

    /** so we don't create a bunch of temporary objects when rendering */
    private static final Color tempColor = Color.BLACK.copy();

    @Override
    public boolean mousePressed(Minecraft minecraft, int xMouse, int yMouse) {
        if (!super.mousePressed(minecraft, xMouse, yMouse)) {
            return false;
        }
        int xOffset = getControlXOffset();
        for (Option option : options.values()) {
            if (xMouse >= option.xBegin + xOffset && xMouse <= option.xEnd + xOffset) {
                if (allowSetNull && selectedValue == option.value) {
                    setSelectedValue(null, true);
                } else {
                    setSelectedValue(option.value, true);
                }
                return true;
            }
        }
        // clicked in the label or the empty space between options
        return false;
    }
}
