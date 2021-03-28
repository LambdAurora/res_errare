extern crate gl;

pub mod game;
use game::graphics;

use std::path::Path;
use glfw::WindowMode;

fn main() {
    let mut glfw = glfw::init(glfw::FAIL_ON_ERRORS).unwrap();

    let mut window = game::window::new(&glfw, 800, 600, "res_errare", WindowMode::Windowed)
        .expect("Failed to create game window.");
    window.handle.set_key_polling(true);
    window.make_current();

    gl::load_with(|symbol| glfw.get_proc_address_raw(symbol) as *const _);

    let mut shader = graphics::Shader::load(Path::new("shaders/shader.vsh"), Path::new("shaders/shader.fsh"))
        .expect("Could not create shader program.");

    while !window.should_close() {
        unsafe {
            gl::ClearColor(0.0, 0.0, 0.0, 1.0);
            gl::Clear(gl::COLOR_BUFFER_BIT);
        }

        window.swap_buffers();
        glfw.poll_events();
        for (_, event) in glfw::flush_messages(&window.events) {
            match event {
                _ => {}
            }
        }
    }

    shader.delete();
}
