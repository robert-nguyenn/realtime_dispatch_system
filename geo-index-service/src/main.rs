use anyhow::Result;
use std::net::SocketAddr;
use tonic::transport::Server;
use tracing::{info, Level};
use tracing_subscriber::FmtSubscriber;

mod geo_index;
mod grpc_service;
mod http_server;
mod metrics;

use geo_index::GeoIndex;
use grpc_service::GeoIndexGrpcService;
use http_server::create_http_server;

// Include the generated protobuf code
pub mod geoindex {
    tonic::include_proto!("geoindex");
}

#[tokio::main]
async fn main() -> Result<()> {
    // Initialize tracing
    let subscriber = FmtSubscriber::builder()
        .with_max_level(Level::INFO)
        .finish();
    tracing::subscriber::set_global_default(subscriber)?;

    info!("Starting Geo-Index Service");

    // Create the geo-index
    let geo_index = GeoIndex::new();

    // Start HTTP server for metrics and health checks
    let http_server = create_http_server(geo_index.clone());
    let http_addr: SocketAddr = "0.0.0.0:8080".parse()?;
    
    tokio::spawn(async move {
        info!("HTTP server listening on {}", http_addr);
        if let Err(e) = axum::Server::bind(&http_addr)
            .serve(http_server.into_make_service())
            .await
        {
            tracing::error!("HTTP server error: {}", e);
        }
    });

    // Start gRPC server
    let grpc_service = GeoIndexGrpcService::new(geo_index);
    let grpc_addr: SocketAddr = "0.0.0.0:50051".parse()?;

    info!("gRPC server listening on {}", grpc_addr);

    Server::builder()
        .add_service(geoindex::geo_index_service_server::GeoIndexServiceServer::new(grpc_service))
        .serve(grpc_addr)
        .await?;

    Ok(())
}
