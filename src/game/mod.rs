extern crate glfw;

pub use glfw::{Glfw, Context, WindowMode};

pub mod window;
pub use window::Window;

struct Client {
    pub glfw_ctx: Glfw
}

impl Client {
    pub fn new() -> Self {
        let glfw = glfw::init(glfw::FAIL_ON_ERRORS).unwrap();
        Client {
            glfw_ctx: glfw
        }
    }
}

pub fn run() {

}