(function () {
    'use strict';

    function define(prototype, name, value) {
        Object.defineProperty(prototype, name, {value: value, configurable: true, writable: true});
    }

    function toInteger(value) {
        const number = +value;
        return number !== number ? 0 : Math.trunc(number);
    }

    function toLength(value) {
        return Math.min(Math.max(toInteger(value), 0), 9007199254740991);
    }

    function atIndex(length, index) {
        const offset = toInteger(index);
        return offset < 0 ? length + offset : offset;
    }

    function requireReceiver(receiver) {
        if (receiver == null) {
            throw new TypeError('Cannot convert null or undefined');
        }
    }

    if (typeof Array.prototype.at !== 'function') {
        define(Array.prototype, 'at', function at(index) {
            requireReceiver(this);
            const length = toLength(this.length);
            const offset = atIndex(length, index);
            return offset >= 0 && offset < length ? this[offset] : undefined;
        });
    }

    if (typeof String.prototype.at !== 'function') {
        define(String.prototype, 'at', function at(index) {
            requireReceiver(this);
            const string = String(this);
            const offset = atIndex(string.length, index);
            return offset >= 0 && offset < string.length ? string[offset] : undefined;
        });
    }

    function flatten(target, source, length, depth, offset) {
        for (let index = 0; index < length; index++) {
            if (!(index in source)) {
                continue;
            }
            var element = source[index];
            if (depth > 0 && Array.isArray(element)) {
                offset = flatten(target, element, toLength(element.length), depth - 1, offset);
            } else {
                if (offset >= 9007199254740991) {
                    throw new TypeError('Flattened array exceeds maximum length');
                }
                Object.defineProperty(target, offset++, {
                    value: element, configurable: true, enumerable: true, writable: true
                });
            }
        }
        return offset;
    }

    if (typeof Array.prototype.flat !== 'function') {
        define(Array.prototype, 'flat', function flat() {
            requireReceiver(this);
            const source = Object(this);
            const length = toLength(source.length);
            const depth = arguments[0] === undefined ? 1 : toInteger(arguments[0]);
            let species = Array.isArray(source) ? source.constructor : undefined;
            if (species != null && (typeof species === 'object' || typeof species === 'function')) {
                species = species[Symbol.species];
                if (species === null) {
                    species = undefined;
                }
            }
            const result = species === undefined ? [] : new species(0);
            flatten(result, source, length, depth, 0);
            return result;
        });
    }

    // Rhino 2001 leaves quotes in the color attribute unescaped.
    if (''.fontcolor('"') !== '<font color="&quot;"></font>') {
        define(String.prototype, 'fontcolor', function fontcolor(color) {
            requireReceiver(this);
            const string = String(this);
            return '<font color="' + String(color).replace(/"/g, '&quot;') + '">' + string + '</font>';
        });
    }

    // JSFuck takes individual characters from the printed forms of these intrinsics.
    const originalToString = Function.prototype.toString;
    const intrinsics = [Array, Boolean, Function, Number, RegExp, String,
        Array.prototype.at, Array.prototype.fill, Array.prototype.filter, Array.prototype.flat];
    define(Function.prototype, 'toString', function toString() {
        if (intrinsics.indexOf(this) >= 0) {
            return 'function ' + this.name + '() { [native code] }';
        }
        return originalToString.call(this);
    });
})();
