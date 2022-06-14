package net.nitakan.githubsearcher.model.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import net.nitakan.githubsearcher.model.entities.GitHubRepositoriesResult
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.model.entities.GitHubViewSubscription
import net.nitakan.githubsearcher.model.entities.SearchRepositoriesCondition

/**
 * GitHubのリポジトリに関する状態を取得および変更する。
 */
interface GitHubRepoRepository {

    /**
     * 内容が更新されたリポジトリ情報
     *
     * 他画面などで更新・取得されたリポジトリ情報を同期させるために利用する。
     * このFlowを購読し、applyRepositoryで反映させる。
     *
     * @see net.nitakan.githubsearcher.ui.extensions.applyRepository
     */
    val updatedRepositories: Flow<GitHubRepository>

    /**
     * GitHubリポジトリを検索する。
     *
     * @param condition 検索条件
     * @param coroutineScope 実行するCoroutineScope
     * @return 検索結果のリポジトリ一覧およびページネーション情報（next関数を含む）
     */
    fun search(condition: SearchRepositoriesCondition, coroutineScope: CoroutineScope): Flow<Result<GitHubRepositoriesResult>>

    /**
     * リポジトリ情報を取得する。
     *
     * @param ownerName リポジトリオーナーのログイン名
     * @param repositoryName リポジトリ名
     * @return 取得結果
     */
    suspend fun get(ownerName: String?, repositoryName: String?): Result<GitHubRepository>

    /**
     * Stargazer状態を更新する。
     *
     * @param repository 対象のリポジトリ
     * @param star 変更する状態
     */
    suspend fun setStar(repository: GitHubRepository, star: Boolean): Result<Boolean>

    /**
     * 通知購読状態を更新する。
     *
     * @param repository 対象のリポジトリ
     * @param subscription 更新する状態
     */
    suspend fun setSubscription(repository: GitHubRepository, subscription: GitHubViewSubscription): Result<GitHubViewSubscription>
}