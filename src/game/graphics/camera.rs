use cgmath::prelude::*;

use crate::game::graphics::Mat4;

use super::{Point3, Vec3};

/**
 * Represents a camera.
 */
pub struct Camera {
    pub position: Point3,
    front: Vec3,
    up: Vec3,
    right: Vec3,
    world_up: Vec3,
    // Angles.
    yaw: f32,
    pitch: f32,
}

impl Camera {
    /// Returns the view matrix of this camera.
    pub fn get_view_matrix(&self) -> Mat4 {
        Mat4::look_to_rh(self.position, self.front, self.up)
    }

    pub fn get_front(&self) -> Vec3 {
        self.front
    }

    pub fn get_right(&self) -> Vec3 {
        self.right
    }

    /// Returns camera's yaw.
    pub fn get_yaw(&self) -> f32 {
        self.yaw
    }

    /// Returns camera's pitch.
    pub fn get_pitch(&self) -> f32 {
        self.pitch
    }

    pub fn set_yaw(&mut self, yaw: f32) {
        self.set_angle(yaw, self.pitch);
    }

    pub fn set_pitch(&mut self, pitch: f32) {
        self.set_angle(self.yaw, pitch);
    }

    pub fn set_angle(&mut self, yaw: f32, pitch: f32) {
        self.yaw = yaw;
        self.pitch = pitch;

        if self.pitch > 89.0 {
            self.pitch = 89.0;
        }
        if self.pitch < -89.0 {
            self.pitch = -89.0;
        }
        self.update_camera_vectors();
    }

    /// Calculates the front vector from the Camera's (updated) Eular Angles.
    fn update_camera_vectors(&mut self) {
        // Calculate the new front vector.
        let front = Vec3 {
            x: self.yaw.to_radians().cos() * self.pitch.to_radians().cos(),
            y: self.pitch.to_radians().sin(),
            z: self.yaw.to_radians().sin() * self.pitch.to_radians().cos(),
        };
        self.front = front.normalize();
        // Also re-calculate the right and up vector.
        self.right = self.front.cross(self.world_up).normalize(); // Normalize the vectors,
        // because their length gets closer to 0 the more you look up or down which results in slower movement.
        self.up = self.right.cross(self.front).normalize();
    }
}

impl Default for Camera {
    fn default() -> Camera {
        let mut camera = Camera {
            position: Point3::new(0.0, 0.0, 0.0),
            front: cgmath::vec3(0.0, 0.0, -1.0),
            up: Vec3::zero(), // initialized later
            right: Vec3::zero(), // initialized later
            world_up: Vec3::unit_y(),
            yaw: 0.0,
            pitch: 0.0,
        };
        camera.update_camera_vectors();
        camera
    }
}
