package tytoo.minegui.helper;

import imgui.ImGui;

import java.util.Objects;

public final class Panel {
    private Panel() {
    }

    public static Builder of(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private float width = 0f;
        private float height = 0f;
        private boolean border = false;
        private int flags = 0;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder width(float width) {
            this.width = width;
            return this;
        }

        public Builder height(float height) {
            this.height = height;
            return this;
        }

        public Builder border(boolean border) {
            this.border = border;
            return this;
        }

        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public void render(Runnable content) {
            Objects.requireNonNull(content, "content");
            if (ImGui.beginChild(id, width, height, border, flags)) {
                content.run();
            }
            ImGui.endChild();
        }
    }
}
