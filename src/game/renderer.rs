extern crate gl;

use cgmath::Matrix4;
use cgmath::prelude::*;

pub use super::Client;
pub use super::graphics;

const NEAR: f32 = 0.1;
const FAR: f32 = 100.0;

pub struct GameRenderer {
    pub projection: Matrix4<f32>,
    pub ortho: Matrix4<f32>
}

impl GameRenderer {
    pub fn new() -> Self {
        GameRenderer::default()
    }

    pub fn update_perspective(&mut self, width: i32, height: i32) {
        self.projection = cgmath::perspective(cgmath::Deg(75.0), width as f32 / height as f32, NEAR, FAR);
    }

    pub fn setup_projection(&mut self, width: i32, height: i32) {
        unsafe { gl::Viewport(0, 0, width, height); }
        self.update_perspective(width, height);
        self.ortho = cgmath::ortho(0.0, width as f32, height as f32, 0.0, 0.0, 1.0);
    }
}

impl Default for GameRenderer {
    fn default() -> Self {
        GameRenderer {
            projection: Matrix4::identity(),
            ortho: Matrix4::identity()
        }
    }
}
