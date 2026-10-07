import { useEffect, useRef } from "react"
import mapboxgl from "mapbox-gl"
import "mapbox-gl/dist/mapbox-gl.css"

type Telemetry = {
    latitude: number
    longitude: number
    heading: number
}

type VehicleMapProps = {
  telemetry: Telemetry[]
}

function VehicleMap({
  telemetry
}: VehicleMapProps) {

  const mapContainer = useRef<HTMLDivElement | null>(null)

  const map = useRef<mapboxgl.Map | null>(null)

  const marker = useRef<mapboxgl.Marker | null>(null)

  const telemetryRef = useRef<Telemetry[]>([])

  telemetryRef.current = telemetry

  // --------------------------------
  // Initialize Mapbox once
  // --------------------------------

  useEffect(() => {

    if (!mapContainer.current) {
      return
    }

    const token = import.meta.env.VITE_MAPBOX_TOKEN

    if (!token) {
      console.error("Mapbox token is missing")
      return
    }

    mapboxgl.accessToken = token

    const initialTelemetry = telemetryRef.current

    const initialPoint =
      initialTelemetry[0]

    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: "mapbox://styles/mapbox/streets-v12",

      center: initialPoint
        ? [
            initialPoint.longitude,
            initialPoint.latitude
          ]
        : [8.4037, 49.0069],

      zoom: 14
    })

    map.current.addControl(
      new mapboxgl.NavigationControl(),
      "top-right"
    )

    map.current.on("load", () => {

      const currentTelemetry =
        telemetryRef.current

      const coordinates =
        currentTelemetry
          .slice()
          .reverse()
          .map(point => [
            point.longitude,
            point.latitude
          ])

      map.current?.addSource(
        "vehicle-route",
        {
          type: "geojson",

          data: {
            type: "Feature",
            properties: {},

            geometry: {
              type: "LineString",
              coordinates
            }
          }
        }
      )

      map.current?.addLayer({
        id: "vehicle-route",

        type: "line",

        source: "vehicle-route",

        layout: {
          "line-join": "round",
          "line-cap": "round"
        },

        paint: {
          "line-color": "#1976d2",
          "line-width": 4
        }
      })

      if (currentTelemetry.length > 0) {

        const latest =
          currentTelemetry[0]

          const markerElement =
              document.createElement("div")

          markerElement.innerHTML = "🚗"

          markerElement.style.fontSize = "28px"
          markerElement.style.lineHeight = "1"
          markerElement.style.cursor = "pointer"

          marker.current =
          new mapboxgl.Marker({
              element: markerElement,
              rotationAlignment: "map"
          })
              .setLngLat([
                  latest.longitude,
                  latest.latitude
              ])
              .setRotation(latest.heading)
              .addTo(map.current!)
      }

    })

    return () => {

      marker.current?.remove()

      marker.current = null

      map.current?.remove()

      map.current = null

    }

  }, [])

    // --------------------------------
// Update marker and route
// --------------------------------

    useEffect(() => {

        if (
            !map.current ||
            telemetry.length === 0
        ) {
            return
        }

        const latest = telemetry[0]

        // Create marker when telemetry becomes available
        if (!marker.current) {

            const markerElement =
                document.createElement("div")

            markerElement.innerHTML = "🚗"

            markerElement.style.fontSize = "28px"
            markerElement.style.lineHeight = "1"
            markerElement.style.cursor = "pointer"

            marker.current =
                new mapboxgl.Marker({
                    element: markerElement,
                    rotationAlignment: "map"
                })
                    .setLngLat([
                        latest.longitude,
                        latest.latitude
                    ])
                    .setRotation(latest.heading)
                    .addTo(map.current)

        } else {

            // Move existing marker
            marker.current.setLngLat([
                latest.longitude,
                latest.latitude
            ])

            marker.current.setRotation(
                latest.heading
            )
        }

        // Update route
        const source =
            map.current.getSource(
                "vehicle-route"
            ) as mapboxgl.GeoJSONSource | undefined

        if (source) {

            const coordinates =
                telemetry
                    .slice()
                    .reverse()
                    .map(point => [
                        point.longitude,
                        point.latitude
                    ])

            source.setData({
                type: "Feature",
                properties: {},

                geometry: {
                    type: "LineString",
                    coordinates
                }
            })
        }

    }, [telemetry])
  return (
    <div
      ref={mapContainer}
      style={{
        width: "100%",
        height: "100%"
      }}
    />
  )
}

export default VehicleMap

