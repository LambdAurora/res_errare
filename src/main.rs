extern crate gl;

use std::path::Path;

use gl::types::*;
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

    // TODO: move this away from main and get a proper API for that stuff
    // TODO: model loading
    let vao = unsafe {
        let vertices: [f32; 18] = [
            // positions         // colors
            0.5, -0.5, 0.0, 1.0, 0.0, 0.0,  // bottom right
            -0.5, -0.5, 0.0, 0.0, 1.0, 0.0,  // bottom left
            0.0, 0.5, 0.0, 0.0, 0.0, 1.0   // top
        ];
        let (mut vbo, mut vao) = (0, 0);
        gl::GenVertexArrays(1, &mut vao);
        gl::GenBuffers(1, &mut vbo);
        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        gl::BindVertexArray(vao);

        gl::BindBuffer(gl::ARRAY_BUFFER, vbo);
        gl::BufferData(gl::ARRAY_BUFFER,
                       (vertices.len() * std::mem::size_of::<GLfloat>()) as GLsizeiptr,
                       &vertices[0] as *const f32 as *const std::ffi::c_void,
                       gl::STATIC_DRAW);

        let stride = 6 * std::mem::size_of::<GLfloat>() as GLsizei;
        // position attribute
        gl::VertexAttribPointer(0, 3, gl::FLOAT, gl::FALSE, stride, std::ptr::null());
        gl::EnableVertexAttribArray(0);
        // color attribute
        gl::VertexAttribPointer(1, 3, gl::FLOAT, gl::FALSE, stride, (3 * std::mem::size_of::<GLfloat>()) as *const std::ffi::c_void);
        gl::EnableVertexAttribArray(1);

        vao
    };

    let mut run = true;
    while run {
        unsafe {
            gl::ClearColor(0.0, 0.0, 0.0, 1.0);
            gl::Clear(gl::COLOR_BUFFER_BIT);

            shader.use_program();
            gl::BindVertexArray(vao);
            gl::DrawArrays(gl::TRIANGLES, 0, 3);
        }

        window.swap_buffers();
        glfw.poll_events();
        for (_, event) in glfw::flush_messages(&window.events) {
            match event {
                glfw::WindowEvent::Key(key, _, _, _) => {
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
