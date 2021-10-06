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

pub use camera::Camera;
pub use mesh::{Mesh, MeshTexture, Vertex};
pub use model::Model;
pub use shader::Shader;
pub use texture::Texture;

pub mod camera;
pub mod cube_map;
pub mod mesh;
pub mod model;
pub mod shader;
pub mod skybox;
pub mod text;
pub mod texture;

pub type Mat4 = cgmath::Matrix4<f32>;
pub type Point3 = cgmath::Point3<f32>;
pub type Vec3 = cgmath::Vector3<f32>;
