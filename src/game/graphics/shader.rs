extern crate gl;

use std::ffi::CString;
use std::fs::File;
use std::io::Read;
use std::iter::repeat;
use std::result::*;

use gl::types::*;

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
        new_shader_program(vertex_shader, fragment_shader)
            .map(|shader| Shader { program: shader })
    }

    pub fn load(vertex_shader: &std::path::Path, fragment_shader: &std::path::Path) -> Result<Shader, String> {
        let mut vertex_shader_src = String::new();
        let mut fragment_shader_src = String::new();

        File::open(vertex_shader).map(|mut v| v.read_to_string(&mut vertex_shader_src))
            .map_err(|err| err.to_string())?;
        File::open(fragment_shader).map(|mut f| f.read_to_string(&mut fragment_shader_src))
            .map_err(|err| err.to_string())?;

        Shader::new(&vertex_shader_src[..], &fragment_shader_src[..])
    }

    pub fn use_program(&mut self)
    {
        unsafe {
            gl::UseProgram(self.program);
            let out_color = CString::new("out_color".as_bytes());
            gl::BindFragDataLocation(self.program, 0, out_color.unwrap().as_ptr());
        }
        //self.bind_pos_attr();
    }

    pub fn delete(&mut self) {
        unsafe {
            gl::DeleteProgram(self.program);
        }
    }
}

/// Loads a shader program using the given vertex and fragment shader sources
pub fn new_shader_program(vertex_shader_src: &str, fragment_shader_src: &str) -> Result<GLuint, String>
{
    let vertex_shader = create_vertex_shader(vertex_shader_src)?;
    let fragment_shader = create_fragment_shader(fragment_shader_src)?;

    let program = create_program(vertex_shader, fragment_shader);

    unsafe {
        gl::DeleteShader(vertex_shader);
        gl::DeleteShader(fragment_shader);
    }

    program
}

pub fn create_vertex_shader(vertex_shader_src: &str) -> Result<GLuint, String>
{
    let vertex_shader: GLuint;
    unsafe { vertex_shader = gl::CreateShader(gl::VERTEX_SHADER); }
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
    let fragment_shader: GLuint;
    unsafe { fragment_shader = gl::CreateShader(gl::FRAGMENT_SHADER); }
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

pub fn create_program(vertex_shader: GLuint, fragment_shader: GLuint) -> Result<GLuint, String>
{
    let program: GLuint;
    unsafe { program = gl::CreateProgram(); }
    if program == 0 {
        return Err(String::from("Could not create shader program."));
    }

    unsafe {
        gl::AttachShader(program, vertex_shader);
        gl::AttachShader(program, fragment_shader);
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
