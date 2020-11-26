package com.vrockk.models.local_sogs

import android.graphics.Bitmap
import java.io.File

data class LocalSongsModel(
    var songName : String = "",
    var songPath : File ?= null,
    var songImage : Bitmap?= null,
    var isSelected : Boolean = false
)