package tytoo.minegui.style;

import java.util.Objects;

final class StyleValidation {
    private StyleValidation() {
    }

    static void validateDescriptor(MGStyleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        ensureFinite(descriptor.getAlpha(), "alpha");
        ensureFinite(descriptor.getDisabledAlpha(), "disabledAlpha");
        ensureFinite(descriptor.getWindowPadding(), "windowPadding");
        ensureFinite(descriptor.getWindowRounding(), "windowRounding");
        ensureFinite(descriptor.getWindowBorderSize(), "windowBorderSize");
        ensureFinite(descriptor.getWindowMinSize(), "windowMinSize");
        ensureFinite(descriptor.getWindowTitleAlign(), "windowTitleAlign");
        ensureFinite(descriptor.getChildRounding(), "childRounding");
        ensureFinite(descriptor.getChildBorderSize(), "childBorderSize");
        ensureFinite(descriptor.getPopupRounding(), "popupRounding");
        ensureFinite(descriptor.getPopupBorderSize(), "popupBorderSize");
        ensureFinite(descriptor.getFramePadding(), "framePadding");
        ensureFinite(descriptor.getFrameRounding(), "frameRounding");
        ensureFinite(descriptor.getFrameBorderSize(), "frameBorderSize");
        ensureFinite(descriptor.getItemSpacing(), "itemSpacing");
        ensureFinite(descriptor.getItemInnerSpacing(), "itemInnerSpacing");
        ensureFinite(descriptor.getCellPadding(), "cellPadding");
        ensureFinite(descriptor.getTouchExtraPadding(), "touchExtraPadding");
        ensureFinite(descriptor.getIndentSpacing(), "indentSpacing");
        ensureFinite(descriptor.getColumnsMinSpacing(), "columnsMinSpacing");
        ensureFinite(descriptor.getScrollbarSize(), "scrollbarSize");
        ensureFinite(descriptor.getScrollbarRounding(), "scrollbarRounding");
        ensureFinite(descriptor.getGrabMinSize(), "grabMinSize");
        ensureFinite(descriptor.getGrabRounding(), "grabRounding");
        ensureFinite(descriptor.getLogSliderDeadzone(), "logSliderDeadzone");
        ensureFinite(descriptor.getTabRounding(), "tabRounding");
        ensureFinite(descriptor.getTabBorderSize(), "tabBorderSize");
        ensureFinite(descriptor.getTabMinWidthForCloseButton(), "tabMinWidthForCloseButton");
        ensureFinite(descriptor.getButtonTextAlign(), "buttonTextAlign");
        ensureFinite(descriptor.getSelectableTextAlign(), "selectableTextAlign");
        ensureFinite(descriptor.getDisplayWindowPadding(), "displayWindowPadding");
        ensureFinite(descriptor.getDisplaySafeAreaPadding(), "displaySafeAreaPadding");
        ensureFinite(descriptor.getMouseCursorScale(), "mouseCursorScale");
        ensureFinite(descriptor.getCurveTessellationTol(), "curveTessellationTol");
        ensureFinite(descriptor.getCircleTessellationMaxError(), "circleTessellationMaxError");
    }

    static void validateDelta(MGStyleDelta delta) {
        Objects.requireNonNull(delta, "delta");
        ensureFinite(delta.getAlpha(), "alpha");
        ensureFinite(delta.getDisabledAlpha(), "disabledAlpha");
        ensureFinite(delta.getWindowPadding(), "windowPadding");
        ensureFinite(delta.getWindowRounding(), "windowRounding");
        ensureFinite(delta.getWindowBorderSize(), "windowBorderSize");
        ensureFinite(delta.getWindowMinSize(), "windowMinSize");
        ensureFinite(delta.getWindowTitleAlign(), "windowTitleAlign");
        ensureFinite(delta.getChildRounding(), "childRounding");
        ensureFinite(delta.getChildBorderSize(), "childBorderSize");
        ensureFinite(delta.getPopupRounding(), "popupRounding");
        ensureFinite(delta.getPopupBorderSize(), "popupBorderSize");
        ensureFinite(delta.getFramePadding(), "framePadding");
        ensureFinite(delta.getFrameRounding(), "frameRounding");
        ensureFinite(delta.getFrameBorderSize(), "frameBorderSize");
        ensureFinite(delta.getItemSpacing(), "itemSpacing");
        ensureFinite(delta.getItemInnerSpacing(), "itemInnerSpacing");
        ensureFinite(delta.getCellPadding(), "cellPadding");
        ensureFinite(delta.getTouchExtraPadding(), "touchExtraPadding");
        ensureFinite(delta.getIndentSpacing(), "indentSpacing");
        ensureFinite(delta.getColumnsMinSpacing(), "columnsMinSpacing");
        ensureFinite(delta.getScrollbarSize(), "scrollbarSize");
        ensureFinite(delta.getScrollbarRounding(), "scrollbarRounding");
        ensureFinite(delta.getGrabMinSize(), "grabMinSize");
        ensureFinite(delta.getGrabRounding(), "grabRounding");
        ensureFinite(delta.getLogSliderDeadzone(), "logSliderDeadzone");
        ensureFinite(delta.getTabRounding(), "tabRounding");
        ensureFinite(delta.getTabBorderSize(), "tabBorderSize");
        ensureFinite(delta.getTabMinWidthForCloseButton(), "tabMinWidthForCloseButton");
        ensureFinite(delta.getButtonTextAlign(), "buttonTextAlign");
        ensureFinite(delta.getSelectableTextAlign(), "selectableTextAlign");
        ensureFinite(delta.getDisplayWindowPadding(), "displayWindowPadding");
        ensureFinite(delta.getDisplaySafeAreaPadding(), "displaySafeAreaPadding");
        ensureFinite(delta.getMouseCursorScale(), "mouseCursorScale");
        ensureFinite(delta.getCurveTessellationTol(), "curveTessellationTol");
        ensureFinite(delta.getCircleTessellationMaxError(), "circleTessellationMaxError");
    }

    private static void ensureFinite(float value, String name) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    private static void ensureFinite(Float value, String name) {
        if (value == null) {
            return;
        }
        ensureFinite(value.floatValue(), name);
    }

    private static void ensureFinite(MGVec2 value, String name) {
        if (value == null) {
            return;
        }
        ensureFinite(value.x(), name + ".x");
        ensureFinite(value.y(), name + ".y");
    }
}
