package net.nitakan.githubsearcher.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Star
import net.nitakan.githubsearcher.R
import net.nitakan.githubsearcher.extensions.toStringWithSiUnitSuffix
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.ui.extensions.fromHexString

typealias OnRepositoryItemClick = (item: GitHubRepository) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubRepositoryListItemCard(item: GitHubRepository, onItemStarClicked: OnRepositoryItemClick, onItemClicked: OnRepositoryItemClick) {
    Card(
        elevation = CardDefaults.cardElevation(),
        modifier = Modifier.fillMaxSize(),
        onClick = { onItemClicked(item) },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            GitHubRepositoryTitle(item, onClickStar = {
                onItemStarClicked(item)
            })
            Spacer(modifier = Modifier.size(8.dp))
            if (item.description != null) {
                Text(item.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.size(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    FontAwesomeIcons.Regular.Star,
                    null,
                    modifier = Modifier
                        .size(12.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(item.star.toStringWithSiUnitSuffix())
                Spacer(modifier = Modifier.size(16.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_code_fork_solid),
                    null,
                    modifier = Modifier
                        .size(12.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(item.fork.toStringWithSiUnitSuffix())
                Spacer(modifier = Modifier.size(16.dp))
                val language = item.languages.firstOrNull()
                if (language != null) {
                    val color = language.color?.let {
                        Color.fromHexString(it)
                    }?: Color.Black
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .size(12.dp)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(language.name)
                }
            }
        }
    }
}


