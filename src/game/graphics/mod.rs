pub use camera::Camera;
pub use mesh::{Mesh, MeshTexture, Vertex};
pub use model::Model;
pub use shader::Shader;
pub use texture::Texture;

pub mod camera;
pub mod mesh;
pub mod model;
pub mod shader;
pub mod texture;

pub type Mat4 = cgmath::Matrix4<f32>;
pub type Point3 = cgmath::Point3<f32>;
pub type Vec3 = cgmath::Vector3<f32>;
