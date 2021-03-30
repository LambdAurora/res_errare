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

use std::ffi::c_void;
use std::path::Path;

use stb_image::image::{Image, LoadResult};

/**
 * Represents a texture.
 */
#[derive(Clone)]
pub struct Texture {
    id: u32,
    width: usize,
    height: usize,
}

impl Texture {
    pub fn new(image: Image<u8>) -> Result<Texture, String> {
        let mut id: u32 = 0;
        unsafe { gl::GenTextures(1, &mut id); }
        if id == 0 {
            return Err(String::from("Could not generate the OpenGL texture."));
        }

        let mut texture = Texture { id, width: image.width, height: image.height };
        texture.upload(image);
        Ok(texture)
    }

    /// Loads the texture from the given path.
    pub fn load(path: &Path, flip: bool) -> Result<Texture, String> {
        unsafe { stb_image::stb_image::bindgen::stbi_set_flip_vertically_on_load(flip as i32); }
        Texture::new(load_image(path)?)
    }

    pub fn get_format(image: &Image<u8>) -> u32 {
        match image.depth {
            1 => gl::RED,
            2 => gl::RG,
            3 => gl::RGB,
            4 => gl::RGBA,
            _ => 0
        }
    }

    /// Binds the texture.
    pub fn bind(&self) {
        unsafe { gl::BindTexture(gl::TEXTURE_2D, self.id); }
    }

    /// Uploads the given image to the texture.
    pub fn upload(&mut self, image: Image<u8>) {
        self.bind();

        self.width = image.width;
        self.height = image.height;

        let format = Texture::get_format(&image);
        unsafe {
            gl::TexImage2D(gl::TEXTURE_2D, 0, format as i32,
                           self.width as i32, self.height as i32,
                           0, format, gl::UNSIGNED_BYTE, image.data.as_ptr() as *const c_void);
            gl::GenerateMipmap(gl::TEXTURE_2D);

            gl::TexParameteri(gl::TEXTURE_2D, gl::TEXTURE_WRAP_S, gl::REPEAT as i32);
            gl::TexParameteri(gl::TEXTURE_2D, gl::TEXTURE_WRAP_T, gl::REPEAT as i32);
            gl::TexParameteri(gl::TEXTURE_2D, gl::TEXTURE_MIN_FILTER, gl::NEAREST_MIPMAP_NEAREST as i32);
            gl::TexParameteri(gl::TEXTURE_2D, gl::TEXTURE_MAG_FILTER, gl::NEAREST as i32);
        }
    }
}

/// Unbinds texture.
pub fn unbind() {
    unsafe { gl::BindTexture(gl::TEXTURE_2D, 0); }
}

pub fn load_image(path: &Path) -> Result<Image<u8>, String> {
    match stb_image::image::load(path) {
        LoadResult::ImageU8(img) => Ok(img),
        LoadResult::ImageF32(_) => Err(String::from("Does not support ImageF32")),
        LoadResult::Error(err) => Err(err)
    }
}
