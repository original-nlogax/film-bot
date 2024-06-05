package com.gorges.bot.userbot;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.AuthorizationState;
import it.tdlight.jni.TdApi.MessageContent;
import it.tdlight.jni.TdApi.MessageSenderUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import java.util.Random;

public final class Userbot {

    private static TdlibApp app;

    public static void start(int apiId, String apiHash, int adminId) throws Exception {
        Init.init();

        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(apiId, apiHash);

            TDLibSettings settings = TDLibSettings.create(apiToken);

            Path sessionPath = Paths.get("brawler-session");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);

            AuthenticationSupplier<?> authenticationData = AuthenticationSupplier.consoleLogin();

            Userbot.app = new TdlibApp(clientBuilder, authenticationData, adminId);
        }
    }

    public static TdlibApp getApp () {
        return app;
    }

    public static class TdlibApp {

        private final SimpleTelegramClient client;

        private final long adminId;

        public TdlibApp(SimpleTelegramClientBuilder clientBuilder,
                        AuthenticationSupplier<?> authenticationData,
                        long adminId) {
            this.adminId = adminId;

            clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);

            this.client = clientBuilder.build(authenticationData);
        }

        public SimpleTelegramClient getClient() {
            return client;
        }

        private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
            AuthorizationState authorizationState = update.authorizationState;
            if (authorizationState instanceof TdApi.AuthorizationStateReady) {
                System.out.println("Logged in");
            } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
                System.out.println("Closing...");
            } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
                System.out.println("Closed");
            } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
                System.out.println("Logging out...");
            }
        }

        private void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
            // Get the message content
            MessageContent messageContent = update.message.content;

            // Get the message text
            String text;
            if (messageContent instanceof TdApi.MessageText messageText) {
                // Get the text of the text message
                text = messageText.text.text;
            } else {
                // We handle only text messages, the other messages will be printed as their type
                text = String.format("(%s)", messageContent.getClass().getSimpleName());
            }

            long chatId = update.message.chatId;

            // Get the chat title
            client.send(new TdApi.GetChat(chatId))
                // Use the async completion handler, to avoid blocking the TDLib response thread accidentally
                .whenCompleteAsync((chatIdResult, error) -> {
                    if (error != null) {
                        // Print error
                        System.err.printf("Can't get chat title of chat %s%n", chatId);
                        error.printStackTrace(System.err);
                    } else {
                        // Get the chat title
                        String title = chatIdResult.title;
                        // Print the message
                        System.out.printf("Received new message from chat %s (%s): %s%n", title, chatId, text);
                    }
                });
        }

        private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            // Check if the sender is the admin
            if (isAdmin(commandSender)) {
                // Stop the client
                System.out.println("Received stop command. closing...");
                client.sendClose();
            }
        }

        public boolean isAdmin(TdApi.MessageSender sender) {
            if (sender instanceof MessageSenderUser messageSenderUser) {
                return messageSenderUser.userId == adminId;
            } else {
                return false;
            }
        }

    }
}
