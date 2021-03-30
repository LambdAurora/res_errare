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

extern crate cgmath;
extern crate gl;

use std::ffi::CString;
use std::fs::File;
use std::io::Read;
use std::iter::repeat;
use std::result::*;

use cgmath::Matrix;
use gl::types::*;

use super::Mat4;

/**
 * Represents a shader program.
 */
pub struct Shader {
    pub program: GLuint
}

impl Shader {
    /**
     * Creates a new shader program given the vertex shader source and the fragment shader source.
     */
    pub fn new(vertex_shader: &str, fragment_shader: &str) -> Result<Shader, String> {
        new_shader_program(vertex_shader, fragment_shader, None)
            .map(|shader| Shader { program: shader })
            .and_then(Shader::apply_block_indexes)
    }

    /**
    * Creates a new shader program given the vertex shader source, the fragment shader source and the geometry shader source.
    */
    pub fn new_with_geometry(vertex_shader: &str, fragment_shader: &str, geometry_shader: &str) -> Result<Shader, String> {
        new_shader_program(vertex_shader, fragment_shader, Some(geometry_shader))
            .map(|shader| Shader { program: shader })
            .and_then(Shader::apply_block_indexes)
    }

    pub fn load(vertex_shader: &std::path::Path, fragment_shader: &std::path::Path) -> Result<Shader, String> {
        let mut vertex_shader_src = String::new();
        let mut fragment_shader_src = String::new();

        File::open(vertex_shader).and_then(|mut v| v.read_to_string(&mut vertex_shader_src))
            .map_err(|err| err.to_string())?;
        File::open(fragment_shader).and_then(|mut f| f.read_to_string(&mut fragment_shader_src))
            .map_err(|err| err.to_string())?;

        Shader::new(&vertex_shader_src[..], &fragment_shader_src[..])
    }

    pub fn load_with_geometry(vertex_shader: &std::path::Path, fragment_shader: &std::path::Path, geometry_shader: &std::path::Path) -> Result<Shader, String> {
        let mut vertex_shader_src = String::new();
        let mut fragment_shader_src = String::new();
        let mut geometry_shader_src = String::new();

        File::open(vertex_shader).and_then(|mut v| v.read_to_string(&mut vertex_shader_src))
            .map_err(|err| err.to_string())?;
        File::open(fragment_shader).and_then(|mut f| f.read_to_string(&mut fragment_shader_src))
            .map_err(|err| err.to_string())?;
        File::open(geometry_shader).and_then(|mut f| f.read_to_string(&mut geometry_shader_src))
            .map_err(|err| err.to_string())?;


        Shader::new_with_geometry(&vertex_shader_src[..], &fragment_shader_src[..], &geometry_shader_src[..])
    }

    pub fn use_program(&mut self)
    {
        unsafe {
            gl::UseProgram(self.program);
        }
    }

    pub fn get_uniform_location(&mut self, uniform: &str) -> i32
    {
        let name = CString::new(uniform.as_bytes()).expect("CString::new failed.");
        let ptr = name.as_ptr();
        let uniform_location = unsafe { gl::GetUniformLocation(self.program, ptr) };

        match uniform_location
        {
            -1 => {
                // uniform not found
                println!("Uniform {} not found!!", uniform);
                uniform_location
            }
            _ => { uniform_location }
        }
    }

    pub fn get_uniform_block_index(&mut self, uniform: &str) -> u32
    {
        let name = CString::new(uniform.as_bytes()).expect("CString::new failed.");
        let ptr = name.as_ptr();
        unsafe { gl::GetUniformBlockIndex(self.program, ptr) }
    }

    pub fn set_bool(&mut self, name: &str, value: bool) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::Uniform1i(uniform, value as i32); }
        }
    }

    pub fn set_i32(&mut self, name: &str, value: i32) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::Uniform1i(uniform, value); }
        }
    }

    pub fn set_float(&mut self, name: &str, value: f32) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::Uniform1f(uniform, value); }
        }
    }

    pub fn set_vec2f(&mut self, name: &str, value: &cgmath::Vector2<f32>) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::Uniform2f(uniform, value.x, value.y); }
        }
    }

    pub fn set_vec3f(&mut self, name: &str, value: &cgmath::Vector3<f32>) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::Uniform3f(uniform, value.x, value.y, value.z); }
        }
    }

    pub fn set_vec4f(&mut self, name: &str, value: &cgmath::Vector4<f32>) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::Uniform4f(uniform, value.x, value.y, value.z, value.w); }
        }
    }

    pub fn set_mat4f(&mut self, name: &str, value: &Mat4) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::UniformMatrix4fv(uniform, 1, gl::FALSE, value.as_ptr()); }
        }
    }

    pub fn set_mat4d(&mut self, name: &str, value: &cgmath::Matrix4<f64>) {
        let uniform = self.get_uniform_location(name);
        if uniform != -1 {
            unsafe { gl::UniformMatrix4dv(uniform, 1, gl::FALSE, value.as_ptr()); }
        }
    }

    pub fn delete(&mut self) {
        unsafe {
            gl::DeleteProgram(self.program);
        }
    }

    fn apply_block_indexes(shader: Shader) -> Result<Shader, String> {
        let mut shader = shader;
        let uniform_block_index = shader.get_uniform_block_index("matrices");
        unsafe {
            gl::UniformBlockBinding(shader.program.into(), uniform_block_index, 0);
        }

        Ok(shader)
    }
}

