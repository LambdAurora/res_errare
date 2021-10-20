package dev.lambdaurora.res_errare.input;

public enum ButtonAction {
	RELEASE(0),
	PRESS(1),
	REPEAT(2),
	NONE(-1);

	private static final ButtonAction[] VALUES = values();
	private final int id;

	ButtonAction(int id) {
		this.id = id;
	}

	public int id() {
		return this.id;
	}

	public boolean isPressing() {
		return this == PRESS || this == REPEAT;
	}

	public static ButtonAction byId(int id) {
		for (var action : VALUES) {
			if (action.id() == id)
				return action;
		}

		return NONE;
	}
}
