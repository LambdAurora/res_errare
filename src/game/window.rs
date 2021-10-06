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

extern crate glfw;

pub use std::sync::mpsc::Receiver;

pub use glfw::{Context, WindowEvent};

pub struct Window {
    pub handle: glfw::Window,
    pub events: Receiver<(f64, WindowEvent)>,
}

impl Window {
    /// Gets the size of the window.
    pub fn get_size(&self) -> (u32, u32) {
        let (width, height) = self.handle.get_size();
        (width as u32, height as u32)
    }

    /// Sets the size of the window.
    pub fn set_size(&mut self, width: u32, height: u32) {
        self.handle.set_size(width as i32, height as i32);
    }

    pub fn get_framebuffer_size(&self) -> (u32, u32) {
        let (width, height) = self.handle.get_framebuffer_size();
        (width as u32, height as u32)
    }

    pub fn get_content_scale(&self) -> (f32, f32) {
        self.handle.get_content_scale()
    }

    pub fn make_current(&mut self) {
        self.handle.make_current();
    }

    /**
     * Swaps the front and back buffers of the window.
     * If the swap interval if greater than zero,
     * the GPU driver waits the specified number of screen updates before swapping the buffers.
     */
    pub fn swap_buffers(&mut self) {
        self.handle.swap_buffers();
    }

    pub fn should_close(&self) -> bool {
        self.handle.should_close()
    }
}

pub fn new(glfw_ctx: &glfw::Glfw, width: u32, height: u32, title: &str, mode: glfw::WindowMode) -> Option<Window> {
    glfw_ctx.create_window(width, height, title, mode)
        .map(|win| Window { handle: win.0, events: win.1 })
        .map(|mut w| {
            w.handle.set_key_polling(true);
            w.handle.set_cursor_pos_polling(true);
            w.handle.set_framebuffer_size_polling(true);
            w
        })
}
