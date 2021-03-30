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

use std::mem::size_of;

use cgmath::prelude::*;

pub use super::Client;
pub use super::graphics;
pub use super::graphics::Mat4;

const NEAR: f32 = 0.1;
const FAR: f32 = 100.0;

pub struct GameRenderer {
    pub ubo: u32,
    pub projection: Mat4,
    pub ortho: Mat4,
}

impl GameRenderer {
    pub fn new() -> Self {
        let mut renderer = GameRenderer::default();

        unsafe {
            let size = size_of::<Mat4>() as isize * 2;

            gl::GenBuffers(1, &mut renderer.ubo);
            renderer.bind_ubo();
            gl::BufferData(gl::UNIFORM_BUFFER, size, std::ptr::null(), gl::STATIC_DRAW);
            renderer.unbind_ubo();
            // define the range of the buffer that links to a uniform binding point
            gl::BindBufferRange(gl::UNIFORM_BUFFER, 0, renderer.ubo.into(), 0, size);
        }

        renderer
    }

    fn bind_ubo(&self) {
        unsafe { gl::BindBuffer(gl::UNIFORM_BUFFER, self.ubo); }
    }

    fn unbind_ubo(&self) {
        unsafe { gl::BindBuffer(gl::UNIFORM_BUFFER, 0); }
    }

    pub fn update_perspective(&mut self, width: i32, height: i32) {
        self.projection = cgmath::perspective(cgmath::Deg(75.0), width as f32 / height as f32, NEAR, FAR);
        self.bind_ubo();
        unsafe {
            gl::BufferSubData(gl::UNIFORM_BUFFER, 0, size_of::<Mat4>() as isize, self.projection.as_ptr() as *const std::ffi::c_void);
        }
        self.unbind_ubo();
    }

    pub fn setup_projection(&mut self, width: i32, height: i32) {
        unsafe { gl::Viewport(0, 0, width, height); }
        self.update_perspective(width, height);
        self.ortho = cgmath::ortho(0.0, width as f32, height as f32, 0.0, 0.0, 1.0);
    }

    pub fn update_view(&self, view: Mat4) {
        self.bind_ubo();
        unsafe {
            gl::BufferSubData(gl::UNIFORM_BUFFER, size_of::<Mat4>() as isize, size_of::<Mat4>() as isize, view.as_ptr() as *const std::ffi::c_void);
        }
        self.unbind_ubo();
    }
}

impl Default for GameRenderer {
    fn default() -> Self {
        GameRenderer {
            ubo: 0,
            projection: Mat4::identity(),
            ortho: Mat4::identity(),
        }
    }
}
