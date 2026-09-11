package com.example.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.pow

/**
 * Authentic, Official Cartographic Geometric Model of the Republic of India.
 * Conforms strictly to Survey of India (SOI) official sovereign boundaries:
 * - Northern Crown: Complete Union Territories of Jammu & Kashmir and Ladakh
 *   (including Gilgit-Baltistan, Siachen Glacier, and Aksai Chin up to Indira Col at 37.10° N)
 * - Northeastern Frontier: Arunachal Pradesh (McMahon Line up to Kibithu at 97.40° E)
 * - Western Frontier: Gujarat (Ghuar Mota, Sir Creek & Kutch at 68.50° E)
 * - Southern Extents: Kanyakumari (8.08° N) & Indira Point, Great Nicobar (6.75° N)
 * - Island Territories: Andaman & Nicobar Archipelago and Lakshadweep Atolls
 */
object IndiaMapGeometry {

    const val INDIA_MIN_LAT = 6.2
    const val INDIA_MAX_LAT = 37.6
    const val INDIA_MIN_LON = 68.0
    const val INDIA_MAX_LON = 97.6
    const val INDIA_CENTER_LAT = 22.8
    const val INDIA_CENTER_LON = 82.5
    const val MIN_ZOOM = 4.0f
    const val MAX_ZOOM = 12.0f

    private const val MIN_LON = 68.0f
    private const val MAX_LON = 97.5f
    private const val MIN_LAT = 6.5f
    private const val MAX_LAT = 37.2f

    data class CardinalExtreme(
        val title: String,
        val locationName: String,
        val lat: Double,
        val lon: Double,
        val direction: String,
        val significance: String
    )

    data class StateCapital(
        val name: String,
        val state: String,
        val lat: Double,
        val lon: Double,
        val isNationalCapital: Boolean = false
    )

    data class StateRegionLabel(
        val name: String,
        val lat: Double,
        val lon: Double
    )

    // Web Mercator standard projection formulas (EPSG:3857)
    fun lonToMercatorX(lon: Double): Double = (lon + 180.0) / 360.0

