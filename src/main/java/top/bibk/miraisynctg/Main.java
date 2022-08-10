package top.bibk.miraisynctg;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public final class Main extends JavaPlugin {
    public static final Main INSTANCE = new Main();
    private static final String PROXY_HOST = "127.0.0.1";
    private static final Integer PROXY_PORT = 7890;
    public static TGBot tgBot;
    public static final String BOT_TOKEN = "";
    public static final String BOT_NAME = "";

    private Main() {
        super(new JvmPluginDescriptionBuilder("top.bibk.miraisynctg", "1.0-SNAPSHOT")
                .name("SyncBot")
                .author("ShrBox")
                .build());
    }

    private void registerTelegramBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            DefaultBotOptions botOptions = new DefaultBotOptions();
            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            tgBot = new TGBot(BOT_TOKEN, BOT_NAME, botOptions);
            botsApi.registerBot(tgBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        getLogger().info("TelegramBot registered");
    }

    @Override
    public void onEnable() {
        registerTelegramBot();
        new ChatSync();
        getLogger().info("mirai-sync-tg loaded");
    }
}