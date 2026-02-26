
package com.playtimechecker.mixin;

import com.playtimechecker.PlayTimeScanner;
import com.playtimechecker.ModerationManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {

        String msg = message.getString();

        // Скрываем playtime сообщения
        if (PlayTimeScanner.getInstance().handleChat(msg)) {
            ci.cancel();
            return;
        }

        // Обработка модерации
        ModerationManager.handle(msg);
    }
}
