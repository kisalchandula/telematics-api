import { useEffect, useState } from "react"
import "./index.css"
import VehicleMap from "./components/VehicleMap"
import VehiclesPage from "./components/VehiclesPage"
import TelemetryPage from "./components/TelemetryPage"

type Telemetry = {
    deviceId: number
    timestamp: string
    latitude: number
    longitude: number
    altitude: number
    heading: number
    satellites: number
    speed: number
}

type DeviceStatus = {
    imei: string
    status: string
    lastSeen: string | null
}

type Device = {
    id: number
    imei: string
    manufacturer: string | null
    model: string | null
    name: string | null
    active: boolean
}

type VehicleTelemetry = {
    imei: string
    telemetry: Telemetry[]
}

function App() {

    const [activeTab, setActiveTab] =
        useState("Dashboard")

    const [devices, setDevices] =
        useState<Device[]>([])

    const [selectedImei, setSelectedImei] =
        useState<string | null>(null)

    const [latestTelemetry, setLatestTelemetry] =
        useState<Telemetry | null>(null)

    const [telemetry, setTelemetry] =
        useState<Telemetry[]>([])

    const [vehicleTelemetry, setVehicleTelemetry] =
        useState<VehicleTelemetry[]>([])

    const [deviceStatus, setDeviceStatus] =
        useState<DeviceStatus | null>(null)

    const [deviceStatuses, setDeviceStatuses] =
        useState<Record<string, DeviceStatus>>({})

    // --------------------------------
    // Load telemetry
    // --------------------------------

    useEffect(() => {

        let intervalId: number | undefined

        const loadTelemetry = () => {

            fetch("/api/devices")
                .then(response =>
                    response.json()
                )
                .then(data => {

                    setDevices(data)

                    if (data.length === 0) {
                        return
                    }

                    // --------------------------------
                    // Select vehicle
                    // --------------------------------

                    const selected =
                        selectedImei ?? data[0].imei

                    // --------------------------------
                    // Load all vehicles
                    // --------------------------------

                    return Promise.all(

                        data.map(
                            (device: Device) =>

                                Promise.all([

                                    // --------------------------------
                                    // Telemetry history
                                    // --------------------------------

                                    fetch(
                                        `/api/devices/${device.imei}/telemetry?limit=100`
                                    )
                                        .then(response =>
                                            response.json()
                                        ),

                                    // --------------------------------
                                    // Latest telemetry
                                    // --------------------------------

                                    fetch(
                                        `/api/devices/${device.imei}/latest`
                                    )
                                        .then(async response => {

                                            if (!response.ok) {
                                                return null
                                            }

                                            const text =
                                                await response.text()

                                            if (!text.trim()) {
                                                return null
                                            }

                                            return JSON.parse(text)

                                        }),

                                    // --------------------------------
                                    // Device status
                                    // --------------------------------

                                    fetch(
                                        `/api/devices/${device.imei}/status`
                                    )
                                        .then(async response => {

                                            if (!response.ok) {
                                                return null
                                            }

                                            const text =
                                                await response.text()

                                            if (!text.trim()) {
                                                return null
                                            }

                                            return JSON.parse(text)

                                        })

                                ])
                                    .then(
                                        ([history, latest, status]) => ({

                                            imei:
                                            device.imei,

                                            telemetry:
                                            history,

                                            latest,

                                            status

                                        })
                                    )
                        )

                    )
                        .then(allVehicles => {

                            // --------------------------------
                            // Update status data
                            // --------------------------------

                            const statuses:
                                Record<string, DeviceStatus> = {}

                            allVehicles.forEach(vehicle => {

                                if (vehicle.status) {

                                    statuses[vehicle.imei] =
                                        vehicle.status

                                }

                            })

                            setDeviceStatuses(statuses)

                            // --------------------------------
                            // Update map data
                            // --------------------------------

                            setVehicleTelemetry(
                                allVehicles.map(
                                    vehicle => ({

                                        imei:
                                        vehicle.imei,

                                        telemetry:
                                        vehicle.telemetry

                                    })
                                )
                            )

                            // --------------------------------
                            // Find selected vehicle
                            // --------------------------------

                            const selectedVehicle =
                                allVehicles.find(
                                    vehicle =>
                                        vehicle.imei ===
                                        selected
                                )

                            if (!selectedVehicle) {
                                return
                            }

                            // --------------------------------
                            // Update Vehicle Details
                            // --------------------------------

                            setLatestTelemetry(
                                selectedVehicle.latest ?? null
                            )

                            setTelemetry(
                                selectedVehicle.telemetry
                            )

                            setDeviceStatus(
                                selectedVehicle.status ?? null
                            )

                        })

                })
                .catch(error => {

                    console.error(
                        "Failed to load telemetry:",
                        error
                    )

                })

        }

        // Initial load
        loadTelemetry()

        // Refresh every 2 seconds
        intervalId =
            window.setInterval(
                loadTelemetry,
                2000
            )

        return () => {

            if (
                intervalId !== undefined
            ) {

                window.clearInterval(
                    intervalId
                )

            }

        }

    }, [])

    // --------------------------------
    // Update selected vehicle details
    // --------------------------------

    useEffect(() => {

        if (!selectedImei) {
            return
        }

        const selectedVehicle =
            vehicleTelemetry.find(
                vehicle =>
                    vehicle.imei === selectedImei
            )

        if (!selectedVehicle) {
            return
        }

        setTelemetry(
            selectedVehicle.telemetry
        )

        setLatestTelemetry(
            selectedVehicle.telemetry.length > 0
                ? selectedVehicle.telemetry[
                selectedVehicle.telemetry.length - 1
                    ]
                : null
        )

        setDeviceStatus(
            deviceStatuses[selectedImei] ?? null
        )

    }, [
        selectedImei,
        vehicleTelemetry,
        deviceStatuses
    ])

    // --------------------------------
    // Render
    // --------------------------------

    return (

        <div className="app">

            <aside className="sidebar">

                <div className="logo">

                    TELEMATICS

                    <span>
                        FLEET PLATFORM
                    </span>

                </div>

                <nav>

                    <button
                        className={
                            activeTab === "Dashboard"
                                ? "nav-item active"
                                : "nav-item"
                        }
                        onClick={() =>
                            setActiveTab("Dashboard")
                        }
                    >
                        Dashboard
                    </button>

                    <button
                        className={
                            activeTab === "Vehicles"
                                ? "nav-item active"
                                : "nav-item"
                        }
                        onClick={() =>
                            setActiveTab("Vehicles")
                        }
                    >
                        Vehicles
                    </button>

                    <button
                        className={
                            activeTab === "Telemetry"
                                ? "nav-item active"
                                : "nav-item"
                        }
                        onClick={() =>
                            setActiveTab("Telemetry")
                        }
                    >
                        Telemetry
                    </button>

                    <button
                        className={
                            activeTab === "System"
                                ? "nav-item active"
                                : "nav-item"
                        }
                        onClick={() =>
                            setActiveTab("System")
                        }
                    >
                        System
                    </button>

                </nav>

                {/* Vehicle selector */}

                <div className="vehicle-selector">

                    <span>
                        Vehicles
                    </span>

                    {devices.map(device => (

                        <button
                            key={device.imei}

                            className={
                                selectedImei ===
                                device.imei
                                    ? "vehicle-button active"
                                    : "vehicle-button"
                            }

                            onClick={() =>
                                setSelectedImei(
                                    device.imei
                                )
                            }
                        >

                            {device.imei}

                        </button>

                    ))}

                </div>

            </aside>

            <main className="main">

                {/* --------------------------------
                Top bar
                -------------------------------- */}

                <header className="topbar">

                    <div>

                        <h1>
                            {
                                activeTab === "Dashboard"
                                    ? "Fleet Overview"
                                    : activeTab
                            }
                        </h1>

                        <p>
                            {
                                activeTab === "Dashboard"
                                    ? "Real-time vehicle telemetry"
                                    : activeTab === "Vehicles"
                                        ? "Registered fleet vehicles"
                                        : activeTab === "Telemetry"
                                            ? "Vehicle telemetry data"
                                            : "Platform status and configuration"
                            }
                        </p>

                    </div>

                    <div className="gateway-status">

                        <span className="status-dot"></span>

                        Gateway Online

                    </div>

                </header>

                {/* --------------------------------
                Vehicles page
                -------------------------------- */}

                {activeTab === "Vehicles" ? (

                    <VehiclesPage
                        devices={devices}
                        deviceStatuses={deviceStatuses}
                        selectedImei={selectedImei}
                        onVehicleSelect={setSelectedImei}
                    />

                ) : (

                    <>

                        {/* --------------------------------
                        Dashboard
                        -------------------------------- */}

                        {activeTab === "Dashboard" && (

                            <>

                                {/* Statistics */}

                                <section className="stats">

                                    <div className="stat-card">

                                        <span className="stat-label">
                                            Vehicles
                                        </span>

                                        <strong>
                                            {devices.length}
                                        </strong>

                                    </div>

                                    <div className="stat-card">

                                        <span className="stat-label">
                                            Online
                                        </span>

                                        <strong>
                                            {
                                                devices.filter(
                                                    device =>
                                                        device.imei ===
                                                        selectedImei &&
                                                        deviceStatus?.status ===
                                                        "ONLINE"
                                                ).length
                                            }
                                        </strong>

                                    </div>

                                    <div className="stat-card">

                                        <span className="stat-label">
                                            Offline
                                        </span>

                                        <strong>
                                            {
                                                devices.filter(
                                                    device =>
                                                        device.imei ===
                                                        selectedImei &&
                                                        deviceStatus?.status ===
                                                        "OFFLINE"
                                                ).length
                                            }
                                        </strong>

                                    </div>

                                    <div className="stat-card">

                                        <span className="stat-label">
                                            Telemetry Events
                                        </span>

                                        <strong>
                                            {telemetry.length}
                                        </strong>

                                    </div>

                                </section>

                                {/* Map + Vehicle Details */}

                                <section className="dashboard-grid">

                                    <div className="panel map-panel">

                                        <div className="panel-header">

                                            <h2>
                                                Vehicle Location
                                            </h2>

                                            <span>
                                                {devices.length} vehicles
                                            </span>

                                        </div>

                                        <div className="map-placeholder">

                                            <VehicleMap
                                                vehicles={
                                                    vehicleTelemetry
                                                }
                                                selectedImei={
                                                    selectedImei
                                                }
                                                onVehicleSelect={
                                                    setSelectedImei
                                                }
                                            />

                                        </div>

                                    </div>

                                    <div className="panel vehicle-panel">

                                        <div className="panel-header">

                                            <h2>
                                                Vehicle Details
                                            </h2>

                                        </div>

                                        <div className="vehicle-info">

                                            <div>

                                                <span>
                                                    Status
                                                </span>

                                                <strong
                                                    className={
                                                        deviceStatus?.status ===
                                                        "ONLINE"
                                                            ? "online"
                                                            : ""
                                                    }
                                                >

                                                    {
                                                        deviceStatus?.status ??
                                                        "UNKNOWN"
                                                    }

                                                </strong>

                                            </div>

                                            <div>

                                                <span>
                                                    IMEI
                                                </span>

                                                <strong>
                                                    {
                                                        selectedImei ??
                                                        devices[0]?.imei ??
                                                        "-"
                                                    }
                                                </strong>

                                            </div>

                                            <div>

                                                <span>
                                                    Speed
                                                </span>

                                                <strong>

                                                    {
                                                        latestTelemetry
                                                            ? `${latestTelemetry.speed} km/h`
                                                            : "-"
                                                    }

                                                </strong>

                                            </div>

                                            <div>

                                                <span>
                                                    Altitude
                                                </span>

                                                <strong>

                                                    {
                                                        latestTelemetry
                                                            ? `${latestTelemetry.altitude} m`
                                                            : "-"
                                                    }

                                                </strong>

                                            </div>

                                            <div>

                                                <span>
                                                    Satellites
                                                </span>

                                                <strong>

                                                    {
                                                        latestTelemetry?.satellites ??
                                                        "-"
                                                    }

                                                </strong>

                                            </div>

                                            <div>

                                                <span>
                                                    Last Seen
                                                </span>

                                                <strong>

                                                    {
                                                        deviceStatus?.lastSeen
                                                            ? new Date(
                                                                deviceStatus.lastSeen
                                                            ).toLocaleTimeString()
                                                            : "-"
                                                    }

                                                </strong>

                                            </div>

                                        </div>

                                    </div>

                                </section>

                                {/* Recent Telemetry */}

                                <section className="panel telemetry-panel">

                                    <div className="panel-header">

                                        <h2>
                                            Recent Telemetry
                                        </h2>

                                        <span>
                                            {telemetry.length} events
                                        </span>

                                    </div>

                                    <table>

                                        <thead>

                                        <tr>

                                            <th>
                                                Time
                                            </th>

                                            <th>
                                                Latitude
                                            </th>

                                            <th>
                                                Longitude
                                            </th>

                                            <th>
                                                Speed
                                            </th>

                                            <th>
                                                Status
                                            </th>

                                        </tr>

                                        </thead>

                                        <tbody>

                                        {telemetry
                                            .slice(0, 5)
                                            .map(event => (

                                                <tr
                                                    key={
                                                        event.timestamp
                                                    }
                                                >

                                                    <td>

                                                        {new Date(
                                                            event.timestamp
                                                        ).toLocaleTimeString()}

                                                    </td>

                                                    <td>

                                                        {event.latitude.toFixed(
                                                            4
                                                        )}

                                                    </td>

                                                    <td>

                                                        {event.longitude.toFixed(
                                                            4
                                                        )}

                                                    </td>

                                                    <td>

                                                        {event.speed} km/h

                                                    </td>

                                                    <td>

                                                        <span className="online">

                                                            {
                                                                deviceStatus?.status ??
                                                                "UNKNOWN"
                                                            }

                                                        </span>

                                                    </td>

                                                </tr>

                                            ))}

                                        </tbody>

                                    </table>

                                </section>

                            </>

                        )}

                        {/* --------------------------------
                        Telemetry page
                        -------------------------------- */}

                        {activeTab === "Telemetry" && (

                            <TelemetryPage
                                vehicleTelemetry={
                                    vehicleTelemetry
                                }
                            />

                        )}

                        {/* --------------------------------
                        System page
                        -------------------------------- */}

                        {activeTab === "System" && (

                            <section className="page-panel">

                                <div className="page-header">

                                    <div>

                                        <h2>
                                            System
                                        </h2>

                                        <p>
                                            Platform status and configuration
                                        </p>

                                    </div>

                                </div>

                                <div className="page-content">

                                    System view coming next.

                                </div>

                            </section>

                        )}

                    </>

                )}

            </main>

        </div>

    )
}

export default App