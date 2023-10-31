package com.uva.metaquotestest.data

import com.uva.metaquotestest.utils.ResourcesProviderImpl
import kotlinx.coroutines.flow.Flow
import java.io.File

class Repository(
    private val remoteDataSource: RemoteDataSource,
) {

    fun downloadAndMatch(url: String, filter: String, appDirectory: File): Flow<String> {
        return remoteDataSource.upload(url, filter, appDirectory)
    }
}
