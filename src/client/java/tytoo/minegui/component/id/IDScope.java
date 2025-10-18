package tytoo.minegui.component.id;

import imgui.ImGui;

import java.util.*;

public final class IDScope {
    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    private IDScope() {
    }

    public static Scope push(Enum<?> key) {
        return pushSegments(sanitizeSegments(normalizeSegment(key)));
    }

    public static Scope push(Enum<?> key, String additional) {
        return pushSegments(sanitizeSegments(normalizeSegment(key), normalizeSegment(additional)));
    }

    public static Scope push(String key) {
        return pushSegments(sanitizeSegments(normalizeSegment(key)));
    }

    public static Scope push(String key, String additional) {
        return pushSegments(sanitizeSegments(normalizeSegment(key), normalizeSegment(additional)));
    }

    public static ComponentScope pushComponent(String baseSegment, String[] overrideSegments) {
        State state = STATE.get();
        String normalizedBase = normalizeBaseSegment(baseSegment);
        String[] segments = sanitizeSegments(overrideSegments);
        if (segments.length == 0) {
            segments = new String[]{state.nextComponentSegment(normalizedBase)};
        }
        return createComponentScope(state, segments);
    }

    public static void reset() {
        STATE.get().reset();
    }

    public static String normalizeSegment(Enum<?> key) {
        if (key == null) {
            return "unnamed";
        }
        return normalizeSegment(key.name());
    }

    public static String normalizeSegment(String value) {
        if (value == null) {
            return "unnamed";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "unnamed";
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        boolean dash = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                builder.append(c);
                dash = false;
                continue;
            }
            if (!dash && !builder.isEmpty()) {
                builder.append('-');
                dash = true;
            }
        }
        int length = builder.length();
        while (length > 0 && builder.charAt(length - 1) == '-') {
            builder.setLength(length - 1);
            length = builder.length();
        }
        if (builder.isEmpty()) {
            return "unnamed";
        }
        return builder.toString();
    }

    private static Scope pushSegments(String[] segments) {
        State state = STATE.get();
        return createScope(state, segments);
    }

    private static Scope createScope(State state, String[] segments) {
        int pushed = state.pushSegments(segments);
        if (pushed == 0) {
            return new Scope(state, 0, false, state.currentPath());
        }
        String path = state.currentPath();
        ImGui.pushID(path);
        return new Scope(state, pushed, true, path);
    }

    private static ComponentScope createComponentScope(State state, String[] segments) {
        Scope scope = createScope(state, segments);
        return new ComponentScope(scope);
    }

    private static String[] sanitizeSegments(String... rawSegments) {
        if (rawSegments == null || rawSegments.length == 0) {
            return new String[0];
        }
        List<String> sanitized = new ArrayList<>(rawSegments.length);
        for (String raw : rawSegments) {
            if (raw == null) {
                continue;
            }
            String normalized = normalizeSegment(raw);
            if (!normalized.isEmpty()) {
                sanitized.add(normalized);
            }
        }
        if (sanitized.isEmpty()) {
            sanitized.add("unnamed");
        }
        return sanitized.toArray(new String[0]);
    }

    private static String normalizeBaseSegment(String value) {
        String normalized = normalizeSegment(value);
        if (normalized.isEmpty() || "unnamed".equals(normalized)) {
            return "component";
        }
        return normalized;
    }

    private static final class State {
        private final Deque<String> segments = new ArrayDeque<>();
        private final Map<String, Integer> counters = new HashMap<>();

        private State() {
            reset();
        }

        private void reset() {
            segments.clear();
            segments.addLast("root");
            counters.clear();
        }

        private int pushSegments(String[] newSegments) {
            if (newSegments == null || newSegments.length == 0) {
                return 0;
            }
            int count = 0;
            for (String segment : newSegments) {
                if (segment == null || segment.isEmpty()) {
                    continue;
                }
                segments.addLast(segment);
                count++;
            }
            return count;
        }

        private void popSegments(int count) {
            int removable = Math.max(0, segments.size() - 1);
            int target = Math.min(removable, count);
            for (int i = 0; i < target; i++) {
                segments.removeLast();
            }
        }

        private String nextComponentSegment(String base) {
            String parent = currentPath();
            String key = parent + "|" + base;
            int index = counters.getOrDefault(key, 0) + 1;
            counters.put(key, index);
            return base + "-" + index;
        }

        private String currentPath() {
            if (segments.isEmpty()) {
                return "root";
            }
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String segment : segments) {
                if (!first) {
                    builder.append('/');
                }
                builder.append(segment);
                first = false;
            }
            return builder.toString();
        }
    }

    public static class Scope implements AutoCloseable {
        private final State state;
        private final int pushed;
        private final boolean imguiPushed;
        private final String path;
        private boolean closed;

        private Scope(State state, int pushed, boolean imguiPushed, String path) {
            this.state = state;
            this.pushed = pushed;
            this.imguiPushed = imguiPushed;
            this.path = path;
            this.closed = false;
        }

        public String path() {
            return path;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            if (imguiPushed) {
                ImGui.popID();
            }
            state.popSegments(pushed);
            closed = true;
        }
    }

    public static final class ComponentScope implements AutoCloseable {
        private final Scope delegate;
        private final String id;

        private ComponentScope(Scope delegate) {
            this.delegate = delegate;
            this.id = delegate.path();
        }

        public String id() {
            return id;
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
