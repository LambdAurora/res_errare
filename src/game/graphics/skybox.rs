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

use std::path::Path;

use super::cube_map::CubeMapTexture;
use super::Shader;

const SKYBOX_VERTICES: [f32; 108] = [
    // Positions
    -1.0, 1.0, -1.0,
    -1.0, -1.0, -1.0,
    1.0, -1.0, -1.0,
    1.0, -1.0, -1.0,
    1.0, 1.0, -1.0,
    -1.0, 1.0, -1.0,
    -1.0, -1.0, 1.0,
    -1.0, -1.0, -1.0,
    -1.0, 1.0, -1.0,
    -1.0, 1.0, -1.0,
    -1.0, 1.0, 1.0,
    -1.0, -1.0, 1.0,
    1.0, -1.0, -1.0,
    1.0, -1.0, 1.0,
    1.0, 1.0, 1.0,
    1.0, 1.0, 1.0,
    1.0, 1.0, -1.0,
    1.0, -1.0, -1.0,
    -1.0, -1.0, 1.0,
    -1.0, 1.0, 1.0,
    1.0, 1.0, 1.0,
    1.0, 1.0, 1.0,
    1.0, -1.0, 1.0,
    -1.0, -1.0, 1.0,
    -1.0, 1.0, -1.0,
    1.0, 1.0, -1.0,
    1.0, 1.0, 1.0,
    1.0, 1.0, 1.0,
    -1.0, 1.0, 1.0,
    -1.0, 1.0, -1.0,
    -1.0, -1.0, -1.0,
    -1.0, -1.0, 1.0,
    1.0, -1.0, -1.0,
    1.0, -1.0, -1.0,
    -1.0, -1.0, 1.0,
    1.0, -1.0, 1.0
];

struct VAOHolder {
    pub vao: u32,
    pub vbo: u32,
}

impl VAOHolder {
    pub fn bind(&mut self) {
        unsafe {
            if SKYBOX_VAO.vao == 0 {
                gl::GenVertexArrays(1, &mut self.vao);
                gl::GenBuffers(1, &mut self.vbo);
                gl::BindVertexArray(self.vao);
                gl::BindBuffer(gl::ARRAY_BUFFER, self.vbo);
                let size = (SKYBOX_VERTICES.len() * std::mem::size_of::<f32>()) as isize;
                let data = &SKYBOX_VERTICES[0] as *const f32 as *const std::ffi::c_void;
                gl::BufferData(gl::ARRAY_BUFFER, size, data, gl::STATIC_DRAW);
                gl::EnableVertexAttribArray(0);
                gl::VertexAttribPointer(0, 3, gl::FLOAT, gl::FALSE,
                                        (std::mem::size_of::<f32>() * 3) as i32, std::ptr::null());
            }

            gl::BindVertexArray(self.vao);
        }
    }
}

impl Drop for VAOHolder {
    fn drop(&mut self) {
        if self.vao != 0 {
            unsafe {
                gl::DeleteVertexArrays(1, &self.vao);
                gl::DeleteBuffers(1, &self.vbo);
            }
        }
    }
}

static mut SKYBOX_VAO: VAOHolder = VAOHolder { vao: 0, vbo: 0 };

/// Represents a skybox.
pub struct Skybox {
    pub cube_map: CubeMapTexture,
    pub shader: Shader,
}

impl Skybox {
    pub fn new(texture: CubeMapTexture, shader: Shader) -> Self {
        let mut skybox = Skybox { cube_map: texture, shader };
        skybox.shader.use_program();
        skybox.shader.set_i32("skybox", 0);
        skybox.scale(1.0);
        skybox
    }

    /// Returns a new skybox using the texture and loads the default skybox shader.
    pub fn load(texture: CubeMapTexture) -> Result<Self, String> {
        let shader = Shader::load(Path::new("assets/shaders/skybox.vsh"), Path::new("assets/shaders/skybox.fsh"))?;
        Ok(Skybox::new(texture, shader))
    }

    /// Sets the scale of the skybox.
    pub fn scale(&mut self, scale: f32) {
        self.shader.use_program();
        self.shader.set_float("scale", scale);
    }

    /// Draws the skybox.
    pub fn draw(&self) {
        unsafe { gl::DepthFunc(gl::LEQUAL); }
        self.shader.use_program();
        unsafe {
            SKYBOX_VAO.bind();
            gl::ActiveTexture(gl::TEXTURE0);
        }
        self.cube_map.bind();
        unsafe {
            gl::DrawArrays(gl::TRIANGLES, 0, 36);
            gl::BindVertexArray(0);
            gl::DepthFunc(gl::LESS);
        }
    }
}
