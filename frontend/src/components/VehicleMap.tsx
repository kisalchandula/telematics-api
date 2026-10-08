import { useEffect, useRef, useState } from "react"
import mapboxgl from "mapbox-gl"
import "mapbox-gl/dist/mapbox-gl.css"
import vehicleMarker from "../assets/vehicle_marker.svg"

type Telemetry = {
  latitude: number
  longitude: number
  heading: number
  timestamp: string
}

type VehicleTelemetry = {
  imei: string
  telemetry: Telemetry[]
}

type VehicleMapProps = {
  vehicles: VehicleTelemetry[]
  selectedImei: string | null
  onVehicleSelect: (imei: string) => void
}
export default function VehicleMap({
                                     vehicles,
                                     selectedImei,
                                     onVehicleSelect,
                                   }: VehicleMapProps) {
  const mapContainer = useRef<HTMLDivElement | null>(null)
  const map = useRef<mapboxgl.Map | null>(null)
  const markers = useRef<Record<string, mapboxgl.Marker>>({})

  /*
   * Stores the telemetry timestamps that already existed
   * when this page was loaded.
   *
   * These points belong to the "previous route".
   */
  const initialTelemetryTimestamps = useRef<
      Record<string, Set<string>>
  >({})

  const [showFullRoute, setShowFullRoute] = useState(false)

  useEffect(() => {
    if (!mapContainer.current || map.current) return

    mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN

    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: "mapbox://styles/mapbox/streets-v12",
      center: [8.4037, 49.0069],
      zoom: 12,
    })

    map.current.addControl(new mapboxgl.NavigationControl())

    return () => {
      Object.values(markers.current).forEach((marker) =>
          marker.remove(),
      )

      markers.current = {}
      initialTelemetryTimestamps.current = {}

      map.current?.remove()
      map.current = null
    }
  }, [])

  /*
   * Capture the telemetry that existed when the component
   * first receives the vehicle data.
   *
   * This effect runs only once for the initial vehicle data.
   */
  const initialDataCaptured = useRef(false)

  useEffect(() => {
    if (initialDataCaptured.current) return

    if (vehicles.length === 0) return

    vehicles.forEach((vehicle) => {
      initialTelemetryTimestamps.current[vehicle.imei] =
          new Set(
              vehicle.telemetry.map(
                  (point) => point.timestamp,
              ),
          )
    })

    initialDataCaptured.current = true
  }, [vehicles])

  useEffect(() => {
    const mapInstance = map.current

    if (!mapInstance) return

    const updateMap = () => {
      const activeImeis = new Set(
          vehicles.map((vehicle) => vehicle.imei),
      )

      // --------------------------------
      // Remove old vehicle markers
      // --------------------------------

      Object.keys(markers.current).forEach((imei) => {
        if (!activeImeis.has(imei)) {
          markers.current[imei].remove()
          delete markers.current[imei]
        }
      })

      vehicles.forEach((vehicle) => {
        const { imei, telemetry } = vehicle

        // API returns newest telemetry first.
        const latest = telemetry[0]

        // --------------------------------
        // Vehicle marker
        // --------------------------------

        if (latest) {
          const isSelected = imei === selectedImei

          let marker = markers.current[imei]

          if (!marker) {
            const element =
                document.createElement("img")

            element.src = vehicleMarker
            element.alt = "Vehicle"

            element.style.width = isSelected
                ? "42px"
                : "34px"

            element.style.height = isSelected
                ? "42px"
                : "34px"

            element.style.objectFit = "contain"
            element.style.cursor = "pointer"
            element.style.userSelect = "none"

            marker = new mapboxgl.Marker({
              element,
              rotationAlignment: "map",
            })
                .setLngLat([
                  latest.longitude,
                  latest.latitude,
                ])
                .setRotation(latest.heading)
                .addTo(mapInstance)

            element.addEventListener("click", () => {
              onVehicleSelect(imei)
            })

            markers.current[imei] = marker
          } else {
            marker.setLngLat([
              latest.longitude,
              latest.latitude,
            ])

            marker.setRotation(latest.heading)

            const element =
                marker.getElement() as HTMLImageElement

            element.style.width = isSelected
                ? "42px"
                : "34px"

            element.style.height = isSelected
                ? "42px"
                : "34px"
          }

          // Follow selected vehicle.
          if (isSelected) {
            mapInstance.easeTo({
              center: [
                latest.longitude,
                latest.latitude,
              ],
              duration: 500,
            })
          }
        }

        // --------------------------------
        // ROUTE
        // --------------------------------

        const initialTimestamps =
            initialTelemetryTimestamps.current[imei] ??
            new Set<string>()

        /*
         * Full route:
         *
         * Everything returned by the API.
         *
         * Recent:
         *
         * Only telemetry that was NOT already present
         * when this page was loaded.
         */
        const pointsToDraw = showFullRoute
            ? telemetry
            : telemetry.filter(
                (point) =>
                    !initialTimestamps.has(
                        point.timestamp,
                    ),
            )

        /*
         * API returns newest first.
         *
         * Reverse so the line is drawn chronologically.
         */
        const coordinates = pointsToDraw
            .slice()
            .reverse()
            .map((point) => [
              point.longitude,
              point.latitude,
            ])

        const sourceId = `route-${imei}`
        const layerId = `route-layer-${imei}`

        // @ts-ignore
        const routeData: GeoJSON.Feature<GeoJSON.LineString> =
            {
              type: "Feature",
              properties: {},
              geometry: {
                type: "LineString",
                coordinates,
              },
            }

        const existingSource = mapInstance.getSource(
            sourceId,
        ) as mapboxgl.GeoJSONSource | undefined

        if (existingSource) {
          existingSource.setData(routeData)
        } else if (coordinates.length >= 2) {
          mapInstance.addSource(sourceId, {
            type: "geojson",
            data: routeData,
          })

          mapInstance.addLayer({
            id: layerId,
            type: "line",
            source: sourceId,
            layout: {
              "line-join": "round",
              "line-cap": "round",
            },
            paint: {
              "line-color": "#000000",
              "line-width": 2,
              "line-opacity": 0.8,
            },
          })
        }
      })
    }

    if (mapInstance.isStyleLoaded()) {
      updateMap()
    } else {
      mapInstance.once("load", updateMap)
    }
  }, [
    vehicles,
    selectedImei,
    onVehicleSelect,
    showFullRoute,
  ])

  return (
      <div
          style={{
            position: "relative",
            width: "100%",
            height: "100%",
          }}
      >
        {/* Route toggle */}
        <div
            style={{
              position: "absolute",
              zIndex: 2,
              top: 12,
              left: 12,
              display: "flex",
              gap: 8,
            }}
        >
          <button
              onClick={() => setShowFullRoute(false)}
              style={{
                padding: "8px 12px",
                border: "1px solid #ccc",
                borderRadius: 6,
                background: showFullRoute
                    ? "#fff"
                    : "#000",
                color: showFullRoute
                    ? "#000"
                    : "#fff",
                cursor: "pointer",
              }}
          >
            Recent
          </button>

          <button
              onClick={() => setShowFullRoute(true)}
              style={{
                padding: "8px 12px",
                border: "1px solid #ccc",
                borderRadius: 6,
                background: showFullRoute
                    ? "#000"
                    : "#fff",
                color: showFullRoute
                    ? "#fff"
                    : "#000",
                cursor: "pointer",
              }}
          >
            Full route
          </button>
        </div>

        <div
            ref={mapContainer}
            style={{
              width: "100%",
              height: "100%",
            }}
        />
      </div>
  )
}