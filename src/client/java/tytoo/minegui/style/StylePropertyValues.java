package tytoo.minegui.style;

import imgui.ImGuiStyle;
import tytoo.minegui.util.ResourceId;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class StylePropertyValues {
    private final EnumMap<StyleProperty, Object> values;
    private final ColorPalette colorPalette;
    private final ResourceId fontKey;
    private final Float fontSize;

    private StylePropertyValues(EnumMap<StyleProperty, Object> values, ColorPalette colorPalette, ResourceId fontKey, Float fontSize) {
        this.values = values;
        this.colorPalette = colorPalette;
        this.fontKey = fontKey;
        this.fontSize = fontSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static StylePropertyValues captureFrom(ImGuiStyle style) {
        return captureFrom(style, ColorPalette.fromStyle(style), null, null);
    }

    public static StylePropertyValues captureFrom(ImGuiStyle style, ColorPalette palette, ResourceId fontKey, Float fontSize) {
        Builder builder = builder();
        for (StyleProperty prop : StyleProperty.values()) {
            Object value = prop.readFrom(style);
            builder.set(prop, value);
        }
        return builder
                .colorPalette(palette)
                .fontKey(fontKey)
                .fontSize(fontSize)
                .build();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(StyleProperty property) {
        return (T) values.get(property);
    }

    public boolean has(StyleProperty property) {
        return values.containsKey(property);
    }

    public ColorPalette colorPalette() {
        return colorPalette;
    }

    public ResourceId fontKey() {
        return fontKey;
    }

    public Float fontSize() {
        return fontSize;
    }

    public void applyTo(ImGuiStyle style) {
        if (style == null) {
            return;
        }
        for (Map.Entry<StyleProperty, Object> entry : values.entrySet()) {
            entry.getKey().applyTo(style, entry.getValue());
        }
        if (colorPalette != null) {
            colorPalette.applyTo(style);
        }
    }

    public StylePropertyValues mergeWith(StylePropertyValues delta) {
        if (delta == null) {
            return this;
        }
        Builder builder = builder();
        for (StyleProperty prop : StyleProperty.values()) {
            Object deltaValue = delta.values.get(prop);
            if (deltaValue != null) {
                builder.set(prop, deltaValue);
            } else {
                Object baseValue = this.values.get(prop);
                if (baseValue != null) {
                    builder.set(prop, baseValue);
                }
            }
        }
        ColorPalette mergedPalette = delta.colorPalette != null
                ? (this.colorPalette != null ? this.colorPalette.mergedWith(delta.colorPalette) : delta.colorPalette)
                : this.colorPalette;
        ResourceId mergedFontKey = delta.fontKey != null ? delta.fontKey : this.fontKey;
        Float mergedFontSize = delta.fontSize != null ? delta.fontSize : this.fontSize;
        return builder
                .colorPalette(mergedPalette)
                .fontKey(mergedFontKey)
                .fontSize(mergedFontSize)
                .build();
    }

    public void validate(String context) {
        for (Map.Entry<StyleProperty, Object> entry : values.entrySet()) {
            StyleProperty prop = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            switch (prop.type()) {
                case FLOAT -> {
                    float f = (Float) value;
                    if (Float.isNaN(f) || Float.isInfinite(f)) {
                        throw new IllegalArgumentException(context + ": " + prop.name() + " must be finite");
                    }
                }
                case VEC2 -> {
                    Vec2 v = (Vec2) value;
                    if (Float.isNaN(v.x()) || Float.isInfinite(v.x())) {
                        throw new IllegalArgumentException(context + ": " + prop.name() + ".x must be finite");
                    }
                    if (Float.isNaN(v.y()) || Float.isInfinite(v.y())) {
                        throw new IllegalArgumentException(context + ": " + prop.name() + ".y must be finite");
                    }
                }
                default -> {
                }
            }
        }
    }

    public static final class Builder {
        private final EnumMap<StyleProperty, Object> values = new EnumMap<>(StyleProperty.class);
        private ColorPalette colorPalette;
        private ResourceId fontKey;
        private Float fontSize;

        private Builder() {
        }

        public Builder set(StyleProperty property, Object value) {
            if (value != null) {
                values.put(property, value);
            }
            return this;
        }

        public Builder alpha(float value) {
            return set(StyleProperty.ALPHA, value);
        }

        public Builder disabledAlpha(float value) {
            return set(StyleProperty.DISABLED_ALPHA, value);
        }

        public Builder windowPadding(float x, float y) {
            return set(StyleProperty.WINDOW_PADDING, Vec2.of(x, y));
        }

        public Builder windowPadding(Vec2 value) {
            return set(StyleProperty.WINDOW_PADDING, Objects.requireNonNull(value, "windowPadding"));
        }

        public Builder windowRounding(float value) {
            return set(StyleProperty.WINDOW_ROUNDING, value);
        }

        public Builder windowBorderSize(float value) {
            return set(StyleProperty.WINDOW_BORDER_SIZE, value);
        }

        public Builder windowMinSize(float x, float y) {
            return set(StyleProperty.WINDOW_MIN_SIZE, Vec2.of(x, y));
        }

        public Builder windowMinSize(Vec2 value) {
            return set(StyleProperty.WINDOW_MIN_SIZE, Objects.requireNonNull(value, "windowMinSize"));
        }

        public Builder windowTitleAlign(float x, float y) {
            return set(StyleProperty.WINDOW_TITLE_ALIGN, Vec2.of(x, y));
        }

        public Builder windowTitleAlign(Vec2 value) {
            return set(StyleProperty.WINDOW_TITLE_ALIGN, Objects.requireNonNull(value, "windowTitleAlign"));
        }

        public Builder windowMenuButtonPosition(int value) {
            return set(StyleProperty.WINDOW_MENU_BUTTON_POSITION, value);
        }

        public Builder childRounding(float value) {
            return set(StyleProperty.CHILD_ROUNDING, value);
        }

        public Builder childBorderSize(float value) {
            return set(StyleProperty.CHILD_BORDER_SIZE, value);
        }

        public Builder popupRounding(float value) {
            return set(StyleProperty.POPUP_ROUNDING, value);
        }

        public Builder popupBorderSize(float value) {
            return set(StyleProperty.POPUP_BORDER_SIZE, value);
        }

        public Builder framePadding(float x, float y) {
            return set(StyleProperty.FRAME_PADDING, Vec2.of(x, y));
        }

        public Builder framePadding(Vec2 value) {
            return set(StyleProperty.FRAME_PADDING, Objects.requireNonNull(value, "framePadding"));
        }

        public Builder frameRounding(float value) {
            return set(StyleProperty.FRAME_ROUNDING, value);
        }

        public Builder frameBorderSize(float value) {
            return set(StyleProperty.FRAME_BORDER_SIZE, value);
        }

        public Builder itemSpacing(float x, float y) {
            return set(StyleProperty.ITEM_SPACING, Vec2.of(x, y));
        }

        public Builder itemSpacing(Vec2 value) {
            return set(StyleProperty.ITEM_SPACING, Objects.requireNonNull(value, "itemSpacing"));
        }

        public Builder itemInnerSpacing(float x, float y) {
            return set(StyleProperty.ITEM_INNER_SPACING, Vec2.of(x, y));
        }

        public Builder itemInnerSpacing(Vec2 value) {
            return set(StyleProperty.ITEM_INNER_SPACING, Objects.requireNonNull(value, "itemInnerSpacing"));
        }

        public Builder cellPadding(float x, float y) {
            return set(StyleProperty.CELL_PADDING, Vec2.of(x, y));
        }

        public Builder cellPadding(Vec2 value) {
            return set(StyleProperty.CELL_PADDING, Objects.requireNonNull(value, "cellPadding"));
        }

        public Builder touchExtraPadding(float x, float y) {
            return set(StyleProperty.TOUCH_EXTRA_PADDING, Vec2.of(x, y));
        }

        public Builder touchExtraPadding(Vec2 value) {
            return set(StyleProperty.TOUCH_EXTRA_PADDING, Objects.requireNonNull(value, "touchExtraPadding"));
        }

        public Builder indentSpacing(float value) {
            return set(StyleProperty.INDENT_SPACING, value);
        }

        public Builder columnsMinSpacing(float value) {
            return set(StyleProperty.COLUMNS_MIN_SPACING, value);
        }

        public Builder scrollbarSize(float value) {
            return set(StyleProperty.SCROLLBAR_SIZE, value);
        }

        public Builder scrollbarRounding(float value) {
            return set(StyleProperty.SCROLLBAR_ROUNDING, value);
        }

        public Builder grabMinSize(float value) {
            return set(StyleProperty.GRAB_MIN_SIZE, value);
        }

        public Builder grabRounding(float value) {
            return set(StyleProperty.GRAB_ROUNDING, value);
        }

        public Builder logSliderDeadzone(float value) {
            return set(StyleProperty.LOG_SLIDER_DEADZONE, value);
        }

        public Builder tabRounding(float value) {
            return set(StyleProperty.TAB_ROUNDING, value);
        }

        public Builder tabBorderSize(float value) {
            return set(StyleProperty.TAB_BORDER_SIZE, value);
        }

        public Builder tabMinWidthForCloseButton(float value) {
            return set(StyleProperty.TAB_MIN_WIDTH_FOR_CLOSE_BUTTON, value);
        }

        public Builder colorButtonPosition(int value) {
            return set(StyleProperty.COLOR_BUTTON_POSITION, value);
        }

        public Builder buttonTextAlign(float x, float y) {
            return set(StyleProperty.BUTTON_TEXT_ALIGN, Vec2.of(x, y));
        }

        public Builder buttonTextAlign(Vec2 value) {
            return set(StyleProperty.BUTTON_TEXT_ALIGN, Objects.requireNonNull(value, "buttonTextAlign"));
        }

        public Builder selectableTextAlign(float x, float y) {
            return set(StyleProperty.SELECTABLE_TEXT_ALIGN, Vec2.of(x, y));
        }

        public Builder selectableTextAlign(Vec2 value) {
            return set(StyleProperty.SELECTABLE_TEXT_ALIGN, Objects.requireNonNull(value, "selectableTextAlign"));
        }

        public Builder displayWindowPadding(float x, float y) {
            return set(StyleProperty.DISPLAY_WINDOW_PADDING, Vec2.of(x, y));
        }

        public Builder displayWindowPadding(Vec2 value) {
            return set(StyleProperty.DISPLAY_WINDOW_PADDING, Objects.requireNonNull(value, "displayWindowPadding"));
        }

        public Builder displaySafeAreaPadding(float x, float y) {
            return set(StyleProperty.DISPLAY_SAFE_AREA_PADDING, Vec2.of(x, y));
        }

        public Builder displaySafeAreaPadding(Vec2 value) {
            return set(StyleProperty.DISPLAY_SAFE_AREA_PADDING, Objects.requireNonNull(value, "displaySafeAreaPadding"));
        }

        public Builder mouseCursorScale(float value) {
            return set(StyleProperty.MOUSE_CURSOR_SCALE, value);
        }

        public Builder antiAliasedLines(boolean value) {
            return set(StyleProperty.ANTI_ALIASED_LINES, value);
        }

        public Builder antiAliasedLinesUseTex(boolean value) {
            return set(StyleProperty.ANTI_ALIASED_LINES_USE_TEX, value);
        }

        public Builder antiAliasedFill(boolean value) {
            return set(StyleProperty.ANTI_ALIASED_FILL, value);
        }

        public Builder curveTessellationTol(float value) {
            return set(StyleProperty.CURVE_TESSELLATION_TOL, value);
        }

        public Builder circleTessellationMaxError(float value) {
            return set(StyleProperty.CIRCLE_TESSELLATION_MAX_ERROR, value);
        }

        public Builder colorPalette(ColorPalette value) {
            this.colorPalette = value;
            return this;
        }

        public Builder fontKey(ResourceId value) {
            this.fontKey = value;
            return this;
        }

        public Builder fontSize(Float value) {
            this.fontSize = value;
            return this;
        }

        public Builder fromStyle(ImGuiStyle style) {
            if (style == null) {
                return this;
            }
            for (StyleProperty prop : StyleProperty.values()) {
                set(prop, prop.readFrom(style));
            }
            return this;
        }

        public Builder fromValues(StylePropertyValues source) {
            if (source == null) {
                return this;
            }
            for (StyleProperty prop : StyleProperty.values()) {
                Object value = source.values.get(prop);
                if (value != null) {
                    set(prop, value);
                }
            }
            if (source.colorPalette != null) {
                colorPalette(source.colorPalette);
            }
            if (source.fontKey != null) {
                fontKey(source.fontKey);
            }
            if (source.fontSize != null) {
                fontSize(source.fontSize);
            }
            return this;
        }

        public StylePropertyValues build() {
            return new StylePropertyValues(
                    new EnumMap<>(values),
                    colorPalette,
                    fontKey,
                    fontSize
            );
        }
    }
}
