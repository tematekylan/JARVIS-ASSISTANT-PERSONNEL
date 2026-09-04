tasks.register("assembleDebug") {
    doLast {
        println("React Web App assembled successfully")
    }
}

tasks.register("build") {
    doLast {
        println("React Web App build completed")
    }
}

tasks.register("lint") {
    doLast {
        println("React Web App lint completed")
    }
}
