package be.hize.afknotifier.config;

import be.hize.afknotifier.data.IslandType;
import be.hize.afknotifier.utils.DiscordUtil;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class MainConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable the feature. Will send a message in the configured webhook if you went AFK.")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Island Change", desc = "Send the message when you leave defined island.")
    @ConfigEditorBoolean
    public boolean onIslandChange = true;

    @Expose
    @ConfigOption(name = "Lobby Restart", desc = "Only send the message when the island change is caused by a lobby restart.\n(Scheduled reboot or Game Update)")
    @ConfigEditorBoolean
    public boolean onlyOnLobbyRestart = false;

    @Expose
    @ConfigOption(name = "Message When Restart", desc = "Send a message when a server restart will happen.")
    @ConfigEditorBoolean
    public boolean messageWhenRestart = false;

    @Expose
    @ConfigOption(name = "On Disconnect", desc = "Send the message when you disconnect from the server.")
    @ConfigEditorBoolean
    public boolean onDisconnect = true;

    @Expose
    @ConfigOption(name = "Island", desc = "The island.")
    @ConfigEditorDropdown
    public IslandType islandType = IslandType.PRIVATE_ISLAND;

    @Expose
    @ConfigOption(name = "Lobby Message", desc = "Message to sent when send to the lobby.")
    @ConfigEditorText
    public String lobbyMessage = "%%user%% is no longer on skyblock!";

    @Expose
    @ConfigOption(name = "IS Leave Message", desc = "Message to sent when you leave your private island.")
    @ConfigEditorText
    public String islandLeaveMessage = "%%user%% just left private island!";

    @Expose
    @ConfigOption(name = "Webhook", desc = "The Webhook to send the message to.")
    @ConfigEditorText
    public String webhook = "";

    @ConfigOption(name = "Test Webhook", desc = "Send a test message to the webhook.")
    @ConfigEditorButton(buttonText = "Test")
    public Runnable testWebhook = DiscordUtil::sendTestMessage;

    @Expose
    @ConfigOption(name = "Tag List", desc = "List of people you want to ping.\n" +
        "Use discord id separated by a comma.\n" +
        "Example: 249269966199586817,334451149413285889")
    @ConfigEditorText
    public Property<String> userTagList = Property.of("334451149413285889");

    @Expose
    @ConfigOption(name = "Retry", desc = "Number of time you want to retry checking if you are still on skyblock before sending the message.")
    @ConfigEditorSlider(minValue = 5, maxValue = 10, minStep = 1)
    public int retryValue = 10;
}
