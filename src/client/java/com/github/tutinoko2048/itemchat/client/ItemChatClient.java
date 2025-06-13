package com.github.tutinoko2048.itemchat.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemChatClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ItemChatClient.class);

    private static ItemStack displayedStack = ItemStack.EMPTY;
    private static long displayUntil = 0;

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();

        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return true;

            ItemStack item = player.getInventory().getMainHandStack();
            if (item.isEmpty()) return true;

            client.execute(() -> {
                Text text = Text.literal(message + " ").append(item.toHoverableText());
                //FIXME: Textのまま送信する方法を探したけどこれだと自分自身にしか送信されない
                player.sendMessage(text);
            });

            displayedStack = item;
            displayUntil = System.currentTimeMillis() + 10_000;

            return false;
        });

//        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
//            LOGGER.info(message.getString());
//        });

        HudRenderCallback.EVENT.register((drawContext, v) -> {
            if (System.currentTimeMillis() > displayUntil) return;

            if (client.inGameHud == null) return;

//            ChatHud chatHud = client.inGameHud.getChatHud();

            renderItemStack(
                drawContext,
                displayedStack,
                client.getWindow().getScaledWidth() / 2 - 8,
                client.getWindow().getScaledHeight() / 2 - 24
            );
        });
    }

    private void renderItemStack(DrawContext drawContext, ItemStack itemStack, int x, int y) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(x, y, 100);
        drawContext.drawItem(itemStack, 0, 0, 1);
        matrices.pop();
    }
}
