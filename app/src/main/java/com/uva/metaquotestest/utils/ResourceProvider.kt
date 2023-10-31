package com.uva.metaquotestest.utils

import android.content.Context
import androidx.annotation.StringRes
import java.io.File

interface ResourceProvider {
    fun getString(
        @StringRes resId: Int,
        vararg args: Any?,
    ): String

    fun getAppDirectory(): File
}

class ResourcesProviderImpl(
    private val context: Context,
) : ResourceProvider {

    override fun getString(
        @StringRes resId: Int,
        vararg args: Any?,
    ): String = context.getString(resId, *args)

    override fun getAppDirectory(): File {
        return context.filesDir
    }
}
