package top.bibk.miraisynctg;

import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.ImageType;
import net.mamoe.mirai.message.data.MessageChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ChatSync {
    private final Logger csLogger = LoggerFactory.getLogger("JituiBot:ChatSync");
    private static Integer fileName = 0;

    public ChatSync() {
        Listener<GroupMessageEvent> listener = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, event -> {
            if (event.getGroup().getId() == TGBot.GroupID) {
                if (TGBot.ChatID == 0) {
                    csLogger.warn("ChatID not set");
                    return;
                }
                MessageChain chain = event.getMessage();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(TGBot.ChatID);
                sendMessage.setText(event.getSender().getNick() + ": " + chain.contentToString());
                try {
                    Main.tgBot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                Image image = (Image) chain.stream().filter(Image.class::isInstance).findFirst().orElse(null);
                if (image != null) {
                    if (image.getImageType() != ImageType.GIF) {
                        SendPhoto sendPhoto = new SendPhoto();
                        sendPhoto.setChatId(TGBot.ChatID);
                        sendPhoto.setPhoto(new InputFile(Image.queryUrl(image)));
                        try {
                            Main.tgBot.execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        SendAnimation sendAnimation = new SendAnimation();
                        sendAnimation.setChatId(TGBot.ChatID);
                        sendAnimation.setAnimation(new InputFile(Image.queryUrl(image)));
                        try {
                            Main.tgBot.execute(sendAnimation);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
