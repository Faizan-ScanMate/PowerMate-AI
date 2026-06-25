package com.powermate.ai.aod

enum class AodStyle(val label: String, val description: String) {
    PixelClean("Pixel Clean", "Simple clock, battery ring and charging speed"),
    MinimalNeon("Minimal Neon", "AMOLED black with cyan pulse"),
    RingMeter("Ring Meter", "Large battery ring for glanceable charging"),
    CyberPulse("Cyber Pulse", "High-energy pulse line display"),
    ClassicBattery("Classic Battery", "Classic percentage and battery icon"),
    UltraMinimal("Ultra Minimal", "Only time, percent and tiny status"),
    SpeedGlow("Speed Glow", "Charging speed focused layout"),
    TextOnly("Text Only", "Lowest visual load and clean typography")
}
