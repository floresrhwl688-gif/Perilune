package com.norva.selkit.perilune.ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.norva.selkit.perilune.game.Descent
import com.norva.selkit.perilune.game.RockKind
import io.github.sceneview.SceneScope
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CameraNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

private const val GROUND_SPAN = 7
private const val GROUND_STEP = 34f
private const val TILE_SCALE = 34f
private const val LANDER_SCALE = 4.4f
private const val LANDER_LIFT = 1.91f
private const val PLUME_SCALE = 5.2f
private const val BOULDER_SCALE = 7.4f
private const val BEACON_SCALE = 6f
private const val POD_SCALE = 3.4f
private const val SPIRE_NODES = 12
private const val BOULDER_NODES = 14
private const val BEACON_NODES = 4
private const val POD_NODES = 4
private const val MAST_NODES = 5

private class Rig {
    val ground = ArrayList<ModelNode>()
    val pad = ArrayList<ModelNode>()
    val lander = ArrayList<ModelNode>()
    val plume = ArrayList<ModelNode>()
    val spires = ArrayList<ModelNode>()
    val masts = ArrayList<ModelNode>()
    val boulders = ArrayList<ModelNode>()
    val beacons = ArrayList<ModelNode>()
    val pods = ArrayList<ModelNode>()
    var last = 0L
    var wobble = 0f
    var camX = 0f
    var camY = 0f
    var camZ = 0f
    var seeded = false
}

@Composable
fun DescentScene(world: Descent, modifier: Modifier = Modifier, onFrameStep: (Float) -> Unit) {
    val filament = rememberEngine(engineCreator = { egl ->
        Engine.Builder()
            .sharedContext(egl)
            .feature("backend.disable_parallel_shader_compile", true)
            .build()
    })
    val modelLoader = rememberModelLoader(filament)
    val rig = remember { Rig() }
    val padScale = remember(world.site.index) { world.site.padRadius * 2.05f }

    val camera = rememberCameraNode(filament)
    val mainLight = rememberMainLightNode(filament) { intensity = 102_000f }
    val bloomOff = remember { View.BloomOptions().apply { enabled = false } }
    val dynResOff = remember { View.DynamicResolutionOptions().apply { enabled = false } }
    val view = rememberView(filament).apply {
        isPostProcessingEnabled = false
        bloomOptions = bloomOff
        dynamicResolutionOptions = dynResOff
        setShadowingEnabled(false)
        setScreenSpaceRefractionEnabled(false)
    }

    SceneView(
        modifier = modifier,
        engine = filament,
        modelLoader = modelLoader,
        view = view,
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface,
        cameraNode = camera,
        mainLightNode = mainLight,
        onFrame = { nanos ->
            view.isPostProcessingEnabled = false
            view.bloomOptions = bloomOff
            view.dynamicResolutionOptions = dynResOff
            view.setShadowingEnabled(false)
            view.setScreenSpaceRefractionEnabled(false)

            val raw = if (rig.last == 0L) 0f else (nanos - rig.last) / 1_000_000_000f
            rig.last = nanos
            val dt = raw.coerceIn(0f, 0.05f)
            rig.wobble += dt * 24f
            onFrameStep(dt)
            if (!rig.seeded) {
                rig.seeded = true
                seedWorld(rig, world, padScale)
                snapCamera(rig, world)
            }
            placeLander(rig, world)
            placePods(rig, world)
            trackCamera(rig, world, camera, dt)
        },
        content = {
            fill(modelLoader, "models/tile.glb", GROUND_SPAN * GROUND_SPAN, rig.ground, TILE_SCALE)
            fill(modelLoader, "models/pad.glb", 2, rig.pad, padScale)
            fill(modelLoader, "models/lander.glb", 1, rig.lander, LANDER_SCALE)
            fill(modelLoader, "models/plume.glb", 1, rig.plume, PLUME_SCALE)
            fill(modelLoader, "models/spire.glb", SPIRE_NODES, rig.spires, 1f)
            fill(modelLoader, "models/mast.glb", MAST_NODES, rig.masts, 1f)
            fill(modelLoader, "models/boulder.glb", BOULDER_NODES, rig.boulders, BOULDER_SCALE)
            fill(modelLoader, "models/beacon.glb", BEACON_NODES, rig.beacons, BEACON_SCALE)
            fill(modelLoader, "models/fuelpod.glb", POD_NODES, rig.pods, POD_SCALE)
        }
    )
}

@Composable
private fun SceneScope.fill(
    modelLoader: ModelLoader,
    path: String,
    count: Int,
    out: ArrayList<ModelNode>,
    scaleToUnits: Float
) {
    val instances: List<ModelInstance> = remember(path) { modelLoader.createInstancedModel(path, count) }
    instances.forEach { instance ->
        ModelNode(
            modelInstance = instance,
            scaleToUnits = scaleToUnits,
            isVisible = false,
            apply = { out.add(this) }
        )
    }
}

