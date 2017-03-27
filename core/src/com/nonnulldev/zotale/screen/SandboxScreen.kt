package com.nonnulldev.zotale.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.nonnulldev.zotale.ZotaleGame
import com.nonnulldev.zotale.config.GameConfig.PLAYER_SIZE
import com.nonnulldev.zotale.config.GameConfig.PPM
import com.nonnulldev.zotale.config.GameConfig.WORLD_HEIGHT
import com.nonnulldev.zotale.config.GameConfig.WORLD_WIDTH
import com.nonnulldev.zotale.util.TiledObjectUtil

class SandboxScreen(private val game: ZotaleGame) : ScreenAdapter() {

    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var box2DDebugRenderer: Box2DDebugRenderer

    private var map: TiledMap = TmxMapLoader().load("maps/sandbox_map.tmx")
    private lateinit var world: World

    private lateinit var player: Body

    override fun show() {
        camera = OrthographicCamera()
        viewport = FitViewport(WORLD_WIDTH * PPM, WORLD_HEIGHT * PPM, camera)

        world = World(Vector2(0f, -9.8f), false)
        box2DDebugRenderer = Box2DDebugRenderer()

        TiledObjectUtil.parseTiledObjectLayer(world, map.layers.get("collision-layer").objects)
        TiledObjectUtil.parseTiledObjectLayer(world, map.layers.get("platform-layer").objects)
        val spawn = (map.layers.get("player-spawn-layer").objects.first() as RectangleMapObject).rectangle
        player = createBox(spawn.x.toInt() + 1, spawn.y.toInt() + 1, (PLAYER_SIZE * PPM).toInt(), (PLAYER_SIZE * PPM).toInt(), false, true)
    }

    override fun render(delta: Float) {
        update(delta)

        Gdx.gl.glClearColor(.25f, .25f, .25f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        box2DDebugRenderer.render(world, camera.combined.scl(PPM))
    }

    fun update(delta: Float) {
        handlePlayerInput(delta)

        world.step(1 / 60f, 6, 2)
        cameraUpdate()
    }

    private fun handlePlayerInput(delta: Float) {
        val velocity = player.linearVelocity
        var xForce = 0f
        var yForce = 0f

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if(velocity.x > -5) xForce = -25f
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if(velocity.x < 5) xForce = 25f
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            xForce = 0f
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            yForce = 600f
        }

        player.applyForceToCenter(Vector2(xForce, yForce), false)
    }

    private fun cameraUpdate() {
        val position = viewport.camera.position
        position.x = viewport.camera.position.x + (player.position.x * PPM - viewport.camera.position.x) * 0.1f
        position.y = viewport.camera.position.y + (player.position.y * PPM - viewport.camera.position.y) * 0.1f
        viewport.camera.position.set(position)

        viewport.camera.update()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun hide() {
        dispose()
    }

    override fun dispose() {
        box2DDebugRenderer.dispose()
        world.dispose()
        map.dispose()
    }

    private fun createBox(x: Int, y: Int, width: Int, height: Int, isStatic: Boolean, fixedRotation: Boolean): Body {
        val body: Body
        val def = BodyDef()

        if(isStatic)
            def.type = BodyDef.BodyType.StaticBody
        else
            def.type = BodyDef.BodyType.DynamicBody

        def.position.set(x / PPM, y / PPM)
        def.linearDamping = 2f
        def.gravityScale = 1f
        def.fixedRotation = fixedRotation
        body = world.createBody(def)

        val shape = PolygonShape()
        shape.setAsBox(width / 2f / PPM, height / 2f / PPM)

        body.createFixture(shape, 1.0f)
        shape.dispose()
        return body
    }
}

