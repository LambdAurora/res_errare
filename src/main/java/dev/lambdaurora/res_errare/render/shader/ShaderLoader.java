package dev.lambdaurora.res_errare.render.shader;

import dev.lambdaurora.res_errare.parser.token.UnknownTokenException;
import dev.lambdaurora.res_errare.render.shader.error.ShaderPreprocessError;
import dev.lambdaurora.res_errare.render.shader.parser.ShaderLexer;
import dev.lambdaurora.res_errare.resource.ResourceManager;
import dev.lambdaurora.res_errare.resource.ResourceType;
import dev.lambdaurora.res_errare.util.Identifier;
import dev.lambdaurora.res_errare.util.InvalidIdentifierException;
import dev.lambdaurora.res_errare.util.Result;
import dev.lambdaurora.res_errare.util.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public record ShaderLoader(ResourceManager resourceManager) {
	public static final ShaderLoader DEFAULT_LOADER = new ShaderLoader(ResourceManager.getDefault(ResourceType.ASSETS));
	private static final String SHADER_BASE_PATH = "shaders";

	public String getRawShaderSource(Identifier shaderId) throws IOException {
		return this.resourceManager.getStringFrom(shaderId.prepend(SHADER_BASE_PATH));
	}

	public Result<String, Shader.CreationException> loadShaderSource(Identifier shaderId) {
		try {
			var source = this.getRawShaderSource(shaderId);

			return preprocessSource(source).mapError(Shader.CreationException::new);
		} catch (IOException e) {
			return Result.fail(new Shader.CreationException("Could not loader shader " + shaderId + ".", e));
		}
	}

	public Result<String, ShaderPreprocessError> preprocessSource(String source) {
		var lexer = new ShaderLexer(source);

		var processedSource = new StringBuilder();
		while (lexer.hasNext()) {
			try {
				var token = lexer.next();

				switch (token.type()) {
					case CHAR, STRING -> processedSource.append(token.value());
					case DIRECTIVE -> {
						var result = readPreprocessorDirective(token.value());
						if (result.hasError())
							return Result.fail(result.getError());

						if (result.get().directive() == DirectiveType.INCLUDE) {
							var params = result.get().params();
							if (params.length != 1)
								return Result.fail(new ShaderPreprocessError("Malformed include directive \"" + token.value() + "\" at line "
										+ token.line() + ": expected 1 parameter found " + params.length + "."));

							try {
								var id = new Identifier(params[0]);

								var loadedShader = this.loadShaderSource(id);
								if (loadedShader.hasError())
									return loadedShader.mapError(ShaderPreprocessError::new);

								processedSource.append('\n').append(loadedShader.get());
							} catch (InvalidIdentifierException e) {
								return Result.fail(new ShaderPreprocessError("Cannot include shader " + params[0] + " as the given identifier is invalid.", e));
							}
						} else {
							processedSource.append(result.get().raw());
						}
					}
					case RAW -> {
						var lines = token.value().split("\n");

						for (int i = 0; i < lines.length; i++) {
							int line = token.line() + i;
							lines[i] = lines[i].replace("__LINE__", String.valueOf(line));
							processedSource.append(lines[i]);
							if (i != lines.length - 1)
								processedSource.append('\n');
						}
					}
				}
			} catch (UnknownTokenException e) {
				return Result.fail(new ShaderPreprocessError(e));
			}
		}

		return Result.ok(processedSource.toString());
	}

	private Result<PreProcessorDirective, ShaderPreprocessError> readPreprocessorDirective(String line) {
		var directiveName = new StringBuilder();
		boolean hasSeenNormalCharacter = false;
		int i;
		for (i = 1; i < line.length(); i++) {
			char c = line.charAt(i);

			if (Character.isWhitespace(c)) {
				if (hasSeenNormalCharacter)
					break;
			} else {
				directiveName.append(c);
				hasSeenNormalCharacter = true;
			}
		}

		var type = DirectiveType.byName(directiveName.toString());
		if (type == null)
			return Result.fail(new ShaderPreprocessError("Could not find "));

		var params = new ArrayList<String>();
		for (int j = i; j < line.length(); j++) {
			while (Character.isWhitespace(StringUtil.getCharAt(line, j))) {
				j++; // skip whitespaces;
			}

			int k = j;
			while (!Character.isWhitespace(StringUtil.getCharAt(line, k)) && k < line.length()) {
				k++;
			}

			params.add(line.substring(j, k));

			j = k;
		}

		return Result.ok(new PreProcessorDirective(type, line, params.toArray(String[]::new)));
	}

	public enum DirectiveType {
		DEFINE("define", -1),
		ELIF("elif", -1),
		ELSE("else", 0),
		ENDIF("endif", 0),
		ERROR("error", -1),
		IF("if", -1),
		IFDEF("ifdef", 1),
		IFNDEF("ifndef", 1),
		INCLUDE("include", 1),
		PRAGMA("pragma", -1),
		UNDEF("undef", 1),
		VERSION("version", 1);

		private static final Map<String, DirectiveType> BY_NAME = new Object2ObjectOpenHashMap<>();
		private final String name;
		private final int expectedParamCount;

		DirectiveType(String name, int expectedParamCount) {
			this.name = name;
			this.expectedParamCount = expectedParamCount;
		}

		public String getName() {
			return this.name;
		}

		public int getExpectedParamCount() {
			return this.expectedParamCount;
		}

		public static @Nullable ShaderLoader.DirectiveType byName(String name) {
			return BY_NAME.get(name);
		}

		static {
			for (var value : values()) {
				BY_NAME.put(value.getName(), value);
			}
		}
	}

	private record PreProcessorDirective(DirectiveType directive, String raw, String... params) {
	}
}
