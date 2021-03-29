#[macro_use]
extern crate memoffset;

extern crate gl;

use glfw::WindowMode;

pub mod game;

fn main() {
    let mut client = {
        let mut glfw = glfw::init(glfw::FAIL_ON_ERRORS).unwrap();
        glfw.window_hint(glfw::WindowHint::ContextVersion(3, 3));
        glfw.window_hint(glfw::WindowHint::OpenGlProfile(glfw::OpenGlProfileHint::Core));
        #[cfg(target_os = "macos")]
            glfw.window_hint(glfw::WindowHint::OpenGlForwardCompat(true));

        let mut window = game::window::new(&glfw, 800, 600, "res_errare", WindowMode::Windowed)
            .expect("Failed to create game window.");
        window.make_current();
        gl::load_with(|symbol| glfw.get_proc_address_raw(symbol) as *const _);

        game::Client::new(glfw, window)
    };

    client.run();
}
