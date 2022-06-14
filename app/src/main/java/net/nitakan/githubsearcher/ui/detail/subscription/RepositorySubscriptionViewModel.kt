package net.nitakan.githubsearcher.ui.detail.subscription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.model.entities.GitHubViewSubscription
import net.nitakan.githubsearcher.model.repositories.GitHubRepoRepository
import net.nitakan.githubsearcher.ui.extensions.applyRepository
import javax.inject.Inject

@HiltViewModel
class RepositorySubscriptionViewModel @Inject constructor(
    private val repository: GitHubRepoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var ownerName: String? = null
    private var repositoryName: String? = null
    private val _isUpdating = MutableStateFlow(false)

    private val _selectedSubscription = MutableStateFlow(GitHubViewSubscription.NONE)
    private val _result = MutableStateFlow<Result<GitHubRepository?>>(Result.success(null))

    val result: StateFlow<Result<GitHubRepository?>> = _result
    val selectedSubscription: StateFlow<GitHubViewSubscription> = _selectedSubscription
    val isUpdating: StateFlow<Boolean> = _isUpdating

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

    fun retry() {
        initialize()
    }

    private suspend fun fetch(ownerName: String?, repositoryName: String?) {
        _result.emit(Result.success(null))
        repository.get(ownerName, repositoryName).let {
            _result.emit(it)
            it.onSuccess { repo ->
                _selectedSubscription.emit(repo.viewerSubscription)
            }.onFailure {
                _selectedSubscription.emit(GitHubViewSubscription.NONE)
            }
        }
    }

    fun selectSubscription(selected: GitHubViewSubscription) {
        _selectedSubscription.value = selected
    }

    suspend fun updateSubscription() {
        _isUpdating.emit(true)
        _result.value.getOrNull()?.let {
            repository.setSubscription(it, _selectedSubscription.value)
                .onFailure {

                }
        }
        _isUpdating.emit(false)
    }
}