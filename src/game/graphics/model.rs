use std::path::Path;

use cgmath::*;
use tobj;

use super::{Mesh, MeshTexture, Vertex};
use super::Shader;

/**
 * Represents a model.
 */
#[derive(Default)]
pub struct Model {
    pub meshes: Vec<Mesh>,
    // Stores all the textures loaded so far, optimization to make sure textures aren't loaded more than once.
    pub textures_loaded: Vec<MeshTexture>,
    flip_texture: bool,
    directory: String,
}

impl Model {
    pub fn new(path: &Path, flip_texture: bool) -> Model {
        let mut model = Model::default();
        model.flip_texture = flip_texture;
        model.load_model(path);
        model
    }

    /// Draws the model.
    pub fn draw(&self, shader: &mut Shader) {
        for mesh in &self.meshes {
            mesh.draw(shader);
        }
    }

    /// Loads a model from file and stores the resulting meshes in the meshes vector.
    fn load_model(&mut self, path: &Path) {
        // retrieve the directory path of the filepath
        self.directory = path.parent().unwrap_or_else(|| Path::new("")).to_str().unwrap().into();
        let obj = tobj::load_obj(path, true).map_err(|err| format!("{:?}", err));

        let (models, materials) = obj.expect(format!("Model at {:?} failed to load", path).as_str());
        for model in models {
            let mesh = &model.mesh;
            let num_vertices = mesh.positions.len() / 3;

            // Data to fill
            let mut vertices: Vec<Vertex> = Vec::with_capacity(num_vertices);
            let indices: Vec<u32> = mesh.indices.clone();

            let (p, n, t) = (&mesh.positions, &mesh.normals, &mesh.texcoords);
            for i in 0..num_vertices {
                vertices.push(Vertex {
                    position: vec3(p[i * 3], p[i * 3 + 1], p[i * 3 + 2]),
                    normal: vec3(n[i * 3], n[i * 3 + 1], n[i * 3 + 2]),
                    texture_coords: vec2(t[i * 2], t[i * 2 + 1]),
                    ..Vertex::default()
                });
            }

            // Process material.
            let mut textures: Vec<MeshTexture> = Vec::new();
            if let Some(material_id) = mesh.material_id {
                let material = &materials[material_id];

                // 1. diffuse map
                if !material.diffuse_texture.is_empty() {
                    let texture = self.load_material_texture(&material.diffuse_texture, "texture_diffuse");
                    textures.push(texture);
                }
                // 2. specular map
                if !material.specular_texture.is_empty() {
                    let texture = self.load_material_texture(&material.specular_texture, "texture_specular");
                    textures.push(texture);
                }
                // 3. normal map
                if !material.normal_texture.is_empty() {
                    let texture = self.load_material_texture(&material.normal_texture, "texture_normal");
                    textures.push(texture);
                }
                // NOTE: no height maps
            }

            self.meshes.push(Mesh::new(vertices, indices, textures));
        }
    }

    fn load_material_texture(&mut self, path: &str, type_name: &str) -> MeshTexture {
        {
            let texture = self.textures_loaded.iter().find(|t| t.path == path);
            if let Some(texture) = texture {
                return texture.clone();
            }
        }

        let texture = MeshTexture {
            texture: super::Texture::load(Path::new(self.directory.as_str()).join(Path::new(path)).as_path(), self.flip_texture)
                .expect(format!("Missing texture {}", path).as_str()),
            texture_type: type_name.into(),
            path: path.into(),
        };
        self.textures_loaded.push(texture.clone());
        texture
    }
}
