package com.openmacro.core.engine.expression

import com.openmacro.core.engine.ExecutionContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recursive-descent expression parser and evaluator.
 *
 * Supports:
 * - Arithmetic: +, -, *, /, %
 * - Comparison: ==, !=, >, <, >=, <=
 * - Boolean: &&, ||, !
 * - String functions: contains(), startsWith(), endsWith(), length(), matches()
 * - Parentheses, literals (numbers, "strings", true/false)
 * - Variable references: v_name (global), lv_name (local)
 *
 * Operator precedence (low → high):
 * 1. || (OR)
 * 2. && (AND)
 * 3. ==, !=, >, <, >=, <= (comparison)
 * 4. +, - (additive)
 * 5. *, /, % (multiplicative)
 * 6. !, unary - (unary)
 * 7. function calls, literals, variables, parentheses (primary)
 */
@Singleton
class ExpressionEngine @Inject constructor() {

    /**
     * Evaluate an expression string and return the result.
     * Variables are resolved from the [context].
     */
    fun evaluate(expression: String, context: ExecutionContext): ExpressionValue {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return ExpressionValue.BoolVal(false)
        val parser = Parser(tokens, context)
        val result = parser.parseExpression()
        if (parser.pos < tokens.size) {
            throw ExpressionException("Unexpected token: ${tokens[parser.pos]}")
        }
        return result
    }

    /**
     * Evaluate an expression and return its boolean value.
     */
    fun evaluateBoolean(expression: String, context: ExecutionContext): Boolean {
        return evaluate(expression, context).toBoolean()
    }

    // ── Tokenizer ──

    private enum class TokenType {
        NUMBER, STRING, BOOL, IDENTIFIER,
        PLUS, MINUS, STAR, SLASH, PERCENT,
        EQ, NEQ, GT, LT, GTE, LTE,
        AND, OR, NOT,
        LPAREN, RPAREN, COMMA, DOT,
    }

    private data class Token(val type: TokenType, val value: String)

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val s = input.trim()

