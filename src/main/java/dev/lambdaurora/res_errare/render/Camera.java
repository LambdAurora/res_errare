package dev.lambdaurora.res_errare.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	private Vector3f position;
	private Vector3f front;
	private Vector3f up;
	private Vector3f right;
	private Vector3f worldUp;

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
		return new Matrix4f().lookAt(this.position, this.front, this.up);
	}

	/**
	 * Calculates the front vector from the Camera's (updated) Eular Angles.
	 */
	private void updateCameraVectors() {
		// Calculate the new front vector.
		double yawRadians = Math.toRadians(this.yaw);
		double pitchRadians = Math.toRadians(this.pitch);
		double pitchCos = Math.cos(pitchRadians);
		var front = new Vector3f((float) (Math.cos(yawRadians) * pitchCos), (float) Math.sin(pitchRadians), (float) (Math.sin(yawRadians) * pitchCos));

		this.front = front.normalize();
		// Also, re-calculate the right and up vector.
		this.right = this.front.cross(this.worldUp).normalize(); // Normalize the vectors,
		// because their length gets closer to 0 the more you look up or down which results in slower movement.
		this.up = this.right.cross(this.front).normalize();
	}
}
