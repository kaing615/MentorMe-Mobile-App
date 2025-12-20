package com.mentorme.app.data.mock

import com.mentorme.app.ui.home.Mentor

/**
 * Mock data dùng cho màn Search (từ danh sách bạn gửi).
 * Giữ nguyên đơn vị VNĐ/giờ và các trường đúng với MentorCard hiện có.
 */
object SearchMockData {
    val mentors: List<Mentor> = listOf(
        Mentor("m1","Nguyễn Văn An","Senior Android Engineer","Google",4.9,156, listOf("Android","Kotlin","Architecture"), 900_000,  isAvailable = true),
        Mentor("m2","Trần Thị Minh","Product Manager","Meta",4.8,203, listOf("Strategy","Analytics","Leadership"), 1_100_000, isAvailable = true),
        Mentor("m3","Lê Hoàng Nam","UX/UI Designer","Apple",4.9,89,  listOf("Figma","Design Systems","User Research"), 800_000,  isAvailable = false),
        Mentor("m4","Phạm Quang Huy","Data Scientist","Grab",4.7,120, listOf("Python","ML","Data Pipeline"),         1_200_000, isAvailable = true),
        Mentor("m5","Võ Như Ý","Frontend Engineer","Shopify",4.8,98, listOf("React","TypeScript","System Design"),   950_000,  isAvailable = true),
        Mentor("m6","Đỗ Trọng Tín","DevOps Engineer","Amazon",4.6,77, listOf("AWS","Kubernetes","CI/CD"),            1_000_000, isAvailable = true),
        Mentor("m7","Ngô Bảo Châu","Backend Engineer","Netflix",4.9,145, listOf("Java","Microservices","Kafka"),     1_150_000, isAvailable = true),
        Mentor("m8","Lý Thu Trang","Product Designer","Spotify",4.7,64, listOf("UX Writing","Prototyping","Research"), 850_000, isAvailable = true),
    )
}
