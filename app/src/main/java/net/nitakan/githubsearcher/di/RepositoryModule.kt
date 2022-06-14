package net.nitakan.githubsearcher.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import net.nitakan.githubsearcher.data.remote.GitHubApi
import net.nitakan.githubsearcher.data.repositories.GitHubRepoRepositoryImpl
import net.nitakan.githubsearcher.model.repositories.GitHubRepoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideGitHubRepoRepository(api: GitHubApi): GitHubRepoRepository {
        return GitHubRepoRepositoryImpl(api, Dispatchers.IO)
    }
}