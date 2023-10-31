package com.uva.metaquotestest

import com.uva.metaquotestest.data.RemoteDataSource
import com.uva.metaquotestest.data.Repository
import com.uva.metaquotestest.utils.ResourcesProviderImpl

object AppContainer {

    val resourceProvider by lazy {
        ResourcesProviderImpl(App.instance.applicationContext)
    }

    private val remoteDataSource by lazy {
        RemoteDataSource()
    }
    val repository by lazy {
        Repository(remoteDataSource)
    }
}
