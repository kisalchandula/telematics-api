package org.kisal.telematics.simulator

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?
)

object GpxRoute {

    fun load(input: InputStream): List<GpsPoint> {

        val document = DocumentBuilderFactory
            .newInstance()
            .apply {
                isNamespaceAware = true
            }
            .newDocumentBuilder()
            .parse(input)

        val points = document
            .getElementsByTagNameNS(
                "http://www.topografix.com/GPX/1/1",
                "trkpt"
            )

        return buildList {

            for (index in 0 until points.length) {

                val point = points.item(index)

                val latitude =
                    point.attributes
                        .getNamedItem("lat")
                        .nodeValue
                        .toDouble()

                val longitude =
                    point.attributes
                        .getNamedItem("lon")
                        .nodeValue
                        .toDouble()

                val elevation =
                    point.childNodes.let { children ->

                        var value: Double? = null

                        for (childIndex in 0 until children.length) {

                            val child = children.item(childIndex)

                            if (child.localName == "ele") {
                                value = child.textContent.toDouble()
                                break
                            }
                        }

                        value
                    }

                add(
                    GpsPoint(
                        latitude = latitude,
                        longitude = longitude,
                        elevation = elevation
                    )
                )
            }
        }
    }
}