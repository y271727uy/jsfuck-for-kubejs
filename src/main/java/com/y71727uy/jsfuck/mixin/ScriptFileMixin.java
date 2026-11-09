package com.y71727uy.jsfuck.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.y71727uy.jsfuck.JsFuckScripts;
import dev.latvian.mods.kubejs.script.ScriptFile;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScriptFile.class, remap = false)
abstract class ScriptFileMixin {
    @WrapOperation(method = "load()V", at = @At(value = "INVOKE",
        target = "Ldev/latvian/mods/rhino/Context;evaluateString(Ldev/latvian/mods/rhino/Scriptable;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/Object;"))
    private Object jsfuck$evaluate(Context context, Scriptable scope, String source, String name, int line,
                                  Object securityDomain, Operation<Object> original) {
        return JsFuckScripts.evaluate(context, source, name, line,
            compiledSource -> original.call(context, scope, compiledSource, name, line, securityDomain));
    }
}
