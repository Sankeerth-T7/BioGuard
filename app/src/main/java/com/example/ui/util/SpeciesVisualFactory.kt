package com.example.ui.util

import android.graphics.*
import com.example.data.model.Species

/**
 * Generates high-fidelity, richly colored, nature-inspired visual Bitmaps for species.
 * Provides authentic illustrations for sample presets and offline catalog visualizers.
 */
object SpeciesVisualFactory {

    private val cache = mutableMapOf<String, Bitmap>()

    fun getSpeciesBitmap(speciesName: String, width: Int = 800, height: Int = 560): Bitmap {
        val key = "${speciesName.lowercase()}_${width}x$height"
        cache[key]?.let { return it }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        when {
            speciesName.contains("Tiger", ignoreCase = true) -> drawTiger(canvas, width, height)
            speciesName.contains("Peafowl", ignoreCase = true) || speciesName.contains("Peacock", ignoreCase = true) -> drawPeafowl(canvas, width, height)
            speciesName.contains("Banyan", ignoreCase = true) -> drawBanyan(canvas, width, height)
            speciesName.contains("Frog", ignoreCase = true) -> drawPurpleFrog(canvas, width, height)
            speciesName.contains("Turtle", ignoreCase = true) || speciesName.contains("Olive", ignoreCase = true) -> drawSeaTurtle(canvas, width, height)
            speciesName.contains("Lion", ignoreCase = true) -> drawAsiaticLion(canvas, width, height)
            speciesName.contains("Rhino", ignoreCase = true) -> drawRhinoceros(canvas, width, height)
            speciesName.contains("Leopard", ignoreCase = true) -> drawSnowLeopard(canvas, width, height)
            speciesName.contains("Neem", ignoreCase = true) -> drawNeemTree(canvas, width, height)
            speciesName.contains("Cobra", ignoreCase = true) -> drawCobra(canvas, width, height)
            speciesName.contains("Gharial", ignoreCase = true) -> drawGharial(canvas, width, height)
            else -> drawGenericNature(canvas, width, height, speciesName)
        }

        cache[key] = bitmap
        return bitmap
    }

