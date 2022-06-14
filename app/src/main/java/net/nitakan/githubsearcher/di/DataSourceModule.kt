package net.nitakan.githubsearcher.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.nitakan.githubsearcher.BuildConfig
import net.nitakan.githubsearcher.data.remote.GitHubApi
import net.nitakan.githubsearcher.data.remote.GitHubApiImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideGitHubApi(): GitHubApi {
        val apiKey = BuildConfig.GITHUB_PERSONAL_ACCESS_KEY
        val hostUrl = BuildConfig.GITHUB_GRAPHQL_URL
        return GitHubApiImpl(apiKey, hostUrl)
    }
}