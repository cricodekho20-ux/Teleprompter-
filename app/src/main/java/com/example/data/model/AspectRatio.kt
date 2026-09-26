package com.example.data.model

enum class AspectRatio(
    val id: String,
    val title: String,
    val subtitle: String,
    val ratioValue: Float, // width / height
    val widthRatio: Int,
    val heightRatio: Int,
    val recommendedResolution: String
) {
    RATIO_9_16(
        id = "9:16",
        title = "9:16",
        subtitle = "Shorts / Reels / TikTok",
        ratioValue = 9f / 16f,
        widthRatio = 9,
        heightRatio = 16,
        recommendedResolution = "1080 x 1920"
    ),
    RATIO_4_5(
        id = "4:5",
        title = "4:5",
        subtitle = "Social Feed / Instagram",
        ratioValue = 4f / 5f,
        widthRatio = 4,
        heightRatio = 5,
        recommendedResolution = "1080 x 1350"
    ),
    RATIO_1_1(
        id = "1:1",
        title = "1:1",
        subtitle = "Square Feed",
        ratioValue = 1f,
        widthRatio = 1,
        heightRatio = 1,
        recommendedResolution = "1080 x 1080"
    ),
    RATIO_16_9(
        id = "16:9",
        title = "16:9",
        subtitle = "YouTube / Landscape",
        ratioValue = 16f / 9f,
        widthRatio = 16,
        heightRatio = 9,
        recommendedResolution = "1920 x 1080"
    ),
    RATIO_3_4(
        id = "3:4",
        title = "3:4",
        subtitle = "Portrait Standard",
        ratioValue = 3f / 4f,
        widthRatio = 3,
        heightRatio = 4,
        recommendedResolution = "1080 x 1440"
    );

    companion object {
        fun fromId(id: String): AspectRatio {
            return entries.find { it.id == id } ?: RATIO_9_16
        }
    }
}
