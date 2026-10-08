type Telemetry = {
    timestamp: string
    latitude: number
    longitude: number
    altitude: number
    satellites: number
    speed: number
}

type VehicleTelemetry = {
    imei: string
    telemetry: Telemetry[]
}

type TelemetryPageProps = {
    vehicleTelemetry: VehicleTelemetry[]
}

function TelemetryPage({
                           vehicleTelemetry
                       }: TelemetryPageProps) {

    const events = vehicleTelemetry.flatMap(
        vehicle =>
            vehicle.telemetry.map(event => ({
                ...event,
                imei: vehicle.imei
            }))
    )

    events.sort(
        (a, b) =>
            new Date(b.timestamp).getTime() -
            new Date(a.timestamp).getTime()
    )

    const visibleEvents = events.slice(0, 50)

    return (
        <section className="page-panel">

            <div className="page-header">

                <div>

                    <h2>
                        Telemetry
                    </h2>

                    <p>
                        Vehicle telemetry data
                    </p>

                </div>

                <span>
                    {visibleEvents.length} events
                </span>

            </div>

            <div className="vehicles-table-container">

                <table className="vehicles-table">

                    <thead>

                    <tr>

                        <th>Time</th>

                        <th>IMEI</th>

                        <th>Latitude</th>

                        <th>Longitude</th>

                        <th>Speed</th>

                        <th>Altitude</th>

                        <th>Satellites</th>

                    </tr>

                    </thead>

                    <tbody>

                    {visibleEvents.map((event, index) => (

                        <tr key={`${event.timestamp}-${event.imei}-${index}`}>

                            <td>
                                {new Date(
                                    event.timestamp
                                ).toLocaleTimeString()}
                            </td>

                            <td>
                                {event.imei}
                            </td>

                            <td>
                                {event.latitude.toFixed(5)}
                            </td>

                            <td>
                                {event.longitude.toFixed(5)}
                            </td>

                            <td>
                                {event.speed} km/h
                            </td>

                            <td>
                                {event.altitude} m
                            </td>

                            <td>
                                {event.satellites}
                            </td>

                        </tr>

                    ))}

                    </tbody>

                </table>

            </div>

        </section>
    )
}

export default TelemetryPage