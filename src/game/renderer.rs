extern crate gl;

use cgmath::Matrix4;
use cgmath::prelude::*;

pub use super::Client;
pub use super::graphics;

pub struct GameRenderer {
    pub projection: Matrix4<f32>
}

impl GameRenderer {
    pub fn new() -> Self {
        GameRenderer {
            projection: Matrix4::identity(),
        }
    }

    pub fn setup_projection(&mut self, width: i32, height: i32) {
        unsafe { gl::Viewport(0, 0, width, height); }
        self.projection = cgmath::perspective(cgmath::Deg(75.0), width as f32 / height as f32, 0.1, 100.0);
    }
}
