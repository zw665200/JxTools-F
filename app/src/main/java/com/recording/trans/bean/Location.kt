package com.recording.trans.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/9/9 11:09
 */
@Parcelize
data class Location(var lat: Double, var lng: Double) : Parcelable
