package com.amazon.oih.es.fba.ruleengine.util

import amazon.platform.config.AppConfig
import com.google.inject.AbstractModule

internal class AppConfigTestInitializer : AbstractModule() {

    companion object {
        private val APP_NAME = "EsFBARuleEngine"
        private val APP_GROUP = "OIH"
        private val REALM = "FEAmazon"
        private val DOMAIN = "test"
        private val ROOT = "configuration/"
        private val ARGS = arrayOf("--realm", REALM, "--domain", DOMAIN, "--root", ROOT)

        fun initialize() {
            if (AppConfig.isInitialized()) {
                AppConfig.destroy()
            }
            AppConfig.initialize(APP_NAME, APP_GROUP, ARGS)
        }
    }

    override fun configure() {
        initialize()
    }

}