package com.kcl.tokenlogin.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

/**
 * Ported from 1.21.11 Yarn version to 26.1.2 Mojang mappings.
 * 
 * Original logic preserved:
 * - Add "TokenLogin - Login From Clipboard" button in top-right
 * - Read token from clipboard on click
 * - Async authentication via McTokenAuth
 * - Replace current user via UserManager on success
 * - Show temporary centered status message (5s auto-hide)
 */
@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {

    @Unique
    private String statusMessage = null;
    @Unique
    private int statusColor = 0xFFFFFFFF;
    @Unique
    private long statusMessageTime = 0;

    protected MultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addTokenLoginButton(CallbackInfo ci) {
        int buttonWidth = 220;
        int buttonHeight = 20;
        int padding = 5;

        this.addRenderableWidget(
            Button.builder(Component.literal("TokenLogin - Login From Clipboard"), button -> {
                if (this.minecraft != null) {
                    loginFromClipboard();
                }
            })
            .bounds(this.width - buttonWidth - padding, padding, buttonWidth, buttonHeight)
            .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(extractor, mouseX, mouseY, delta);

        if (statusMessage != null) {
            // Auto-hide message after 5 seconds
            if (System.currentTimeMillis() - statusMessageTime > 5000) {
                statusMessage = null;
                return;
            }

            int x = this.width / 2;
            int y = this.height / 2;

            extractor.centeredText(
                this.font,
                Component.literal(statusMessage),
                x,
                y,
                statusColor
            );
        }
    }

    @Unique
    private void loginFromClipboard() {
        if (this.minecraft == null || this.minecraft.keyboardHandler == null) {
            setStatusMessage("Failed to read clipboard", 0xFFFF0000);
            return;
        }

        String clipboardText = this.minecraft.keyboardHandler.getClipboard();
        if (clipboardText == null || clipboardText.trim().isEmpty()) {
            setStatusMessage("Clipboard is empty!", 0xFFFF0000);
            return;
        }

        String token = clipboardText.trim();
        setStatusMessage("Authenticating...", 0xFFFFFFFF);

        CompletableFuture.supplyAsync(() -> {
            return com.kcl.tokenlogin.client.auth.McTokenAuth.INSTANCE.authenticate(token);
        }).thenAccept(result -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> {
                    if (result instanceof com.kcl.tokenlogin.client.auth.McTokenAuth.AuthResult.Success) {
                        boolean success = com.kcl.tokenlogin.client.auth.UserManager.INSTANCE.setUser(token);
                        if (success) {
                            setStatusMessage("Login successful!", 0xFF00FF00);
                        } else {
                            setStatusMessage("Failed to set session", 0xFFFF0000);
                        }
                    } else if (result instanceof com.kcl.tokenlogin.client.auth.McTokenAuth.AuthResult.Failure) {
                        String message = ((com.kcl.tokenlogin.client.auth.McTokenAuth.AuthResult.Failure) result).getMessage();
                        setStatusMessage("Login failed: " + message, 0xFFFF0000);
                    }
                });
            }
        }).exceptionally(throwable -> {
            if (this.minecraft != null) {
                this.minecraft.execute(() -> {
                    setStatusMessage("Error: " + throwable.getMessage(), 0xFFFF0000);
                });
            }
            return null;
        });
    }

    @Unique
    private void setStatusMessage(String message, int color) {
        this.statusMessage = message;
        this.statusColor = color;
        this.statusMessageTime = System.currentTimeMillis();
    }
}
