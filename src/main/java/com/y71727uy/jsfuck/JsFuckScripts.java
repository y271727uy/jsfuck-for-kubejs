package com.y71727uy.jsfuck;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public final class JsFuckScripts {
    private static final String BUILTINS_PATH = "/jsfuck/builtins.js";
    private static final ThreadLocal<Context> ENCODED_SCRIPT_CONTEXT = new ThreadLocal<>();

    private JsFuckScripts() {
    }

    public static void installBuiltins(Context context, Scriptable scope) {
        try (InputStream stream = JsFuckScripts.class.getResourceAsStream(BUILTINS_PATH)) {
            if (stream == null) {
                throw new IOException("Missing " + BUILTINS_PATH);
            }
            String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            context.evaluateString(scope, source, BUILTINS_PATH, 1, null);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public static Object evaluate(Context context, String source, String name, int line,
                                  Function<String, Object> original) {
        Context previous = ENCODED_SCRIPT_CONTEXT.get();
        boolean encoded = isEncodedScript(source);
        // Regular script loads nested inside a JSFuck script keep Rhino's indirect eval behavior.
        ENCODED_SCRIPT_CONTEXT.set(encoded ? context : null);
        try {
            String compiledSource = encoded ? AdditionChains.split(context, source, name, line) : source;
            return original.apply(compiledSource);
        } finally {
            if (previous == null) {
                ENCODED_SCRIPT_CONTEXT.remove();
            } else {
                ENCODED_SCRIPT_CONTEXT.set(previous);
            }
        }
    }

    public static boolean allowsIndirectEval(Context context) {
        return ENCODED_SCRIPT_CONTEXT.get() == context;
    }

    public static Object indirectEval(Context context, Scriptable scope, Object[] args) {
        Scriptable global = ScriptableObject.getTopLevelScope(scope);
        // The wrapper gets indirect eval. Its decoded JavaScript runs under the usual rules.
        ENCODED_SCRIPT_CONTEXT.remove();
        try {
            return ScriptRuntime.evalSpecial(context, global, global, args, null, 1);
        } finally {
            ENCODED_SCRIPT_CONTEXT.set(context);
        }
    }

    private static boolean isEncodedScript(String source) {
        boolean hasToken = false;
        for (int i = 0; i < source.length(); i++) {
            char character = source.charAt(i);
            switch (character) {
                case '[', ']', '(', ')', '!', '+' -> hasToken = true;
                default -> {
                    if (!Character.isWhitespace(character) && !Character.isSpaceChar(character)
                        && character != '\uFEFF') {
                        return false;
                    }
                }
            }
        }
        return hasToken;
    }
}
