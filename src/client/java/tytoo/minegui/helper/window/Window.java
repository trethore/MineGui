package tytoo.minegui.helper.window;

import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import tytoo.minegui.helper.constraint.*;
import tytoo.minegui.helper.constraint.constraints.Constraints;
import tytoo.minegui.helper.constraint.constraints.PixelConstraint;
import tytoo.minegui.view.View;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Window {
    private static final float DEFAULT_WIDTH = 200f;
    private static final float DEFAULT_HEIGHT = 200f;
    private static final Map<String, WindowState> STATE_BY_TITLE = new ConcurrentHashMap<>();

    private Window() {
    }

    public static Builder of(String title) {
        return new Builder(title);
    }

    public static Builder of(View view, String displayTitle) {
        Objects.requireNonNull(view, "view");
        return of(view.scopedWindowTitle(displayTitle));
    }

    private static float sanitizeCoordinate(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return value;
    }

    private static float sanitizeLength(float value) {
        if (!Float.isFinite(value) || value <= 0f) {
            return 0f;
        }
        return value;
    }

    public static final class Builder {
        private final String title;
        private int flags;
        private ImBoolean openHandle;
        private Runnable onOpen;
        private Runnable onClose;
        private Float rawX;
        private Float rawY;
        private Float rawWidth;
        private Float rawHeight;
        private XConstraint xConstraint;
        private YConstraint yConstraint;
        private WidthConstraint widthConstraint;
        private HeightConstraint heightConstraint;
        private Float enforcedX;
        private Float enforcedY;
        private XConstraint enforcedXConstraint;
        private YConstraint enforcedYConstraint;
        private Float enforcedWidth;
        private Float enforcedHeight;
        private WidthConstraint enforcedWidthConstraint;
        private HeightConstraint enforcedHeightConstraint;

        private Builder(String title) {
            this.title = Objects.requireNonNull(title, "title");
        }

        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public Builder initPos(float x, float y) {
            rawX = sanitizeCoordinate(x);
            rawY = sanitizeCoordinate(y);
            xConstraint = null;
            yConstraint = null;
            return this;
        }

        public Builder initPos(XConstraint x, YConstraint y) {
            xConstraint = Objects.requireNonNull(x, "x");
            yConstraint = Objects.requireNonNull(y, "y");
            rawX = null;
            rawY = null;
            return this;
        }

        public Builder initDimensions(float width, float height) {
            rawWidth = sanitizeLength(width);
            rawHeight = sanitizeLength(height);
            widthConstraint = null;
            heightConstraint = null;
            return this;
        }

        public Builder initDimensions(WidthConstraint width, HeightConstraint height) {
            widthConstraint = Objects.requireNonNull(width, "width");
            heightConstraint = Objects.requireNonNull(height, "height");
            rawWidth = null;
            rawHeight = null;
            return this;
        }

        public Builder pos(float x, float y) {
            enforcedX = sanitizeCoordinate(x);
            enforcedY = sanitizeCoordinate(y);
            enforcedXConstraint = null;
            enforcedYConstraint = null;
            return this;
        }

        public Builder pos(XConstraint x, YConstraint y) {
            enforcedXConstraint = Objects.requireNonNull(x, "x");
            enforcedYConstraint = Objects.requireNonNull(y, "y");
            enforcedX = null;
            enforcedY = null;
            return this;
        }

        public Builder dimensions(float width, float height) {
            enforcedWidth = sanitizeLength(width);
            enforcedHeight = sanitizeLength(height);
            enforcedWidthConstraint = null;
            enforcedHeightConstraint = null;
            return this;
        }

        public Builder dimensions(WidthConstraint width, HeightConstraint height) {
            enforcedWidthConstraint = Objects.requireNonNull(width, "width");
            enforcedHeightConstraint = Objects.requireNonNull(height, "height");
            enforcedWidth = null;
            enforcedHeight = null;
            return this;
        }

        public Builder open(ImBoolean openHandle) {
            this.openHandle = Objects.requireNonNull(openHandle, "openHandle");
            return this;
        }

        public Builder onOpen(Runnable onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        public Builder onClose(Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        public void render(Runnable content) {
            Objects.requireNonNull(content, "content");
            WindowState state = STATE_BY_TITLE.computeIfAbsent(title, WindowState::new);
            applyInitialPlacement(state);
            applyForcedPlacement();
            boolean beginResult;
            if (openHandle != null) {
                beginResult = ImGui.begin(title, openHandle, flags);
            } else {
                beginResult = ImGui.begin(title, flags);
            }
            boolean openNow = openHandle == null || openHandle.get();
            boolean wasOpen = state.wasOpen;
            if (!openNow && wasOpen && onClose != null) {
                onClose.run();
            }
            if (!openNow) {
                ImGui.end();
                state.wasOpen = false;
                return;
            }
            if (!beginResult) {
                ImGui.end();
                state.wasOpen = true;
                return;
            }
            if (onOpen != null && ImGui.isWindowAppearing()) {
                onOpen.run();
            }
            try {
                content.run();
            } finally {
                ImGui.end();
            }
            state.wasOpen = true;
        }

        private void applyInitialPlacement(WindowState state) {
            if (state.initialized) {
                return;
            }
            boolean applyPos = rawX != null || rawY != null || xConstraint != null || yConstraint != null;
            float sanitizedWidth = resolveWidth();
            float sanitizedHeight = resolveHeight();
            PositionAndSize resolved = resolvePlacement(
                    rawX,
                    rawY,
                    xConstraint,
                    yConstraint,
                    rawWidth,
                    rawHeight,
                    widthConstraint,
                    heightConstraint,
                    sanitizedWidth,
                    sanitizedHeight
            );
            if (applyPos) {
                ImGuiViewport viewport = ImGui.getMainViewport();
                float fallbackPosX = sanitizeCoordinate(viewport.getPosX()) + resolveXFallback();
                float fallbackPosY = sanitizeCoordinate(viewport.getPosY()) + resolveYFallback();
                float posX = Float.isFinite(resolved.x()) ? resolved.x() : fallbackPosX;
                float posY = Float.isFinite(resolved.y()) ? resolved.y() : fallbackPosY;
                ImGui.setNextWindowPos(posX, posY, ImGuiCond.FirstUseEver);
            }
            float width = resolved.width() > 0f ? resolved.width() : sanitizedWidth;
            float height = resolved.height() > 0f ? resolved.height() : sanitizedHeight;
            ImGui.setNextWindowSize(width, height, ImGuiCond.FirstUseEver);
            state.initialized = true;
        }

        private void applyForcedPlacement() {
            boolean enforcePos = enforcedX != null || enforcedY != null || enforcedXConstraint != null || enforcedYConstraint != null;
            boolean enforceSize = enforcedWidth != null || enforcedHeight != null || enforcedWidthConstraint != null || enforcedHeightConstraint != null;
            if (!enforcePos && !enforceSize) {
                return;
            }
            float fallbackWidth = enforcedWidth != null ? enforcedWidth : resolveWidth();
            float fallbackHeight = enforcedHeight != null ? enforcedHeight : resolveHeight();
            PositionAndSize resolved = resolvePlacement(
                    enforcedX,
                    enforcedY,
                    enforcedXConstraint,
                    enforcedYConstraint,
                    enforcedWidth,
                    enforcedHeight,
                    enforcedWidthConstraint,
                    enforcedHeightConstraint,
                    fallbackWidth,
                    fallbackHeight
            );
            if (enforcePos) {
                float posX = Float.isFinite(resolved.x()) ? resolved.x() : fallbackAbsoluteX();
                float posY = Float.isFinite(resolved.y()) ? resolved.y() : fallbackAbsoluteY();
                ImGui.setNextWindowPos(posX, posY, ImGuiCond.Always);
            }
            if (enforceSize) {
                float width = resolved.width() > 0f ? resolved.width() : fallbackWidth;
                float height = resolved.height() > 0f ? resolved.height() : fallbackHeight;
                ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
            }
        }

        private float fallbackAbsoluteX() {
            ImGuiViewport viewport = ImGui.getMainViewport();
            float local = enforcedX != null ? enforcedX : resolveXFallback();
            return sanitizeCoordinate(viewport.getPosX()) + local;
        }

        private float fallbackAbsoluteY() {
            ImGuiViewport viewport = ImGui.getMainViewport();
            float local = enforcedY != null ? enforcedY : resolveYFallback();
            return sanitizeCoordinate(viewport.getPosY()) + local;
        }

        private float resolveWidth() {
            if (rawWidth != null && rawWidth > 0f) {
                return rawWidth;
            }
            return DEFAULT_WIDTH;
        }

        private float resolveHeight() {
            if (rawHeight != null && rawHeight > 0f) {
                return rawHeight;
            }
            return DEFAULT_HEIGHT;
        }

        private PositionAndSize resolvePlacement(
                Float xValue,
                Float yValue,
                XConstraint xConstraintValue,
                YConstraint yConstraintValue,
                Float widthValue,
                Float heightValue,
                WidthConstraint widthConstraintValue,
                HeightConstraint heightConstraintValue,
                float fallbackWidth,
                float fallbackHeight) {
            ImGuiViewport viewport = ImGui.getMainViewport();
            float viewportWidth = sanitizeLength(viewport.getSizeX());
            float viewportHeight = sanitizeLength(viewport.getSizeY());
            Constraints constraints = new Constraints(ConstraintTarget.of(viewportWidth, viewportHeight));
            WidthConstraint activeWidthConstraint = widthConstraintValue != null
                    ? widthConstraintValue
                    : new PixelConstraint(widthValue != null ? widthValue : fallbackWidth);
            HeightConstraint activeHeightConstraint = heightConstraintValue != null
                    ? heightConstraintValue
                    : new PixelConstraint(heightValue != null ? heightValue : fallbackHeight);
            XConstraint activeXConstraint = xConstraintValue != null
                    ? xConstraintValue
                    : new PixelConstraint(xValue != null ? xValue : 0f);
            YConstraint activeYConstraint = yConstraintValue != null
                    ? yConstraintValue
                    : new PixelConstraint(yValue != null ? yValue : 0f);
            constraints.setWidth(activeWidthConstraint);
            constraints.setHeight(activeHeightConstraint);
            constraints.setX(activeXConstraint);
            constraints.setY(activeYConstraint);
            LayoutConstraintSolver.LayoutFrame frame = LayoutConstraintSolver.LayoutFrame.of(
                    viewportWidth,
                    viewportHeight,
                    fallbackWidth,
                    fallbackHeight
            );
            LayoutConstraintSolver.LayoutResult result = LayoutConstraintSolver.resolve(constraints, frame);
            float offsetX = sanitizeCoordinate(viewport.getPosX());
            float offsetY = sanitizeCoordinate(viewport.getPosY());
            float x = offsetX + result.x();
            float y = offsetY + result.y();
            return new PositionAndSize(x, y, result.width(), result.height());
        }

        private float resolveXFallback() {
            if (rawX != null) {
                return rawX;
            }
            return 0f;
        }

        private float resolveYFallback() {
            if (rawY != null) {
                return rawY;
            }
            return 0f;
        }
    }

    private static final class WindowState {
        private boolean initialized;
        private boolean wasOpen;

        private WindowState(String title) {
        }
    }

    private record PositionAndSize(float x, float y, float width, float height) {
    }
}
