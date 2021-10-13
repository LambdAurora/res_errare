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

package dev.lambdaurora.res_errare.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	private Vector3f position;
	private Vector3f front;
	private Vector3f up;
	private Vector3f right;
	private Vector3f worldUp;
	private final Matrix4f viewMatrix = new Matrix4f();

	private float yaw;
	private float pitch;

	public Camera() {
		this(new Vector3f(), new Vector3f(0, 1, 0), 0, 0);
	}

	public Camera(Vector3f position, Vector3f worldUp, float yaw, float pitch) {
		this.position = position;
		this.worldUp = worldUp;
		this.yaw = yaw;
		this.pitch = pitch;

		this.updateCameraVectors();
	}

	public void setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}

	public Vector3f getPosition() {
		return this.position;
	}

	/**
	 * {@return this camera's yaw}
	 */
	public float getYaw() {
		return this.yaw;
	}

	public void setYaw(float yaw) {
		this.setAngles(yaw, this.pitch);
	}

	/**
	 * {@return this camera's pitch}
	 */
	public float getPitch() {
		return this.pitch;
	}

	public void setPitch(float pitch) {
		this.setAngles(this.yaw, pitch);
	}

	public void setAngles(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;

		if (this.pitch > 89.f) {
			this.pitch = 89.f;
		}
		if (this.pitch < -89.f) {
			this.pitch = -89.f;
		}

		this.updateCameraVectors();
	}

	public Matrix4f getViewMatrix() {
		return this.viewMatrix.setLookAt(this.position, this.front, this.up);
	}

	/**
	 * Calculates the front vector from the Camera's (updated) Euler Angles.
	 */
	private void updateCameraVectors() {
		// Calculate the new front vector.
		double yawRadians = Math.toRadians(this.yaw);
		double pitchRadians = Math.toRadians(this.pitch);
		double pitchCos = Math.cos(pitchRadians);
		var front = new Vector3f((float) (Math.cos(yawRadians) * pitchCos), (float) Math.sin(pitchRadians), (float) (Math.sin(yawRadians) * pitchCos));

		this.front = front.normalize();
		// Also, re-calculate the right and up vector.
		this.right = new Vector3f(this.front).cross(this.worldUp).normalize(); // Normalize the vectors,
		// because their length gets closer to 0 the more you look up or down which results in slower movement.
		this.up = new Vector3f(this.right).cross(this.front).normalize();
	}
}
