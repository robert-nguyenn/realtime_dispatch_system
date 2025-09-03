use crate::geo_index::{DriverStatus, GeoIndex};
use crate::geoindex::{
    geo_index_service_server::GeoIndexService, DriverLocation, FindNearestDriversRequest,
    FindNearestDriversResponse, GetDriverLocationRequest, GetDriverLocationResponse,
    RemoveDriverRequest, RemoveDriverResponse, UpdateDriverLocationRequest,
    UpdateDriverLocationResponse,
};
use tonic::{Request, Response, Status};
use tracing::{debug, error, info, warn};

/// gRPC service implementation for the geo-index
pub struct GeoIndexGrpcService {
    geo_index: GeoIndex,
}

impl GeoIndexGrpcService {
    pub fn new(geo_index: GeoIndex) -> Self {
        Self { geo_index }
    }
}

#[tonic::async_trait]
impl GeoIndexService for GeoIndexGrpcService {
    async fn find_nearest_drivers(
        &self,
        request: Request<FindNearestDriversRequest>,
    ) -> Result<Response<FindNearestDriversResponse>, Status> {
        let req = request.into_inner();
        
        debug!(
            "Finding nearest drivers for location ({}, {}) with max_drivers={} and max_radius_km={}",
            req.lat, req.lng, req.max_drivers, req.max_radius_km
        );

        // Validate input
        if !(-90.0..=90.0).contains(&req.lat) {
            return Err(Status::invalid_argument("Invalid latitude"));
        }
        if !(-180.0..=180.0).contains(&req.lng) {
            return Err(Status::invalid_argument("Invalid longitude"));
        }
        if req.max_drivers <= 0 {
            return Err(Status::invalid_argument("max_drivers must be positive"));
        }
        if req.max_radius_km <= 0.0 {
            return Err(Status::invalid_argument("max_radius_km must be positive"));
        }

        let drivers = self.geo_index.find_nearest_drivers(
            req.lat,
            req.lng,
            req.max_drivers as usize,
            req.max_radius_km,
        );

        let driver_locations: Vec<DriverLocation> = drivers
            .into_iter()
            .map(|d| DriverLocation {
                driver_id: d.driver.id,
                lat: d.driver.lat,
                lng: d.driver.lng,
                distance_km: d.distance_km,
                status: map_driver_status_to_proto(d.driver.status) as i32,
                last_update_timestamp: d.driver.last_update as i64,
            })
            .collect();

        debug!("Found {} nearby drivers", driver_locations.len());

        Ok(Response::new(FindNearestDriversResponse {
            drivers: driver_locations,
        }))
    }

    async fn update_driver_location(
        &self,
        request: Request<UpdateDriverLocationRequest>,
    ) -> Result<Response<UpdateDriverLocationResponse>, Status> {
        let req = request.into_inner();
        
        debug!(
            "Updating location for driver {} to ({}, {}) with status {:?}",
            req.driver_id, req.lat, req.lng, req.status
        );

        // Validate input
        if req.driver_id.is_empty() {
            return Err(Status::invalid_argument("driver_id cannot be empty"));
        }
        if !(-90.0..=90.0).contains(&req.lat) {
            return Err(Status::invalid_argument("Invalid latitude"));
        }
        if !(-180.0..=180.0).contains(&req.lng) {
            return Err(Status::invalid_argument("Invalid longitude"));
        }

        let status = map_proto_status_to_driver(req.status);

        match self.geo_index.update_driver_location(
            req.driver_id.clone(),
            req.lat,
            req.lng,
            status,
        ) {
            Ok(()) => {
                info!(
                    "Successfully updated location for driver {} to ({}, {})",
                    req.driver_id, req.lat, req.lng
                );
                Ok(Response::new(UpdateDriverLocationResponse {
                    success: true,
                    message: "Location updated successfully".to_string(),
                }))
            }
            Err(e) => {
                error!("Failed to update driver location: {}", e);
                Ok(Response::new(UpdateDriverLocationResponse {
                    success: false,
                    message: format!("Failed to update location: {}", e),
                }))
            }
        }
    }

    async fn remove_driver(
        &self,
        request: Request<RemoveDriverRequest>,
    ) -> Result<Response<RemoveDriverResponse>, Status> {
        let req = request.into_inner();
        
        debug!("Removing driver {}", req.driver_id);

        if req.driver_id.is_empty() {
            return Err(Status::invalid_argument("driver_id cannot be empty"));
        }

        let success = self.geo_index.remove_driver(&req.driver_id);

        if success {
            info!("Successfully removed driver {}", req.driver_id);
            Ok(Response::new(RemoveDriverResponse {
                success: true,
                message: "Driver removed successfully".to_string(),
            }))
        } else {
            warn!("Driver {} not found for removal", req.driver_id);
            Ok(Response::new(RemoveDriverResponse {
                success: false,
                message: "Driver not found".to_string(),
            }))
        }
    }

    async fn get_driver_location(
        &self,
        request: Request<GetDriverLocationRequest>,
    ) -> Result<Response<GetDriverLocationResponse>, Status> {
        let req = request.into_inner();
        
        debug!("Getting location for driver {}", req.driver_id);

        if req.driver_id.is_empty() {
            return Err(Status::invalid_argument("driver_id cannot be empty"));
        }

        match self.geo_index.get_driver(&req.driver_id) {
            Some(driver) => {
                debug!("Found driver {} at ({}, {})", req.driver_id, driver.lat, driver.lng);
                Ok(Response::new(GetDriverLocationResponse {
                    driver: Some(DriverLocation {
                        driver_id: driver.id,
                        lat: driver.lat,
                        lng: driver.lng,
                        distance_km: 0.0, // Distance not applicable for single driver lookup
                        status: map_driver_status_to_proto(driver.status) as i32,
                        last_update_timestamp: driver.last_update as i64,
                    }),
                    found: true,
                }))
            }
            None => {
                debug!("Driver {} not found", req.driver_id);
                Ok(Response::new(GetDriverLocationResponse {
                    driver: None,
                    found: false,
                }))
            }
        }
    }
}

/// Map internal driver status to protobuf enum
fn map_driver_status_to_proto(status: DriverStatus) -> crate::geoindex::DriverStatus {
    match status {
        DriverStatus::Offline => crate::geoindex::DriverStatus::Offline,
        DriverStatus::Available => crate::geoindex::DriverStatus::Available,
        DriverStatus::Busy => crate::geoindex::DriverStatus::Busy,
        DriverStatus::EnRoute => crate::geoindex::DriverStatus::EnRoute,
    }
}

/// Map protobuf enum to internal driver status
fn map_proto_status_to_driver(status: i32) -> DriverStatus {
    match status {
        0 => DriverStatus::Offline,
        1 => DriverStatus::Available,
        2 => DriverStatus::Busy,
        3 => DriverStatus::EnRoute,
        _ => DriverStatus::Offline, // Default to offline for unknown status
    }
}
