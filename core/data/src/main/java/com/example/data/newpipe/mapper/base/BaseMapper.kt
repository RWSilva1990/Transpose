package com.example.data.newpipe.mapper.base

import com.example.data.newpipe.utils.NewPipeUtils

object BaseMapper {
    fun getHighestResThumbnail(url: String?) = NewPipeUtils.getHighestResolutionThumbnail(url)
}