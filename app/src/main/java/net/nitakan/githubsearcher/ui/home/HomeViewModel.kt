package net.nitakan.githubsearcher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.nitakan.githubsearcher.model.entities.*
import net.nitakan.githubsearcher.model.repositories.GitHubRepoRepository
import net.nitakan.githubsearcher.ui.extensions.applyRepository
import net.nitakan.githubsearcher.ui.widgets.GitHubInfiniteScrollable
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GitHubRepoRepository,
) : ViewModel(), GitHubInfiniteScrollable {

    private val _result = MutableStateFlow(Result.success(GitHubRepositoriesResult.empty))
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val result: StateFlow<Result<GitHubRepositoriesResult>> = _result
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing

    init {
        initialize()

        // Repository状態をリアルタイムに反映する
        viewModelScope.launch {
            repository.updatedRepositories.collect(_result::applyRepository)
        }
    }

    fun initialize() {
        viewModelScope.launch {
            _result.emit(Result.success(GitHubRepositoriesResult.empty))
            _isRefreshing.emit(true)
            repository.search(
                SearchRepositoriesCondition(
                    "Android",
                    20,
                    sortBy = SearchRepositorySort.Stargazers,
                ), this
            ).collect { searchResult ->
                if (_isRefreshing.value) {
                    _isRefreshing.emit(false)
                }
                searchResult.onSuccess {
                    val list =
                        this@HomeViewModel.result.value.getOrNull()?.repositories ?: emptyList()
                    val r = list + it.repositories
                    _result.emit(Result.success(it.copy(repositories = r)))
                }.onFailure {
                    _result.emit(Result.failure(it))
                }
            }
        }
    }

    fun toggleStar(selected: GitHubRepository) {
        viewModelScope.launch {
            repository.setStar(selected, !selected.viewerHasStarred)
        }
    }

    override fun next(pagination: GithubPagination) {
        // 重複呼び出しを抑制する
        if (_isLoading.value) {
            return
        }
        viewModelScope.launch {
            _isLoading.emit(true)
            pagination.next()
            _isLoading.emit(false)
        }
    }

    override fun retry() {
        // 重複呼び出しを抑制する
        if (_isLoading.value) {
            return
        }
        initialize()
    }
}