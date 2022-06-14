package net.nitakan.githubsearcher.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.ui.extensions.starIcon

@Composable
fun GitHubRepositoryTitle(
    repository: GitHubRepository,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    titleMaxLine: Int? = 1,
    onClickStar: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)
        ) {
            Box(modifier = Modifier.padding(top = 4.dp)) {
                AsyncImage(
                    model = repository.owner.avatarUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            Spacer(Modifier.size(8.dp))
            Column {
                Text(
                    repository.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = titleMaxLine ?: Int.MAX_VALUE,
                    overflow = titleOverflow
                )
                Text(
                    repository.owner.name,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        IconButton(
            onClick = { onClickStar?.invoke() },
            enabled = onClickStar != null,
            modifier = Modifier.then(Modifier.size(24.dp))
        ) {

            Icon(
                imageVector = repository.starIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }


}