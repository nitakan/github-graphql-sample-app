package net.nitakan.githubsearcher.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.model.repositories.GitHubRepoRepository
import net.nitakan.githubsearcher.ui.extensions.applyRepository
import javax.inject.Inject

@HiltViewModel
class RepositoryDetailViewModel @Inject constructor(
    private val repository: GitHubRepoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var ownerName: String? = null
    private var repositoryName: String? = null
    private val _result = MutableStateFlow<Result<GitHubRepository?>>(Result.success(null))

    val result: StateFlow<Result<GitHubRepository?>> = _result
    val isLoading = _result.map {
        it.isSuccess && it.getOrNull() == null
    }

    init {
        ownerName = savedStateHandle.get<String>("owner")
        repositoryName = savedStateHandle.get<String>("name")
        initialize()

        // Star状態をリアルタイムに反映する
        viewModelScope.launch {
            repository.updatedRepositories.collect(_result::applyRepository)
        }
    }

    fun initialize() {
        viewModelScope.launch {
            fetch(ownerName, repositoryName)
        }
    }

    fun toggleStar() {
        viewModelScope.launch {
            _result.value.getOrNull()?.let {
                repository.setStar(it, !it.viewerHasStarred)
            }
        }
    }

    fun retry() {
        initialize()
    }

    private suspend fun fetch(ownerName: String?, repositoryName: String?) {
        _result.emit(Result.success(null))
        _result.emit(repository.get(ownerName, repositoryName))
    }
}