package kcl.tokenlogin.client.mixin;

import kcl.tokenlogin.client.auth.TokenLogin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
	protected JoinMultiplayerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void tokenlogin$addButtons(CallbackInfo ci) {
		int buttonWidth = 220;
		int buttonHeight = 20;
		int padding = 5;
		int x = this.width - buttonWidth - padding;
		this.addRenderableWidget(
			Button.builder(Component.translatable("tokenlogin.button.login"), button -> {
				if (this.minecraft != null) {
					TokenLogin.INSTANCE.loginFromClipboard(this.minecraft);
				}
			})
				.bounds(x, padding, buttonWidth, buttonHeight)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("tokenlogin.button.refresh"), button -> {
				if (this.minecraft != null) {
					TokenLogin.INSTANCE.refreshOriginal(this.minecraft);
				}
			})
				.bounds(x, padding + buttonHeight + padding, buttonWidth, buttonHeight)
				.build()
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
		super.extractRenderState(extractor, mouseX, mouseY, delta);
		TokenLogin.INSTANCE.extractStatus(extractor, this.font, this.width, this.height);
	}
}
