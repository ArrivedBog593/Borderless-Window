package com.github.arrivedbog593.borderlesswindow.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
 * - WIDGET: a row of one or two option widgets (CycleButton, slider...).
 *   Widget positions are set on every render because the list moves them
 *   as it scrolls.
 * <p>
 * Options are laid out in two columns, like vanilla's Video Settings
 * (OptionsList: 150 px buttons, 160 px apart, 310 px rows), so the list
 * is about half as long. The rules:
 * - Consecutive options of the same section share a row. A text longer
 *   than its column scrolls sideways, as in vanilla (renderScrollingString).
 *   Fitting each text instead was tried: once every value of a button or
 *   slider was measured, so many options needed a whole row that the
 *   list barely got shorter and sections mixed one- and two-column rows.
 * - The last option of a section with an odd count takes the whole row,
 *   so no row leaves a gap.
 * - A header always starts a new row, so a row never mixes two sections.
 * <p>
 * Adding a feature later means calling addHeader() + addOption() instead of
 * computing manual positions.
 */
public class BorderlessOptionsList extends ContainerObjectSelectionList<BorderlessOptionsList.Entry> {

    /** Row width, same as vanilla's OptionsList. */
    private static final int ROW_WIDTH = 310;

    /** One column: vanilla's small option button. */
    private static final int COLUMN_WIDTH = 150;

    /** Where the second column starts, from the row's left edge. */
    private static final int COLUMN_OFFSET = 160;

    /** The last row, while it still has room for a second column. */
    @Nullable
    private WidgetEntry openRow;

    public BorderlessOptionsList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    /** Adds a non-interactive section title. */
    public void addHeader(Component title) {
        this.openRow = null;
        this.addEntry(new HeaderEntry(title));
    }

    /**
     * Adds one option widget: next to the previous one if that row still
     * has a free column, otherwise on a new row. A widget alone on its row
     * spans the whole row; pairing shrinks both to a column.
     */
    public void addOption(AbstractWidget widget) {
        if (this.openRow != null) {
            this.openRow.add(widget);
            this.openRow = null;
            return;
        }
        widget.setWidth(ROW_WIDTH);
        WidgetEntry row = new WidgetEntry(widget);
        this.addEntry(row);
        this.openRow = row;
    }

    /**
     * Adds one option widget on a row of its own, across the whole row
     * (vanilla's OptionsList.addBig). For an option that should not pair
     * with its neighbor, so the options after it pair the right way.
     */
    public void addBigOption(AbstractWidget widget) {
        this.openRow = null;
        widget.setWidth(ROW_WIDTH);
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
        public void extractContent(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                  boolean hovering, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            // Sits low in its row so it reads as a title for what follows.
            int y = this.getContentY() + this.getContentHeight() - minecraft.font.lineHeight - 1;
            guiGraphics.centeredText(minecraft.font, this.title,
                    this.getContentX() + this.getContentWidth() / 2, y, 0xFFA0A0A0);
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

    /**
     * A row holding one option widget across the whole row, or two side
     * by side in the columns.
     */
    private static class WidgetEntry extends Entry {

        private final List<AbstractWidget> widgets = new ArrayList<>(2);

        WidgetEntry(AbstractWidget widget) {
            this.widgets.add(widget);
        }

        /** Second column: both widgets shrink to a column. */
        void add(AbstractWidget widget) {
            this.widgets.add(widget);
            for (AbstractWidget each : this.widgets) {
                each.setWidth(COLUMN_WIDTH);
            }
        }

        @Override
        public void extractContent(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                  boolean hovering, float partialTick) {
            // The list scrolls, so the widgets are repositioned every frame.
            int x = this.getContentX() + (this.getContentWidth() - ROW_WIDTH) / 2;
            for (AbstractWidget widget : this.widgets) {
                widget.setX(x);
                widget.setY(this.getContentY() + (this.getContentHeight() - widget.getHeight()) / 2);
                widget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                x += COLUMN_OFFSET;
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return this.widgets;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }
    }
}
