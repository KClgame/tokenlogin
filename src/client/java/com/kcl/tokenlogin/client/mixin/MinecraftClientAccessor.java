package com.kcl.tokenlogin.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {

    @Accessor("user")
    User getUserField();

    @Accessor("user")
    @Mutable
    void setUserField(User user);
}
