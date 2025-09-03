use crate::geo_index::GeoIndex;
use axum::{
    extract::State,
    http::StatusCode,
    response::Json,
    routing::{get, post},
    Router,
};
use serde_json::{json, Value};
use std::sync::Arc;
use tower_http::cors::CorsLayer;
use tracing::info;

/// Create HTTP server for metrics and health checks
pub fn create_http_server(geo_index: GeoIndex) -> Router {
    let shared_state = Arc::new(AppState { geo_index });

    Router::new()
        .route("/health", get(health_check))
        .route("/metrics", get(metrics_handler))
        .route("/stats", get(stats_handler))
        .route("/cleanup", post(cleanup_handler))
        .with_state(shared_state)
        .layer(CorsLayer::permissive())
}

#[derive(Clone)]
struct AppState {
    geo_index: GeoIndex,
}

/// Health check endpoint
async fn health_check() -> Result<Json<Value>, StatusCode> {
    Ok(Json(json!({
        "status": "healthy",
        "service": "geo-index-service",
        "timestamp": chrono::Utc::now().to_rfc3339()
    })))
}

/// Prometheus metrics endpoint
async fn metrics_handler(State(state): State<Arc<AppState>>) -> Result<String, StatusCode> {
    let stats = state.geo_index.get_stats();
    
    // Generate Prometheus format metrics
    let metrics = format!(
        r#"# HELP geo_index_total_drivers Total number of drivers in index
# TYPE geo_index_total_drivers gauge
geo_index_total_drivers {}

# HELP geo_index_active_drivers Number of active drivers (not offline)
# TYPE geo_index_active_drivers gauge
geo_index_active_drivers {}

# HELP geo_index_available_drivers Number of available drivers
# TYPE geo_index_available_drivers gauge
geo_index_available_drivers {}

# HELP geo_index_total_buckets Total number of geohash buckets
# TYPE geo_index_total_buckets gauge
geo_index_total_buckets {}

# HELP geo_index_precision Geohash precision level
# TYPE geo_index_precision gauge
geo_index_precision {}
"#,
        stats.total_drivers,
        stats.active_drivers,
        stats.available_drivers,
        stats.total_buckets,
        stats.precision
    );

    Ok(metrics)
}

/// Stats endpoint returning JSON
async fn stats_handler(State(state): State<Arc<AppState>>) -> Result<Json<Value>, StatusCode> {
    let stats = state.geo_index.get_stats();
    
    Ok(Json(json!({
        "total_drivers": stats.total_drivers,
        "active_drivers": stats.active_drivers,
        "available_drivers": stats.available_drivers,
        "total_buckets": stats.total_buckets,
        "precision": stats.precision,
        "timestamp": chrono::Utc::now().to_rfc3339()
    })))
}

/// Cleanup stale drivers endpoint
async fn cleanup_handler(State(state): State<Arc<AppState>>) -> Result<Json<Value>, StatusCode> {
    // Clean up drivers older than 10 minutes
    let max_age_seconds = 10 * 60;
    let removed_count = state.geo_index.cleanup_stale_drivers(max_age_seconds);
    
    info!("Cleaned up {} stale drivers", removed_count);
    
    Ok(Json(json!({
        "removed_count": removed_count,
        "max_age_seconds": max_age_seconds,
        "timestamp": chrono::Utc::now().to_rfc3339()
    })))
}
