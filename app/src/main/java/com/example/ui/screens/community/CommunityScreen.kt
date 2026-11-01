package com.example.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.MintPrimary

data class CommunityPost(
    val id: String,
    val authorName: String,
    val timeAgo: String,
    val type: String, // "MILESTONE", "RESULT", "ANNOUNCEMENT"
    val title: String,
    val body: String,
    val likesCount: Int = 12
)

@Composable
fun CommunityScreen(
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit
) {
    val likedMap = remember { mutableStateMapOf<String, Boolean>() }

    val posts = listOf(
        CommunityPost(
            id = "p1",
            authorName = "WicketX Scorer",
            timeAgo = "10 mins ago",
            type = "MILESTONE",
            title = "🔥 50 Up for Rohit Sharma!",
            body = "Tremendous batting display at Gymkhana Arena! Rohit reached his half-century off just 28 balls with 4 sixes and 5 fours."
        ),
        CommunityPost(
            id = "p2",
            authorName = "Gymkhana Cricket League",
            timeAgo = "1 hour ago",
            type = "RESULT",
            title = "🏆 Match Result: Gujarat Super Titans Won",
            body = "Gujarat Super Titans successfully chased down 169 in 18.5 overs, defeating Rajasthan Strikers by 6 wickets!"
        ),
        CommunityPost(
            id = "p3",
            authorName = "Tournament Desk",
            timeAgo = "3 hours ago",
            type = "ANNOUNCEMENT",
            title = "📢 Grassroots Premier Cup 2026 Quarter-Finals",
            body = "Fixtures have been published. Matches commence this Saturday at 9:00 AM. Free live scoring on WicketX."
        ),
        CommunityPost(
            id = "p4",
            authorName = "Bumrah Cricket Academy",
            timeAgo = "5 hours ago",
            type = "MILESTONE",
            title = "🎯 5-Wicket Haul by Jasprit Bumrah",
            body = "Stunning fast bowling spell: 4.0 overs, 1 maiden, 18 runs, 5 wickets! Clean bowled three top-order batters."
        )
    )

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Cricket Community Feed",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                onSearchClick = onSearchClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
            ) {
                items(posts) { post ->
                    val isLiked = likedMap[post.id] ?: false
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("feed_post_${post.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MintPrimary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsCricket,
                                        contentDescription = null,
                                        tint = MintPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = post.authorName,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = post.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            val (badgeBg, badgeFg) = when (post.type) {
                                "MILESTONE" -> Pair(MintPrimary.copy(alpha = 0.18f), MintPrimary)
                                "RESULT" -> Pair(ChampagneHighlight.copy(alpha = 0.18f), ChampagneHighlight)
                                else -> Pair(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.8f))
                            }
                            GlassBadge(
                                text = post.type,
                                textColor = badgeFg,
                                backgroundColor = badgeBg,
                                borderColor = badgeFg.copy(alpha = 0.35f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = post.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { likedMap[post.id] = !isLiked }
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) ErrorCrimson else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = "${post.likesCount + if (isLiked) 1 else 0} Cheers",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isLiked) MintPrimary else Color.White.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
