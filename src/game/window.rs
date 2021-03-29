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
            w.handle.set_framebuffer_size_polling(true);
            w
        })
}
