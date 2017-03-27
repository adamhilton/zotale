package com.nonnulldev.zotale.util

import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.nonnulldev.zotale.config.GameConfig.PPM

object TiledObjectUtil {
    fun parseTiledObjectLayer(world: World, objects: MapObjects) {
        objects.forEach {
            val shape: Shape
            if (it is PolylineMapObject) {
                shape = createPolyline(it)

                val body: Body
                val bodyDef = BodyDef()
                bodyDef.type = BodyDef.BodyType.StaticBody
                body = world.createBody(bodyDef)
                body.createFixture(shape, 1.0f)
                shape.dispose()
            }
        }
    }

    private fun createPolyline(polylineMapObject: PolylineMapObject): Shape {
        val vertices = polylineMapObject.polyline.transformedVertices
        val worldVertices = Array(vertices.size / 2, { Vector2() })

        for (i in 0..worldVertices.lastIndex) {
            worldVertices[i] = Vector2(
                    vertices[i * 2] / PPM,
                    vertices[i * 2 + 1] / PPM)
        }

        val chainShape = ChainShape()
        chainShape.createChain(worldVertices)
        return chainShape
    }
}