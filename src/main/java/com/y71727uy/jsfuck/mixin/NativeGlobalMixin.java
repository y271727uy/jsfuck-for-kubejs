package com.y71727uy.jsfuck.mixin;

import com.y71727uy.jsfuck.JsFuckScripts;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeGlobal;
import dev.latvian.mods.rhino.Scriptable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NativeGlobal.class, remap = false)
abstract class NativeGlobalMixin {
    @Inject(method = "js_eval", at = @At("HEAD"), cancellable = true)
    private static void jsfuck$evalInGlobalScope(Context context, Scriptable scope, Object[] args,
                                                 CallbackInfoReturnable<Object> callback) {
        if (JsFuckScripts.allowsIndirectEval(context)) {
            callback.setReturnValue(JsFuckScripts.indirectEval(context, scope, args));
        }
    }
}