/// Loads a shader program using the given vertex and fragment shader sources
pub fn new_shader_program(vertex_shader_src: &str, fragment_shader_src: &str, geometry_shader_src: Option<&str>) -> Result<GLuint, String>
{
    let vertex_shader = create_vertex_shader(vertex_shader_src)?;
    let fragment_shader = create_fragment_shader(fragment_shader_src)?;
    let geometry_shader: Option<GLuint> = match geometry_shader_src
        .map(|src| create_geometry_shader(src)) {
        None => None,
        Some(res) => Some(res?)
    };

    let program = create_program(vertex_shader, fragment_shader, geometry_shader);

    unsafe {
        gl::DeleteShader(vertex_shader);
        gl::DeleteShader(fragment_shader);
    }

    program
}

pub fn create_vertex_shader(vertex_shader_src: &str) -> Result<GLuint, String>
{
    let vertex_shader = unsafe { gl::CreateShader(gl::VERTEX_SHADER) };
    if vertex_shader == 0 {
        return Err(String::from("Could not create vertex shader."));
    }

    let vertex_shader_str = CString::new(vertex_shader_src.as_bytes()).unwrap();

    unsafe {
        gl::ShaderSource(vertex_shader, 1, &vertex_shader_str.as_ptr(), std::ptr::null());
        gl::CompileShader(vertex_shader);
    }
    check_shader_error(vertex_shader)
}

pub fn create_fragment_shader(fragment_shader_src: &str) -> Result<GLuint, String>
{
    let fragment_shader = unsafe { gl::CreateShader(gl::FRAGMENT_SHADER) };
    if fragment_shader == 0 {
        return Err(String::from("Could not create fragment shader."));
    }

    let fragment_shader_str = CString::new(fragment_shader_src.as_bytes()).unwrap();

    unsafe {
        gl::ShaderSource(fragment_shader, 1, &fragment_shader_str.as_ptr(), std::ptr::null());
        gl::CompileShader(fragment_shader);
    }

    check_shader_error(fragment_shader)
}

pub fn create_geometry_shader(geometry_shader_src: &str) -> Result<GLuint, String>
{
    let geometry_shader = unsafe { gl::CreateShader(gl::GEOMETRY_SHADER) };
    if geometry_shader == 0 {
        return Err(String::from("Could not create geometry shader."));
    }

    let geometry_shader_str = CString::new(geometry_shader_src.as_bytes()).unwrap();

    unsafe {
        gl::ShaderSource(geometry_shader, 1, &geometry_shader_str.as_ptr(), std::ptr::null());
        gl::CompileShader(geometry_shader);
    }

    check_shader_error(geometry_shader)
}

pub fn create_program(vertex_shader: GLuint, fragment_shader: GLuint, geometry_shader: Option<GLuint>) -> Result<GLuint, String>
{
    let program = unsafe { gl::CreateProgram() };
    if program == 0 {
        return Err(String::from("Could not create shader program."));
    }

    unsafe {
        gl::AttachShader(program, vertex_shader);
        gl::AttachShader(program, fragment_shader);
        match geometry_shader {
            None => {}
            Some(shader) => gl::AttachShader(program, shader)
        }
        gl::LinkProgram(program);
    }

    Ok(program)
}

/// Checks if the provided shader handle is valid
fn check_shader_error(shader: GLuint) -> Result<GLuint, String> {
    let mut compiles: i32 = 0;

    unsafe {
        gl::GetShaderiv(shader, gl::COMPILE_STATUS, &mut compiles);

        if compiles == 0
        {
            let mut info_log_len = 0;

            gl::GetShaderiv(shader, gl::INFO_LOG_LENGTH, &mut info_log_len);

            if info_log_len > 0
            {
                // Error check for fail to allocate memory omitted.
                let mut chars_written = 0;
                let info_log: String = repeat(' ').take(info_log_len as usize).collect();

                let c_str = CString::new(info_log.as_bytes()).unwrap();
                gl::GetShaderInfoLog(shader, info_log_len, &mut chars_written, c_str.as_ptr() as *mut _);

                let bytes = c_str.as_bytes();
                let bytes = &bytes[..bytes.len() - 1];
                return Err(format!("Shader compilation failed: {}", std::str::from_utf8(bytes).unwrap()));
            }
        }
    }

    Ok(shader)
}
