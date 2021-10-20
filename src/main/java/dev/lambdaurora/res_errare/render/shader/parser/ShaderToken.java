package dev.lambdaurora.res_errare.render.shader.parser;

/**
 * Represents the token of lexed shader code.
 */
public record ShaderToken(ShaderTokenType type, String value, int line, int offset) {
}
