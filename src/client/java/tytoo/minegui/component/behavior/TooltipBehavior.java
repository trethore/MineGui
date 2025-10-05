package tytoo.minegui.component.behavior;

import imgui.ImGui;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.state.State;

import java.util.function.Supplier;

public class TooltipBehavior implements Behavior<MGComponent<?>> {
    private final Supplier<String> tooltipSupplier;

    public TooltipBehavior(String tooltip) {
        this.tooltipSupplier = () -> tooltip;
    }

    public TooltipBehavior(State<String> tooltipState) {
        this.tooltipSupplier = tooltipState::get;
    }

    public TooltipBehavior(Supplier<String> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
    }

    public static TooltipBehavior of(String tooltip) {
        return new TooltipBehavior(tooltip);
    }

    public static TooltipBehavior of(State<String> tooltipState) {
        return new TooltipBehavior(tooltipState);
    }

    @Override
    public void postRender(MGComponent<?> component) {
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(tooltipSupplier.get());
        }
    }
}
