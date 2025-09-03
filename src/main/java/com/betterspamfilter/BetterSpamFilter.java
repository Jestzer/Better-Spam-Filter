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

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent event) {
        if (!event.getEventName().equals("chatFilterCheck")) {
            return;
        }

        int[] intStack = client.getIntStack();
        int intStackSize = client.getIntStackSize();
        Object[] objectStack = client.getObjectStack();
        int objectStackSize = client.getObjectStackSize();

        final int messageId = intStack[intStackSize - 1];
        String message = (String) objectStack[objectStackSize - 1];

        final MessageNode messageNode = client.getMessages().get(messageId);

        String sender = messageNode.getSender();

        isSpam = message.contains("donations") || message.contains("Dancing for money") || message.contains("Dancing for items");

        if (isSpam) {
            intStack[intStackSize - 3] = 0;
            message = "";
        }

        objectStack[objectStackSize - 1] = message;

    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        String message = event.getOverheadText();

        isSpam = message.contains("donations") || message.contains("Dancing for money") || message.contains("Dancing for items");

        if (isSpam) {
            event.getActor().setOverheadText(" ");
        }
    }

    private boolean isSpam;

    @Provides
    BetterSpamFilterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BetterSpamFilterConfig.class);
    }
}
