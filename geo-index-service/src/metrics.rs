use metrics::{counter, gauge, histogram};
use metrics_exporter_prometheus::PrometheusBuilder;
use std::time::Instant;

/// Initialize metrics system
pub fn init_metrics() -> Result<(), Box<dyn std::error::Error>> {
    PrometheusBuilder::new()
        .install()?;
    Ok(())
}

/// Metrics for tracking geo-index operations
pub struct GeoIndexMetrics;

impl GeoIndexMetrics {
    /// Record a driver location update
    pub fn record_driver_update() {
        counter!("geo_index_driver_updates_total").increment(1);
    }

    /// Record a driver removal
    pub fn record_driver_removal() {
        counter!("geo_index_driver_removals_total").increment(1);
    }

    /// Record a nearest driver search
    pub fn record_search(duration: std::time::Duration, drivers_found: usize) {
        histogram!("geo_index_search_duration_seconds").record(duration.as_secs_f64());
        histogram!("geo_index_search_results_count").record(drivers_found as f64);
        counter!("geo_index_searches_total").increment(1);
    }

    /// Update current driver counts
    pub fn update_driver_counts(total: usize, active: usize, available: usize) {
        gauge!("geo_index_total_drivers").set(total as f64);
        gauge!("geo_index_active_drivers").set(active as f64);
        gauge!("geo_index_available_drivers").set(available as f64);
    }

    /// Record gRPC request
    pub fn record_grpc_request(method: &str, duration: std::time::Duration, success: bool) {
        let labels = [("method", method), ("success", &success.to_string())];
        counter!("geo_index_grpc_requests_total", &labels).increment(1);
        histogram!("geo_index_grpc_request_duration_seconds", &labels[..1])
            .record(duration.as_secs_f64());
    }
}

/// RAII helper for measuring operation duration
pub struct MetricsTimer {
    start: Instant,
    operation: String,
}

impl MetricsTimer {
    pub fn new(operation: impl Into<String>) -> Self {
        Self {
            start: Instant::now(),
            operation: operation.into(),
        }
    }
}

impl Drop for MetricsTimer {
    fn drop(&mut self) {
        let duration = self.start.elapsed();
        histogram!("geo_index_operation_duration_seconds", "operation" => self.operation.clone())
            .record(duration.as_secs_f64());
    }
}
