import { useEffect, useState } from "react"
import "./index.css"
import VehicleMap from "./components/VehicleMap"

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
  imei: string
}

function App() { {

  const [devices, setDevices] = useState<Device[]>([])
  const [latestTelemetry, setLatestTelemetry] =
    useState<Telemetry | null>(null)

  const [telemetry, setTelemetry] =
    useState<Telemetry[]>([])

  const [deviceStatus, setDeviceStatus] =
    useState<DeviceStatus | null>(null)

  useEffect(() => {

    let intervalId: number | undefined

    const loadTelemetry = () => {

      fetch("/api/devices")
        .then(response => response.json())
        .then(data => {

          setDevices(data)

          if (data.length === 0) {
            return
          }

          const imei = data[0].imei

          return Promise.all([

            fetch(`/api/devices/${imei}/latest`)
    .then(response => response.json()),

    fetch(`/api/devices/${imei}/telemetry?limit=100`)
        .then(response => response.json()),

    fetch(`/api/devices/${imei}/status`)
        .then(response => response.json())

])
.then(([latest, history, status]) => {

  setLatestTelemetry(latest)
  setTelemetry(history)
  setDeviceStatus(status)

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
intervalId = window.setInterval(
    loadTelemetry,
    2000
)

// Cleanup when component is removed
return () => {

  if (intervalId !== undefined) {
    window.clearInterval(intervalId)
  }

}

}, [])

// @ts-ignore
  return (

    <div className="app">

      <aside className="sidebar">

        <div className="logo">
          TELEMATICS
          <span>FLEET PLATFORM</span>
        </div>

        <nav>

          <a className="nav-item active">
            Dashboard
          </a>

          <a className="nav-item">
            Vehicles
          </a>

          <a className="nav-item">
            Telemetry
          </a>

          <a className="nav-item">
            System
          </a>

        </nav>

      </aside>

      <main className="main">

        <header className="topbar">

          <div>

            <h1>
              Fleet Overview
            </h1>

            <p>
              Real-time vehicle telemetry
            </p>

          </div>

          <div className="gateway-status">

            <span className="status-dot"></span>

            Gateway Online

          </div>

        </header>

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
              {deviceStatus?.status === "ONLINE"
                  ? 1
                  : 0}
            </strong>

          </div>

          <div className="stat-card">

            <span className="stat-label">
              Offline
            </span>

            <strong>
              {deviceStatus?.status === "OFFLINE"
                  ? 1
                  : 0}
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

        <section className="dashboard-grid">

          <div className="panel map-panel">

            <div className="panel-header">

              <h2>
                Vehicle Location
              </h2>

              <span>
                {devices.length} vehicle
              </span>

            </div>

            <div className="map-placeholder">

              <VehicleMap
                  telemetry={telemetry}
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
                      deviceStatus?.status === "ONLINE"
                          ? "online"
                          : ""
                    }
                >
                  {deviceStatus?.status ?? "UNKNOWN"}
                </strong>

              </div>

              <div>

                <span>
                  IMEI
                </span>

                <strong>{devices[0]?.imei ?? "-"}</strong>

              </div>

              <div>

                <span>
                  Speed
                </span>

                <strong>
                  {latestTelemetry
                      ? `${latestTelemetry.speed} km/h`
                      : "-"}
                </strong>

              </div>

              <div>

                <span>
                  Altitude
                </span>

                <strong>
                  {latestTelemetry
                      ? `${latestTelemetry.altitude} m`
                      : "-"}
                </strong>

              </div>

              <div>

                <span>
                  Satellites
                </span>

                <strong>
                  {latestTelemetry?.satellites ?? "-"}
                </strong>

              </div>

              <div>

                <span>
                  Last Seen
                </span>

                <strong>
                  {deviceStatus?.lastSeen
                      ? new Date(
                          deviceStatus.lastSeen
                      ).toLocaleTimeString()
                      : "-"}
                </strong>

              </div>

            </div>

          </div>

        </section>

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

                    <tr key={event.timestamp}>

                      <td>
                        {new Date(
                            event.timestamp
                        ).toLocaleTimeString()}
                      </td>

                      <td>
                        {event.latitude.toFixed(4)}
                      </td>

                      <td>
                        {event.longitude.toFixed(4)}
                      </td>

                      <td>
                        {event.speed} km/h
                      </td>

                      <td>

                      <span className="online">
                        {deviceStatus?.status ??
                            "UNKNOWN"}
                      </span>

                      </td>

                    </tr>

                ))}

            </tbody>

          </table>

        </section>

      </main>

    </div>

)
}}

export default App