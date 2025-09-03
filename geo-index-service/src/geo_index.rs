use dashmap::DashMap;
use geo::{HaversineDistance, Point};
use geohash::{encode, neighbors, Direction};
use serde::{Deserialize, Serialize};
use std::collections::HashSet;
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};
use uuid::Uuid;

/// Driver information stored in the geo-index
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Driver {
    pub id: String,
    pub lat: f64,
    pub lng: f64,
    pub status: DriverStatus,
    pub last_update: u64,
}

/// Driver status enum
#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq)]
pub enum DriverStatus {
    Offline = 0,
    Available = 1,
    Busy = 2,
    EnRoute = 3,
}

impl From<i32> for DriverStatus {
    fn from(value: i32) -> Self {
        match value {
            0 => DriverStatus::Offline,
            1 => DriverStatus::Available,
            2 => DriverStatus::Busy,
            3 => DriverStatus::EnRoute,
            _ => DriverStatus::Offline,
        }
    }
}

/// Driver with calculated distance
#[derive(Debug, Clone)]
pub struct DriverWithDistance {
    pub driver: Driver,
    pub distance_km: f64,
}

/// High-performance geo-spatial index using geohash-based grid buckets
#[derive(Debug, Clone)]
pub struct GeoIndex {
    /// Grid buckets: geohash -> set of driver IDs
    grid: Arc<DashMap<String, HashSet<String>>>,
    /// Driver data: driver_id -> driver info
    drivers: Arc<DashMap<String, Driver>>,
    /// Geohash precision (higher = more precise, smaller buckets)
    precision: usize,
}

impl GeoIndex {
    /// Create a new geo-index with default precision
    pub fn new() -> Self {
        Self::with_precision(7) // ~150m precision
    }

    /// Create a new geo-index with specified precision
    pub fn with_precision(precision: usize) -> Self {
        Self {
            grid: Arc::new(DashMap::new()),
            drivers: Arc::new(DashMap::new()),
            precision,
        }
    }

    /// Update driver location in the index
    pub fn update_driver_location(
        &self,
        driver_id: String,
        lat: f64,
        lng: f64,
        status: DriverStatus,
    ) -> Result<(), String> {
        // Validate coordinates
        if !(-90.0..=90.0).contains(&lat) {
            return Err("Invalid latitude".to_string());
        }
        if !(-180.0..=180.0).contains(&lng) {
            return Err("Invalid longitude".to_string());
        }

        let current_timestamp = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();

        // Remove driver from old geohash bucket if exists
        if let Some(old_driver) = self.drivers.get(&driver_id) {
            let old_geohash = encode(old_driver.lat, old_driver.lng, self.precision).unwrap();
            if let Some(mut bucket) = self.grid.get_mut(&old_geohash) {
                bucket.remove(&driver_id);
                if bucket.is_empty() {
                    drop(bucket);
                    self.grid.remove(&old_geohash);
                }
            }
        }

        // Calculate new geohash
        let geohash = encode(lat, lng, self.precision)
            .map_err(|e| format!("Failed to encode geohash: {}", e))?;

        // Create or update driver
        let driver = Driver {
            id: driver_id.clone(),
            lat,
            lng,
            status,
            last_update: current_timestamp,
        };

        // Update driver data
        self.drivers.insert(driver_id.clone(), driver);

        // Add to grid bucket (only if not offline)
        if status != DriverStatus::Offline {
            self.grid
                .entry(geohash)
                .or_insert_with(HashSet::new)
                .insert(driver_id);
        }

        Ok(())
    }

    /// Remove driver from the index
    pub fn remove_driver(&self, driver_id: &str) -> bool {
        if let Some((_, old_driver)) = self.drivers.remove(driver_id) {
            let old_geohash = encode(old_driver.lat, old_driver.lng, self.precision).unwrap();
            if let Some(mut bucket) = self.grid.get_mut(&old_geohash) {
                bucket.remove(driver_id);
                if bucket.is_empty() {
                    drop(bucket);
                    self.grid.remove(&old_geohash);
                }
            }
            true
        } else {
            false
        }
    }

    /// Get driver location
    pub fn get_driver(&self, driver_id: &str) -> Option<Driver> {
        self.drivers.get(driver_id).map(|d| d.clone())
    }

