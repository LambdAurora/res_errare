/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

extern crate glfw;

use std::path::Path;

use cgmath::*;
pub use glfw::{Context, Glfw, WindowMode};

use graphics::{Camera, Mat4};
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
    pub cursor_locked: bool,
}

impl Client {
    pub fn new(glfw: Glfw, window: Window) -> Self {
        Client {
            glfw,
            window,
            renderer: GameRenderer::new(),
            running: true,
            cursor_locked: false,
        }
    }

    pub fn lock_cursor(&mut self) {
        self.cursor_locked = true;
        self.window.handle.set_cursor_mode(glfw::CursorMode::Disabled);
        let (width, height) = self.window.get_size();
        self.window.handle.set_cursor_pos(width as f64 / 2.0, height as f64 / 2.0);
    }

    pub fn unlock_cursor(&mut self) {
        self.cursor_locked = false;
        self.window.handle.set_cursor_mode(glfw::CursorMode::Normal);
    }

    pub fn process_movement(&self, camera: &mut Camera, direction: i32, delta_time: f32) {
        let velocity = 2.5*delta_time;
        let front = camera.get_front();
        let right = camera.get_right();
        if direction == 1 {
            camera.position += front * velocity;
        }
        if direction == 2 {
            camera.position += -(front * velocity);
        }
        if direction == 3 {
            camera.position += -(right * velocity);
        }
        if direction == 4 {
            camera.position += right * velocity;
        }
    }

    pub fn run(&mut self) {
        self.lock_cursor();

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

        let mut camera = Camera::default();
        camera.position = cgmath::point3(0., 0., 3.);
        camera.set_yaw(-90.0);

        let mut wireframe = false;

        let mut delta_time: f32; // time between current frame and last frame
        let mut last_frame: f32 = 0.0;

        while self.running {
            let current_frame = self.glfw.get_time() as f32;
            delta_time = current_frame - last_frame;
            last_frame = current_frame;

            self.renderer.update_view(camera.get_view_matrix());

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
            let mut cursor_locked = self.cursor_locked;
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
                            glfw::Key::T => { if action == Action::Release { cursor_locked = !cursor_locked; } }
                            _ => {}
                        }
                    }
                    glfw::WindowEvent::CursorPos(x_pos, y_pos) => {
                        if self.cursor_locked {
                            let (x_pos, y_pos) = (x_pos as f32, y_pos as f32);

                            let (width, height) = self.window.get_size();

                            let x_offset = x_pos - (width as f32 / 2.0);
                            let y_offset = (height as f32 / 2.0) - y_pos; // reversed since y-coordinates go from bottom to top

                            camera.set_angle(camera.get_yaw() + x_offset * 0.05, camera.get_pitch() + y_offset * 0.05);

                            self.window.handle.set_cursor_pos(width as f64 / 2.0, height as f64 / 2.0);
                        }
                    }
                    glfw::WindowEvent::FramebufferSize(width, height) => {
                        self.renderer.setup_projection(width, height);
                        shader.use_program();
                    }
                    _ => {}
                }
            }

            if self.window.handle.get_key(glfw::Key::W) == Action::Press {
                self.process_movement(&mut camera, 1, delta_time);
            }
            if self.window.handle.get_key(glfw::Key::S) == Action::Press {
                self.process_movement(&mut camera, 2, delta_time);
            }
            if self.window.handle.get_key(glfw::Key::A) == Action::Press {
                self.process_movement(&mut camera, 3, delta_time);
            }
            if self.window.handle.get_key(glfw::Key::D) == Action::Press {
                self.process_movement(&mut camera, 4, delta_time);
            }

            if cursor_locked != self.cursor_locked {
                if cursor_locked { self.lock_cursor(); } else { self.unlock_cursor(); }
            }

            if self.window.should_close() { self.running = false; }
        }

        shader.delete();
    }
}