    private fun drawTiger(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background: Lush Indian jungle golden hour
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(20, 60, 35), Color.rgb(45, 95, 45), Color.rgb(15, 40, 20)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Ambient sunbeams filtering through canopy
        paint.color = Color.argb(45, 255, 230, 150)
        val beamPath = android.graphics.Path().apply {
            moveTo(w * 0.1f, 0f)
            lineTo(w * 0.45f, 0f)
            lineTo(w * 0.75f, h.toFloat())
            lineTo(w * 0.35f, h.toFloat())
            close()
        }
        canvas.drawPath(beamPath, paint)

        // Jungle leaves in background
        paint.color = Color.argb(120, 10, 45, 20)
        for (i in 0..5) {
            val cx = w * (0.15f + i * 0.16f)
            canvas.drawCircle(cx, h * 0.3f, w * 0.14f, paint)
        }

        // Tiger body base: Rich golden-orange tawny fur
        paint.color = Color.rgb(225, 115, 25)
        canvas.drawOval(RectF(w * 0.18f, h * 0.32f, w * 0.82f, h * 0.88f), paint)

        // Tiger chest / neck ruff: Creamy white fur
        paint.color = Color.rgb(245, 240, 230)
        canvas.drawOval(RectF(w * 0.32f, h * 0.48f, w * 0.68f, h * 0.84f), paint)

        // Tiger Head
        paint.color = Color.rgb(235, 120, 20)
        canvas.drawCircle(w * 0.5f, h * 0.42f, w * 0.22f, paint)

        // Ears
        paint.color = Color.rgb(30, 30, 30)
        canvas.drawCircle(w * 0.34f, h * 0.25f, w * 0.065f, paint)
        canvas.drawCircle(w * 0.66f, h * 0.25f, w * 0.065f, paint)
        paint.color = Color.rgb(250, 245, 240)
        canvas.drawCircle(w * 0.34f, h * 0.25f, w * 0.035f, paint)
        canvas.drawCircle(w * 0.66f, h * 0.25f, w * 0.035f, paint)

        // White muzzle & cheek patches
        paint.color = Color.rgb(248, 245, 238)
        canvas.drawOval(RectF(w * 0.36f, h * 0.44f, w * 0.64f, h * 0.58f), paint)
        canvas.drawOval(RectF(w * 0.30f, h * 0.38f, w * 0.40f, h * 0.50f), paint)
        canvas.drawOval(RectF(w * 0.60f, h * 0.38f, w * 0.70f, h * 0.50f), paint)

        // Black stripes on head & forehead
        paint.color = Color.rgb(25, 20, 20)
        paint.strokeWidth = w * 0.016f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        // Forehead stripes
        canvas.drawLine(w * 0.5f, h * 0.26f, w * 0.5f, h * 0.35f, paint)
        canvas.drawLine(w * 0.44f, h * 0.29f, w * 0.48f, h * 0.36f, paint)
        canvas.drawLine(w * 0.56f, h * 0.29f, w * 0.52f, h * 0.36f, paint)

        // Cheek stripes
        canvas.drawLine(w * 0.32f, h * 0.36f, w * 0.38f, h * 0.42f, paint)
        canvas.drawLine(w * 0.30f, h * 0.42f, w * 0.36f, h * 0.46f, paint)
        canvas.drawLine(w * 0.68f, h * 0.36f, w * 0.62f, h * 0.42f, paint)
        canvas.drawLine(w * 0.70f, h * 0.42f, w * 0.64f, h * 0.46f, paint)

        // Body stripes
        canvas.drawLine(w * 0.24f, h * 0.52f, w * 0.32f, h * 0.70f, paint)
        canvas.drawLine(w * 0.76f, h * 0.52f, w * 0.68f, h * 0.70f, paint)
        paint.style = Paint.Style.FILL

        // Amber Predator Eyes
        paint.color = Color.rgb(240, 185, 45)
        canvas.drawCircle(w * 0.42f, h * 0.38f, w * 0.032f, paint)
        canvas.drawCircle(w * 0.58f, h * 0.38f, w * 0.032f, paint)
        paint.color = Color.rgb(15, 15, 15)
        canvas.drawCircle(w * 0.42f, h * 0.38f, w * 0.016f, paint)
        canvas.drawCircle(w * 0.58f, h * 0.38f, w * 0.016f, paint)
        // Eye catchlights
        paint.color = Color.WHITE
        canvas.drawCircle(w * 0.41f, h * 0.37f, w * 0.007f, paint)
        canvas.drawCircle(w * 0.57f, h * 0.37f, w * 0.007f, paint)

        // Nose
        paint.color = Color.rgb(230, 130, 140)
        val nosePath = android.graphics.Path().apply {
            moveTo(w * 0.47f, h * 0.47f)
            lineTo(w * 0.53f, h * 0.47f)
            lineTo(w * 0.5f, h * 0.51f)
            close()
        }
        canvas.drawPath(nosePath, paint)

        // Watermark tag
        drawHeaderBadge(canvas, w, h, "ROYAL BENGAL TIGER", "Panthera tigris tigris • Endangered")
    }

