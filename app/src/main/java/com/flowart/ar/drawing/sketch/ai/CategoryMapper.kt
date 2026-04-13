package com.flowart.ar.drawing.sketch.ai

/**
 * CategoryMapper — Mapping label AI → category trong database app
 *
 * ML Kit trả về labels tiếng Anh (VD: "Dog", "Flower", "Person")
 * App có categories: anime(1), cartoon(2), animal(3), chibi(4), flower(5)
 *
 * Class này kết nối 2 thứ đó lại:
 * - "Dog" → category 3 (animal)
 * - "Rose" → category 5 (flower)
 * - "Person" → category 1 (anime) hoặc 4 (chibi)
 */
object CategoryMapper {

    /**
     * Kết quả mapping: category ID + tên hiển thị + emoji
     */
    data class SuggestedCategory(
        val categoryId: Int,       // ID trong database (1=anime, 2=cartoon, 3=animal, 4=chibi, 5=flower)
        val displayName: String,   // Tên hiển thị cho user
        val emoji: String          // Emoji trang trí
    )

    // Bảng mapping: keyword trong label → category
    // Mỗi keyword được check bằng contains (không phân biệt hoa thường)
    private val animalKeywords = listOf(
        "dog", "cat", "bird", "fish", "animal", "pet", "puppy", "kitten",
        "turtle", "rabbit", "horse", "butterfly", "insect", "wildlife",
        "lion", "tiger", "bear", "elephant", "monkey", "dolphin", "shark",
        "snake", "frog", "duck", "chicken", "cow", "pig", "sheep", "deer"
    )

    private val flowerKeywords = listOf(
        "flower", "rose", "tulip", "sunflower", "daisy", "lily", "orchid",
        "plant", "garden", "blossom", "petal", "floral", "bouquet", "lotus"
    )

    private val personKeywords = listOf(
        "person", "people", "face", "human", "man", "woman", "boy", "girl",
        "child", "kid", "baby", "selfie", "portrait", "smile"
    )

    /**
     * Từ danh sách labels AI → tìm category phù hợp nhất trong app.
     *
     * @param labels Danh sách labels từ ImageAnalyzer
     * @return Category phù hợp nhất, hoặc null nếu không match
     */
    fun findBestCategory(labels: List<ImageAnalyzer.DetectedLabel>): SuggestedCategory? {
        // Duyệt từng label (đã sắp xếp theo confidence cao → thấp)
        for (detectedLabel in labels) {
            val labelLower = detectedLabel.label.lowercase()

            // Check animal keywords
            if (animalKeywords.any { labelLower.contains(it) }) {
                return SuggestedCategory(
                    categoryId = 3,
                    displayName = "Animal",
                    emoji = "🐾"
                )
            }

            // Check flower keywords
            if (flowerKeywords.any { labelLower.contains(it) }) {
                return SuggestedCategory(
                    categoryId = 5,
                    displayName = "Flower",
                    emoji = "🌸"
                )
            }

            // Check person keywords → gợi ý anime hoặc chibi
            if (personKeywords.any { labelLower.contains(it) }) {
                return SuggestedCategory(
                    categoryId = 1,  // anime
                    displayName = "Anime",
                    emoji = "✏️"
                )
            }
        }

        // Không match keyword nào → không gợi ý
        return null
    }

    /**
     * Lấy tất cả categories có thể match (không chỉ best).
     * Dùng nếu muốn hiển thị nhiều gợi ý.
     */
    fun findAllCategories(labels: List<ImageAnalyzer.DetectedLabel>): List<SuggestedCategory> {
        val result = mutableSetOf<Int>()  // Dùng Set để tránh trùng
        val categories = mutableListOf<SuggestedCategory>()

        for (detectedLabel in labels) {
            val labelLower = detectedLabel.label.lowercase()

            if (animalKeywords.any { labelLower.contains(it) } && 3 !in result) {
                result.add(3)
                categories.add(SuggestedCategory(3, "Animal", "🐾"))
            }

            if (flowerKeywords.any { labelLower.contains(it) } && 5 !in result) {
                result.add(5)
                categories.add(SuggestedCategory(5, "Flower", "🌸"))
            }

            if (personKeywords.any { labelLower.contains(it) } && 1 !in result) {
                result.add(1)
                categories.add(SuggestedCategory(1, "Anime", "✏️"))
            }
        }

        return categories
    }
}
