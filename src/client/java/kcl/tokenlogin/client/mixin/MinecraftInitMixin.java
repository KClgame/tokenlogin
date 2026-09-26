package kcl.tokenlogin.client.mixin;

import kcl.tokenlogin.client.auth.OriginalSession;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftInitMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void tokenlogin$captureOriginalUser(CallbackInfo ci) {
		OriginalSession.INSTANCE.capture((Minecraft) (Object) this);
	}
}
