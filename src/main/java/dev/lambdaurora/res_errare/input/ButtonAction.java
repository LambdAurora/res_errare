/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
