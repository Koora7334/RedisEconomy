package dev.unnm3d.rediseconomy.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.util.List;

@SuppressWarnings("unused")
@Configuration
public class Settings {
    @Comment({"This is automatically generated on server startup",
            "Change it only if you have disabled plugin messages on the proxy"})
    public String serverId = System.currentTimeMillis() + "";
    @Comment("Language file")
    public String lang = "en-US";
    @Comment("Webeditor URL")
    public String webEditorUrl = "https://webui.advntr.dev/";
    @Comment("Activate this before reporting an issue")
    public boolean debug = false;
    @Comment({"if true, migrates the bukkit offline uuids accounts to the new system",
            "During the migration, the plugin will be disabled. Restart all RedisEconomy instances after the migration."})
    public boolean migrationEnabled = false;
    @Comment({"redis[s]://[password@]host[:port][/database][?option=value]", "Guide: https://github.com/Emibergo02/RedisEconomy/wiki/Install-redis"})
    public String redisUri = "redis://localhost:6379/0?timeout=20s&clientName=RedisEconomy";
    @Comment({"How many chars are needed for a command autocompletion", "Increase if you have a lot of players to list"})
    public int tab_complete_chars = 0;
    @Comment("Currencies")
    public List<CurrencySettings> currencies = List.of(new CurrencySettings("vault", "euro", "euros", 0, 0, true), new CurrencySettings("dollar", "$", "$", 0, 0, false));

    public record CurrencySettings(String currencyName, String currencySingle, String currencyPlural,
                                   double startingBalance, double payTax, boolean bankEnabled) {
    }
}
