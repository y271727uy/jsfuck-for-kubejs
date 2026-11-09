package com.y71727uy.jsfuck.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.y71727uy.jsfuck.JsFuckScripts;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScriptManager.class, remap = false)
abstract class ScriptManagerMixin {
    @WrapOperation(method = "load()V", at = @At(value = "INVOKE",
        target = "Ldev/latvian/mods/rhino/Context;initStandardObjects()Ldev/latvian/mods/rhino/ScriptableObject;"))
    private ScriptableObject jsfuck$initScope(Context context, Operation<ScriptableObject> original) {
        ScriptableObject scope = original.call(context);
        JsFuckScripts.installBuiltins(context, scope);
        return scope;
    }
}
