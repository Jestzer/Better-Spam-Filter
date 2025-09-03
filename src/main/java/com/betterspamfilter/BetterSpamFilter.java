package com.betterspamfilter;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.MessageNode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Better Spam Filter"
)
public class BetterSpamFilter extends Plugin {
    @Inject
    private Client client;

    @Inject
    private BetterSpamFilterConfig config;

    @Override
    protected void startUp() throws Exception {
        log.info("Plugin started!");
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Plugin stopped!");
    }

    // For textbox chat. It is handled separately from overhead chat.
    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent event) {
        if (!event.getEventName().equals("chatFilterCheck")) {
            return;
        }

        int[] intStack = client.getIntStack();
        int intStackSize = client.getIntStackSize();
        Object[] objectStack = client.getObjectStack();
        int objectStackSize = client.getObjectStackSize();

        String message = (String) objectStack[objectStackSize - 1];

        final int messageId = intStack[intStackSize - 1];

        final MessageNode messageNode = client.getMessages().get(messageId);

        String playerName = messageNode.getName();

        int combatLevel = combatLevel(playerName);
        message =(message.toLowerCase()); // To make it case in-sensitive.
        boolean isSpam = isSpam(message, combatLevel);

        if (isSpam) {
            intStack[intStackSize - 3] = 0;
            message = "";
        }

        objectStack[objectStackSize - 1] = message;

    }

    // For overhead chat.
    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        String message = event.getOverheadText();

        String playerName = event.getActor().getName();

        int combatLevel = combatLevel(playerName);
        message =(message.toLowerCase()); // To make it case in-sensitive.
        boolean isSpam = isSpam(message, combatLevel);

        if (isSpam) {
            event.getActor().setOverheadText(" ");
        }
    }

    public List<Player> getPlayers() {
        return
                client.getLocalPlayer().getWorldView().players().stream().collect(Collectors.toList());
    }

    public int combatLevel(String playerName) {

        Player matchingPlayer;

        List<Player> playerList = getPlayers();

        int combatLevel = 0;

        if (playerList != null && playerName != null) {

            Optional<Player> playerOptional = playerList.stream()
                    .filter(player -> player != null &&
                            player.getName() != null &&
                            player.getName().equalsIgnoreCase(playerName)) // Names can be case-insensitive.
                    .findFirst(); // The first match should do.

            // Check if a player was found in the Optional.
            if (playerOptional.isPresent()) {
                matchingPlayer = playerOptional.get();
                combatLevel = matchingPlayer.getCombatLevel();
            }
        }
        return combatLevel;
    }

    public boolean isSpam(String message, int combatLevel) {
        return  message.contains(("donations")) ||
                message.contains("dancing for") ||
                message.contains("@@@") ||
                message.contains("doubling") ||
                (combatLevel > 0 && combatLevel < 10);
    }

        @Provides
        BetterSpamFilterConfig provideConfig (ConfigManager configManager){
            return configManager.getConfig(BetterSpamFilterConfig.class);
        }
    }
