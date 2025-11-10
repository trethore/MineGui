package tytoo.minegui.helper.layout.sizing;

import imgui.ImGui;
import tytoo.minegui.helper.constraint.LayoutConstraintSolver;
import tytoo.minegui.helper.constraint.constraints.Constraints;
import tytoo.minegui.helper.layout.LayoutConstraints;
import tytoo.minegui.helper.layout.cursor.LayoutContext;

public final class SizeHints {
    private SizeHints() {
    }

    public static float itemWidth(float width) {
        float sanitized = sanitizeLength(width);
        if (sanitized > 0f) {
            ImGui.setNextItemWidth(sanitized);
        }
        return sanitized;
    }

    public static float itemWidth(float width, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        return itemWidth(chosen.applyWidth(width));
    }

    public static float itemWidth(float width, SizeRange range) {
        float sanitized = sanitizeLength(width);
        if (range != null) {
            sanitized = range.clamp(sanitized);
        }
        if (sanitized > 0f) {
            ImGui.setNextItemWidth(sanitized);
        }
        return sanitized;
    }

    public static float itemWidth(LayoutConstraints request) {
        return itemWidth(request, null, LayoutContext.capture());
    }

    public static float itemWidth(LayoutConstraints request, SizeRange range) {
        return itemWidth(request, range, LayoutContext.capture());
    }

    public static float itemWidth(LayoutConstraints request, LayoutContext context) {
        return itemWidth(request, null, context);
    }

    public static float itemWidth(LayoutConstraints request, SizeRange range, LayoutContext context) {
        NextSize size = itemSize(request, range, null, context);
        return size.width();
    }

    public static NextSize itemSize(LayoutConstraints request) {
        return itemSize(request, null, null, LayoutContext.capture());
    }

    public static NextSize itemSize(LayoutConstraints request, SizeRange widthRange, SizeRange heightRange) {
        return itemSize(request, widthRange, heightRange, LayoutContext.capture());
    }

    public static NextSize itemSize(LayoutConstraints request, LayoutContext context) {
        return itemSize(request, null, null, context);
    }

    public static NextSize itemSize(LayoutConstraints request, SizeRange widthRange, SizeRange heightRange, LayoutContext context) {
        LayoutContext layoutContext = context != null ? context : LayoutContext.capture();
        ComputedSize computed = computeSize(request, layoutContext);
        float resolvedWidth = widthRange != null ? widthRange.clamp(computed.width()) : computed.width();
        float resolvedHeight = heightRange != null ? heightRange.clamp(computed.height()) : computed.height();
        if (resolvedWidth > 0f) {
            ImGui.setNextItemWidth(resolvedWidth);
        }
        return new NextSize(resolvedWidth, resolvedHeight);
    }

    public static NextSize windowSize(float width, float height) {
        float sanitizedWidth = sanitizeLength(width);
        float sanitizedHeight = sanitizeLength(height);
        if (sanitizedWidth > 0f || sanitizedHeight > 0f) {
            ImGui.setNextWindowSize(nonZero(sanitizedWidth), nonZero(sanitizedHeight));
        }
        return new NextSize(sanitizedWidth, sanitizedHeight);
    }

    public static NextSize windowSize(float width, float height, ScaleUnit unit) {
        ScaleUnit chosen = unit != null ? unit : ScaleUnit.RAW;
        float resolvedWidth = chosen.applyWidth(width);
        float resolvedHeight = chosen.applyHeight(height);
        return windowSize(resolvedWidth, resolvedHeight);
    }

    public static NextSize windowSize(LayoutConstraints request) {
        return windowSize(request, null, null, LayoutContext.capture());
    }

    public static NextSize windowSize(LayoutConstraints request, LayoutContext context) {
        return windowSize(request, null, null, context);
    }

    public static NextSize windowSize(LayoutConstraints request, SizeRange widthRange, SizeRange heightRange, LayoutContext context) {
        LayoutContext layoutContext = context != null ? context : LayoutContext.capture();
        ComputedSize computed = computeSize(request, layoutContext);
        float resolvedWidth = widthRange != null ? widthRange.clamp(computed.width()) : computed.width();
        float resolvedHeight = heightRange != null ? heightRange.clamp(computed.height()) : computed.height();
        if (resolvedWidth > 0f || resolvedHeight > 0f) {
            ImGui.setNextWindowSize(nonZero(resolvedWidth), nonZero(resolvedHeight));
        }
        return new NextSize(resolvedWidth, resolvedHeight);
    }

    private static ComputedSize computeSize(LayoutConstraints request, LayoutContext context) {
        if (request == null) {
            float width = context != null ? context.contentRegionAvailX() : 0f;
            float height = 0f;
            return new ComputedSize(width, height);
        }
        Float widthOverrideValue = request.widthOverrideValue();
        Float heightOverrideValue = request.heightOverrideValue();
        float resolvedWidth = widthOverrideValue != null ? widthOverrideValue : 0f;
        float resolvedHeight = heightOverrideValue != null ? heightOverrideValue : 0f;
        Constraints constraints = request.directConstraints();
        LayoutConstraintSolver.LayoutResult result = null;
        if (constraints != null) {
            LayoutConstraintSolver.LayoutFrame frame = context != null
                    ? context.toLayoutFrame(request)
                    : LayoutContext.capture().toLayoutFrame(request);
            result = LayoutConstraintSolver.resolve(constraints, frame);
            if (resolvedWidth <= 0f) {
                resolvedWidth = sanitizeLength(result.width());
            }
            if (resolvedHeight <= 0f) {
                resolvedHeight = sanitizeLength(result.height());
            }
        }
        if (resolvedWidth <= 0f && context != null) {
            resolvedWidth = sanitizeLength(context.contentRegionAvailX());
        }
        if (resolvedWidth <= 0f && context != null) {
            resolvedWidth = sanitizeLength(context.contentRegionWidth());
        }
        if (resolvedHeight <= 0f && result != null) {
            resolvedHeight = sanitizeLength(result.height());
        }
        if (resolvedHeight <= 0f && context != null) {
            resolvedHeight = sanitizeLength(context.contentRegionAvailY());
        }
        if (resolvedHeight <= 0f && context != null) {
            resolvedHeight = sanitizeLength(context.contentRegionHeight());
        }
        return new ComputedSize(sanitizeLength(resolvedWidth), sanitizeLength(resolvedHeight));
    }

    private static float sanitizeLength(float value) {
        if (!Float.isFinite(value) || value < 0f) {
            return 0f;
        }
        return value;
    }

    private static float nonZero(float value) {
        return value > 0f ? value : Float.MIN_NORMAL;
    }

    public record NextSize(float width, float height) {
    }

    private record ComputedSize(float width, float height) {
    }
}
