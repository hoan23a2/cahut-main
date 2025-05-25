class LeaderboardEntry (
    val id: String,
    val rank: Int,
    val username: String,
    val score: Int,
    val userImage: Int,
    val isCorrectForLastQuestion: Boolean? = null
)
