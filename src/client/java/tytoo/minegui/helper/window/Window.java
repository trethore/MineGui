package tytoo.minegui.helper.window;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
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

    public static void dispose(String title) {
        if (title == null) {
            return;
        }
        STATE_BY_TITLE.remove(title);
    }

    public static void disposeAll() {
        STATE_BY_TITLE.clear();
    }

    public static Builder of(String title) {
        return new Builder(title);
    }

    public static Builder of(View view, String displayTitle) {
        Objects.requireNonNull(view, "view");
        return of(view.scopedWindowTitle(displayTitle));
    }

    public static final class Builder {
        private final String title;
        private int flags;
        private ImBoolean openHandle;
        private Runnable onOpen;
        private Runnable onClose;
        private Float initialX;
        private Float initialY;
        private Float initialWidth;
        private Float initialHeight;
        private Float forcedX;
        private Float forcedY;
        private Float forcedWidth;
        private Float forcedHeight;
        private int posCondition = ImGuiCond.FirstUseEver;
        private int sizeCondition = ImGuiCond.FirstUseEver;

        private Builder(String title) {
            this.title = Objects.requireNonNull(title, "title");
        }

        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public Builder initPos(float x, float y) {
            this.initialX = x;
            this.initialY = y;
            return this;
        }

        public Builder initDimensions(float width, float height) {
            this.initialWidth = width;
            this.initialHeight = height;
            return this;
        }

        public Builder pos(float x, float y) {
            this.forcedX = x;
            this.forcedY = y;
            this.posCondition = ImGuiCond.Always;
            return this;
        }

        public Builder pos(float x, float y, int condition) {
            this.forcedX = x;
            this.forcedY = y;
            this.posCondition = condition;
            return this;
        }

        public Builder dimensions(float width, float height) {
            this.forcedWidth = width;
            this.forcedHeight = height;
            this.sizeCondition = ImGuiCond.Always;
            return this;
        }

        public Builder dimensions(float width, float height, int condition) {
            this.forcedWidth = width;
            this.forcedHeight = height;
            this.sizeCondition = condition;
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
            WindowState state = STATE_BY_TITLE.computeIfAbsent(title, ignored -> new WindowState());

            if (forcedX != null && forcedY != null) {
                ImGui.setNextWindowPos(forcedX, forcedY, posCondition);
            } else if (initialX != null && initialY != null) {
                ImGui.setNextWindowPos(initialX, initialY, ImGuiCond.FirstUseEver);
            }

            if (forcedWidth != null && forcedHeight != null) {
                ImGui.setNextWindowSize(forcedWidth, forcedHeight, sizeCondition);
            } else if (initialWidth != null && initialHeight != null) {
                ImGui.setNextWindowSize(initialWidth, initialHeight, ImGuiCond.FirstUseEver);
            }

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
                Window.dispose(title);
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
    }

    private static final class WindowState {
        private boolean wasOpen;

        private WindowState() {
        }
    }
}
