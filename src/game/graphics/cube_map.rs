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

use std::path::Path;

use stb_image::image::Image;

use crate::game::graphics::Texture;

use super::texture::load_image;

pub struct CubeMapTexture {
    pub id: u32
}

impl CubeMapTexture {
    pub fn new(right: Image<u8>, left: Image<u8>, top: Image<u8>, bottom: Image<u8>, back: Image<u8>, front: Image<u8>)
               -> Result<CubeMapTexture, String> {
        let mut id: u32 = 0;
        unsafe { gl::GenTextures(1, &mut id); };
        if id == 0 {
            return Err(String::from("Could not generate the OpenGL texture."));
        }

        let texture = CubeMapTexture { id };
        texture.bind();

        unsafe {
            upload_texture(gl::TEXTURE_CUBE_MAP_POSITIVE_X, &right);
            upload_texture(gl::TEXTURE_CUBE_MAP_NEGATIVE_X, &left);
            upload_texture(gl::TEXTURE_CUBE_MAP_POSITIVE_Y, &top);
            upload_texture(gl::TEXTURE_CUBE_MAP_NEGATIVE_Y, &bottom);
            upload_texture(gl::TEXTURE_CUBE_MAP_POSITIVE_Z, &back);
            upload_texture(gl::TEXTURE_CUBE_MAP_NEGATIVE_Z, &front);

            gl::TexParameteri(gl::TEXTURE_CUBE_MAP, gl::TEXTURE_MIN_FILTER, gl::LINEAR as i32);
            gl::TexParameteri(gl::TEXTURE_CUBE_MAP, gl::TEXTURE_MAG_FILTER, gl::LINEAR as i32);
            gl::TexParameteri(gl::TEXTURE_CUBE_MAP, gl::TEXTURE_WRAP_S, gl::CLAMP_TO_EDGE as i32);
            gl::TexParameteri(gl::TEXTURE_CUBE_MAP, gl::TEXTURE_WRAP_T, gl::CLAMP_TO_EDGE as i32);
            gl::TexParameteri(gl::TEXTURE_CUBE_MAP, gl::TEXTURE_WRAP_R, gl::CLAMP_TO_EDGE as i32);
        }
        unbind();
        Ok(texture)
    }

    /// Loads the cube map from the given paths.
    pub fn load(right: &Path, left: &Path, top: &Path, bottom: &Path, back: &Path, front: &Path, flip: bool)
                -> Result<CubeMapTexture, String> {
        unsafe { stb_image::stb_image::bindgen::stbi_set_flip_vertically_on_load(flip as i32); }
        CubeMapTexture::new(
            load_image(right)?,
            load_image(left)?,
            load_image(top)?,
            load_image(bottom)?,
            load_image(back)?,
            load_image(front)?,
        )
    }

    /// Loads the cube map from the given directory.
    pub fn load_from_directory(directory: &Path, ext: &String, flip: bool)
                               -> Result<CubeMapTexture, String> {
        CubeMapTexture::load(
            directory.join(Path::new((String::from("right.") + ext).as_str())).as_path(),
            directory.join(Path::new((String::from("left.") + ext).as_str())).as_path(),
            directory.join(Path::new((String::from("top.") + ext).as_str())).as_path(),
            directory.join(Path::new((String::from("bottom.") + ext).as_str())).as_path(),
            directory.join(Path::new((String::from("back.") + ext).as_str())).as_path(),
            directory.join(Path::new((String::from("front.") + ext).as_str())).as_path(),
            flip,
        )
    }

    pub fn bind(&self) {
        unsafe {
            gl::BindTexture(gl::TEXTURE_CUBE_MAP, self.id);
        }
    }
}

fn upload_texture(target: u32, image: &Image<u8>) {
    let format = Texture::get_format(image);
    unsafe {
        gl::TexImage2D(target, 0, format as i32,
                       image.width as i32, image.height as i32,
                       0, format, gl::UNSIGNED_BYTE, image.data.as_ptr() as *const std::ffi::c_void);
    }
}

pub fn unbind() {
    unsafe {
        gl::BindTexture(gl::TEXTURE_CUBE_MAP, 0);
    }
}
