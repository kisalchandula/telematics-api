import { useState } from "react"

type Device = {
    id: number
    imei: string
    manufacturer: string | null
    model: string | null
    name: string | null
    active: boolean
}

type DeviceStatus = {
    status: string
    lastSeen: string | null
}

type VehiclesPageProps = {
    devices: Device[]
    deviceStatuses: Record<string, DeviceStatus>
    selectedImei: string | null
    onVehicleSelect: (imei: string) => void
}

function VehiclesPage({
                          devices,
                          deviceStatuses,
                          selectedImei,
                          onVehicleSelect
                      }: VehiclesPageProps) {

    const [showForm, setShowForm] = useState(false)

    const [imei, setImei] = useState("")
    const [manufacturer, setManufacturer] = useState("")
    const [model, setModel] = useState("")
    const [name, setName] = useState("")

    const handleSubmit = async (
        event: React.FormEvent
    ) => {

        event.preventDefault()

        const response = await fetch(
            "/api/devices",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    imei,
                    manufacturer: manufacturer || null,
                    model: model || null,
                    name: name || null
                })
            }
        )

        if (!response.ok) {
            alert("Failed to create device")
            return
        }

        setImei("")
        setManufacturer("")
        setModel("")
        setName("")
        setShowForm(false)
    }

    const handleDelete = async (imei: string) => {

        const confirmed = window.confirm(
            `Remove device ${imei}?`
        )

        if (!confirmed) {
            return
        }

        const response = await fetch(
            `/api/devices/${imei}`,
            {
                method: "DELETE"
            }
        )

        if (!response.ok) {
            alert("Failed to remove device")
            return
        }
    }

    return (
        <section className="page-panel">

            <div className="page-header">

                <div>

                    <h2>
                        Vehicles
                    </h2>

                    <p>
                        Registered fleet vehicles
                    </p>

                </div>

                <div className="page-header-actions">

                    <span>
                        {devices.length} devices
                    </span>

                    <button
                        className="add-device-button"
                        onClick={() =>
                            setShowForm(!showForm)
                        }
                    >
                        + Add Device
                    </button>

                </div>

            </div>

            {showForm && (

                <div className="device-form-container">

                    <form
                        className="device-form"
                        onSubmit={handleSubmit}
                    >

                        <div className="device-form-field">

                            <label>
                                IMEI
                            </label>

                            <input
                                value={imei}
                                onChange={event =>
                                    setImei(event.target.value)
                                }
                                placeholder="356307042441016"
                                required
                            />

                        </div>

                        <div className="device-form-field">

                            <label>
                                Manufacturer
                            </label>

                            <input
                                value={manufacturer}
                                onChange={event =>
                                    setManufacturer(event.target.value)
                                }
                                placeholder="Teltonika"
                            />

                        </div>

                        <div className="device-form-field">

                            <label>
                                Model
                            </label>

                            <input
                                value={model}
                                onChange={event =>
                                    setModel(event.target.value)
                                }
                                placeholder="FMB920"
                            />

                        </div>

                        <div className="device-form-field">

                            <label>
                                Name
                            </label>

                            <input
                                value={name}
                                onChange={event =>
                                    setName(event.target.value)
                                }
                                placeholder="Karlsruhe Test Device"
                            />

                        </div>

                        <div className="device-form-actions">

                            <button
                                type="button"
                                className="cancel-device-button"
                                onClick={() =>
                                    setShowForm(false)
                                }
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                className="save-device-button"
                            >
                                Add Device
                            </button>

                        </div>

                    </form>

                </div>

            )}

            <div className="vehicles-table-container">

                <table className="vehicles-table">

                    <thead>

                    <tr>
                        <th>
                            Device
                        </th>

                        <th>
                            IMEI
                        </th>

                        <th>
                            Manufacturer
                        </th>

                        <th>
                            Model
                        </th>

                        <th>
                            Status
                        </th>

                        <th>
                            Last Seen
                        </th>

                        <th>
                            Actions
                        </th>
                    </tr>

                    </thead>

                    <tbody>

                    {devices.map(device => {

                        const status =
                            deviceStatuses[device.imei]

                        return (

                            <tr
                                key={device.imei}
                                className={
                                    selectedImei === device.imei
                                        ? "selected-row"
                                        : ""
                                }
                            >

                                <td>

                                    <div className="vehicle-name">

                                        <strong>
                                            {device.name || "Unnamed Vehicle"}
                                        </strong>

                                    </div>

                                </td>

                                <td>
                                    {device.imei}
                                </td>

                                <td>
                                    {device.manufacturer || "—"}
                                </td>

                                <td>
                                    {device.model || "—"}
                                </td>

                                <td>

                                    <span
                                        className={
                                            status?.status === "ONLINE"
                                                ? "status-badge online"
                                                : status?.status === "OFFLINE"
                                                    ? "status-badge offline"
                                                    : "status-badge registered"
                                        }
                                    >
                                        {status?.status ?? "REGISTERED"}
                                    </span>

                                </td>

                                <td>

                                    {status?.lastSeen
                                        ? new Date(
                                            status.lastSeen
                                        ).toLocaleTimeString()
                                        : "—"}

                                </td>

                                <td>

                                    <div className="device-actions">

                                        <button
                                            className="view-button"
                                            onClick={() =>
                                                onVehicleSelect(
                                                    device.imei
                                                )
                                            }
                                        >
                                            View
                                        </button>

                                        <button
                                            className="remove-button"
                                            onClick={() =>
                                                handleDelete(device.imei)
                                            }
                                        >
                                            Remove
                                        </button>

                                    </div>

                                </td>

                            </tr>

                        )

                    })}

                    </tbody>

                </table>

            </div>

        </section>
    )
}

export default VehiclesPage