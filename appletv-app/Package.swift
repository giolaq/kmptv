// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AppleTVApp",
    platforms: [
        .tvOS(.v16),
        .macOS(.v13)
    ],
    products: [
        .executable(
            name: "AppleTVApp",
            targets: ["AppleTVApp"]
        ),
    ],
    dependencies: [],
    targets: [
        .executableTarget(
            name: "AppleTVApp",
            dependencies: [],
            path: "Sources"
        ),
        .testTarget(
            name: "AppleTVAppTests",
            dependencies: ["AppleTVApp"],
            path: "Tests"
        ),
    ]
)