    private fun drawPeafowl(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Deep twilight jungle background
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(10, 30, 45), Color.rgb(15, 55, 65), Color.rgb(10, 35, 30)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Magnificent fanned peacock tail feathers
        val featherColors = intArrayOf(
            Color.rgb(16, 185, 129), // Emerald
            Color.rgb(14, 165, 233), // Sky blue
            Color.rgb(245, 158, 11), // Gold amber
            Color.rgb(30, 64, 175)   // Deep royal blue
        )

        for (angle in -75..75 step 12) {
            val rad = Math.toRadians(angle.toDouble())
            val fx = w * 0.5f + (w * 0.42f * Math.sin(rad)).toFloat()
            val fy = h * 0.70f - (h * 0.52f * Math.cos(rad)).toFloat()

            // Feather shaft
            paint.color = Color.argb(140, 50, 160, 110)
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(w * 0.5f, h * 0.70f, fx, fy, paint)
            paint.style = Paint.Style.FILL

            // Ocellus (Peacock eye)
            paint.color = featherColors[0]
            canvas.drawCircle(fx, fy, w * 0.045f, paint)
            paint.color = featherColors[2]
            canvas.drawCircle(fx, fy, w * 0.032f, paint)
            paint.color = featherColors[3]
            canvas.drawCircle(fx, fy, w * 0.020f, paint)
            paint.color = Color.rgb(6, 182, 212)
            canvas.drawCircle(fx, fy, w * 0.009f, paint)
        }

        // Body: Iridescent royal blue / turquoise
        paint.color = Color.rgb(14, 85, 185)
        canvas.drawOval(RectF(w * 0.40f, h * 0.42f, w * 0.60f, h * 0.78f), paint)

        // Elegant neck & head
        paint.color = Color.rgb(2, 132, 199)
        canvas.drawOval(RectF(w * 0.46f, h * 0.22f, w * 0.54f, h * 0.50f), paint)
        canvas.drawCircle(w * 0.50f, h * 0.22f, w * 0.05f, paint)

        // Crest (Head fan)
        paint.color = Color.rgb(6, 182, 212)
        paint.strokeWidth = 3f
        for (offset in listOf(-16, -8, 0, 8, 16)) {
            canvas.drawLine(w * 0.5f, h * 0.18f, w * 0.5f + offset, h * 0.11f, paint)
            canvas.drawCircle(w * 0.5f + offset, h * 0.11f, 6f, paint)
        }

        // Beak & Eyes
        paint.color = Color.rgb(220, 200, 170)
        val beak = android.graphics.Path().apply {
            moveTo(w * 0.53f, h * 0.21f)
            lineTo(w * 0.60f, h * 0.23f)
            lineTo(w * 0.53f, h * 0.24f)
            close()
        }
        canvas.drawPath(beak, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(w * 0.49f, h * 0.21f, w * 0.015f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(w * 0.49f, h * 0.21f, w * 0.008f, paint)

        drawHeaderBadge(canvas, w, h, "INDIAN PEAFOWL", "Pavo cristatus • National Bird of India")
    }

    private fun drawBanyan(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Warm dawn forest atmosphere
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(250, 235, 215), Color.rgb(220, 240, 225), Color.rgb(35, 80, 45)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Sprawling green canopy
        val canopyColors = listOf(
            Color.rgb(20, 90, 45),
            Color.rgb(35, 120, 60),
            Color.rgb(15, 70, 35),
            Color.rgb(60, 150, 75)
        )
        for (i in 0..12) {
            val cx = w * (0.12f + (i % 5) * 0.18f + (i / 5) * 0.05f)
            val cy = h * (0.15f + (i / 4) * 0.12f)
            val r = w * (0.12f + (i % 3) * 0.04f)
            paint.color = canopyColors[i % canopyColors.size]
            canvas.drawCircle(cx, cy, r, paint)
        }

        // Mighty Trunk & massive limbs
        paint.color = Color.rgb(90, 60, 40)
        paint.strokeWidth = w * 0.08f
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE
        canvas.drawLine(w * 0.5f, h * 0.85f, w * 0.5f, h * 0.45f, paint)
        canvas.drawLine(w * 0.5f, h * 0.55f, w * 0.28f, h * 0.35f, paint)
        canvas.drawLine(w * 0.5f, h * 0.55f, w * 0.72f, h * 0.35f, paint)

        // Aerial Prop Roots cascading down to the earth
        paint.color = Color.rgb(120, 85, 55)
        paint.strokeWidth = w * 0.015f
        for (rx in listOf(0.24f, 0.32f, 0.38f, 0.44f, 0.56f, 0.62f, 0.68f, 0.76f)) {
            canvas.drawLine(w * rx, h * 0.42f, w * (rx + 0.01f), h * 0.88f, paint)
        }

        // Ground & grass cover
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(30, 75, 35)
        canvas.drawRect(0f, h * 0.85f, w.toFloat(), h.toFloat(), paint)

        drawHeaderBadge(canvas, w, h, "SACRED BANYAN TREE", "Ficus benghalensis • Keystone Canopy Species")
    }

    private fun drawPurpleFrog(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Moist Western Ghats rainforest floor with leaf litter & moss
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(25, 35, 30), Color.rgb(50, 40, 30), Color.rgb(20, 25, 20)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Subterranean burrows and rainforest pebbles
        paint.color = Color.argb(140, 70, 60, 50)
        canvas.drawCircle(w * 0.22f, h * 0.75f, w * 0.12f, paint)
        canvas.drawCircle(w * 0.80f, h * 0.70f, w * 0.15f, paint)

        // Glossy dark purple / plum body
        paint.color = Color.rgb(95, 35, 95)
        canvas.drawOval(RectF(w * 0.24f, h * 0.35f, w * 0.76f, h * 0.80f), paint)

        // Powerful digging hind limbs
        paint.color = Color.rgb(80, 28, 80)
        canvas.drawOval(RectF(w * 0.14f, h * 0.50f, w * 0.34f, h * 0.82f), paint)
        canvas.drawOval(RectF(w * 0.66f, h * 0.50f, w * 0.86f, h * 0.82f), paint)

        // Unique pointed white/pinkish snout
        paint.color = Color.rgb(130, 60, 115)
        canvas.drawCircle(w * 0.5f, h * 0.38f, w * 0.14f, paint)
        paint.color = Color.rgb(240, 190, 200)
        val snout = android.graphics.Path().apply {
            moveTo(w * 0.46f, h * 0.32f)
            lineTo(w * 0.54f, h * 0.32f)
            lineTo(w * 0.50f, h * 0.24f)
            close()
        }
        canvas.drawPath(snout, paint)

        // Small beady eyes
        paint.color = Color.rgb(250, 240, 150)
        canvas.drawCircle(w * 0.44f, h * 0.33f, w * 0.022f, paint)
        canvas.drawCircle(w * 0.56f, h * 0.33f, w * 0.022f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(w * 0.44f, h * 0.33f, w * 0.012f, paint)
        canvas.drawCircle(w * 0.56f, h * 0.33f, w * 0.012f, paint)

        drawHeaderBadge(canvas, w, h, "PURPLE FROG", "Nasikabatrachus sahyadrensis • Living Fossil (EDGE)")
    }

    private fun drawSeaTurtle(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Tropical ocean azure to deep blue gradient
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(10, 150, 180), Color.rgb(5, 90, 130), Color.rgb(2, 40, 70)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Water caustics & sun rays
        paint.color = Color.argb(40, 255, 255, 255)
        for (i in 0..4) {
            val ray = android.graphics.Path().apply {
                moveTo(w * (0.1f + i * 0.2f), 0f)
                lineTo(w * (0.25f + i * 0.2f), 0f)
                lineTo(w * (0.15f + i * 0.2f), h.toFloat())
                close()
            }
            canvas.drawPath(ray, paint)
        }

        // Heart-shaped Olive Green Carapace (Shell)
        paint.color = Color.rgb(65, 105, 60)
        canvas.drawOval(RectF(w * 0.30f, h * 0.28f, w * 0.70f, h * 0.76f), paint)

        // Carapace scutes / shell patterns
        paint.color = Color.rgb(90, 130, 80)
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawOval(RectF(w * 0.36f, h * 0.35f, w * 0.64f, h * 0.68f), paint)
        canvas.drawLine(w * 0.5f, h * 0.28f, w * 0.5f, h * 0.76f, paint)
        paint.style = Paint.Style.FILL

        // Front Flippers (Wings of the ocean)
        paint.color = Color.rgb(80, 120, 85)
        val leftFlipper = android.graphics.Path().apply {
            moveTo(w * 0.34f, h * 0.38f)
            cubicTo(w * 0.15f, h * 0.25f, w * 0.05f, h * 0.40f, w * 0.12f, h * 0.55f)
            cubicTo(w * 0.22f, h * 0.55f, w * 0.30f, h * 0.48f, w * 0.34f, h * 0.45f)
            close()
        }
        canvas.drawPath(leftFlipper, paint)

        val rightFlipper = android.graphics.Path().apply {
            moveTo(w * 0.66f, h * 0.38f)
            cubicTo(w * 0.85f, h * 0.25f, w * 0.95f, h * 0.40f, w * 0.88f, h * 0.55f)
            cubicTo(w * 0.78f, h * 0.55f, w * 0.70f, h * 0.48f, w * 0.66f, h * 0.45f)
            close()
        }
        canvas.drawPath(rightFlipper, paint)

        // Head & Neck
        paint.color = Color.rgb(95, 135, 90)
        canvas.drawCircle(w * 0.50f, h * 0.20f, w * 0.07f, paint)

        drawHeaderBadge(canvas, w, h, "OLIVE RIDLEY SEA TURTLE", "Lepidochelys olivacea • Vulnerable (Arribada Nester)")
    }

    private fun drawAsiaticLion(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Gir dry deciduous teak savanna sunset
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(220, 110, 45), Color.rgb(180, 90, 40), Color.rgb(75, 45, 20)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Dark tawny mane
        paint.color = Color.rgb(90, 50, 25)
        canvas.drawCircle(w * 0.50f, h * 0.42f, w * 0.26f, paint)

        // Lion face
        paint.color = Color.rgb(225, 175, 100)
        canvas.drawCircle(w * 0.50f, h * 0.42f, w * 0.16f, paint)

        // Muzzle & whiskers
        paint.color = Color.rgb(245, 235, 210)
        canvas.drawOval(RectF(w * 0.40f, h * 0.42f, w * 0.60f, h * 0.56f), paint)

        // Nose
        paint.color = Color.rgb(40, 25, 20)
        canvas.drawOval(RectF(w * 0.47f, h * 0.44f, w * 0.53f, h * 0.49f), paint)

        // Piercing golden eyes
        paint.color = Color.rgb(245, 190, 50)
        canvas.drawCircle(w * 0.44f, h * 0.38f, w * 0.024f, paint)
        canvas.drawCircle(w * 0.56f, h * 0.38f, w * 0.024f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(w * 0.44f, h * 0.38f, w * 0.012f, paint)
        canvas.drawCircle(w * 0.56f, h * 0.38f, w * 0.012f, paint)

        drawHeaderBadge(canvas, w, h, "ASIATIC LION", "Panthera leo persica • Endangered (Gir Sanctuary)")
    }

    private fun drawRhinoceros(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Kaziranga Brahmaputra floodplain morning mist
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(130, 160, 140), Color.rgb(70, 105, 80), Color.rgb(40, 65, 45)),
            floatArrayOf(0f, 0.4f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Elephant grass
        paint.color = Color.argb(120, 140, 180, 80)
        paint.strokeWidth = 4f
        for (i in 0..25) {
            val gx = w * (i / 25f)
            canvas.drawLine(gx, h * 0.95f, gx + (i % 3 - 1) * 20, h * 0.65f, paint)
        }

        // Armored slate-grey body with tubercular skin folds
        paint.color = Color.rgb(105, 115, 120)
        canvas.drawOval(RectF(w * 0.20f, h * 0.32f, w * 0.80f, h * 0.85f), paint)

        // Head
        paint.color = Color.rgb(115, 125, 130)
        canvas.drawOval(RectF(w * 0.12f, h * 0.38f, w * 0.45f, h * 0.72f), paint)

        // Iconic Single Horn
        paint.color = Color.rgb(45, 40, 40)
        val horn = android.graphics.Path().apply {
            moveTo(w * 0.14f, h * 0.48f)
            lineTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.16f, h * 0.28f)
            close()
        }
        canvas.drawPath(horn, paint)

        drawHeaderBadge(canvas, w, h, "ONE-HORNED RHINOCEROS", "Rhinoceros unicornis • Vulnerable (Kaziranga)")
    }

    private fun drawSnowLeopard(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Himalayan peaks & cold alpine twilight
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(180, 205, 225), Color.rgb(120, 145, 170), Color.rgb(55, 75, 95)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Snow-capped peak silhouettes
        paint.color = Color.argb(160, 240, 245, 255)
        val peaks = android.graphics.Path().apply {
            moveTo(0f, h * 0.55f)
            lineTo(w * 0.25f, h * 0.25f)
            lineTo(w * 0.50f, h * 0.45f)
            lineTo(w * 0.75f, h * 0.20f)
            lineTo(w.toFloat(), h * 0.50f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(peaks, paint)

        // Ghost of the mountains: Thick smoky grey coat with charcoal rosettes
        paint.color = Color.rgb(215, 220, 225)
        canvas.drawOval(RectF(w * 0.26f, h * 0.38f, w * 0.74f, h * 0.88f), paint)
        canvas.drawCircle(w * 0.50f, h * 0.42f, w * 0.18f, paint)

        // Rosette spots
        paint.color = Color.rgb(75, 80, 85)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        for (offset in listOf(-60, -30, 0, 30, 60)) {
            canvas.drawCircle(w * 0.5f + offset, h * 0.65f, 16f, paint)
        }
        paint.style = Paint.Style.FILL

        // Pale jade-green eyes
        paint.color = Color.rgb(160, 210, 185)
        canvas.drawCircle(w * 0.43f, h * 0.38f, w * 0.026f, paint)
        canvas.drawCircle(w * 0.57f, h * 0.38f, w * 0.026f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(w * 0.43f, h * 0.38f, w * 0.013f, paint)
        canvas.drawCircle(w * 0.57f, h * 0.38f, w * 0.013f, paint)

        drawHeaderBadge(canvas, w, h, "SNOW LEOPARD", "Panthera uncia • Vulnerable (Ghost of the Mountains)")
    }

    private fun drawNeemTree(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(240, 245, 235), Color.rgb(180, 220, 190), Color.rgb(50, 95, 60)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Lush green neem foliage
        paint.color = Color.rgb(40, 140, 65)
        canvas.drawCircle(w * 0.50f, h * 0.35f, w * 0.28f, paint)
        paint.color = Color.rgb(65, 175, 85)
        canvas.drawCircle(w * 0.38f, h * 0.32f, w * 0.18f, paint)
        canvas.drawCircle(w * 0.62f, h * 0.32f, w * 0.18f, paint)

        // Trunk
        paint.color = Color.rgb(95, 65, 40)
        paint.strokeWidth = w * 0.05f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(w * 0.5f, h * 0.90f, w * 0.5f, h * 0.45f, paint)

        drawHeaderBadge(canvas, w, h, "NEEM TREE", "Azadirachta indica • Nature's Living Pharmacy")
    }

    private fun drawCobra(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(35, 45, 40), Color.rgb(70, 60, 45), Color.rgb(25, 20, 15)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Upright hood & coiled body
        paint.color = Color.rgb(45, 40, 35)
        canvas.drawOval(RectF(w * 0.32f, h * 0.25f, w * 0.68f, h * 0.52f), paint)
        canvas.drawRect(w * 0.44f, h * 0.45f, w * 0.56f, h * 0.85f, paint)

        // Spectacle Mark on rear of hood
        paint.color = Color.rgb(235, 225, 195)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawCircle(w * 0.44f, h * 0.36f, w * 0.04f, paint)
        canvas.drawCircle(w * 0.56f, h * 0.36f, w * 0.04f, paint)
        canvas.drawLine(w * 0.48f, h * 0.36f, w * 0.52f, h * 0.36f, paint)
        paint.style = Paint.Style.FILL

        drawHeaderBadge(canvas, w, h, "SPECTACLED COBRA", "Naja naja • Keystone Rodent Predator")
    }

    private fun drawGharial(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(40, 100, 110), Color.rgb(75, 120, 110), Color.rgb(25, 55, 50)),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Slender olive body & ultra-long narrow snout
        paint.color = Color.rgb(65, 90, 75)
        canvas.drawOval(RectF(w * 0.25f, h * 0.42f, w * 0.75f, h * 0.75f), paint)
        canvas.drawRect(w * 0.12f, h * 0.46f, w * 0.35f, h * 0.54f, paint)

        // Ghara (bulbous nasal growth on mature males)
        paint.color = Color.rgb(45, 60, 50)
        canvas.drawCircle(w * 0.13f, h * 0.50f, w * 0.035f, paint)

        drawHeaderBadge(canvas, w, h, "GHARIAL CROCODILE", "Gavialis gangeticus • Critically Endangered")
    }

    private fun drawGenericNature(canvas: Canvas, w: Int, h: Int, title: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bgGrad = LinearGradient(0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.rgb(18, 55, 35), Color.rgb(30, 85, 50), Color.rgb(12, 35, 20)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Forest silhouette
        paint.color = Color.argb(120, 10, 35, 18)
        for (i in 0..8) {
            val cx = w * (i / 8f)
            canvas.drawCircle(cx, h * 0.6f, w * 0.15f, paint)
        }

        drawHeaderBadge(canvas, w, h, title.uppercase(), "BioGuard Cataloged Species • Indian Subcontinent")
    }

    private fun drawHeaderBadge(canvas: Canvas, w: Int, h: Int, title: String, subtitle: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Bottom gradient overlay for legible typography
        val textGrad = LinearGradient(0f, h * 0.65f, 0f, h.toFloat(),
            intArrayOf(Color.TRANSPARENT, Color.argb(210, 5, 15, 10)),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        paint.shader = textGrad
        canvas.drawRect(0f, h * 0.60f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Title
        paint.color = Color.WHITE
        paint.textSize = h * 0.058f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.setShadowLayer(8f, 0f, 2f, Color.BLACK)
        canvas.drawText(title, w * 0.05f, h * 0.86f, paint)

        // Subtitle
        paint.color = Color.rgb(167, 243, 208) // MintLight
        paint.textSize = h * 0.038f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(subtitle, w * 0.05f, h * 0.93f, paint)

        // Top tag pill
        paint.color = Color.argb(180, 10, 30, 20)
        canvas.drawRoundRect(RectF(w * 0.05f, h * 0.06f, w * 0.42f, h * 0.15f), 18f, 18f, paint)
        paint.color = Color.rgb(52, 211, 153)
        paint.textSize = h * 0.034f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🌿 BIOGUARD FIELD SPECIMEN", w * 0.07f, h * 0.12f, paint)
    }
}