    /// Find nearest drivers to a given location
    pub fn find_nearest_drivers(
        &self,
        lat: f64,
        lng: f64,
        max_drivers: usize,
        max_radius_km: f64,
    ) -> Vec<DriverWithDistance> {
        let search_point = Point::new(lng, lat);
        let mut candidates = Vec::new();

        // Calculate initial geohash
        let center_geohash = match encode(lat, lng, self.precision) {
            Ok(hash) => hash,
            Err(_) => return Vec::new(),
        };

        // Collect geohashes to search (center + neighbors)
        let mut search_hashes = HashSet::new();
        search_hashes.insert(center_geohash.clone());

        // Add neighboring geohashes for broader search
        if let Ok(neighbors) = neighbors(&center_geohash) {
            for neighbor in neighbors.values() {
                search_hashes.insert(neighbor.clone());
            }
        }

        // Search through geohash buckets
        for geohash in search_hashes {
            if let Some(bucket) = self.grid.get(&geohash) {
                for driver_id in bucket.iter() {
                    if let Some(driver) = self.drivers.get(driver_id) {
                        // Only consider available drivers
                        if driver.status != DriverStatus::Available {
                            continue;
                        }

                        let driver_point = Point::new(driver.lng, driver.lat);
                        let distance_m = search_point.haversine_distance(&driver_point);
                        let distance_km = distance_m / 1000.0;

                        // Check if within max radius
                        if distance_km <= max_radius_km {
                            candidates.push(DriverWithDistance {
                                driver: driver.clone(),
                                distance_km,
                            });
                        }
                    }
                }
            }
        }

        // Sort by distance and limit results
        candidates.sort_by(|a, b| a.distance_km.partial_cmp(&b.distance_km).unwrap());
        candidates.truncate(max_drivers);

        candidates
    }

    /// Get statistics about the index
    pub fn get_stats(&self) -> IndexStats {
        let total_drivers = self.drivers.len();
        let active_drivers = self
            .drivers
            .iter()
            .filter(|entry| entry.status != DriverStatus::Offline)
            .count();
        let available_drivers = self
            .drivers
            .iter()
            .filter(|entry| entry.status == DriverStatus::Available)
            .count();
        let total_buckets = self.grid.len();

        IndexStats {
            total_drivers,
            active_drivers,
            available_drivers,
            total_buckets,
            precision: self.precision,
        }
    }

    /// Clean up stale drivers (older than specified seconds)
    pub fn cleanup_stale_drivers(&self, max_age_seconds: u64) -> usize {
        let current_time = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();

        let mut removed_count = 0;
        let stale_drivers: Vec<String> = self
            .drivers
            .iter()
            .filter(|entry| {
                current_time - entry.last_update > max_age_seconds
            })
            .map(|entry| entry.id.clone())
            .collect();

        for driver_id in stale_drivers {
            if self.remove_driver(&driver_id) {
                removed_count += 1;
            }
        }

        removed_count
    }
}

/// Index statistics
#[derive(Debug, Clone, Serialize)]
pub struct IndexStats {
    pub total_drivers: usize,
    pub active_drivers: usize,
    pub available_drivers: usize,
    pub total_buckets: usize,
    pub precision: usize,
}

impl Default for GeoIndex {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_basic_operations() {
        let index = GeoIndex::new();

        // Test adding driver
        assert!(index
            .update_driver_location(
                "driver1".to_string(),
                40.7589,
                -73.9851,
                DriverStatus::Available
            )
            .is_ok());

        // Test getting driver
        let driver = index.get_driver("driver1").unwrap();
        assert_eq!(driver.id, "driver1");
        assert_eq!(driver.status, DriverStatus::Available);

        // Test finding nearest drivers
        let nearest = index.find_nearest_drivers(40.7589, -73.9851, 10, 10.0);
        assert_eq!(nearest.len(), 1);
        assert!(nearest[0].distance_km < 0.1); // Should be very close

        // Test removing driver
        assert!(index.remove_driver("driver1"));
        assert!(index.get_driver("driver1").is_none());
    }

    #[test]
    fn test_distance_calculation() {
        let index = GeoIndex::new();

        // Add drivers at different locations in NYC
        index
            .update_driver_location(
                "driver1".to_string(),
                40.7589, // Times Square
                -73.9851,
                DriverStatus::Available,
            )
            .unwrap();

        index
            .update_driver_location(
                "driver2".to_string(),
                40.7505, // Empire State Building (~1km away)
                -73.9934,
                DriverStatus::Available,
            )
            .unwrap();

        index
            .update_driver_location(
                "driver3".to_string(),
                40.6892, // Brooklyn Bridge (~10km away)
                -74.0445,
                DriverStatus::Available,
            )
            .unwrap();

        // Search from Times Square
        let nearest = index.find_nearest_drivers(40.7589, -73.9851, 10, 15.0);

        assert_eq!(nearest.len(), 3);
        assert!(nearest[0].distance_km < nearest[1].distance_km);
        assert!(nearest[1].distance_km < nearest[2].distance_km);
    }
}
