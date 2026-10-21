package com.resonance.audio.data

import com.resonance.audio.core.model.PodcastEpisode
import com.resonance.audio.core.model.Station
import com.resonance.audio.core.model.StreamTrack

/**
 * Curated open-web catalog (Creative Commons radio, CC track mirrors, RSS shows).
 */
object StreamCatalog {

    val stations = listOf(
        Station(
            id = "st-lofi",
            name = "Lo-Fi Midnight Chillout",
            provider = "SomaFM Relay",
            subtitle = "24/7 Lossless 48kHz Stream • Tokyo Node",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
            bitrateLabel = "128 kbps",
            listeners = "18.4k",
            formatLabel = "MP3 / Web",
        ),
        Station(
            id = "st-synth",
            name = "Synthwave Odyssey",
            provider = "SomaFM Relay",
            subtitle = "DEF CON Radio / Retro Synth",
            streamUrl = "https://ice1.somafm.com/defcon-128-mp3",
            bitrateLabel = "320 kbps",
            listeners = "4.2k",
            formatLabel = "MP3 / Web",
        ),
        Station(
            id = "st-indie",
            name = "Indie Rock Pulse",
            provider = "KEXP Open Stream",
            subtitle = "Seattle Indie / Live DJ Rotation",
            streamUrl = "https://kexp.streamguys1.com/kexp160.aac",
            bitrateLabel = "160 kbps",
            listeners = "9.8k",
            formatLabel = "AAC / Web",
        ),
        Station(
            id = "st-ambient",
            name = "Deep Space One",
            provider = "SomaFM Relay",
            subtitle = "Ambient Drones For Sleeping",
            streamUrl = "https://ice1.somafm.com/dronezone-128-mp3",
            bitrateLabel = "128 kbps",
            listeners = "7.7k",
            formatLabel = "MP3 / Web",
        ),
    )

    val feature = stations.first()

    val secondary = listOf(stations[1], stations[2])

    val downloads = listOf(
        StreamTrack(
            id = "dl-1",
            title = "Ethereal Quantum Waves",
            author = "Astrosphere",
            source = "Free Music Archive",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            formatLine = "MP3 • 320 kbps",
            sizeLabel = "38.4 MB",
            license = "CC-BY",
        ),
        StreamTrack(
            id = "dl-2",
            title = "Subterranean Echoes",
            author = "Kavinsky Drift",
            source = "Jamendo Community",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            formatLine = "OGG • 320 kbps",
            sizeLabel = "12.2 MB",
            license = "CC-0",
        ),
        StreamTrack(
            id = "dl-3",
            title = "Velvet Night Reflections",
            author = "Solitude Project",
            source = "LibreSound",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            formatLine = "MP3 • 256 kbps",
            sizeLabel = "21.4 MB",
            license = "CC-BY-SA",
        ),
        StreamTrack(
            id = "dl-4",
            title = "Neon Monolith Drift",
            author = "Vector Field",
            source = "Free Music Archive",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            formatLine = "MP3 • 320 kbps",
            sizeLabel = "29.8 MB",
            license = "CC-BY",
        ),
    )

    val podcasts = listOf(
        PodcastEpisode(
            id = "pc-1",
            show = "Open Source Sound Lab",
            title = "Real-time DSP in Rust",
            episodeLine = "Ep. 42 • Real-time DSP in Rust",
            endLine = "58 min • RSS Synced",
            tag = "Tech & Code",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
        ),
        PodcastEpisode(
            id = "pc-2",
            show = "Analog Tape Mastery",
            title = "Mastering for Vinyl",
            episodeLine = "Ep. 18 • Mastering for Vinyl",
            endLine = "44 min • RSS Synced",
            tag = "Acoustics",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
        ),
        PodcastEpisode(
            id = "pc-3",
            show = "Signal Chain Quarterly",
            title = "Convolution Reverb Deep Dive",
            episodeLine = "Ep. 12 • Convolution Reverb",
            endLine = "51 min • RSS Synced",
            tag = "DSP",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
        ),
    )

    fun search(query: String): List<Any> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val hits = ArrayList<Any>()
        for (s in stations) {
            if ("${s.name} ${s.provider}".lowercase().contains(q)) hits.add(s)
        }
        for (t in downloads) {
            if ("${t.title} ${t.author} ${t.source}".lowercase().contains(q)) hits.add(t)
        }
        for (p in podcasts) {
            if ("${p.show} ${p.title} ${p.tag}".lowercase().contains(q)) hits.add(p)
        }
        return hits
    }
}
