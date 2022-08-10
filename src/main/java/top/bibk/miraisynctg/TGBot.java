package top.bibk.miraisynctg;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;

public class TGBot extends AbilityBot {
    public static Long ChatID = 0L;
    public static Long GroupID = 0L;
    public static Long BotQQ = 0L;

    public TGBot(String token, String username, DefaultBotOptions botOptions) {
        super(token, username, botOptions);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public long creatorId() {
        return 0;
    }

    public Ability getChatID() {
        return Ability
                .builder()
                .name("get")
                .info("Get ChatID")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(messageContext.chatId());
                    sendMessage.setText("ChatID: " + ChatID);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                })
                .build();
    }

    public Ability tgSyncToQQ() {
        return Ability
                .builder()
                .name("qq")
                .info("Send message to QQ Group")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.GROUP)
                .action(messageContext -> {
                    if (Objects.equals(messageContext.chatId(), ChatID)) {
                        Group group = Bot.getInstance(BotQQ).getGroup(GroupID);
                        if (group != null) {
                            group.sendMessage(messageContext.user().getFirstName() + "(" + messageContext.user().getUserName() + "): " + Arrays.toString(messageContext.arguments())
                                    .replace("[", "").replace("]", "").replace(",", ""));
                        } else {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(messageContext.chatId());
                            sendMessage.setText("Group not found");
                        }
                    }
                })
                .build();
    }

    private String getFilePath(String file_id) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TelegramFileJson telegramFileJson = mapper.readValue(new URL("https://api.telegram.org/bot" + Main.BOT_TOKEN + "/getFile?file_id=" + file_id), TelegramFileJson.class);
        return telegramFileJson.result.get("file_path");
    }

    public Ability tgReplyToQQ() {
        return Ability
                .builder()
                .name(DEFAULT)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.GROUP)
                .flag(Flag.REPLY)
                .input(0)
                .action(messageContext -> {
                    Message msg = messageContext.update().getMessage();
                    Message replyMsg = msg.getReplyToMessage();
                    try {
                        if (replyMsg.getFrom().getUserName().equals(Main.tgBot.getMe().getUserName())) {
                            Group group = Bot.getInstance(BotQQ).getGroup(GroupID);
                            if (group != null) {
                                MessageSource source = new MessageSourceBuilder().sender(BotQQ).target(BotQQ).messages(new PlainText(replyMsg.getText())).build(BotQQ, MessageSourceKind.GROUP);
                                MessageChainBuilder messageChainBuilder = new MessageChainBuilder().append(new QuoteReply(source)).append(messageContext.user().getFirstName() + "(" + messageContext.user().getUserName() + "): ");
                                if (msg.hasText()) {
                                    messageChainBuilder.append(Arrays.toString(messageContext.arguments()).replace("[", "").replace("]", "").replace(",", ""));
                                }
                                if (msg.hasPhoto()) {
                                    String fileURL = File.getFileUrl(Main.BOT_TOKEN, getFilePath(msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId()));
                                    messageChainBuilder.append(group.uploadImage(ExternalResource.create(new URL(fileURL).openStream()))).build();
                                }
                                if (msg.hasAnimation()) {
                                    String fileURL = File.getFileUrl(Main.BOT_TOKEN, getFilePath(msg.getAnimation().getFileId()));
                                    messageChainBuilder.append(group.uploadImage(ExternalResource.create(new URL(fileURL).openStream()))).build();
                                }
                                group.sendMessage(messageChainBuilder.build());
                            }
                        }
                    } catch (TelegramApiException | IOException e) {
                        e.printStackTrace();
                    }
                })
                .build();
    }
}