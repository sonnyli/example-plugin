package com.inventoryrunes;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;
import java.util.*;

@Slf4j
@PluginDescriptor(
        name = "Inventory Runes",
        description = "Displays rune counts in inventory.",
        tags = {"inventory", "runes"}
)
public class InventoryRunes extends Plugin {
    private static final Set<Integer> runeIDs = new HashSet<Integer>(Arrays.asList(
            ItemID.FIRE_RUNE,
            ItemID.WATER_RUNE,
            ItemID.AIR_RUNE,
            ItemID.EARTH_RUNE,
            ItemID.MIND_RUNE,
            ItemID.BODY_RUNE,
            ItemID.DEATH_RUNE,
            ItemID.NATURE_RUNE,
            ItemID.CHAOS_RUNE,
            ItemID.LAW_RUNE,
            ItemID.COSMIC_RUNE,
            ItemID.BLOOD_RUNE,
            ItemID.SOUL_RUNE,
            ItemID.ASTRAL_RUNE,
            ItemID.WRATH_RUNE,

            // combination runes
            ItemID.MIST_RUNE,
            ItemID.DUST_RUNE,
            ItemID.MUD_RUNE,
            ItemID.SMOKE_RUNE,
            ItemID.STEAM_RUNE,
            ItemID.LAVA_RUNE
    ));
    private final Map<Integer, RuneCounter> allRuneCounters = new HashMap<>();
    private final Map<Integer, RuneCounter> displayedRuneCounters = new HashMap<>();
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ItemManager itemManager;
    @Inject
    private InventoryRunesConfig config;
    @Inject
    private InfoBoxManager infoBoxManager;

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
        }
    }

    @Provides
    InventoryRunesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(InventoryRunesConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        clientThread.invokeLater(() ->
        {
            // create all rune counters, and only display ones in the users inventory.
            for (var id : runeIDs) {
                allRuneCounters.put(id, new RuneCounter(this, id, 0, "", itemManager.getImage(id)));
            }

            final ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);

            if (container == null) {
                return;
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
        for (var entry : displayedRuneCounters.entrySet()) {
            infoBoxManager.removeInfoBox(entry.getValue());
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY)) {
            return;
        }

        updateRuneCounters(event.getItemContainer());
    }

    private void updateRuneCounters(ItemContainer itemContainer) {
        // stop displaying any counters that have been removed from inventory.
        Iterator<Map.Entry<Integer, RuneCounter>> iter = displayedRuneCounters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, RuneCounter> entry = iter.next();
            if (!itemContainer.contains(entry.getKey())) {
                iter.remove();
                infoBoxManager.removeInfoBox(entry.getValue());
            }
        }

        // add or update all counters that are in inventory
        for (var item : itemContainer.getItems()) {
            if (!isRuneItem(item)) {
                continue;
            }

            RuneCounter counter = allRuneCounters.get(item.getId());
            counter.setCount(item.getQuantity());

            if (!displayedRuneCounters.containsKey(item.getId())) {
                displayedRuneCounters.put(item.getId(), counter);
                infoBoxManager.addInfoBox(counter);
            }
        }
    }

    private boolean isRuneItem(Item item) {
        return runeIDs.contains(item.getId());
    }

}
