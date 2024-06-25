package be.hize.afknotifier.config;

import be.hize.afknotifier.AFKNotifier;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;

public class Features extends Config {

    @Override
    public void saveNow(){
        AFKNotifier.configManager.saveConfig("close-gui");
    }

    @Override
    public String getTitle(){
        return "AFKNotifier " + AFKNotifier.getVersion() + " by §bHiZe§r, config by §5Moulberry §rand §5nea89";
    }

    @Expose
    @Category(name = "Settings", desc = "")
    public MainConfig main = new MainConfig();

    @Expose
    @Category(name = "Update", desc = "")
    public UpdateConfig update = new UpdateConfig();
}
