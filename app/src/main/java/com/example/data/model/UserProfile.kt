package com.example.data.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val bio: String = "أوتاكو ومتابع أنمي في بلاك انمي 🔥",
    val favoriteAnime: String = "هجوم العمالقة",
    val favoriteCharacter: String = "ليفاي أكرمان",
    val favoriteAnimeList: List<String> = listOf("هجوم العمالقة", "ون بيس", "جوجوتسو كايسن", "سولو ليفلينغ"),
    val role: String = "أوتاكو مميز",
    val isAdmin: Boolean = false,
    val isModerator: Boolean = false,
    val isVerified: Boolean = false,
    val moderatorPermissions: List<String> = emptyList(),
    val isBanned: Boolean = false,
    val banReason: String = "",
    val level: Long = 1L,
    val xp: Long = 0L,
    val coins: Long = 500L,
    val gems: Long = 50L,
    val selectedFrame: String = "",
    val selectedBadge: String = "",
    val unlockedFrames: List<String> = listOf("default"),
    val unlockedBadges: List<String> = listOf("rookie"),
    val dailyRewardLastClaimTimestamp: Long = 0L,
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    val friendsCount: Long = 0L,
    val friends: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val joinedAt: Long = System.currentTimeMillis()
) {
    fun isSuperAdmin(currentUserEmail: String? = null): Boolean {
        val emailToCheck = currentUserEmail?.trim() ?: email.trim()
        return isAdmin || emailToCheck.equals("m774545471@gmail.com", ignoreCase = true)
    }

    fun hasAdminPrivileges(currentUserEmail: String? = null): Boolean {
        return isSuperAdmin(currentUserEmail) ||
                isModerator ||
                role.contains("مدير", ignoreCase = true) ||
                role.contains("admin", ignoreCase = true) ||
                role.contains("مشرف", ignoreCase = true)
    }

    fun canPerformAction(permission: String, currentUserEmail: String? = null): Boolean {
        if (isSuperAdmin(currentUserEmail)) return true
        if (isModerator && moderatorPermissions.contains(permission)) return true
        return false
    }

    val isRedBadge: Boolean
        get() = isAdmin || isModerator || email.equals("m774545471@gmail.com", ignoreCase = true)

    val isBlueBadge: Boolean
        get() = isVerified && !isRedBadge
}
