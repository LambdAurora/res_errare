extern crate gl;

use std::path::Path;

use glfw::WindowMode;

use game::graphics;

pub mod game;

fn main() {
    let mut glfw = glfw::init(glfw::FAIL_ON_ERRORS).unwrap();
    glfw.window_hint(glfw::WindowHint::ContextVersion(3, 3));
    glfw.window_hint(glfw::WindowHint::OpenGlProfile(glfw::OpenGlProfileHint::Core));
    #[cfg(target_os = "macos")]
        glfw.window_hint(glfw::WindowHint::OpenGlForwardCompat(true));

    let mut window = game::window::new(&glfw, 800, 600, "res_errare", WindowMode::Windowed)
        .expect("Failed to create game window.");
    window.make_current();

    gl::load_with(|symbol| glfw.get_proc_address_raw(symbol) as *const _);

    let mut shader = graphics::Shader::load(Path::new("shaders/shader.vsh"), Path::new("shaders/shader.fsh"))
        .expect("Could not create shader program.");

    let mut run = true;
    while run {
        unsafe {
            gl::ClearColor(0.0, 0.0, 0.0, 1.0);
            gl::Clear(gl::COLOR_BUFFER_BIT);
        }

        window.swap_buffers();
        glfw.poll_events();
        for (_, event) in glfw::flush_messages(&window.events) {
            match event {
                glfw::WindowEvent::Key(key, scancode, action, modifiers) => {
                    match key {
                        glfw::Key::Escape => { run = false; }
                        _ => {}
                    }
                }
                _ => {}
            }
        }

        if window.should_close() { run = false; }
    }

    shader.delete();
}
