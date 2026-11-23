package com.hengx.twohandedoffhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hengx.twohandedoffhand.TwoHanded;

import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;

/**
 * 服务端侧的「使用」拦截:即使客户端被绕过(改包/自定义客户端),
 * 双手武器状态下的副手使用请求也不会被服务端执行。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Inject(method = "m_5760_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$rejectOffhandUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (packet.getHand() == InteractionHand.OFF_HAND
                && TwoHanded.isWielding(((ServerGamePacketListenerImpl) (Object) this).player)) {
            ci.cancel();
        }
    }

    @Inject(method = "m_6371_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void twohandedoffhand$rejectOffhandUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (packet.getHand() == InteractionHand.OFF_HAND
                && TwoHanded.isWielding(((ServerGamePacketListenerImpl) (Object) this).player)) {
            ci.cancel();
        }
    }
}
