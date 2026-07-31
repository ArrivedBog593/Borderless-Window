package com.github.arrivedbog593.borderlesswindow.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Scrollable list used by BorderlessConfigScreen. This is the same widget
 * family vanilla uses for its own Video/Controls settings, so it looks and
 * behaves native: mouse wheel scrolling, a scrollbar, keyboard navigation
 * and narration all come for free.
 * <p>
 * Two entry types are supported:
 * - HEADER: a centered, non-interactive section title ("Window", "Fog"...).
 *   It has no children, so it is skipped by keyboard focus and narration.
 * - WIDGET: wraps one option widget (CycleButton, slider...) and keeps it
 *   centered and vertically aligned inside its row. Widget positions are
 *   set on every render because the list moves them as it scrolls.
 * <p>
 * Adding a feature later means calling addHeader() + addOption() instead of
 * computing manual Y offsets, which is what made the old flat screen
 * overflow once the fog and cloud options arrived.
 */
public class BorderlessOptionsList extends ContainerObjectSelectionList<BorderlessOptionsList.Entry> {

    /** Row width; matches the 200 px option buttons plus breathing room. */
    private static final int ROW_WIDTH = 310;

    public BorderlessOptionsList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    /** Adds a non-interactive section title. */
    public void addHeader(Component title) {
        this.addEntry(new HeaderEntry(title));
    }

    /** Adds one option widget as its own row. */
    public void addOption(AbstractWidget widget) {
        this.addEntry(new WidgetEntry(widget));
    }

    @Override
    public int getRowWidth() {
        return ROW_WIDTH;
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    /** A centered section title. Not focusable: children() is empty. */
    private static class HeaderEntry extends Entry {

        private final Component title;

        HeaderEntry(Component title) {
            this.title = title;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY,
                           boolean hovering, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            // Sits low in its row so it reads as a title for what follows.
            int y = top + height - minecraft.font.lineHeight - 1;
            guiGraphics.drawCenteredString(minecraft.font, this.title,
                    left + width / 2, y, 0xFFA0A0A0);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    /** A row holding a single option widget. */
    private static class WidgetEntry extends Entry {

        private final AbstractWidget widget;

        WidgetEntry(AbstractWidget widget) {
            this.widget = widget;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY,
                           boolean hovering, float partialTick) {
            // The list scrolls, so the widget is repositioned every frame.
            this.widget.setX(left + (width - this.widget.getWidth()) / 2);
            this.widget.setY(top + (height - this.widget.getHeight()) / 2);
            this.widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(this.widget);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(this.widget);
        }
    }
}