package be.hize.afknotifier.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class UpdateConfig {
    @Expose
    @ConfigOption(name = "Check for Updates", desc = "Check for updates on startup")
    @ConfigEditorBoolean
    public boolean enableAutoUpdateCheck = true;
}