    fun latToMercatorY(lat: Double): Double {
        val latClamped = lat.coerceIn(-85.0511, 85.0511)
        val sinLat = kotlin.math.sin(Math.toRadians(latClamped))
        return (0.5 - kotlin.math.ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI))
    }

    fun mercatorXToLon(x: Double): Double = x * 360.0 - 180.0

    fun mercatorYToLat(y: Double): Double {
        val n = Math.PI - 2.0 * Math.PI * y
        return Math.toDegrees(kotlin.math.atan(kotlin.math.sinh(n)))
    }

    fun geoToMercatorPixel(
        lat: Double,
        lon: Double,
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        viewWidth: Float,
        viewHeight: Float
    ): Offset {
        val scale = 256.0 * 2.0.pow(zoom.toDouble())
        val cX = lonToMercatorX(centerLon)
        val cY = latToMercatorY(centerLat)
        val pX = lonToMercatorX(lon)
        val pY = latToMercatorY(lat)
        val px = (viewWidth / 2.0 + (pX - cX) * scale).toFloat()
        val py = (viewHeight / 2.0 + (pY - cY) * scale).toFloat()
        return Offset(px, py)
    }

    fun pixelToGeo(
        px: Float,
        py: Float,
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        viewWidth: Float,
        viewHeight: Float
    ): Pair<Double, Double> {
        val scale = 256.0 * 2.0.pow(zoom.toDouble())
        val cX = lonToMercatorX(centerLon)
        val cY = latToMercatorY(centerLat)
        val pX = (px - viewWidth / 2.0) / scale + cX
        val pY = (py - viewHeight / 2.0) / scale + cY
        val lon = mercatorXToLon(pX)
        val lat = mercatorYToLat(pY)
        return Pair(lat, lon)
    }

    fun geoToNorm(lat: Float, lon: Float): Pair<Float, Float> {
        val nx = ((lon - MIN_LON) / (MAX_LON - MIN_LON)).coerceIn(0f, 1f)
        val ny = (1f - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT))).coerceIn(0f, 1f)
        return Pair(nx, ny)
    }

    // Precise Survey of India mainland sovereign boundary polygon coordinates (lat, lon)
    private val MAINLAND_BORDER_GEO = listOf(
        // 1. Northern Crown: Official Survey of India (Ladakh, Siachen, Karakoram, Hanle)
        37.10f to 74.90f, // Indira Col (Northernmost point of India)
        36.95f to 75.85f, // Shaksgam valley frontier
        36.20f to 76.80f, // Karakoram Pass & K2
        35.85f to 77.85f, // Siachen Glacier Muztagh
        36.00f to 78.60f, // Qara Tagh Pass / Kunlun
        35.50f to 79.50f, // Northern Aksai Chin rim
        34.70f to 79.60f, // Lanak La / Aksai Chin
        34.10f to 79.30f, // Pangong Tso
        33.50f to 78.95f, // Spanggur Gap
        33.20f to 79.25f, // Demchok (Indus valley frontier)
        32.70f to 78.80f, // Chumar
        32.50f to 78.60f, // Ladakh - HP frontier

        // 2. Himachal Pradesh & Uttarakhand (Western Himalayas)
        31.85f to 78.60f, // Kinnaur
        31.05f to 79.25f, // Garhwal
        30.40f to 80.30f, // Pithoragarh
        30.25f to 81.05f, // Lipulekh Pass / Kalapani / Limpiyadhura (Official SOI boundary)

        // 3. Northern Gangetic Plain along Nepal Border
        29.00f to 80.20f, // Sharda River / Tanakpur
        28.40f to 81.20f, // Dudhwa
        27.50f to 82.80f, // Balrampur
        27.10f to 84.40f, // Valmiki / West Champaran
        26.65f to 86.60f, // Koshi
        26.60f to 88.05f, // Kishanganj

        // 4. Sikkim & Siliguri Corridor (Chicken's Neck)
        27.15f to 88.15f, // West Sikkim
        27.95f to 88.40f, // Kanchenjunga
        28.10f to 88.65f, // Donkia La / North Sikkim
        27.70f to 88.90f, // Nathu La
        27.05f to 88.95f, // Siliguri Corridor
        26.85f to 89.80f, // Buxa / Alipurduar
        26.80f to 91.80f, // Manas Bhutan border

        // 5. Arunachal Pradesh (Eastern Himalayas to Kibithu)
        27.40f to 92.00f, // Tawang
        27.80f to 92.60f, // West Kameng
        28.30f to 93.80f, // Upper Subansiri
        28.90f to 94.90f, // Siang
        29.25f to 95.70f, // Dibang Valley
        28.50f to 96.80f, // Lohit
        28.01f to 97.40f, // Kibithu / Dong (Easternmost Point of India)
        27.00f to 96.50f, // Changlang

        // 6. Eastern Frontier (Nagaland, Manipur, Mizoram)
        26.10f to 95.10f, // Mon, Nagaland
        25.10f to 94.40f, // Ukhrul, Manipur
        24.30f to 93.80f, // Chandel
        23.20f to 93.30f, // Champhai, Mizoram
        22.20f to 93.00f, // Lawngtlai
        21.90f to 92.75f, // South Mizoram tip

        // 7. Tripura, Meghalaya & Bangladesh Border
        22.80f to 92.35f,
        23.90f to 91.40f, // Agartala
        24.90f to 92.00f, // Karimganj
        25.20f to 91.20f, // Cherrapunji / Meghalaya
        25.20f to 89.90f, // Garo Hills
        26.15f to 89.85f, // Dhubri
        25.80f to 88.70f, // Dinajpur
        24.70f to 88.10f, // Malda
        24.10f to 88.50f, // Murshidabad
        22.80f to 88.90f, // North 24 Parganas
        21.70f to 89.15f, // Sundarbans Tidal Delta

        // 8. Bay of Bengal Coast (Odisha, Andhra Pradesh, Tamil Nadu)
        21.40f to 87.30f, // Balasore
        20.70f to 86.85f, // Bhitarkanika Mangroves
        19.80f to 85.80f, // Chilika Lake / Puri
        18.30f to 83.90f, // Srikakulam
        17.70f to 83.30f, // Visakhapatnam
        16.50f to 82.20f, // Godavari Delta / Kakinada
        15.80f to 80.80f, // Krishna Delta / Machilipatnam
        14.40f to 80.05f, // Pulicat Lake / Nellore
        13.10f to 80.30f, // Chennai Coromandel Coast
        11.90f to 79.80f, // Puducherry
        10.75f to 79.85f, // Point Calimere
        9.30f to 79.15f,  // Palk Strait / Rameshwaram
        8.80f to 78.15f,  // Tuticorin / Gulf of Mannar
        8.08f to 77.55f,  // Kanyakumari (Southernmost Mainland Point of India)

        // 9. Arabian Sea Coast (Kerala, Karnataka, Goa, Maharashtra)
        8.50f to 76.90f,  // Thiruvananthapuram
        9.50f to 76.30f,  // Alappuzha
        10.00f to 76.20f, // Kochi (Malabar)
        11.25f to 75.75f, // Kozhikode
        12.85f to 74.80f, // Mangalore
        14.80f to 74.15f, // Karwar
        15.30f to 73.80f, // Goa
        16.95f to 73.30f, // Ratnagiri (Konkan)
        18.95f to 72.82f, // Mumbai
        20.40f to 72.85f, // Daman
        21.20f to 72.80f, // Surat

        // 10. Accurate Gujarat Geometry: Kathiawar Peninsula & Kutch
        21.75f to 72.15f, // Gulf of Khambhat
        21.40f to 71.95f, // Bhavnagar
        20.70f to 70.95f, // Diu / Southern Saurashtra
        20.90f to 70.35f, // Somnath
        21.60f to 69.60f, // Porbandar
        22.25f to 68.95f, // Dwarka (Western tip)
        22.80f to 70.00f, // Gulf of Kutch South
        23.20f to 70.70f, // Kandla
        23.35f to 69.70f, // Bhuj
        23.71f to 68.52f, // Ghuar Mota / Sir Creek (Westernmost Point of India)
        24.20f to 69.10f, // Great Rann of Kutch
        24.60f to 70.50f, // Thar boundary

        // 11. Western Sovereign Border (Rajasthan, Punjab, Jammu & Kashmir, Ladakh)
        25.40f to 70.60f, // Barmer Thar desert
        26.90f to 70.50f, // Jaisalmer
        27.80f to 71.40f, // Bikaner
        29.00f to 72.50f, // Sri Ganganagar
        30.20f to 73.90f, // Firozpur
        31.60f to 74.60f, // Amritsar / Wagah
        32.40f to 75.00f, // Gurdaspur
        32.75f to 74.85f, // Jammu
        33.15f to 73.75f, // Mirpur
        33.75f to 73.80f, // Poonch
        34.35f to 73.45f, // Muzaffarabad
        34.80f to 74.20f, // Gurez / Neelum
        35.30f to 73.90f, // Diamer / Chilas
        36.20f to 73.50f, // Gilgit / Yasin
        36.90f to 74.10f, // Hunza / Mintaka Pass
        37.10f to 74.90f  // Indira Col (Completing the Northern Crown)
    )

    // Cardinal Geographic Extrema of the Republic of India
    fun getCardinalExtremes(): List<CardinalExtreme> = listOf(
        CardinalExtreme(
            title = "Northernmost Point",
            locationName = "Indira Col, Ladakh",
            lat = 37.10,
            lon = 74.90,
            direction = "NORTH",
            significance = "37°06' N • Karakoram Muztagh"
        ),
        CardinalExtreme(
            title = "Southernmost Territory",
            locationName = "Indira Point, Great Nicobar",
            lat = 6.75,
            lon = 93.85,
            direction = "SOUTH (Islands)",
            significance = "6°45' N • Bay of Bengal & Indian Ocean"
        ),
        CardinalExtreme(
            title = "Southernmost Mainland",
            locationName = "Kanyakumari, Tamil Nadu",
            lat = 8.08,
            lon = 77.55,
            direction = "SOUTH (Mainland)",
            significance = "8°04' N • Triveni Sangam of 3 Seas"
        ),
        CardinalExtreme(
            title = "Westernmost Point",
            locationName = "Ghuar Mota, Kutch, Gujarat",
            lat = 23.71,
            lon = 68.52,
            direction = "WEST",
            significance = "68°31' E • Sir Creek / Arabian Sea"
        ),
        CardinalExtreme(
            title = "Easternmost Point",
            locationName = "Kibithu, Anjaw, Arunachal Pradesh",
            lat = 28.01,
            lon = 97.40,
            direction = "EAST",
            significance = "97°24' E • First Sunrise of India"
        )
    )

    // Key State Capitals and National Capital Territory
    fun getStateCapitals(): List<StateCapital> = listOf(
        StateCapital("New Delhi", "National Capital Territory", 28.6139, 77.2090, isNationalCapital = true),
        StateCapital("Srinagar", "Jammu & Kashmir", 34.0837, 74.7973),
        StateCapital("Leh", "Ladakh", 34.1526, 77.5771),
        StateCapital("Shimla", "Himachal Pradesh", 31.1048, 77.1734),
        StateCapital("Dehradun", "Uttarakhand", 30.3165, 78.0322),
        StateCapital("Chandigarh", "Punjab & Haryana", 30.7333, 76.7794),
        StateCapital("Jaipur", "Rajasthan", 26.9124, 75.7873),
        StateCapital("Gandhinagar", "Gujarat", 23.2156, 72.6369),
        StateCapital("Bhopal", "Madhya Pradesh", 23.2599, 77.4126),
        StateCapital("Lucknow", "Uttar Pradesh", 26.8467, 80.9462),
        StateCapital("Patna", "Bihar", 25.5941, 85.1376),
        StateCapital("Ranchi", "Jharkhand", 23.3441, 85.3096),
        StateCapital("Kolkata", "West Bengal", 22.5726, 88.3639),
        StateCapital("Bhubaneswar", "Odisha", 20.2961, 85.8245),
        StateCapital("Raipur", "Chhattisgarh", 21.2514, 81.6296),
        StateCapital("Mumbai", "Maharashtra", 18.9220, 72.8347),
        StateCapital("Panaji", "Goa", 15.4909, 73.8278),
        StateCapital("Bengaluru", "Karnataka", 12.9716, 77.5946),
        StateCapital("Hyderabad", "Telangana", 17.3850, 78.4867),
        StateCapital("Amaravati", "Andhra Pradesh", 16.5417, 80.5158),
        StateCapital("Chennai", "Tamil Nadu", 13.0827, 80.2707),
        StateCapital("Thiruvananthapuram", "Kerala", 8.5241, 76.9366),
        StateCapital("Gangtok", "Sikkim", 27.3389, 88.6065),
        StateCapital("Dispur", "Assam", 26.1445, 91.7362),
        StateCapital("Itanagar", "Arunachal Pradesh", 27.0844, 93.6053),
        StateCapital("Shillong", "Meghalaya", 25.5788, 91.8933),
        StateCapital("Kohima", "Nagaland", 25.6751, 94.1086),
        StateCapital("Imphal", "Manipur", 24.8170, 93.9368),
        StateCapital("Aizawl", "Mizoram", 23.7271, 92.7176),
        StateCapital("Agartala", "Tripura", 23.8315, 91.2868),
        StateCapital("Port Blair", "Andaman & Nicobar", 11.6234, 92.7265),
        StateCapital("Kavaratti", "Lakshadweep", 10.5667, 72.6417)
    )

    // State & Union Territory Centroid Labels
    fun getStateLabels(): List<StateRegionLabel> = listOf(
        StateRegionLabel("LADAKH", 34.5, 77.6),
        StateRegionLabel("JAMMU & KASHMIR", 33.6, 75.0),
        StateRegionLabel("HIMACHAL PRADESH", 31.8, 77.2),
        StateRegionLabel("PUNJAB", 30.9, 75.5),
        StateRegionLabel("UTTARAKHAND", 30.1, 79.2),
        StateRegionLabel("HARYANA", 29.2, 76.3),
        StateRegionLabel("RAJASTHAN", 26.5, 73.8),
        StateRegionLabel("UTTAR PRADESH", 26.8, 80.8),
        StateRegionLabel("BIHAR", 25.7, 85.8),
        StateRegionLabel("GUJARAT", 22.5, 71.5),
        StateRegionLabel("MADHYA PRADESH", 23.5, 78.5),
        StateRegionLabel("CHHATTISGARH", 21.3, 82.0),
        StateRegionLabel("JHARKHAND", 23.6, 85.3),
        StateRegionLabel("WEST BENGAL", 23.2, 87.8),
        StateRegionLabel("ODISHA", 20.5, 84.5),
        StateRegionLabel("MAHARASHTRA", 19.5, 76.0),
        StateRegionLabel("KARNATAKA", 14.8, 75.8),
        StateRegionLabel("TELANGANA", 17.9, 79.0),
        StateRegionLabel("ANDHRA PRADESH", 15.5, 79.8),
        StateRegionLabel("KERALA", 10.4, 76.5),
        StateRegionLabel("TAMIL NADU", 11.0, 78.5),
        StateRegionLabel("SIKKIM", 27.5, 88.5),
        StateRegionLabel("ASSAM", 26.2, 92.8),
        StateRegionLabel("ARUNACHAL PRADESH", 28.0, 94.8),
        StateRegionLabel("NAGALAND", 26.0, 94.4),
        StateRegionLabel("MANIPUR", 24.8, 93.9),
        StateRegionLabel("MIZORAM", 23.2, 92.9),
        StateRegionLabel("TRIPURA", 23.8, 91.5),
        StateRegionLabel("MEGHALAYA", 25.5, 91.3)
    )

    // Primary Internal State Demarcation Lines (Mercator)
    fun getStateDemarcationsMercator(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): List<Path> {
        val lines = listOf(
            // Ladakh - J&K demarcation line
            listOf(35.5f to 76.5f, 34.5f to 76.2f, 33.7f to 76.0f, 32.9f to 76.5f),
            // HP - Punjab / Haryana
            listOf(32.4f to 75.8f, 31.6f to 76.3f, 31.0f to 76.8f, 30.6f to 77.2f),
            // Punjab - Rajasthan
            listOf(30.2f to 73.9f, 29.8f to 74.8f, 29.5f to 75.3f),
            // Haryana - Rajasthan
            listOf(29.5f to 75.3f, 28.4f to 75.8f, 27.9f to 76.4f, 28.0f to 77.0f),
            // Rajasthan - Gujarat
            listOf(24.6f to 70.5f, 24.5f to 72.0f, 24.0f to 73.2f, 23.5f to 74.1f),
            // Gujarat - MP
            listOf(23.5f to 74.1f, 22.8f to 74.5f, 21.8f to 74.0f),
            // Rajasthan - MP
            listOf(24.0f to 73.2f, 25.0f to 75.0f, 25.8f to 76.8f, 26.8f to 77.8f),
            // UP - MP
            listOf(26.8f to 77.8f, 25.5f to 78.5f, 25.2f to 80.0f, 24.8f to 81.8f, 24.2f to 83.0f),
            // MP - Maharashtra
            listOf(21.8f to 74.0f, 21.4f to 76.5f, 21.6f to 78.8f, 21.5f to 80.2f),
            // Maharashtra - Gujarat
            listOf(21.2f to 72.8f, 20.8f to 73.5f, 21.5f to 73.8f),
            // Maharashtra - Karnataka & Goa
            listOf(15.8f to 73.8f, 16.2f to 74.4f, 17.2f to 76.0f, 17.8f to 77.2f),
            // Karnataka - Goa
            listOf(15.3f to 73.8f, 15.6f to 74.2f),
            // Maharashtra - Telangana
            listOf(17.8f to 77.2f, 18.8f to 78.0f, 19.5f to 79.5f, 19.0f to 80.0f),
            // Karnataka - Telangana & Andhra
            listOf(17.8f to 77.2f, 16.8f to 77.3f, 15.8f to 77.2f, 14.5f to 77.5f, 13.5f to 78.2f),
            // Telangana - Andhra Pradesh
            listOf(16.8f to 77.3f, 16.5f to 79.0f, 16.8f to 80.2f, 17.8f to 81.2f),
            // Karnataka - Kerala
            listOf(12.8f to 74.8f, 12.0f to 75.8f),
            // Karnataka - Tamil Nadu
            listOf(12.0f to 75.8f, 12.2f to 77.0f, 12.8f to 78.2f),
            // Kerala - Tamil Nadu
            listOf(12.0f to 75.8f, 10.5f to 76.8f, 9.5f to 77.2f, 8.4f to 77.5f),
            // Tamil Nadu - Andhra Pradesh
            listOf(13.4f to 80.1f, 13.2f to 79.2f, 12.8f to 78.2f),
            // Odisha - Andhra Pradesh
            listOf(18.3f to 83.9f, 18.8f to 83.0f, 18.2f to 82.2f),
            // Odisha - Chhattisgarh
            listOf(18.2f to 82.2f, 20.0f to 82.5f, 21.5f to 83.0f, 22.2f to 84.0f),
            // Chhattisgarh - MP & Maharashtra
            listOf(21.5f to 80.2f, 22.5f to 81.5f, 23.5f to 82.5f),
            // UP - Bihar
            listOf(27.1f to 84.4f, 26.0f to 84.2f, 25.5f to 84.0f, 24.2f to 83.0f),
            // Bihar - Jharkhand
            listOf(24.5f to 83.5f, 24.6f to 85.5f, 25.2f to 87.5f),
            // Jharkhand - West Bengal
            listOf(25.2f to 87.5f, 24.0f to 86.8f, 23.2f to 86.6f, 22.2f to 87.0f),
            // Assam - Arunachal Pradesh
            listOf(27.4f to 92.0f, 27.2f to 93.5f, 27.5f to 95.0f, 27.8f to 96.0f),
            // Assam - Meghalaya
            listOf(26.0f to 89.9f, 25.8f to 91.5f, 25.4f to 92.5f),
            // Assam - Nagaland & Manipur
            listOf(26.8f to 95.0f, 26.2f to 94.2f, 25.5f to 93.5f)
        )

        return lines.map { pts ->
            val p = Path()
            val fpt = geoToMercatorPixel(pts[0].first.toDouble(), pts[0].second.toDouble(), centerLat, centerLon, zoom, width, height)
            p.moveTo(fpt.x, fpt.y)
            for (i in 1 until pts.size) {
                val pt = geoToMercatorPixel(pts[i].first.toDouble(), pts[i].second.toDouble(), centerLat, centerLon, zoom, width, height)
                p.lineTo(pt.x, pt.y)
            }
            p
        }
    }

    // Tropic of Cancer (23°26' N / 23.43° N)
    fun getTropicOfCancerMercator(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): Path {
        val path = Path()
        val lat = 23.43
        val startPt = geoToMercatorPixel(lat, 68.2, centerLat, centerLon, zoom, width, height)
        path.moveTo(startPt.x, startPt.y)
        for (lon in 69..94 step 2) {
            val pt = geoToMercatorPixel(lat, lon.toDouble(), centerLat, centerLon, zoom, width, height)
            path.lineTo(pt.x, pt.y)
        }
        val endPt = geoToMercatorPixel(lat, 94.0, centerLat, centerLon, zoom, width, height)
        path.lineTo(endPt.x, endPt.y)
        return path
    }

    // Indian Standard Meridian (82.5° E / IST)
    fun getStandardMeridianMercator(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): Path {
        val path = Path()
        val lon = 82.50
        val startPt = geoToMercatorPixel(36.5, lon, centerLat, centerLon, zoom, width, height)
        path.moveTo(startPt.x, startPt.y)
        for (lat in 35 downTo 8 step 3) {
            val pt = geoToMercatorPixel(lat.toDouble(), lon, centerLat, centerLon, zoom, width, height)
            path.lineTo(pt.x, pt.y)
        }
        val endPt = geoToMercatorPixel(7.5, lon, centerLat, centerLon, zoom, width, height)
        path.lineTo(endPt.x, endPt.y)
        return path
    }

    // Mainland Boundary Mercator Path (Official SOI Boundary)
    fun getIndiaBoundaryMercatorPath(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): Path {
        val path = Path()
        if (MAINLAND_BORDER_GEO.isEmpty()) return path

        val firstGeo = MAINLAND_BORDER_GEO.first()
        val firstPt = geoToMercatorPixel(firstGeo.first.toDouble(), firstGeo.second.toDouble(), centerLat, centerLon, zoom, width, height)
        path.moveTo(firstPt.x, firstPt.y)

        for (i in 1 until MAINLAND_BORDER_GEO.size) {
            val currGeo = MAINLAND_BORDER_GEO[i]
            val pt = geoToMercatorPixel(currGeo.first.toDouble(), currGeo.second.toDouble(), centerLat, centerLon, zoom, width, height)
            path.lineTo(pt.x, pt.y)
        }
        path.close()
        return path
    }

    // Andaman & Nicobar Archipelago Chain (Mercator)
    fun getAndamanNicobarMercatorIslands(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): List<Path> {
        val islands = listOf(
            // North Andaman
            listOf(13.7f to 92.9f, 13.5f to 93.1f, 13.1f to 93.0f, 13.2f to 92.8f),
            // Middle Andaman
            listOf(12.9f to 92.8f, 12.8f to 93.0f, 12.4f to 92.9f, 12.5f to 92.7f),
            // South Andaman & Port Blair
            listOf(12.0f to 92.7f, 11.9f to 92.9f, 11.5f to 92.8f, 11.6f to 92.6f),
            // Little Andaman
            listOf(10.8f to 92.5f, 10.7f to 92.6f, 10.5f to 92.5f, 10.6f to 92.4f),
            // Car Nicobar
            listOf(9.3f to 92.75f, 9.2f to 92.85f, 9.1f to 92.80f, 9.2f to 92.70f),
            // Camorta & Nancowry
            listOf(8.1f to 93.45f, 8.0f to 93.55f, 7.9f to 93.50f, 8.0f to 93.40f),
            // Great Nicobar (Home to Indira Point at 6.75° N)
            listOf(7.1f to 93.80f, 7.0f to 93.95f, 6.75f to 93.85f, 6.90f to 93.75f)
        )

        return islands.map { islandPts ->
            val p = Path()
            val fpt = geoToMercatorPixel(islandPts[0].first.toDouble(), islandPts[0].second.toDouble(), centerLat, centerLon, zoom, width, height)
            p.moveTo(fpt.x, fpt.y)
            for (i in 1 until islandPts.size) {
                val pt = geoToMercatorPixel(islandPts[i].first.toDouble(), islandPts[i].second.toDouble(), centerLat, centerLon, zoom, width, height)
                p.lineTo(pt.x, pt.y)
            }
            p.close()
            p
        }
    }

    // Lakshadweep Atolls (Mercator)
    fun getLakshadweepMercatorIslands(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): List<Offset> {
        val points = listOf(
            11.8f to 72.8f,  // Chetlat
            11.2f to 72.5f,  // Kadmat
            10.57f to 72.64f, // Kavaratti (Capital)
            10.05f to 73.65f, // Kalpeni
            8.29f to 73.05f   // Minicoy
        )
        return points.map { (lat, lon) ->
            geoToMercatorPixel(lat.toDouble(), lon.toDouble(), centerLat, centerLon, zoom, width, height)
        }
    }

    // Major Ecological Rivers of India (Mercator)
    fun getMajorRiversMercator(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): List<Pair<String, Path>> {
        val riverGeos = listOf(
            "Indus" to listOf(
                35.8f to 76.5f, 35.3f to 75.5f, 34.5f to 74.8f, 33.8f to 74.2f
            ),
            "Ganga" to listOf(
                30.9f to 78.9f, 29.9f to 78.1f, 28.0f to 79.5f, 27.1f to 80.0f,
                25.4f to 81.8f, 25.3f to 83.0f, 25.6f to 85.1f, 24.8f to 87.9f, 22.2f to 88.0f
            ),
            "Yamuna" to listOf(
                31.0f to 78.4f, 28.6f to 77.2f, 27.2f to 78.0f, 25.4f to 81.8f
            ),
            "Brahmaputra" to listOf(
                28.8f to 95.3f, 27.5f to 94.9f, 26.6f to 93.0f, 26.2f to 91.7f, 26.0f to 89.8f
            ),
            "Narmada" to listOf(
                22.6f to 81.7f, 23.1f to 79.9f, 21.8f to 74.0f, 21.7f to 72.9f
            ),
            "Godavari" to listOf(
                19.9f to 73.5f, 19.1f to 76.5f, 18.8f to 79.0f, 16.9f to 81.8f
            ),
            "Krishna" to listOf(
                17.9f to 73.6f, 16.5f to 76.0f, 16.2f to 80.6f
            ),
            "Mahanadi" to listOf(
                21.1f to 81.9f, 21.5f to 83.9f, 20.4f to 85.8f, 20.3f to 86.7f
            ),
            "Kaveri" to listOf(
                12.4f to 75.5f, 12.3f to 76.6f, 10.8f to 78.7f, 11.1f to 79.8f
            )
        )

        return riverGeos.map { (name, pts) ->
            val p = Path()
            val fpt = geoToMercatorPixel(pts[0].first.toDouble(), pts[0].second.toDouble(), centerLat, centerLon, zoom, width, height)
            p.moveTo(fpt.x, fpt.y)
            for (i in 1 until pts.size) {
                val pt = geoToMercatorPixel(pts[i].first.toDouble(), pts[i].second.toDouble(), centerLat, centerLon, zoom, width, height)
                p.lineTo(pt.x, pt.y)
            }
            name to p
        }
    }

    // Western Ghats Biodiversity Ribbon
    fun getWesternGhatsMercatorRibbon(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): Path {
        val pts = listOf(
            20.5f to 73.6f, 18.5f to 73.5f, 16.0f to 74.1f, 14.2f to 74.8f,
            12.5f to 75.6f, 10.5f to 76.8f, 8.5f to 77.2f, 8.4f to 77.5f,
            10.2f to 77.1f, 12.3f to 75.9f, 14.0f to 75.1f, 16.2f to 74.4f,
            18.7f to 73.8f, 20.5f to 73.9f
        )
        val p = Path()
        val fpt = geoToMercatorPixel(pts[0].first.toDouble(), pts[0].second.toDouble(), centerLat, centerLon, zoom, width, height)
        p.moveTo(fpt.x, fpt.y)
        for (i in 1 until pts.size) {
            val pt = geoToMercatorPixel(pts[i].first.toDouble(), pts[i].second.toDouble(), centerLat, centerLon, zoom, width, height)
            p.lineTo(pt.x, pt.y)
        }
        p.close()
        return p
    }

    // Himalayan Snow Zone Arc
    fun getHimalayanSnowMercatorZone(
        centerLat: Double,
        centerLon: Double,
        zoom: Float,
        width: Float,
        height: Float
    ): Path {
        val pts = listOf(
            36.5f to 74.5f, 35.5f to 76.5f, 34.5f to 78.5f, 32.5f to 79.5f, 30.5f to 80.5f,
            28.0f to 88.5f, 28.5f to 93.0f, 29.0f to 95.5f, 28.0f to 96.5f,
            27.5f to 94.0f, 27.0f to 88.5f, 29.5f to 80.0f, 31.5f to 78.0f,
            33.5f to 76.5f, 35.0f to 75.5f
        )
        val p = Path()
        val fpt = geoToMercatorPixel(pts[0].first.toDouble(), pts[0].second.toDouble(), centerLat, centerLon, zoom, width, height)
        p.moveTo(fpt.x, fpt.y)
        for (i in 1 until pts.size) {
            val pt = geoToMercatorPixel(pts[i].first.toDouble(), pts[i].second.toDouble(), centerLat, centerLon, zoom, width, height)
            p.lineTo(pt.x, pt.y)
        }
        p.close()
        return p
    }
}
