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

extern crate gl;

use std::ffi::c_void;
use std::mem::size_of;

use cgmath::*;

use super::Shader;
use super::Texture;

/**
 * Represents a vertex.
 */
#[repr(C)]
pub struct Vertex {
    // position
    pub position: Vector3<f32>,
    // normal
    pub normal: Vector3<f32>,
    // Texture coordinates
    pub texture_coords: Vector2<f32>,
}

impl Default for Vertex {
    fn default() -> Self {
        Vertex {
            position: Vector3::zero(),
            normal: Vector3::zero(),
            texture_coords: Vector2::zero(),
        }
    }
}

/**
 * Represents a mesh texture.
 *
 * Contains data about its path and texture type.
 */
#[derive(Clone)]
pub struct MeshTexture {
    pub texture: Texture,
    pub texture_type: String,
    pub path: String,
}

/**
 * Represents a mesh.
 */
pub struct Mesh {
    /*  Mesh Data  */
    pub vertices: Vec<Vertex>,
    pub indices: Vec<u32>,
    pub textures: Vec<MeshTexture>,
    pub vao: u32,

    /*  Render data  */
    vbo: u32,
    ebo: u32,
}

impl Mesh {
    pub fn new(vertices: Vec<Vertex>, indices: Vec<u32>, textures: Vec<MeshTexture>) -> Mesh {
        let mut mesh = Mesh {
            vertices,
            indices,
            textures,
            vao: 0,
            vbo: 0,
            ebo: 0,
        };

        // now that we have all the required data, set the vertex buffers and its attribute pointers.
        unsafe { mesh.setup_mesh() }
        mesh
    }

    /// Renders the mesh.
    pub fn draw(&self, shader: &mut Shader) {
        shader.use_program();

        // bind appropriate textures
        let mut diffuse_n = 0;
        let mut specular_n = 0;
        let mut normal_n = 0;
        let mut height_n = 0;
        for (i, texture) in self.textures.iter().enumerate() {
            unsafe { gl::ActiveTexture(gl::TEXTURE0 + i as u32); } // Active proper texture unit before binding
            // Retrieve texture number (the N in diffuse_textureN)
            let name = &texture.texture_type;
            let number = match name.as_str() {
                "texture_diffuse" => {
                    diffuse_n += 1;
                    diffuse_n
                }
                "texture_specular" => {
                    specular_n += 1;
                    specular_n
                }
                "texture_normal" => {
                    normal_n += 1;
                    normal_n
                }
                "texture_height" => {
                    height_n += 1;
                    height_n
                }
                _ => panic!("unknown texture type")
            };
            // now set the sampler to the correct texture unit
            shader.set_i32(format!("{}{}", name, number).as_str(), i as i32);
            // and finally bind the texture.
            texture.texture.bind();
        }

        // Draw mesh.
        unsafe {
            gl::BindVertexArray(self.vao);
            gl::DrawElements(gl::TRIANGLES, self.indices.len() as i32, gl::UNSIGNED_INT, std::ptr::null());
            gl::BindVertexArray(0);

            gl::ActiveTexture(gl::TEXTURE0);
        }

        // Reset stuff.
        unsafe { gl::ActiveTexture(gl::TEXTURE0); }
        super::texture::unbind();
    }

    unsafe fn setup_mesh(&mut self) {
        // Create buffers/arrays.
        gl::GenVertexArrays(1, &mut self.vao);
        gl::GenBuffers(1, &mut self.vbo);
        gl::GenBuffers(1, &mut self.ebo);

        gl::BindVertexArray(self.vao);
        // Load data into vertex buffers.
        gl::BindBuffer(gl::ARRAY_BUFFER, self.vbo);
        // A great thing about structs with repr(C) is that their memory layout is sequential for all its items.
        // The effect is that we can simply pass a pointer to the struct and it translates perfectly to a glm::vec3/2 array which
        // again translates to 3/2 floats which translates to a byte array.
        let size = (self.vertices.len() * size_of::<Vertex>()) as isize;
        let data = &self.vertices[0] as *const Vertex as *const c_void;
        gl::BufferData(gl::ARRAY_BUFFER, size, data, gl::STATIC_DRAW);

        gl::BindBuffer(gl::ELEMENT_ARRAY_BUFFER, self.ebo);
        let size = (self.indices.len() * size_of::<u32>()) as isize;
        let data = &self.indices[0] as *const u32 as *const c_void;
        gl::BufferData(gl::ELEMENT_ARRAY_BUFFER, size, data, gl::STATIC_DRAW);

        // Set the vertex attribute pointers.
        let size = size_of::<Vertex>() as i32;
        // vertex position
        gl::EnableVertexAttribArray(0);
        gl::VertexAttribPointer(0, 3, gl::FLOAT, gl::FALSE, size, offset_of!(Vertex, position) as *const c_void);
        // vertex normals
        gl::EnableVertexAttribArray(1);
        gl::VertexAttribPointer(1, 3, gl::FLOAT, gl::FALSE, size, offset_of!(Vertex, normal) as *const c_void);
        // vertex texture coords
        gl::EnableVertexAttribArray(2);
        gl::VertexAttribPointer(2, 2, gl::FLOAT, gl::FALSE, size, offset_of!(Vertex, texture_coords) as *const c_void);

        gl::BindVertexArray(0);
    }
}

impl Drop for Mesh {
    fn drop(&mut self) {
        unsafe {
            gl::DeleteVertexArrays(1, &self.vao);
            gl::DeleteBuffers(1, &self.vbo);
            gl::DeleteBuffers(1, &self.ebo);
        }
    }
}
