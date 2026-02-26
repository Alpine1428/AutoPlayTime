
package com.playtimechecker.mixin;

import com.playtimechecker.PlayTimeScanner;
import com.playtimechecker.ModerationManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatMixin {

    @Inject(method = "addMessage", at = @At("HEAD"), cancellable = true)
    private void onMessage(Text message,
                           MessageSignatureData signatureData,
                           MessageIndicator indicator,
                           CallbackInfo ci) {

        String msg = message.getString();

        if (PlayTimeScanner.getInstance().handleChat(msg)) {
            ci.cancel();
            return;
        }

        ModerationManager.handle(msg);
    }
}
