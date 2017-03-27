package com.nonnulldev.zotale

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Logger
import com.nonnulldev.zotale.screen.SandboxScreen

class ZotaleGame : Game() {

    override fun create() {
        Gdx.app.logLevel = Logger.DEBUG
        setScreen(SandboxScreen(this))
    }

}
