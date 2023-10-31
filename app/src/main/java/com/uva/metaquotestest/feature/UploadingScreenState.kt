package com.uva.metaquotestest.feature

data class UploadingScreenState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val showUploadUI: Boolean = true,
    val result: List<String> = emptyList(),
)

sealed class UploadingScreenEffect {
    data class ShowToast(val text: String): UploadingScreenEffect()
}