private fun seedWorld(rig: Rig, world: Descent, padScale: Float) {
    for (i in rig.ground.indices) {
        val row = i / GROUND_SPAN
        val col = i % GROUND_SPAN
        val node = rig.ground[i]
        node.position = Position(
            (col - GROUND_SPAN / 2) * GROUND_STEP,
            -TILE_SCALE * 0.0516f,
            (row - GROUND_SPAN / 2) * GROUND_STEP
        )
        node.rotation = Rotation(-90f, 0f, 0f)
        node.isVisible = true
    }
    rig.pad.getOrNull(0)?.let { node ->
        node.position = Position(world.site.padX, Descent.PAD_TOP - padScale * 0.0865f, world.site.padZ)
        node.rotation = Rotation(0f, 0f, 0f)
        node.isVisible = true
    }
    rig.pad.getOrNull(1)?.let { node ->
        if (world.site.ferry) {
            node.position = Position(world.site.pad2X, Descent.PAD_TOP - padScale * 0.0865f, world.site.pad2Z)
            node.rotation = Rotation(0f, 0f, 0f)
            node.isVisible = true
        } else {
            node.isVisible = false
        }
    }
    var spire = 0
    var mast = 0
    var boulder = 0
    for (rock in world.rocks) {
        when (rock.kind) {
            RockKind.Spire -> {
                if (spire >= rig.spires.size) continue
                val node = rig.spires[spire++]
                node.scale = io.github.sceneview.math.Scale(rock.height / 1.902f)
                node.position = Position(rock.x, rock.height * 0.5f, rock.z)
                node.rotation = Rotation(0f, rock.spin, 0f)
                node.isVisible = true
            }
            RockKind.Mast -> {
                if (mast >= rig.masts.size) continue
                val node = rig.masts[mast++]
                node.scale = io.github.sceneview.math.Scale(rock.height / 1.9f)
                node.position = Position(rock.x, rock.height * 0.5f, rock.z)
                node.rotation = Rotation(0f, rock.spin, 0f)
                node.isVisible = true
            }
            RockKind.Boulder -> {
                if (boulder >= rig.boulders.size) continue
                val node = rig.boulders[boulder++]
                node.position = Position(rock.x, BOULDER_SCALE * 0.379f, rock.z)
                node.rotation = Rotation(0f, rock.spin, 0f)
                node.isVisible = true
            }
        }
    }
    for (i in rig.beacons.indices) {
        val node = rig.beacons[i]
        val angle = i * 1.5708f + 0.7854f
        val reach = world.site.padRadius + 5.5f
        node.position = Position(
            world.site.padX + reach * sin(angle),
            BEACON_SCALE * 0.5f,
            world.site.padZ + reach * kotlin.math.cos(angle)
        )
        node.rotation = Rotation(0f, 0f, 0f)
        node.isVisible = true
    }
}

private fun placeLander(rig: Rig, world: Descent) {
    val node = rig.lander.firstOrNull() ?: return
    node.position = Position(world.x, world.y + LANDER_LIFT, world.z)
    node.rotation = Rotation(
        Math.toDegrees(world.tiltZ.toDouble()).toFloat(),
        0f,
        -Math.toDegrees(world.tiltX.toDouble()).toFloat()
    )
    node.isVisible = true
    val flame = rig.plume.firstOrNull() ?: return
    if (world.plume <= 0.02f) {
        flame.isVisible = false
        return
    }
    val stretch = PLUME_SCALE * (0.55f + world.plume * 0.6f)
    flame.scale = io.github.sceneview.math.Scale(stretch / 1.903f)
    flame.position = Position(world.x, world.y - stretch * 0.42f, world.z)
    flame.rotation = Rotation(
        Math.toDegrees(world.tiltZ.toDouble()).toFloat(),
        0f,
        -Math.toDegrees(world.tiltX.toDouble()).toFloat()
    )
    flame.isVisible = true
}

private fun placePods(rig: Rig, world: Descent) {
    var cursor = 0
    for (pod in world.pods) {
        if (pod.taken) continue
        if (cursor >= rig.pods.size) break
        val node = rig.pods[cursor++]
        node.position = Position(pod.x, pod.y, pod.z)
        node.rotation = Rotation(0f, (world.elapsed * 62f) % 360f, 0f)
        node.isVisible = true
    }
    for (i in cursor until rig.pods.size) rig.pods[i].isVisible = false
}

private fun snapCamera(rig: Rig, world: Descent) {
    rig.camX = world.x * 0.85f
    rig.camY = world.y + cameraLift(world.y)
    rig.camZ = world.z - cameraBack(world.y)
}

private fun cameraLift(altitude: Float): Float = 7f + altitude * 0.05f

private fun cameraBack(altitude: Float): Float = 19f + altitude * 0.07f

private fun trackCamera(rig: Rig, world: Descent, camera: CameraNode, dt: Float) {
    val wantX = world.x * 0.85f
    val wantY = world.y + cameraLift(world.y)
    val wantZ = world.z - cameraBack(world.y)
    val ease = (dt * 4.5f).coerceIn(0f, 1f)
    rig.camX += (wantX - rig.camX) * ease
    rig.camY += (wantY - rig.camY) * ease
    rig.camZ += (wantZ - rig.camZ) * ease

    val jolt = if (world.shake > 0f) sin(rig.wobble) * world.shake * 0.22f else 0f
    camera.position = Position(rig.camX + jolt, rig.camY, rig.camZ)

    val aimY = world.y - (7f + world.y * 0.5f)
    val fx = world.x - rig.camX
    val fy = aimY - rig.camY
    val fz = world.z - rig.camZ
    val len = sqrt(fx * fx + fy * fy + fz * fz).coerceAtLeast(0.001f)
    val pitch = Math.toDegrees(asin((fy / len).toDouble())).toFloat()
    val yaw = Math.toDegrees(atan2(fx.toDouble(), fz.toDouble())).toFloat()
    camera.rotation = Rotation(pitch, 180f + yaw, 0f)
}

fun bearingToPad(world: Descent): Float {
    val dx = world.targetPadX() - world.x
    val dz = world.targetPadZ() - world.z
    if (hypot(dx, dz) < 0.01f) return 0f
    return Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
}