        while (i < s.length) {
            when {
                s[i].isWhitespace() -> i++

                s[i] == '"' -> {
                    // String literal
                    val sb = StringBuilder()
                    i++ // skip opening quote
                    while (i < s.length && s[i] != '"') {
                        if (s[i] == '\\' && i + 1 < s.length) {
                            i++
                            when (s[i]) {
                                'n' -> sb.append('\n')
                                't' -> sb.append('\t')
                                '"' -> sb.append('"')
                                '\\' -> sb.append('\\')
                                else -> { sb.append('\\'); sb.append(s[i]) }
                            }
                        } else {
                            sb.append(s[i])
                        }
                        i++
                    }
                    if (i < s.length) i++ // skip closing quote
                    tokens.add(Token(TokenType.STRING, sb.toString()))
                }

                s[i] == '\'' -> {
                    // Also support single-quoted strings
                    val sb = StringBuilder()
                    i++
                    while (i < s.length && s[i] != '\'') {
                        sb.append(s[i])
                        i++
                    }
                    if (i < s.length) i++
                    tokens.add(Token(TokenType.STRING, sb.toString()))
                }

                s[i].isDigit() || (s[i] == '.' && i + 1 < s.length && s[i + 1].isDigit()) -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    tokens.add(Token(TokenType.NUMBER, s.substring(start, i)))
                }

                s[i].isLetter() || s[i] == '_' -> {
                    val start = i
                    while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_')) i++
                    val word = s.substring(start, i)
                    when (word) {
                        "true" -> tokens.add(Token(TokenType.BOOL, "true"))
                        "false" -> tokens.add(Token(TokenType.BOOL, "false"))
                        else -> tokens.add(Token(TokenType.IDENTIFIER, word))
                    }
                }

                s[i] == '&' && i + 1 < s.length && s[i + 1] == '&' -> {
                    tokens.add(Token(TokenType.AND, "&&")); i += 2
                }
                s[i] == '|' && i + 1 < s.length && s[i + 1] == '|' -> {
                    tokens.add(Token(TokenType.OR, "||")); i += 2
                }
                s[i] == '!' && i + 1 < s.length && s[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.NEQ, "!=")); i += 2
                }
                s[i] == '=' && i + 1 < s.length && s[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.EQ, "==")); i += 2
                }
                s[i] == '>' && i + 1 < s.length && s[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.GTE, ">=")); i += 2
                }
                s[i] == '<' && i + 1 < s.length && s[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.LTE, "<=")); i += 2
                }
                s[i] == '>' -> { tokens.add(Token(TokenType.GT, ">")); i++ }
                s[i] == '<' -> { tokens.add(Token(TokenType.LT, "<")); i++ }
                s[i] == '!' -> { tokens.add(Token(TokenType.NOT, "!")); i++ }
                s[i] == '+' -> { tokens.add(Token(TokenType.PLUS, "+")); i++ }
                s[i] == '-' -> { tokens.add(Token(TokenType.MINUS, "-")); i++ }
                s[i] == '*' -> { tokens.add(Token(TokenType.STAR, "*")); i++ }
                s[i] == '/' -> { tokens.add(Token(TokenType.SLASH, "/")); i++ }
                s[i] == '%' -> { tokens.add(Token(TokenType.PERCENT, "%")); i++ }
                s[i] == '(' -> { tokens.add(Token(TokenType.LPAREN, "(")); i++ }
                s[i] == ')' -> { tokens.add(Token(TokenType.RPAREN, ")")); i++ }
                s[i] == ',' -> { tokens.add(Token(TokenType.COMMA, ",")); i++ }
                s[i] == '.' -> { tokens.add(Token(TokenType.DOT, ".")); i++ }
                else -> throw ExpressionException("Unexpected character: '${s[i]}' at position $i")
            }
        }
        return tokens
    }

    // ── Parser ──

    private inner class Parser(
        private val tokens: List<Token>,
        private val context: ExecutionContext,
    ) {
        var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun advance(): Token = tokens[pos++]
        private fun match(type: TokenType): Boolean {
            if (peek()?.type == type) { advance(); return true }
            return false
        }

        fun parseExpression(): ExpressionValue = parseOr()

        // Precedence level 1: ||
        private fun parseOr(): ExpressionValue {
            var left = parseAnd()
            while (peek()?.type == TokenType.OR) {
                advance()
                val right = parseAnd()
                left = ExpressionValue.BoolVal(left.toBoolean() || right.toBoolean())
            }
            return left
        }

        // Precedence level 2: &&
        private fun parseAnd(): ExpressionValue {
            var left = parseComparison()
            while (peek()?.type == TokenType.AND) {
                advance()
                val right = parseComparison()
                left = ExpressionValue.BoolVal(left.toBoolean() && right.toBoolean())
            }
            return left
        }

        // Precedence level 3: ==, !=, >, <, >=, <=
        private fun parseComparison(): ExpressionValue {
            var left = parseAdditive()
            while (true) {
                val op = peek()?.type
                if (op in listOf(
                        TokenType.EQ, TokenType.NEQ,
                        TokenType.GT, TokenType.LT,
                        TokenType.GTE, TokenType.LTE,
                    )
                ) {
                    advance()
                    val right = parseAdditive()
                    left = ExpressionValue.BoolVal(compareValues(left, right, op!!))
                } else break
            }
            return left
        }

        private fun compareValues(
            left: ExpressionValue,
            right: ExpressionValue,
            op: TokenType,
        ): Boolean {
            // If both can be numbers, compare numerically
            val ln = (left as? ExpressionValue.NumberVal)?.value
                ?: (left as? ExpressionValue.StringVal)?.value?.toDoubleOrNull()
            val rn = (right as? ExpressionValue.NumberVal)?.value
                ?: (right as? ExpressionValue.StringVal)?.value?.toDoubleOrNull()

            if (ln != null && rn != null) {
                return when (op) {
                    TokenType.EQ -> ln == rn
                    TokenType.NEQ -> ln != rn
                    TokenType.GT -> ln > rn
                    TokenType.LT -> ln < rn
                    TokenType.GTE -> ln >= rn
                    TokenType.LTE -> ln <= rn
                    else -> false
                }
            }

            // Fall back to string comparison
            val ls = left.toStringVal()
            val rs = right.toStringVal()
            return when (op) {
                TokenType.EQ -> ls == rs
                TokenType.NEQ -> ls != rs
                TokenType.GT -> ls > rs
                TokenType.LT -> ls < rs
                TokenType.GTE -> ls >= rs
                TokenType.LTE -> ls <= rs
                else -> false
            }
        }

        // Precedence level 4: +, -
        private fun parseAdditive(): ExpressionValue {
            var left = parseMultiplicative()
            while (true) {
                when (peek()?.type) {
                    TokenType.PLUS -> {
                        advance()
                        val right = parseMultiplicative()
                        left = if (left is ExpressionValue.StringVal || right is ExpressionValue.StringVal) {
                            ExpressionValue.StringVal(left.toStringVal() + right.toStringVal())
                        } else {
                            ExpressionValue.NumberVal(left.toNumber() + right.toNumber())
                        }
                    }
                    TokenType.MINUS -> {
                        advance()
                        val right = parseMultiplicative()
                        left = ExpressionValue.NumberVal(left.toNumber() - right.toNumber())
                    }
                    else -> break
                }
            }
            return left
        }

        // Precedence level 5: *, /, %
        private fun parseMultiplicative(): ExpressionValue {
            var left = parseUnary()
            while (true) {
                when (peek()?.type) {
                    TokenType.STAR -> {
                        advance()
                        val right = parseUnary()
                        left = ExpressionValue.NumberVal(left.toNumber() * right.toNumber())
                    }
                    TokenType.SLASH -> {
                        advance()
                        val right = parseUnary()
                        val divisor = right.toNumber()
                        if (divisor == 0.0) throw ExpressionException("Division by zero")
                        left = ExpressionValue.NumberVal(left.toNumber() / divisor)
                    }
                    TokenType.PERCENT -> {
                        advance()
                        val right = parseUnary()
                        val divisor = right.toNumber()
                        if (divisor == 0.0) throw ExpressionException("Modulo by zero")
                        left = ExpressionValue.NumberVal(left.toNumber() % divisor)
                    }
                    else -> break
                }
            }
            return left
        }

        // Precedence level 6: !, unary -
        private fun parseUnary(): ExpressionValue {
            if (peek()?.type == TokenType.NOT) {
                advance()
                val operand = parseUnary()
                return ExpressionValue.BoolVal(!operand.toBoolean())
            }
            if (peek()?.type == TokenType.MINUS) {
                advance()
                val operand = parseUnary()
                return ExpressionValue.NumberVal(-operand.toNumber())
            }
            return parsePostfix()
        }

        // Precedence level 7: method calls on values (e.g., value.contains("x"))
        private fun parsePostfix(): ExpressionValue {
            var value = parsePrimary()
            while (peek()?.type == TokenType.DOT) {
                advance()
                val methodName = if (peek()?.type == TokenType.IDENTIFIER) advance().value
                else throw ExpressionException("Expected method name after '.'")

                if (!match(TokenType.LPAREN)) {
                    throw ExpressionException("Expected '(' after method name '$methodName'")
                }

                val args = mutableListOf<ExpressionValue>()
                if (peek()?.type != TokenType.RPAREN) {
                    args.add(parseExpression())
                    while (match(TokenType.COMMA)) {
                        args.add(parseExpression())
                    }
                }
                if (!match(TokenType.RPAREN)) {
                    throw ExpressionException("Expected ')' after method arguments")
                }

                value = callMethod(value, methodName, args)
            }
            return value
        }

        // Primary: literals, variables, function calls, parenthesized expressions
        private fun parsePrimary(): ExpressionValue {
            val token = peek() ?: throw ExpressionException("Unexpected end of expression")
            return when (token.type) {
                TokenType.NUMBER -> {
                    advance()
                    ExpressionValue.NumberVal(token.value.toDouble())
                }
                TokenType.STRING -> {
                    advance()
                    ExpressionValue.StringVal(token.value)
                }
                TokenType.BOOL -> {
                    advance()
                    ExpressionValue.BoolVal(token.value == "true")
                }
                TokenType.LPAREN -> {
                    advance()
                    val expr = parseExpression()
                    if (!match(TokenType.RPAREN)) {
                        throw ExpressionException("Expected ')'")
                    }
                    expr
                }
                TokenType.IDENTIFIER -> {
                    advance()
                    val name = token.value

                    // Check if it's a built-in function call
                    if (peek()?.type == TokenType.LPAREN) {
                        advance()
                        val args = mutableListOf<ExpressionValue>()
                        if (peek()?.type != TokenType.RPAREN) {
                            args.add(parseExpression())
                            while (match(TokenType.COMMA)) {
                                args.add(parseExpression())
                            }
                        }
                        if (!match(TokenType.RPAREN)) {
                            throw ExpressionException("Expected ')' after function arguments")
                        }
                        callFunction(name, args)
                    } else {
                        // Variable reference
                        resolveVariable(name)
                    }
                }
                else -> throw ExpressionException("Unexpected token: ${token.value}")
            }
        }

        private fun resolveVariable(name: String): ExpressionValue {
            // Local variable (lv_ prefix)
            if (name.startsWith("lv_")) {
                val value = context.localVariables[name]
                    ?: return ExpressionValue.StringVal("")
                return parseValueString(value)
            }

            // Global variable (v_ prefix)
            if (name.startsWith("v_")) {
                val varName = name.removePrefix("v_")
                val value = context.variableStore?.getGlobal(varName)
                    ?: return ExpressionValue.StringVal("")
                return parseValueString(value)
            }

            // Try as global variable without prefix (convenience)
            val value = context.variableStore?.getGlobal(name)
            if (value != null) return parseValueString(value)

            // Unknown — treat as empty string
            return ExpressionValue.StringVal("")
        }

        private fun parseValueString(value: String): ExpressionValue {
            // Try to parse as number
            value.toDoubleOrNull()?.let { return ExpressionValue.NumberVal(it) }
            // Try as boolean
            if (value == "true") return ExpressionValue.BoolVal(true)
            if (value == "false") return ExpressionValue.BoolVal(false)
            // Strip JSON quotes if present
            if (value.startsWith("\"") && value.endsWith("\"") && value.length >= 2) {
                return ExpressionValue.StringVal(value.substring(1, value.length - 1))
            }
            return ExpressionValue.StringVal(value)
        }

        private fun callFunction(name: String, args: List<ExpressionValue>): ExpressionValue {
            return when (name) {
                "length" -> {
                    if (args.size != 1) throw ExpressionException("length() takes 1 argument")
                    ExpressionValue.NumberVal(args[0].toStringVal().length.toDouble())
                }
                "contains" -> {
                    if (args.size != 2) throw ExpressionException("contains() takes 2 arguments")
                    ExpressionValue.BoolVal(args[0].toStringVal().contains(args[1].toStringVal(), ignoreCase = true))
                }
                "startsWith" -> {
                    if (args.size != 2) throw ExpressionException("startsWith() takes 2 arguments")
                    ExpressionValue.BoolVal(args[0].toStringVal().startsWith(args[1].toStringVal()))
                }
                "endsWith" -> {
                    if (args.size != 2) throw ExpressionException("endsWith() takes 2 arguments")
                    ExpressionValue.BoolVal(args[0].toStringVal().endsWith(args[1].toStringVal()))
                }
                "matches" -> {
                    if (args.size != 2) throw ExpressionException("matches() takes 2 arguments")
                    val regex = try { Regex(args[1].toStringVal()) } catch (_: Exception) {
                        throw ExpressionException("Invalid regex: ${args[1].toStringVal()}")
                    }
                    ExpressionValue.BoolVal(regex.containsMatchIn(args[0].toStringVal()))
                }
                "toNumber" -> {
                    if (args.size != 1) throw ExpressionException("toNumber() takes 1 argument")
                    ExpressionValue.NumberVal(args[0].toNumber())
                }
                "toString" -> {
                    if (args.size != 1) throw ExpressionException("toString() takes 1 argument")
                    ExpressionValue.StringVal(args[0].toStringVal())
                }
                "abs" -> {
                    if (args.size != 1) throw ExpressionException("abs() takes 1 argument")
                    ExpressionValue.NumberVal(kotlin.math.abs(args[0].toNumber()))
                }
                "min" -> {
                    if (args.size != 2) throw ExpressionException("min() takes 2 arguments")
                    ExpressionValue.NumberVal(kotlin.math.min(args[0].toNumber(), args[1].toNumber()))
                }
                "max" -> {
                    if (args.size != 2) throw ExpressionException("max() takes 2 arguments")
                    ExpressionValue.NumberVal(kotlin.math.max(args[0].toNumber(), args[1].toNumber()))
                }
                "round" -> {
                    if (args.size != 1) throw ExpressionException("round() takes 1 argument")
                    ExpressionValue.NumberVal(kotlin.math.round(args[0].toNumber()))
                }
                "floor" -> {
                    if (args.size != 1) throw ExpressionException("floor() takes 1 argument")
                    ExpressionValue.NumberVal(kotlin.math.floor(args[0].toNumber()))
                }
                "ceil" -> {
                    if (args.size != 1) throw ExpressionException("ceil() takes 1 argument")
                    ExpressionValue.NumberVal(kotlin.math.ceil(args[0].toNumber()))
                }
                else -> throw ExpressionException("Unknown function: $name()")
            }
        }

        private fun callMethod(
            receiver: ExpressionValue,
            name: String,
            args: List<ExpressionValue>,
        ): ExpressionValue {
            val str = receiver.toStringVal()
            return when (name) {
                "contains" -> {
                    if (args.size != 1) throw ExpressionException(".contains() takes 1 argument")
                    ExpressionValue.BoolVal(str.contains(args[0].toStringVal(), ignoreCase = true))
                }
                "startsWith" -> {
                    if (args.size != 1) throw ExpressionException(".startsWith() takes 1 argument")
                    ExpressionValue.BoolVal(str.startsWith(args[0].toStringVal()))
                }
                "endsWith" -> {
                    if (args.size != 1) throw ExpressionException(".endsWith() takes 1 argument")
                    ExpressionValue.BoolVal(str.endsWith(args[0].toStringVal()))
                }
                "length" -> {
                    ExpressionValue.NumberVal(str.length.toDouble())
                }
                "matches" -> {
                    if (args.size != 1) throw ExpressionException(".matches() takes 1 argument")
                    val regex = try { Regex(args[0].toStringVal()) } catch (_: Exception) {
                        throw ExpressionException("Invalid regex: ${args[0].toStringVal()}")
                    }
                    ExpressionValue.BoolVal(regex.containsMatchIn(str))
                }
                "trim" -> ExpressionValue.StringVal(str.trim())
                "uppercase" -> ExpressionValue.StringVal(str.uppercase())
                "lowercase" -> ExpressionValue.StringVal(str.lowercase())
                "substring" -> {
                    val start = args.getOrNull(0)?.toNumber()?.toInt() ?: 0
                    val end = args.getOrNull(1)?.toNumber()?.toInt() ?: str.length
                    ExpressionValue.StringVal(str.substring(start.coerceIn(0, str.length), end.coerceIn(0, str.length)))
                }
                "replace" -> {
                    if (args.size != 2) throw ExpressionException(".replace() takes 2 arguments")
                    ExpressionValue.StringVal(str.replace(args[0].toStringVal(), args[1].toStringVal()))
                }
                "indexOf" -> {
                    if (args.size != 1) throw ExpressionException(".indexOf() takes 1 argument")
                    ExpressionValue.NumberVal(str.indexOf(args[0].toStringVal()).toDouble())
                }
                else -> throw ExpressionException("Unknown method: .$name()")
            }
        }
    }
}
