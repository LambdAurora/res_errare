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

import dev.lambdaurora.res_errare.render.buffer.BufferLayout;
import dev.lambdaurora.res_errare.render.buffer.BufferTarget;
import dev.lambdaurora.res_errare.render.buffer.BufferUsage;
import dev.lambdaurora.res_errare.render.buffer.GraphicsBuffer;
import dev.lambdaurora.res_errare.system.GL;
import org.joml.Matrix4f;

public class GameRenderer implements AutoCloseable {
	private static final float NEAR = .1f;
	private static final float FAR = 100.f;

	private final GraphicsBuffer ubo = GraphicsBuffer.of(BufferTarget.UNIFORM, BufferUsage.STREAM_DRAW, BufferLayout.builder()
			.addMatrix4fRange()
			.addMatrix4fRange()
			.build()
	);
	private Matrix4f projection = new Matrix4f().identity();
	private Matrix4f ortho = new Matrix4f().identity();

	public GameRenderer() {
		this.init();
	}

	private void init() {
		this.ubo.reset();
		this.ubo.unbind();
		// Define the range of the buffer that links to a uniform binding point.
		GL.get().bindBufferRange(this.ubo.target(), 0, this.ubo.id(), 0, this.ubo.layout().size());
	}

	private void updatePerspective(int width, int height) {
		this.projection = projection.setPerspective((float) Math.toRadians(75), (float) width / height, NEAR, FAR);
		this.ubo.bind();
		this.ubo.layout().<Matrix4f>get(0).set(this.ubo, this.projection);
		this.ubo.unbind();
	}

	public void setupProjection(int width, int height) {
		GL.get().viewport(0, 0, width, height);
		this.updatePerspective(width, height);
		this.ortho = this.ortho.ortho(0.f, width, height, 0.f, 0.f, 1.f);
	}

	public void updateView(Matrix4f view) {
		this.ubo.bind();
		this.ubo.layout().<Matrix4f>get(1).set(this.ubo, view);
		this.ubo.unbind();
	}

	@Override
	public void close() {
		this.ubo.close();
	}
}
