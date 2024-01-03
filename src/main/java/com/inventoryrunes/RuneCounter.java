package com.inventoryrunes;

import lombok.Getter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.image.BufferedImage;

class RuneCounter extends Counter {
    @Getter
    private final int itemID;
    private final String name;

    RuneCounter(Plugin plugin, int itemID, int count, String name, BufferedImage image)
    {
        super(image, plugin, count);
        this.itemID = itemID;
        this.name = name;
    }

    @Override
    public String getTooltip()
    {
        return name;
    }
}
