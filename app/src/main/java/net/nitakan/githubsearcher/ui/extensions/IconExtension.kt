package net.nitakan.githubsearcher.ui.extensions

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Bell
import compose.icons.fontawesomeicons.regular.BellSlash
import compose.icons.fontawesomeicons.regular.Star
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.Star
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.model.entities.GitHubViewSubscription

fun GitHubRepository.subscriptionIcon(): ImageVector {
    return when (this.viewerSubscription) {
        GitHubViewSubscription.SUBSCRIBED -> FontAwesomeIcons.Solid.Bell
        GitHubViewSubscription.UNSUBSCRIBED -> FontAwesomeIcons.Regular.Bell
        else -> FontAwesomeIcons.Regular.BellSlash
    }
}

fun GitHubRepository.starIcon(): ImageVector {
    return if (this.viewerHasStarred) {
        FontAwesomeIcons.Solid.Star
    } else {
        FontAwesomeIcons.Regular.Star
    }
}