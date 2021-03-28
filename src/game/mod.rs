extern crate glfw;

use std::ffi::c_void;
use std::path::Path;

use cgmath::*;
use gl::types::*;
pub use glfw::{Context, Glfw, WindowMode};

pub use renderer::GameRenderer;
pub use window::Window;

pub mod graphics;
pub mod renderer;
pub mod window;

pub struct Client {
    pub glfw: Glfw,
    pub window: Window,
    pub renderer: GameRenderer,
    pub running: bool,
}

impl Client {
    pub fn new(glfw: Glfw, window: Window) -> Self {
        Client {
            glfw,
            window,
            renderer: GameRenderer::new(),
            running: true,
        }
    }

    pub fn run(&mut self) {
        let mut shader = graphics::Shader::load(Path::new("shaders/shader.vsh"), Path::new("shaders/shader.fsh"))
            .expect("Could not create shader program.");

        // TODO: get a proper API for that stuff
        // TODO: model loading
        let vao = unsafe {
            let vertices: [f32; 180] = [
                -0.5, -0.5, -0.5, 0.0, 0.0,
                0.5, -0.5, -0.5, 1.0, 0.0,
                0.5, 0.5, -0.5, 1.0, 1.0,
                0.5, 0.5, -0.5, 1.0, 1.0,
                -0.5, 0.5, -0.5, 0.0, 1.0,
                -0.5, -0.5, -0.5, 0.0, 0.0,
                -0.5, -0.5, 0.5, 0.0, 0.0,
                0.5, -0.5, 0.5, 1.0, 0.0,
                0.5, 0.5, 0.5, 1.0, 1.0,
                0.5, 0.5, 0.5, 1.0, 1.0,
                -0.5, 0.5, 0.5, 0.0, 1.0,
                -0.5, -0.5, 0.5, 0.0, 0.0,
                -0.5, 0.5, 0.5, 1.0, 0.0,
                -0.5, 0.5, -0.5, 1.0, 1.0,
                -0.5, -0.5, -0.5, 0.0, 1.0,
                -0.5, -0.5, -0.5, 0.0, 1.0,
                -0.5, -0.5, 0.5, 0.0, 0.0,
                -0.5, 0.5, 0.5, 1.0, 0.0,
                0.5, 0.5, 0.5, 1.0, 0.0,
                0.5, 0.5, -0.5, 1.0, 1.0,
                0.5, -0.5, -0.5, 0.0, 1.0,
                0.5, -0.5, -0.5, 0.0, 1.0,
                0.5, -0.5, 0.5, 0.0, 0.0,
                0.5, 0.5, 0.5, 1.0, 0.0,
                -0.5, -0.5, -0.5, 0.0, 1.0,
                0.5, -0.5, -0.5, 1.0, 1.0,
                0.5, -0.5, 0.5, 1.0, 0.0,
                0.5, -0.5, 0.5, 1.0, 0.0,
                -0.5, -0.5, 0.5, 0.0, 0.0,
                -0.5, -0.5, -0.5, 0.0, 1.0,
                -0.5, 0.5, -0.5, 0.0, 1.0,
                0.5, 0.5, -0.5, 1.0, 1.0,
                0.5, 0.5, 0.5, 1.0, 0.0,
                0.5, 0.5, 0.5, 1.0, 0.0,
                -0.5, 0.5, 0.5, 0.0, 0.0,
                -0.5, 0.5, -0.5, 0.0, 1.0
            ];
            let (mut vbo, mut vao) = (0, 0);
            gl::GenVertexArrays(1, &mut vao);
            gl::GenBuffers(1, &mut vbo);
            // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
            gl::BindVertexArray(vao);

            gl::BindBuffer(gl::ARRAY_BUFFER, vbo);
            gl::BufferData(gl::ARRAY_BUFFER,
                           (vertices.len() * std::mem::size_of::<GLfloat>()) as GLsizeiptr,
                           &vertices[0] as *const f32 as *const c_void,
                           gl::STATIC_DRAW);

            let stride = 5 * std::mem::size_of::<GLfloat>() as GLsizei;
            // position attribute
            gl::VertexAttribPointer(0, 3, gl::FLOAT, gl::FALSE, stride, std::ptr::null());
            gl::EnableVertexAttribArray(0);
            // texture coord attribute
            gl::VertexAttribPointer(1, 2, gl::FLOAT, gl::FALSE, stride, (3 * std::mem::size_of::<GLfloat>()) as *const c_void);
            gl::EnableVertexAttribArray(1);

            vao
        };

        // world space positions of our cubes
        let cube_positions: [Vector3<f32>; 10] = [vec3(0.0, 0.0, 0.0),
            vec3(2.0, 5.0, -15.0),
            vec3(-1.5, -2.2, -2.5),
            vec3(-3.8, -2.0, -12.3),
            vec3(2.4, -0.4, -3.5),
            vec3(-1.7, 3.0, -7.5),
            vec3(1.3, -2.0, -2.5),
            vec3(1.5, 2.0, -2.5),
            vec3(1.5, 0.2, -1.5),
            vec3(-1.3, 1.0, -1.5)];

        {
            let (width, height) = self.window.handle.get_framebuffer_size();
            self.renderer.setup_projection(width, height);
            shader.use_program();
            shader.set_mat4f("projection", &self.renderer.projection);
        }

        //unsafe { gl::PolygonMode(gl::FRONT_AND_BACK, gl::LINE); }

        while self.running {
            unsafe {
                gl::ClearColor(0.0, 0.0, 0.0, 1.0);
                gl::Clear(gl::COLOR_BUFFER_BIT | gl::DEPTH_BUFFER_BIT);

                shader.use_program();
                let model: Matrix4<f32> = Matrix4::from_axis_angle(vec3(0.5, 1.0, 0.0).normalize(),
                                                                   Rad(self.glfw.get_time() as f32));
                let view: Matrix4<f32> = Matrix4::from_translation(vec3(0., 0., -3.));
                shader.set_mat4f("model", &model);
                shader.set_mat4f("view", &view);

                gl::Enable(gl::DEPTH_TEST);
                gl::BindVertexArray(vao);
                for (i, position) in cube_positions.iter().enumerate() {
                    // calculate the model matrix for each object and pass it to shader before drawing
                    let mut model: Matrix4<f32> = Matrix4::from_translation(*position);
                    let angle = 20.0 * (i + 1) as f32;
                    model = model * Matrix4::from_axis_angle(vec3(1.0, 0.3, 0.5).normalize(), Deg(angle) * self.glfw.get_time() as f32);
                    shader.set_mat4f("model", &model);

                    gl::DrawArrays(gl::TRIANGLES, 0, 36);
                }
            }

            self.window.swap_buffers();
            self.glfw.poll_events();
            for (_, event) in glfw::flush_messages(&self.window.events) {
                match event {
                    glfw::WindowEvent::Key(key, _, _, _) => {
                        match key {
                            glfw::Key::Escape => { self.running = false; }
                            _ => {}
                        }
                    }
                    glfw::WindowEvent::FramebufferSize(width, height) => {
                        self.renderer.setup_projection(width, height);
                        shader.use_program();
                        shader.set_mat4f("projection", &self.renderer.projection);
                    }
                    _ => {}
                }
            }

            if self.window.should_close() { self.running = false; }
        }

        shader.delete();
    }
}
