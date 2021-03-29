extern crate glfw;

use std::path::Path;

use cgmath::*;
pub use glfw::{Context, Glfw, WindowMode};

use graphics::Mat4;
pub use renderer::GameRenderer;
pub use window::Window;

use self::glfw::Action;

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
        let mut shader = graphics::Shader::load(Path::new("assets/shaders/shader.vsh"), Path::new("assets/shaders/shader.fsh"))
            .expect("Could not create shader program.");

        let brazier_model = graphics::Model::new(Path::new("assets/models/brazier.obj"), true);

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
        }

        let mut wireframe = false;

        while self.running {
            let view: Mat4 = Mat4::from_translation(vec3(0., 0., -3.));
            self.renderer.update_view(view);

            unsafe {
                gl::ClearColor(0.0, 0.0, 0.0, 1.0);
                gl::Clear(gl::COLOR_BUFFER_BIT | gl::DEPTH_BUFFER_BIT);

                shader.use_program();
                let model: Mat4 = Mat4::from_axis_angle(vec3(0.5, 1.0, 0.6).normalize(),
                                                        Rad(self.glfw.get_time() as f32));
                shader.set_mat4f("model", &model);

                gl::Enable(gl::DEPTH_TEST);
            }
            for (i, position) in cube_positions.iter().enumerate() {
                // calculate the model matrix for each object and pass it to shader before drawing
                let mut model: Mat4 = Mat4::from_translation(*position);
                let angle = 20.0 * (i + 1) as f32;
                model = model * Mat4::from_axis_angle(vec3(1.0, 0.3, 0.5).normalize(), Deg(angle) * self.glfw.get_time() as f32);
                shader.set_mat4f("model", &model);

                brazier_model.draw(&mut shader);
            }

            self.window.swap_buffers();
            self.glfw.poll_events();
            for (_, event) in glfw::flush_messages(&self.window.events) {
                match event {
                    glfw::WindowEvent::Key(key, _, action, _) => {
                        match key {
                            glfw::Key::Enter => {
                                if action == Action::Release {
                                    wireframe = !wireframe;
                                    unsafe { gl::PolygonMode(gl::FRONT_AND_BACK, if wireframe { gl::LINE } else { gl::FILL }); }
                                }
                            }
                            glfw::Key::Escape => { self.running = false; }
                            _ => {}
                        }
                    }
                    glfw::WindowEvent::FramebufferSize(width, height) => {
                        self.renderer.setup_projection(width, height);
                        shader.use_program();
                    }
                    _ => {}
                }
            }

            if self.window.should_close() { self.running = false; }
        }

        shader.delete();
    }
}
