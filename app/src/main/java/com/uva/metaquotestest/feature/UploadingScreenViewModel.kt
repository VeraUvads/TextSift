package com.uva.metaquotestest.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uva.metaquotestest.R
import com.uva.metaquotestest.data.Repository
import com.uva.metaquotestest.utils.ResourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class UploadingViewModel(
    private val repository: Repository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(UploadingScreenState())
    val state: StateFlow<UploadingScreenState> = _state

    private val _effect: Channel<UploadingScreenEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    private var downloadJob: Job? = null

    // Тут можно было сделать сохранение файла его последующее переиспользование,
    // но потенциально может привести к ошибкам, если файл по ссылке заменят
    fun downloadAndMatch(url: String, filter: String) {
        downloadJob = viewModelScope.launch {
            reduce { copy(isLoading = true, isError = false) }
            val buffer = mutableListOf<String>()
            repository.downloadAndMatch(url, filter, resourceProvider.getAppDirectory())
                .catch {
                    reduce {
                        copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = it.message,
                        )
                    }

                    if (state.value.isLoading) {
                        setEffect {
                            UploadingScreenEffect.ShowToast(
                                it.message
                                    ?: resourceProvider.getString(R.string.something_went_wrong),
                            )
                        }
                    }
                }
                .cancellable()
                .flowOn(Dispatchers.IO)
                .onCompletion {
                    updateList(buffer)
                    reduce { copy(isLoading = false) }
                }
                .collect {
                    buffer.add(it)
                    reduce { copy(showUploadUI = false) }
                    if (buffer.size == 20) updateList(buffer)
                }
        }
    }

    private fun updateList(buffer: MutableList<String>) {
        reduce {
            val newList = result.toMutableList()
            newList.addAll(buffer)
            buffer.clear()
            copy(result = newList)
        }
    }

    fun showUploadUI() {
        downloadJob?.cancel()
        downloadJob = null
        if (state.value.isLoading) {
            setEffect { UploadingScreenEffect.ShowToast(resourceProvider.getString(R.string.downloading_cancelled)) }
        }
        reduce {
            copy(
                isLoading = false,
                isError = false,
                showUploadUI = true,
                result = emptyList(),
            )
        }
    }

    fun onCopyClick() {
        setEffect { UploadingScreenEffect.ShowToast(resourceProvider.getString(R.string.copied_to_clpboard)) }
    }

    private val queueDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val queueScope = CoroutineScope(queueDispatcher + SupervisorJob())

    private fun reduce(block: UploadingScreenState.() -> UploadingScreenState) {
        queueScope.launch() { _state.value = block(state.value) }
    }

    private fun setEffect(builder: () -> UploadingScreenEffect) {
        viewModelScope.launch { _effect.send(builder()) }
    }

    override fun onCleared() {
        super.onCleared()
        queueScope.cancel()
    }
}
