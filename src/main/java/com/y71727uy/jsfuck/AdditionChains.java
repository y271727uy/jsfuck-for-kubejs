package com.y71727uy.jsfuck;

import dev.latvian.mods.rhino.CompilerEnvirons;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Node;
import dev.latvian.mods.rhino.Parser;
import dev.latvian.mods.rhino.Token;
import dev.latvian.mods.rhino.ast.ArrayLiteral;
import dev.latvian.mods.rhino.ast.AstNode;
import dev.latvian.mods.rhino.ast.ElementGet;
import dev.latvian.mods.rhino.ast.ExpressionStatement;
import dev.latvian.mods.rhino.ast.FunctionCall;
import dev.latvian.mods.rhino.ast.InfixExpression;
import dev.latvian.mods.rhino.ast.ParenthesizedExpression;
import dev.latvian.mods.rhino.ast.UnaryExpression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class AdditionChains {
    private static final int CHUNK_SIZE = 128;

    private AdditionChains() {
    }

    static String split(Context context, String source, String name, int line) {
        if (source.length() <= CHUNK_SIZE * 3) {
            return source;
        }

        var compiler = new CompilerEnvirons();
        compiler.initFromContext(context);
        var pending = new ArrayDeque<AstNode>();
        for (Node statement : new Parser(context, compiler).parse(source, name, line)) {
            pending.add((AstNode) statement);
        }

        Map<Integer, String> insertions = new TreeMap<>();
        while (!pending.isEmpty()) {
            AstNode node = pending.pop();
            if (node instanceof InfixExpression addition && node.getType() == Token.ADD) {
                splitChain(addition, pending, insertions);
            } else if (node instanceof ExpressionStatement statement) {
                pending.push(statement.getExpression());
            } else if (node instanceof ParenthesizedExpression group) {
                pending.push(group.getExpression());
            } else if (node instanceof UnaryExpression unary) {
                pending.push(unary.getOperand());
            } else if (node instanceof ElementGet element) {
                pending.push(element.getTarget());
                pending.push(element.getElement());
            } else if (node instanceof FunctionCall call) {
                pending.push(call.getTarget());
                pending.addAll(call.getArguments());
            } else if (node instanceof ArrayLiteral array) {
                pending.addAll(array.getElements());
            }
        }

        if (insertions.isEmpty()) {
            return source;
        }
        var result = new StringBuilder(source.length());
        int previous = 0;
        for (var insertion : insertions.entrySet()) {
            int position = insertion.getKey();
            result.append(source, previous, position).append(insertion.getValue());
            previous = position;
        }
        return result.append(source, previous, source.length()).toString();
    }

    private static void splitChain(InfixExpression root, Deque<AstNode> pending,
                                   Map<Integer, String> insertions) {
        int position = 0;
        List<Integer> operators = new ArrayList<>();
        AstNode node = root;
        while (node instanceof InfixExpression addition && node.getType() == Token.ADD) {
            operators.add(position + addition.getOperatorPosition());
            pending.push(addition.getRight());
            node = addition.getLeft();
            position += node.getPosition();
        }
        pending.push(node);

        if (operators.size() <= CHUNK_SIZE) {
            return;
        }

        // Keep addition left-associative, including the timing of each operand's coercion.
        // A local accumulator gives Rhino shallow trees and preserves the source line numbers.
        int start = root.getAbsolutePosition();
        insertions.put(start, "(function(){var sum=");
        for (int index = operators.size() - 1 - CHUNK_SIZE; index >= 0; index -= CHUNK_SIZE) {
            insertions.put(start + operators.get(index), ";sum=sum");
        }
        insertions.put(start + root.getLength(), ";return sum;})()");
    }
}
