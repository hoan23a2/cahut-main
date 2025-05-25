class LeaderboardEntry (
    val id: String,
    val rank: Int,
    val username: String,
    val score: Int,
    val isCorrectForLastQuestion: Boolean? = null
)
