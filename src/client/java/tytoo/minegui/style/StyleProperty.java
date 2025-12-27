package tytoo.minegui.style;

import imgui.ImGuiStyle;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum StyleProperty {
    ALPHA(PropertyType.FLOAT,
            ImGuiStyle::getAlpha,
            (style, val) -> style.setAlpha((Float) val)),
    DISABLED_ALPHA(PropertyType.FLOAT,
            ImGuiStyle::getDisabledAlpha,
            (style, val) -> style.setDisabledAlpha((Float) val)),
    WINDOW_PADDING(PropertyType.VEC2,
            style -> Vec2.of(style.getWindowPaddingX(), style.getWindowPaddingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setWindowPadding(v.x(), v.y());
            }),
    WINDOW_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getWindowRounding,
            (style, val) -> style.setWindowRounding((Float) val)),
    WINDOW_BORDER_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getWindowBorderSize,
            (style, val) -> style.setWindowBorderSize((Float) val)),
    WINDOW_MIN_SIZE(PropertyType.VEC2,
            style -> Vec2.of(style.getWindowMinSizeX(), style.getWindowMinSizeY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setWindowMinSize(v.x(), v.y());
            }),
    WINDOW_TITLE_ALIGN(PropertyType.VEC2,
            style -> Vec2.of(style.getWindowTitleAlignX(), style.getWindowTitleAlignY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setWindowTitleAlign(v.x(), v.y());
            }),
    WINDOW_MENU_BUTTON_POSITION(PropertyType.INT,
            ImGuiStyle::getWindowMenuButtonPosition,
            (style, val) -> style.setWindowMenuButtonPosition((Integer) val)),
    CHILD_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getChildRounding,
            (style, val) -> style.setChildRounding((Float) val)),
    CHILD_BORDER_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getChildBorderSize,
            (style, val) -> style.setChildBorderSize((Float) val)),
    POPUP_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getPopupRounding,
            (style, val) -> style.setPopupRounding((Float) val)),
    POPUP_BORDER_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getPopupBorderSize,
            (style, val) -> style.setPopupBorderSize((Float) val)),
    FRAME_PADDING(PropertyType.VEC2,
            style -> Vec2.of(style.getFramePaddingX(), style.getFramePaddingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setFramePadding(v.x(), v.y());
            }),
    FRAME_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getFrameRounding,
            (style, val) -> style.setFrameRounding((Float) val)),
    FRAME_BORDER_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getFrameBorderSize,
            (style, val) -> style.setFrameBorderSize((Float) val)),
    ITEM_SPACING(PropertyType.VEC2,
            style -> Vec2.of(style.getItemSpacingX(), style.getItemSpacingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setItemSpacing(v.x(), v.y());
            }),
    ITEM_INNER_SPACING(PropertyType.VEC2,
            style -> Vec2.of(style.getItemInnerSpacingX(), style.getItemInnerSpacingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setItemInnerSpacing(v.x(), v.y());
            }),
    CELL_PADDING(PropertyType.VEC2,
            style -> Vec2.of(style.getCellPaddingX(), style.getCellPaddingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setCellPadding(v.x(), v.y());
            }),
    TOUCH_EXTRA_PADDING(PropertyType.VEC2,
            style -> Vec2.of(style.getTouchExtraPaddingX(), style.getTouchExtraPaddingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setTouchExtraPadding(v.x(), v.y());
            }),
    INDENT_SPACING(PropertyType.FLOAT,
            ImGuiStyle::getIndentSpacing,
            (style, val) -> style.setIndentSpacing((Float) val)),
    COLUMNS_MIN_SPACING(PropertyType.FLOAT,
            ImGuiStyle::getColumnsMinSpacing,
            (style, val) -> style.setColumnsMinSpacing((Float) val)),
    SCROLLBAR_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getScrollbarSize,
            (style, val) -> style.setScrollbarSize((Float) val)),
    SCROLLBAR_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getScrollbarRounding,
            (style, val) -> style.setScrollbarRounding((Float) val)),
    GRAB_MIN_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getGrabMinSize,
            (style, val) -> style.setGrabMinSize((Float) val)),
    GRAB_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getGrabRounding,
            (style, val) -> style.setGrabRounding((Float) val)),
    LOG_SLIDER_DEADZONE(PropertyType.FLOAT,
            ImGuiStyle::getLogSliderDeadzone,
            (style, val) -> style.setLogSliderDeadzone((Float) val)),
    TAB_ROUNDING(PropertyType.FLOAT,
            ImGuiStyle::getTabRounding,
            (style, val) -> style.setTabRounding((Float) val)),
    TAB_BORDER_SIZE(PropertyType.FLOAT,
            ImGuiStyle::getTabBorderSize,
            (style, val) -> style.setTabBorderSize((Float) val)),
    TAB_MIN_WIDTH_FOR_CLOSE_BUTTON(PropertyType.FLOAT,
            ImGuiStyle::getTabMinWidthForCloseButton,
            (style, val) -> style.setTabMinWidthForCloseButton((Float) val)),
    COLOR_BUTTON_POSITION(PropertyType.INT,
            ImGuiStyle::getColorButtonPosition,
            (style, val) -> style.setColorButtonPosition((Integer) val)),
    BUTTON_TEXT_ALIGN(PropertyType.VEC2,
            style -> Vec2.of(style.getButtonTextAlignX(), style.getButtonTextAlignY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setButtonTextAlign(v.x(), v.y());
            }),
    SELECTABLE_TEXT_ALIGN(PropertyType.VEC2,
            style -> Vec2.of(style.getSelectableTextAlignX(), style.getSelectableTextAlignY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setSelectableTextAlign(v.x(), v.y());
            }),
    DISPLAY_WINDOW_PADDING(PropertyType.VEC2,
            style -> Vec2.of(style.getDisplayWindowPaddingX(), style.getDisplayWindowPaddingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setDisplayWindowPadding(v.x(), v.y());
            }),
    DISPLAY_SAFE_AREA_PADDING(PropertyType.VEC2,
            style -> Vec2.of(style.getDisplaySafeAreaPaddingX(), style.getDisplaySafeAreaPaddingY()),
            (style, val) -> {
                Vec2 v = (Vec2) val;
                style.setDisplaySafeAreaPadding(v.x(), v.y());
            }),
    MOUSE_CURSOR_SCALE(PropertyType.FLOAT,
            ImGuiStyle::getMouseCursorScale,
            (style, val) -> style.setMouseCursorScale((Float) val)),
    ANTI_ALIASED_LINES(PropertyType.BOOLEAN,
            ImGuiStyle::getAntiAliasedLines,
            (style, val) -> style.setAntiAliasedLines((Boolean) val)),
    ANTI_ALIASED_LINES_USE_TEX(PropertyType.BOOLEAN,
            ImGuiStyle::getAntiAliasedLinesUseTex,
            (style, val) -> style.setAntiAliasedLinesUseTex((Boolean) val)),
    ANTI_ALIASED_FILL(PropertyType.BOOLEAN,
            ImGuiStyle::getAntiAliasedFill,
            (style, val) -> style.setAntiAliasedFill((Boolean) val)),
    CURVE_TESSELLATION_TOL(PropertyType.FLOAT,
            ImGuiStyle::getCurveTessellationTol,
            (style, val) -> style.setCurveTessellationTol((Float) val)),
    CIRCLE_TESSELLATION_MAX_ERROR(PropertyType.FLOAT,
            ImGuiStyle::getCircleTessellationMaxError,
            (style, val) -> style.setCircleTessellationMaxError((Float) val));

    private final PropertyType type;
    private final Function<ImGuiStyle, Object> getter;
    private final BiConsumer<ImGuiStyle, Object> setter;

    StyleProperty(PropertyType type, Function<ImGuiStyle, Object> getter, BiConsumer<ImGuiStyle, Object> setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    public PropertyType type() {
        return type;
    }

    public Object readFrom(ImGuiStyle style) {
        return getter.apply(style);
    }

    public void applyTo(ImGuiStyle style, Object value) {
        if (value != null) {
            setter.accept(style, value);
        }
    }

    public enum PropertyType {
        FLOAT,
        INT,
        BOOLEAN,
        VEC2
    }
}
