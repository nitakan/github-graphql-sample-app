@file:OptIn(ExperimentalMaterial3Api::class)

package net.nitakan.githubsearcher.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.brands.GithubSquare
import compose.icons.fontawesomeicons.regular.Star
import compose.icons.fontawesomeicons.solid.Link
import compose.icons.fontawesomeicons.solid.Star
import net.nitakan.githubsearcher.R
import net.nitakan.githubsearcher.extensions.toStringWithComma
import net.nitakan.githubsearcher.extensions.toStringWithSiUnitSuffix
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.model.exceptions.NotFoundException
import net.nitakan.githubsearcher.ui.Router
import net.nitakan.githubsearcher.ui.extensions.fromHexString
import net.nitakan.githubsearcher.ui.extensions.starIcon
import net.nitakan.githubsearcher.ui.extensions.subscriptionIcon
import net.nitakan.githubsearcher.ui.widgets.GitHubTopic
import net.nitakan.githubsearcher.ui.widgets.NotFoundRepositoryCard
import net.nitakan.githubsearcher.ui.widgets.RetryableCard

@Composable
fun RepositoryDetailScreen(
    viewModel: RepositoryDetailViewModel,
    navController: NavController,
) {
    val scrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior( scrollState ) }
    val state by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmallTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                title = {
                    Text("Repository Detail")
                },
                actions = {
                    val url = state.getOrNull()?.url
                    if (url != null) {
                        IconButton(onClick = {
                            uriHandler.openUri(url)
                        }) {
                            Icon(
                                FontAwesomeIcons.Brands.GithubSquare,
                                null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            state.onFailure { error ->
                when (error) {
                    is NotFoundException -> {
                        NotFoundRepositoryCard(modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        RetryableCard(modifier = Modifier.align(Alignment.Center)) {
                            viewModel.retry()
                        }
                    }
                }
            }.onSuccess { repository ->
                if (isLoading || repository == null) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                    return@onSuccess
                }
                GitHubRepositoryDetailContent(viewModel, repository, navController)
            }
        }
    }
}

@Composable
fun GitHubRepositoryDetailContent(
    viewModel: RepositoryDetailViewModel,
    repository: GitHubRepository,
    navController: NavController,
) {
    val columnCount = 2
    val span = GridItemSpan(2)
    val uriHandler = LocalUriHandler.current
    LazyVerticalGrid(
        GridCells.Fixed(columnCount),
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item(span = { span }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Title(viewModel, repository, navController)

                Spacer(Modifier.size(8.dp))

                if (repository.description != null) {
                    Text(repository.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.size(8.dp))
                }

                if (repository.homePageUrl != null && repository.homePageUrl.isNotBlank()) {
                    Box(Modifier.clickable {
                        uriHandler.openUri(repository.homePageUrl)
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(FontAwesomeIcons.Solid.Link, null, Modifier.size(12.dp))
                            Spacer(Modifier.size(4.dp))
                            Text(repository.homePageUrl)
                        }
                    }
                }

                Spacer(Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        FontAwesomeIcons.Regular.Star,
                        null,
                        modifier = Modifier
                            .size(12.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(repository.star.toStringWithSiUnitSuffix())
                    Spacer(modifier = Modifier.size(16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_code_fork_solid),
                        null,
                        modifier = Modifier
                            .size(12.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(repository.fork.toStringWithSiUnitSuffix())
                    Spacer(modifier = Modifier.size(16.dp))
                    val language = repository.languages.firstOrNull()
                    if (language != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                navController.navigate("${Router.SEARCH}?query=language:${language.name}")
                            }
                        ) {
                            val color = language.color?.let {
                                Color.fromHexString(it)
                            } ?: Color.Black
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
                Spacer(Modifier.size(8.dp))

                Divider(Modifier.padding(8.dp))

                if (repository.topics.isNotEmpty()) {
                    FlowRow {
                        repository.topics.forEach { topic ->
                            Box(
                                Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .clickable {
                                        navController.navigate("${Router.SEARCH}?query=topic:${topic.name}")
                                    }) {
                                GitHubTopic(title = { Text(topic.name) }, icon = {
                                    if (topic.isStared) {
                                        Icon(
                                            FontAwesomeIcons.Solid.Star,
                                            null,
                                            Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            FontAwesomeIcons.Regular.Star,
                                            null,
                                            Modifier.size(16.dp)
                                        )
                                    }
                                }, modifier = Modifier
                                    .padding()
                                )
                            }
                        }
                    }
                    Divider(Modifier.padding(8.dp))
                }
            }
        }

        val list = listOf(
            CardItem("Issue", repository.issuesCount?.toStringWithComma() ?: "0", "issues"),
            CardItem(
                "Pull Request",
                repository.pullRequestCount?.toStringWithComma() ?: "0", "pulls"
            ),
            CardItem(
                "Discussion",
                repository.discussionCount?.toStringWithComma() ?: "0",
                "discussions"
            ),
            CardItem("Watcher", repository.watchersCount?.toStringWithComma() ?: "0", "watchers"),
        )

        items(list) { item ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clickable {
                        uriHandler.openUri("${repository.url}/${item.link}")
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {

                    Text(item.title)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        item.description,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }

        if (repository.license != null) {
            item(span = { span }) {

                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            if (repository.license.url != null) {
                                uriHandler.openUri(repository.license.url)
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {

                        Text("Licence")
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            repository.license.nickname ?: repository.license.name,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        item(span = { span }) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clickable(enabled = repository.latestRelease != null) {
                        uriHandler.openUri(repository.latestRelease!!.url)
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {

                    Text("Release")
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "${repository.releaseCount?.toStringWithComma() ?: 0}",
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    if (repository.latestRelease != null) {
                        Divider(Modifier.padding(4.dp))
                        Row(Modifier.padding(4.dp)) {
                            Text(repository.latestRelease.name)
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(repository.latestRelease.publishedAt ?: "")
                        }
                    }
                }
            }
        }
    }
}

data class CardItem(val title: String, val description: String, val link: String)


@Composable
fun Title(
    viewModel: RepositoryDetailViewModel,
    repository: GitHubRepository,
    navController: NavController,
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
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Visible
                )
                Text(
                    repository.owner.name,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable {
                        navController.navigate("${Router.SEARCH}?query=user:${repository.owner.name}")
                    })
            }
        }
        IconButton(
            onClick = { viewModel.toggleStar() },
        ) {
            Icon(
                imageVector = repository.starIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.then(Modifier.size(24.dp))
            )
        }
        IconButton(
            onClick = { navController.navigate("${Router.REPOSITORY_SUBSCRIPTION}/${repository.owner.name}/${repository.name}") },
        ) {
            Icon(
                imageVector = repository.subscriptionIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.then(Modifier.size(24.dp))
            )
        }
    }
}